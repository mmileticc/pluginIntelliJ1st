package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class RoastSelectionAction : AnAction("Roast my code") {
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true
        e.presentation.isEnabledAndVisible = hasSelection
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return

        val selectedText = selectionModel.selectedText ?: return

        // Build prompt based on roast level and language
        val settings = ApplicationManager.getApplication().getService(RoastSettings::class.java)
        val level = settings.getLevel()
        val style = when (level) {
            RoastSettings.Level.LOW -> "lighthearted and mildly teasing"
            RoastSettings.Level.MEDIUM -> "funny, sarcastic, and boldly teasing"
            RoastSettings.Level.HIGH -> "savage, unapologetically brutal"
        }

        val languageInstruction = when (settings.getLanguage()) {
            RoastSettings.Language.ENGLISH -> "Respond in English."
            RoastSettings.Language.SERBIAN -> "Respond in Serbian (Latin script)."
        }

        val start = editor.selectionModel.selectionStart
        val end = editor.selectionModel.selectionEnd

        val prompt = """
            Roast this code in a $style tone.
            $languageInstruction
            Keep the roast to a 1-2 sentences and do it in the following format
            // ROAST: {Roasting}\n
            // HELP: {Helpful insight and suggestions}\n
            Keep the insight short as well
            In case the code works correctly and the syntax is all right, and it does not need to be roasted, just respond with a message in the following format:
            // NICE: {Simple message praising the code and the developer}
            
            If Code has any lines starting with // ROAST, // HELP or // NICE, ignore them
            
            Code:
            $selectedText
        """.trimIndent()

        // Prepare the GIF label for the popup (EDT)
        var popupHandle: CornerPopup.Handle? = null
        ApplicationManager.getApplication().invokeLater {
            val icon = javax.swing.ImageIcon(RoastSelectionAction::class.java.getResource("/icons/kodee.gif"))
            val label = JLabel("", icon, SwingConstants.LEADING)
            label.border = JBUI.Borders.empty(3)

            val wrapper = JPanel(BorderLayout()).apply {
                isOpaque = false
                border = JBUI.Borders.empty(22)
                add(label, BorderLayout.CENTER)
            }

            popupHandle = CornerPopup.showBottomRight(project, wrapper)
        }

        // Run API call asynchronously to avoid blocking UI and file locks
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Roasting selection with OpenAI", false) {
            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                indicator.text = "Roasting Selection..."
                try {
                    val response = OpenAIAPI.ask(prompt)
                    val roastText = OpenAIAPI.extractContent(response).replace("*/", "*∕")

                    ApplicationManager.getApplication().invokeLater {
                        if (!project.isDisposed) {
                            insertOrReplaceGeneratedComments(project, editor, psiFile, roastText, start, end)
                        }
                    }
                } catch (t: Throwable) {
                    val fallback = "// ROAST: Failed to contact OpenAI.\n// HELP: " + (t.message ?: "Unknown error") + "\n"
                    ApplicationManager.getApplication().invokeLater {
                        if (!project.isDisposed) {
                            insertOrReplaceGeneratedComments(project, editor, psiFile, fallback, start, end)
                        }
                    }
                }
            }

            override fun onFinished() {
                ApplicationManager.getApplication().invokeLater {
                    CornerPopup.close(popupHandle)
                }
            }
        })
    }

    private fun insertOrReplaceGeneratedComments(
        project: Project,
        editor: Editor,
        psiFile: com.intellij.psi.PsiFile,
        roastText: String,
        selectionStart: Int,
        selectionEnd: Int
    ) {
        val document = editor.document
        val startLine = document.getLineNumber(selectionStart)
        val endLine = document.getLineNumber(selectionEnd)

        // 1) Determine which lines to delete (selected generated comments + contiguous generated comment block directly above selection)
        val linesToDelete = mutableSetOf<Int>()

        fun isGeneratedCommentLine(lineIndex: Int): Boolean {
            if (lineIndex < 0 || lineIndex >= document.lineCount) return false
            val ls = document.getLineStartOffset(lineIndex)
            val le = document.getLineEndOffset(lineIndex)
            val text = document.getText(TextRange(ls, le))
            val trimmed = text.trimStart()
            val isComment = trimmed.startsWith("//")
            val hasMarker = text.contains("ROAST:", ignoreCase = true)
                    || text.contains("HELP:", ignoreCase = true)
                    || text.contains("NICE:", ignoreCase = true)
            return isComment && hasMarker
        }

        fun isBlankLine(lineIndex: Int): Boolean {
            if (lineIndex < 0 || lineIndex >= document.lineCount) return false
            val ls = document.getLineStartOffset(lineIndex)
            val le = document.getLineEndOffset(lineIndex)
            val text = document.getText(TextRange(ls, le))
            return text.isBlank()
        }

        // Find the first non-blank line inside the selection. If none, fall back to startLine.
        val firstNonBlankSelectedLine: Int = run {
            var idx = startLine
            var found = -1
            while (idx <= endLine) {
                if (!isBlankLine(idx)) { found = idx; break }
                idx++
            }
            if (found == -1) startLine else found
        }

        // Within selection
        for (l in startLine..endLine) {
            if (isGeneratedCommentLine(l)) linesToDelete.add(l)
        }

        // Contiguous block directly above the first non-blank selected line,
        // allowing whitespace-only lines between code and the generated comments.
        run {
            var l = firstNonBlankSelectedLine - 1
            // Temporarily collect whitespace lines directly above code.
            val whitespaceBuffer = mutableListOf<Int>()
            while (l >= 0 && isBlankLine(l)) {
                whitespaceBuffer.add(l)
                l--
            }

            // If the line above (after skipping blanks) is a generated comment, we will
            // delete that whole generated block AND the intervening blank lines.
            var foundGenerated = false
            var cursor = l
            while (cursor >= 0 && (isGeneratedCommentLine(cursor) || (foundGenerated && isBlankLine(cursor)))) {
                if (isGeneratedCommentLine(cursor)) foundGenerated = true
                linesToDelete.add(cursor)
                cursor--
            }

            if (foundGenerated) {
                // Include the whitespace directly above code as well, so comments end up
                // directly above the code when we re-insert.
                linesToDelete.addAll(whitespaceBuffer)
            }
        }

        val deletedAboveCount = linesToDelete.count { it < firstNonBlankSelectedLine }
        val targetInsertLine = (firstNonBlankSelectedLine - deletedAboveCount).coerceAtLeast(0)

        val comment = "$roastText\n"

        WriteCommandAction.runWriteCommandAction(project) {
            // Delete lines from bottom to top to keep offsets valid
            linesToDelete.toList().sortedDescending().forEach { lineIdx ->
                val ls = document.getLineStartOffset(lineIdx)
                // Include line break
                val le = if (lineIdx + 1 < document.lineCount) document.getLineStartOffset(lineIdx + 1) else document.textLength
                document.deleteString(ls, le)
            }

            // After deletions, compute the new insertion offset for the target line
            val insertLineIndex = targetInsertLine.coerceAtMost(document.lineCount.coerceAtLeast(1) - 1)
            val lineStartOffset = document.getLineStartOffset(insertLineIndex)
            document.insertString(lineStartOffset, comment)

            val manager = PsiDocumentManager.getInstance(project)
            manager.doPostponedOperationsAndUnblockDocument(document)
            manager.commitDocument(document)
        }
    }
}

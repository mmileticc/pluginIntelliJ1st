package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

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

        // Call API synchronously (simple approach as in existing code)
        val response = OpenAIAPI.ask(prompt)
        val roastText = OpenAIAPI.extractContent(response).replace("*/", "*∕")

        insertCommentAboveSelection(project, editor, psiFile, roastText)
    }

    private fun insertCommentAboveSelection(project: Project, editor: Editor, psiFile: com.intellij.psi.PsiFile, roastText: String) {
        val document = editor.document
        val start = editor.selectionModel.selectionStart
        val line = document.getLineNumber(start)
        val lineStartOffset = document.getLineStartOffset(line)
        val comment = "$roastText\n"

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(lineStartOffset, comment)
            val manager = PsiDocumentManager.getInstance(project)
            manager.doPostponedOperationsAndUnblockDocument(document)
            manager.commitDocument(document)
        }
    }
}

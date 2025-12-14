package com.github.mmileticc.pluginintellij1st

import ai.grazie.text.replace
import com.github.mmileticc.pluginintellij1st.toolWindow.TodoToolWindowFactory
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory

class HelpLineMarkerProvider: LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiComment) return null
        if (!element.text.startsWith("// HELP")) return null

        return LineMarkerInfo(
            element,
            element.textRange,
            Icons.HELP,
            { "Get detailed help" },
            { _, _ -> makeTodo(element) },
            GutterIconRenderer.Alignment.RIGHT
        )
    }

    private fun makeTodo(comment: PsiComment) {
        val project = comment.project
        val document = PsiDocumentManager
            .getInstance(project)
            .getDocument(comment.containingFile)
            ?: return

        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project)
            {
                val line = document.getLineNumber(comment.textOffset)
                val start = document.getLineStartOffset(line)
                val end = document.getLineEndOffset(line)

                val text = document.getText(TextRange(start, end)) // Extracted line
                val alteredText = text.replace("// HELP", "// TODO")
                comment.parent.addAfter(PsiElementFactory.getInstance(project).createCommentFromText(alteredText, comment), comment)
                PsiDocumentManager.getInstance(project).commitDocument(document)
                TodoToolWindowFactory.todoPanelInstance?.refresh()
            }
        }
    }
}
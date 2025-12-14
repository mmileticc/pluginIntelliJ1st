package com.github.mmileticc.pluginintellij1st.roasting

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class DeleteGeneratedCommentsAction : AnAction("Delete all generated comments") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        removeGeneratedComments(project, file)
    }

    private fun removeGeneratedComments(project: Project, file: PsiFile) {
        WriteCommandAction.runWriteCommandAction(project) {
            val comments = PsiTreeUtil.collectElementsOfType(file, PsiComment::class.java).toList()
            for (c in comments) {
                val text = c.text
                if (text.contains("ROAST:", ignoreCase = true)
                    || text.contains("HELP:", ignoreCase=true)
                    || text.contains("NICE:", ignoreCase=true)){
                    c.delete()
                }
            }

            val docManager = PsiDocumentManager.getInstance(project)
            docManager.getDocument(file)?.let { doc ->
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}

class DeleteNiceCommentsAction : AnAction("Delete all NICE comments") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        removeGeneratedComments(project, file)
    }

    private fun removeGeneratedComments(project: Project, file: PsiFile) {
        WriteCommandAction.runWriteCommandAction(project) {
            val comments = PsiTreeUtil.collectElementsOfType(file, PsiComment::class.java).toList()
            for (c in comments) {
                val text = c.text
                if (text.contains("NICE:", ignoreCase=true)){
                    c.delete()
                }
            }

            val docManager = PsiDocumentManager.getInstance(project)
            docManager.getDocument(file)?.let { doc ->
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}

class DeleteRoastCommentsAction : AnAction("Delete all ROAST and HELP comments") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        removeGeneratedComments(project, file)
    }

    private fun removeGeneratedComments(project: Project, file: PsiFile) {
        WriteCommandAction.runWriteCommandAction(project) {
            val comments = PsiTreeUtil.collectElementsOfType(file, PsiComment::class.java).toList()
            for (c in comments) {
                val text = c.text
                if (text.contains("ROAST:", ignoreCase = true)
                    || text.contains("HELP:", ignoreCase=true)){
                    c.delete()
                }
            }

            val docManager = PsiDocumentManager.getInstance(project)
            docManager.getDocument(file)?.let { doc ->
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}

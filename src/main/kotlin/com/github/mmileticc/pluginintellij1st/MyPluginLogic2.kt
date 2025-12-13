package com.github.mmileticc.pluginintellij1st

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.awt.Font

class MyCommentAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiComment &&
            element.text.startsWith("// ROAST")
        ) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(
                    TextAttributesKey.createTextAttributesKey(
                    "MY_CUSTOM_COMMENT",
                        TextAttributes(JBColor.magenta, null, null, null, Font.PLAIN)
                ))
                .range(element)
                .create()
        }
    }
}


object MyPluginLogic2 {
    fun run(project: Project, file: PsiFile) {
        val factory = KtPsiFactory(project)

        WriteCommandAction.runWriteCommandAction(project) {
            val functions = file.children.filterIsInstance<KtNamedFunction>()

            for (funDecl in functions) {
                val comment = factory.createComment("// MY_TAG Funkcija: ${funDecl.name}")
                file.addBefore(comment, funDecl)
            }

            val docManager = PsiDocumentManager.getInstance(project)
            docManager.getDocument(file)?.let { doc ->
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}
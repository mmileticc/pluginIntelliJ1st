package com.github.mmileticc.pluginintellij1st

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Font

class MyCommentAnnotator : Annotator {

    companion object {
        private val ROAST_KEY = TextAttributesKey.createTextAttributesKey("ROAST_COMMENT")
        private val HELP_KEY = TextAttributesKey.createTextAttributesKey("HELP_COMMENT")
        private val NICE_KEY = TextAttributesKey.createTextAttributesKey("NICE_COMMENT")
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiComment) return

        when {
            element.text.startsWith("// ROAST") -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .enforcedTextAttributes(
                        TextAttributes(JBColor.PINK, null, null, null, Font.BOLD)
                    )
                    .range(element)
                    .create()
            }

            element.text.startsWith("// HELP") -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .enforcedTextAttributes(
                        TextAttributes(Color(207,187,151), Color(57,37,1), null, null, Font.PLAIN)
                    )
                    .range(element)
                    .create()
            }

            element.text.startsWith("// NICE") -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .enforcedTextAttributes(
                        TextAttributes(JBColor.CYAN, null, null, null, Font.PLAIN)
                    )
                    .range(element)
                    .create()
            }

            element.text.startsWith("// DONE") -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .enforcedTextAttributes(
                        TextAttributes(JBColor.CYAN, null, null, null, Font.PLAIN)
                    )
                    .range(element)
                    .create()
            }
        }
    }
}
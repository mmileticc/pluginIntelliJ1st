package com.github.mmileticc.pluginintellij1st

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
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
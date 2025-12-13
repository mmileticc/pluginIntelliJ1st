package com.github.mmileticc.pluginintellij1st

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import org.apache.xmlgraphics.image.codec.png.PNGEncodeParam
import java.awt.Color
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
                        TextAttributes(JBColor.pink, null, null, null, Font.PLAIN)
                    ))
                .range(element)
                .create()
        } else if (element is PsiComment && element.text.startsWith("// HELP"))
        {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(
                    TextAttributesKey.createTextAttributesKey(
                        "MY_CUSTOM_COMMENT",
                        TextAttributes(Color(207,187,151), null, null, null, Font.PLAIN)
                    ))
                .range(element)
                .create()
        } else if (element is PsiComment && element.text.startsWith("// NICE"))
        {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(
                    TextAttributesKey.createTextAttributesKey(
                        "MY_CUSTOM_COMMENT",
                        TextAttributes(JBColor.cyan, null, null, null, Font.PLAIN)
                    ))
                .range(element)
                .create()
        }
    }
}
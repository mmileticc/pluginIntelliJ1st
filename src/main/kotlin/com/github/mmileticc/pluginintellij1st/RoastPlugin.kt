package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

object RoastPlugin {
    fun run(project: Project, file: PsiFile) {
        val factory = KtPsiFactory(project)


        WriteCommandAction.runWriteCommandAction(project) {
            val functions = file.collectDescendantsOfType<KtNamedFunction>()

            for (funDecl in functions) {

                val roast = OpenAIAPI.ask(
                    """
                    Isprozivaj ovu Kotlin funkciju na duhovit, sarkastičan i malo bezobrazan način.
                    Ne objašnjavaj kod, samo ga roast-uj.
                    Vrati jednu rečenicu.
                    
                    Kod:
                    ${funDecl.text}
                    """.trimIndent()
                )

                val roastText = OpenAIAPI.extractContent(roast)
                val comment = factory.createComment("// ROAST:  $roastText")

                //funDecl.addBefore(comment, funDecl)
                funDecl.parent.addBefore(comment, funDecl)

                val newline = factory.createWhiteSpace("\n")
                funDecl.parent.addBefore(newline, funDecl)
            }

            val docManager = PsiDocumentManager.getInstance(project)
            docManager.getDocument(file)?.let { doc ->
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}
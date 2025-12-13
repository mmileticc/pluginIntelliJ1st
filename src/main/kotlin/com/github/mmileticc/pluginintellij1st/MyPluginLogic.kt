package com.github.mmileticc.pluginintellij1st
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtPsiFactory

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import org.json.JSONObject



object MyPluginLogic {
    fun run(project: Project, file: PsiFile) {
        val factory = KtPsiFactory(project)


        val json = OpenAIAPI.ask("Daj mi neki novi nasumican glavni grad evrope")
        val answer = OpenAIAPI.extractContent(json)

        val comment = factory.createComment("/* $answer */")


        WriteCommandAction.runWriteCommandAction(project) {
            // koristi već napravljeni PSI komentar
            val anchor = file.children.firstOrNull { it !is PsiWhiteSpace } ?: file.firstChild
            file.addBefore(comment, anchor)

            val docManager = PsiDocumentManager.getInstance(project)
            val doc = docManager.getDocument(file)
            if (doc != null) {
                docManager.doPostponedOperationsAndUnblockDocument(doc)
                docManager.commitDocument(doc)
            }
        }
    }
}




object OpenAIAPI {

    private val API_KEY = System.getenv("OPENAI_API_KEY") ?: ""

    fun ask(question: String): String {
        val client = HttpClient.newHttpClient()

        val body = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {"role": "user", "content": "$question"}
              ]
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $API_KEY")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun extractContent(json: String): String {
        val obj = JSONObject(json)

        if (!obj.has("choices")) {
            return "OpenAI error: ${obj.toString(2)}"
        }

        return obj
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
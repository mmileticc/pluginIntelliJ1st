package com.github.mmileticc.pluginintellij1st

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


object OpenAIAPI {

    private val API_KEY = System.getenv("OPENAI_API_KEY") ?: ""

    fun ask(question: String): String {
        val client = HttpClient.newHttpClient()

        val body = """
            {
              "model": "gpt-5.2",
              "messages": [
                {
                    "role": "user", 
                    "content":  ${JSONObject.quote(question)}

                }
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
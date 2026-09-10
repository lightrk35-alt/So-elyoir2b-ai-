package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProvider : AIProvider {

    override val providerName: String = "Gemini-3.5-Flash"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateResponse(
        systemInstruction: String,
        recentHistory: List<Pair<String, String>>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("NO_API_KEY")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        // Build Gemini contents JSON
        val requestJson = JSONObject()

        // System Instruction
        val systemInstructionObj = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", systemInstruction) })
            }
            put("parts", partsArray)
        }
        requestJson.put("systemInstruction", systemInstructionObj)

        // Contents array
        val contentsArray = JSONArray()

        // Append recent conversational turns
        for ((sender, text) in recentHistory) {
            val role = if (sender == "USER") "user" else "model"
            val contentObj = JSONObject().apply {
                put("role", role)
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                }
                put("parts", parts)
            }
            contentsArray.put(contentObj)
        }

        // Current user message
        val currentMsgObj = JSONObject().apply {
            put("role", "user")
            val parts = JSONArray().apply {
                put(JSONObject().apply { put("text", userMessage) })
            }
            put("parts", parts)
        }
        contentsArray.put(currentMsgObj)

        requestJson.put("contents", contentsArray)

        // Generation config for temperature and JSON output
        val generationConfig = JSONObject().apply {
            put("temperature", 0.7)
            put("topP", 0.95)
            put("topK", 40)
            put("responseMimeType", "application/json")
        }
        requestJson.put("generationConfig", generationConfig)

        val body = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API HTTP ${response.code}: $responseBody")
        }

        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val textPart = parts?.optJSONObject(0)?.optString("text")

        textPart ?: throw RuntimeException("Empty response parts from Gemini")
    }
}

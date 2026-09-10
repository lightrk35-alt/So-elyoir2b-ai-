package com.example.ai

import com.example.model.AiResponse
import com.example.model.CharacterEmotion
import com.example.model.MemoryCandidate
import org.json.JSONObject

object ResponseParser {

    fun parse(rawResponse: String): AiResponse {
        val cleaned = cleanJsonString(rawResponse)

        try {
            val json = JSONObject(cleaned)
            val dialogue = json.optString("dialogue", "").trim()
            val emotionStr = json.optString("emotion", "NORMAL")
            val intensity = json.optDouble("intensity", 0.5).toFloat()

            val memoryCandidate = if (json.has("memory_candidate") && !json.isNull("memory_candidate")) {
                val memObj = json.optJSONObject("memory_candidate")
                if (memObj != null && memObj.has("key") && memObj.has("value")) {
                    MemoryCandidate(
                        category = memObj.optString("category", "GENERAL"),
                        key = memObj.optString("key", ""),
                        value = memObj.optString("value", ""),
                        importance = memObj.optString("importance", "MEDIUM")
                    )
                } else null
            } else null

            if (dialogue.isNotEmpty()) {
                return AiResponse(
                    dialogue = dialogue,
                    emotion = CharacterEmotion.fromString(emotionStr),
                    intensity = intensity.coerceIn(0.1f, 1.0f),
                    memoryCandidate = memoryCandidate
                )
            }
        } catch (e: Exception) {
            // Graceful fallback parser below
        }

        // Resilient Fallback if JSON was irregular
        return parseFallback(rawResponse)
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        text = text.trim()

        // Locate first '{' and last '}'
        val startIdx = text.indexOf('{')
        val endIdx = text.lastIndexOf('}')
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            text = text.substring(startIdx, endIdx + 1)
        }
        return text
    }

    private fun parseFallback(raw: String): AiResponse {
        var cleanText = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()

        // Try regex extraction for "dialogue": "..."
        val dialogueRegex = Regex("\"dialogue\"\\s*:\\s*\"([^\"]+)\"")
        val match = dialogueRegex.find(cleanText)
        val extractedDialogue = match?.groupValues?.getOrNull(1)

        val emotionRegex = Regex("\"emotion\"\\s*:\\s*\"([^\"]+)\"")
        val emotionMatch = emotionRegex.find(cleanText)
        val extractedEmotion = emotionMatch?.groupValues?.getOrNull(1)

        val finalDialogue = when {
            !extractedDialogue.isNullOrBlank() -> extractedDialogue
            cleanText.startsWith("{") -> cleanText.replace(Regex("[{}\"]"), "").trim()
            cleanText.isNotBlank() -> cleanText
            else -> "..."
        }

        val finalEmotion = CharacterEmotion.fromString(extractedEmotion ?: deduceEmotionFromText(finalDialogue))

        return AiResponse(
            dialogue = finalDialogue,
            emotion = finalEmotion,
            intensity = 0.5f,
            memoryCandidate = null
        )
    }

    private fun deduceEmotionFromText(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("أحب") || lower.contains("اشتقت") || lower.contains("معك") || lower.contains("لطيف") -> "AFFECTIONATE"
            lower.contains("هههه") || lower.contains("مضحك") || lower.contains("haha") -> "LAUGHING"
            lower.contains("غبي") || lower.contains("أحمق") || lower.contains("سخيف") || lower.contains("fool") -> "TEASING"
            lower.contains("لماذا") || lower.contains("كيف") || lower.contains("أخبرني") || lower.contains("?") -> "CURIOUS"
            lower.contains("غضب") || lower.contains("توقف") || lower.contains("كفى") -> "ANGRY"
            lower.contains("حزن") || lower.contains("للأسف") || lower.contains("مؤسف") -> "SAD"
            lower.contains("أخرى") || lower.contains("غيري") || lower.contains("من هذه") -> "JEALOUS"
            else -> "NORMAL"
        }
    }
}

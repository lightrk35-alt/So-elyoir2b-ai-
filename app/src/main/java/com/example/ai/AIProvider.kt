package com.example.ai

interface AIProvider {
    val providerName: String
    suspend fun generateResponse(
        systemInstruction: String,
        recentHistory: List<Pair<String, String>>, // sender to text
        userMessage: String
    ): String
}

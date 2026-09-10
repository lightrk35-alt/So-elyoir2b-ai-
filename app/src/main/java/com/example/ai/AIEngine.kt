package com.example.ai

import com.example.BuildConfig
import com.example.model.AiResponse
import com.example.model.CharacterState
import com.example.model.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIEngine(
    private val geminiProvider: GeminiProvider = GeminiProvider(),
    private val localFallbackProvider: LocalFallbackProvider = LocalFallbackProvider()
) {

    private val _connectionStatus = MutableStateFlow(
        if (hasValidApiKey()) ConnectionStatus.ONLINE else ConnectionStatus.OFFLINE
    )
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    suspend fun processUserMessage(
        characterName: String = "2B",
        userMessage: String,
        characterState: CharacterState,
        memories: List<String>,
        recentHistory: List<Pair<String, String>>
    ): AiResponse {
        val systemInstruction = PersonaPrompt.buildSystemInstruction(
            characterName = characterName,
            currentEmotion = characterState.currentEmotion.name,
            affection = characterState.affection,
            trust = characterState.trust,
            mood = characterState.mood,
            memories = memories
        )

        var rawResponse = ""

        if (hasValidApiKey()) {
            try {
                _connectionStatus.value = ConnectionStatus.CONNECTING
                rawResponse = geminiProvider.generateResponse(
                    systemInstruction = systemInstruction,
                    recentHistory = recentHistory,
                    userMessage = userMessage
                )
                _connectionStatus.value = ConnectionStatus.ONLINE
            } catch (e: Exception) {
                // If cloud fails (timeout, rate limit, connectivity), switch seamlessly to local engine
                _connectionStatus.value = ConnectionStatus.OFFLINE
                rawResponse = localFallbackProvider.generateResponse(
                    systemInstruction = systemInstruction,
                    recentHistory = recentHistory,
                    userMessage = userMessage
                )
            }
        } else {
            // Local neural engine with rich personality
            _connectionStatus.value = ConnectionStatus.OFFLINE
            rawResponse = localFallbackProvider.generateResponse(
                systemInstruction = systemInstruction,
                recentHistory = recentHistory,
                userMessage = userMessage
            )
        }

        return ResponseParser.parse(rawResponse)
    }

    private fun hasValidApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }
}

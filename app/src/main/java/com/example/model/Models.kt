package com.example.model

data class CharacterState(
    val affection: Float = 0.5f,
    val trust: Float = 0.5f,
    val mood: String = "CALM",
    val stress: Float = 0.1f,
    val anger: Float = 0.05f,
    val curiosity: Float = 0.7f,
    val currentEmotion: CharacterEmotion = CharacterEmotion.NORMAL,
    val syncRate: Float = 98.6f
)

data class MemoryCandidate(
    val category: String = "GENERAL",
    val key: String,
    val value: String,
    val importance: String = "MEDIUM"
)

data class AiResponse(
    val dialogue: String,
    val emotion: CharacterEmotion = CharacterEmotion.NORMAL,
    val intensity: Float = 0.5f,
    val memoryCandidate: MemoryCandidate? = null
)

enum class ConnectionStatus {
    ONLINE,
    CONNECTING,
    OFFLINE
}

data class ChatMessage(
    val id: Long = 0,
    val conversationId: Long = 1,
    val sender: String, // "USER" or "ELYOR"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: CharacterEmotion = CharacterEmotion.NORMAL,
    val intensity: Float = 0.5f
)

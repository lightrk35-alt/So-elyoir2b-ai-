package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "المستخدم",
    val characterName: String = "2B",
    val defaultImageUri: String = "", // custom URI or drawable resource identifier
    val backgroundUri: String = "", // custom URI, preset id, or empty for OLED black
    val setupCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "character_assets")
data class CharacterAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emotion: String, // from CharacterEmotion.name
    val imageUri: String, // content:// URI or internal file path
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val title: String,
    val summary: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "USER" or "ELYOR"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String = "NORMAL",
    val intensity: Float = 0.5f
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "INTEREST", "PREFERENCE", "FACT", "TRAIT"
    val key: String,
    val value: String,
    val importance: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "character_state")
data class CharacterStateEntity(
    @PrimaryKey val id: Int = 1,
    val affection: Float = 0.55f,
    val trust: Float = 0.60f,
    val mood: String = "CALM",
    val stress: Float = 0.10f,
    val anger: Float = 0.05f,
    val curiosity: Float = 0.75f,
    val currentEmotion: String = "NORMAL",
    val syncRate: Float = 98.4f
)


package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.entity.CharacterAssetEntity
import com.example.data.database.entity.CharacterStateEntity
import com.example.data.database.entity.ConversationEntity
import com.example.data.database.entity.MemoryEntity
import com.example.data.database.entity.MessageEntity
import com.example.data.database.entity.UserProfileEntity
import com.example.model.CharacterEmotion
import com.example.model.CharacterState
import com.example.model.ChatMessage
import com.example.model.MemoryCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {

    val userProfile: Flow<UserProfileEntity?> =
        database.userProfileDao().getUserProfile()

    val characterAssets: Flow<List<CharacterAssetEntity>> =
        database.characterAssetDao().getAllAssets()

    val conversations: Flow<List<ConversationEntity>> =
        database.conversationDao().getAllConversations()

    val allMemories: Flow<List<MemoryEntity>> =
        database.memoryDao().getAllMemories()

    suspend fun getUserProfile(): UserProfileEntity = withContext(Dispatchers.IO) {
        database.userProfileDao().getUserProfileSync() ?: UserProfileEntity().also {
            database.userProfileDao().saveUserProfile(it)
        }
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        database.userProfileDao().saveUserProfile(profile)
    }

    suspend fun updateCharacterName(name: String) = withContext(Dispatchers.IO) {
        database.userProfileDao().updateCharacterName(name)
    }

    suspend fun updateDefaultImageUri(uri: String) = withContext(Dispatchers.IO) {
        database.userProfileDao().updateDefaultImageUri(uri)
    }

    suspend fun updateBackgroundUri(uri: String) = withContext(Dispatchers.IO) {
        database.userProfileDao().updateBackgroundUri(uri)
    }

    suspend fun completeSetup(characterName: String, defaultImageUri: String, backgroundUri: String) =
        withContext(Dispatchers.IO) {
            val existing = database.userProfileDao().getUserProfileSync() ?: UserProfileEntity()
            database.userProfileDao().saveUserProfile(
                existing.copy(
                    characterName = characterName.ifBlank { "2B" },
                    defaultImageUri = defaultImageUri,
                    backgroundUri = backgroundUri,
                    setupCompleted = true
                )
            )
        }

    suspend fun setEmotionAsset(emotion: String, imageUri: String) = withContext(Dispatchers.IO) {
        val existing = database.characterAssetDao().getAssetForEmotion(emotion)
        if (existing != null) {
            database.characterAssetDao().insertOrUpdate(
                existing.copy(imageUri = imageUri, updatedAt = System.currentTimeMillis())
            )
        } else {
            database.characterAssetDao().insertOrUpdate(
                CharacterAssetEntity(emotion = emotion, imageUri = imageUri)
            )
        }
    }

    suspend fun deleteEmotionAsset(emotion: String) = withContext(Dispatchers.IO) {
        database.characterAssetDao().deleteAssetForEmotion(emotion)
    }

    suspend fun getAssetForEmotion(emotion: String): CharacterAssetEntity? = withContext(Dispatchers.IO) {
        database.characterAssetDao().getAssetForEmotion(emotion)
    }

    fun getMessagesForConversation(convId: Long): Flow<List<ChatMessage>> {
        return database.messageDao().getMessagesForConversation(convId).map { list ->
            list.map { entity ->
                ChatMessage(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    sender = entity.sender,
                    text = entity.text,
                    timestamp = entity.timestamp,
                    emotion = CharacterEmotion.fromString(entity.emotion),
                    intensity = entity.intensity
                )
            }
        }
    }

    suspend fun getRecentMessages(convId: Long, limit: Int = 12): List<MessageEntity> =
        withContext(Dispatchers.IO) {
            database.messageDao().getRecentMessages(convId, limit)
        }

    suspend fun insertMessage(
        convId: Long,
        sender: String,
        text: String,
        emotion: CharacterEmotion = CharacterEmotion.NORMAL,
        intensity: Float = 0.5f
    ): Long = withContext(Dispatchers.IO) {
        val id = database.messageDao().insertMessage(
            MessageEntity(
                conversationId = convId,
                sender = sender,
                text = text,
                timestamp = System.currentTimeMillis(),
                emotion = emotion.name,
                intensity = intensity
            )
        )
        // Update conversation date and summary
        val conv = database.conversationDao().getConversationById(convId)
        if (conv != null) {
            val preview = if (text.length > 50) text.take(50) + "..." else text
            database.conversationDao().updateConversation(
                conv.copy(date = System.currentTimeMillis(), summary = preview)
            )
        }
        id
    }

    suspend fun createNewConversation(title: String = "جلسة عصبية جديدة"): Long =
        withContext(Dispatchers.IO) {
            val conv = ConversationEntity(
                title = title,
                date = System.currentTimeMillis(),
                summary = "محادثة نشطة مع سو إيلوار"
            )
            val newId = database.conversationDao().insertConversation(conv)
            // Welcome message for new conversation
            database.messageDao().insertMessage(
                MessageEntity(
                    conversationId = newId,
                    sender = "ELYOR",
                    text = "أنا جاهزة، ما الأمر؟",
                    timestamp = System.currentTimeMillis(),
                    emotion = "NORMAL",
                    intensity = 0.5f
                )
            )
            newId
        }

    suspend fun deleteConversation(convId: Long) = withContext(Dispatchers.IO) {
        database.messageDao().deleteMessagesForConversation(convId)
        database.conversationDao().deleteConversation(convId)
    }

    suspend fun getRelevantMemories(limit: Int = 6): List<MemoryEntity> =
        withContext(Dispatchers.IO) {
            database.memoryDao().getTopMemories(limit)
        }

    suspend fun saveOrUpdateMemory(candidate: MemoryCandidate) =
        withContext(Dispatchers.IO) {
            val existing = database.memoryDao().findByKey(candidate.key)
            if (existing != null) {
                database.memoryDao().insertMemory(
                    existing.copy(
                        value = candidate.value,
                        category = candidate.category,
                        importance = candidate.importance,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                database.memoryDao().insertMemory(
                    MemoryEntity(
                        category = candidate.category,
                        key = candidate.key,
                        value = candidate.value,
                        importance = candidate.importance,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        database.memoryDao().deleteMemory(id)
    }

    val characterState: Flow<CharacterState> =
        database.characterStateDao().getState().map { entity ->
            if (entity != null) {
                CharacterState(
                    affection = entity.affection,
                    trust = entity.trust,
                    mood = entity.mood,
                    stress = entity.stress,
                    anger = entity.anger,
                    curiosity = entity.curiosity,
                    currentEmotion = CharacterEmotion.fromString(entity.currentEmotion),
                    syncRate = entity.syncRate
                )
            } else {
                CharacterState()
            }
        }

    suspend fun updateCharacterEmotion(emotion: CharacterEmotion, intensity: Float) =
        withContext(Dispatchers.IO) {
            val current = database.characterStateDao().getStateSync()
            val state = current ?: CharacterStateEntity()

            // Dynamic adjustment based on emotion and intensity
            val affectionDelta = when (emotion) {
                CharacterEmotion.AFFECTIONATE -> 0.03f * intensity
                CharacterEmotion.HAPPY, CharacterEmotion.LAUGHING -> 0.02f * intensity
                CharacterEmotion.PLAYFUL, CharacterEmotion.TEASING -> 0.015f * intensity
                CharacterEmotion.JEALOUS -> 0.01f * intensity
                CharacterEmotion.ANNOYED -> -0.01f * intensity
                CharacterEmotion.ANGRY -> -0.025f * intensity
                else -> 0.005f * intensity
            }

            val newAffection = (state.affection + affectionDelta).coerceIn(0.1f, 1.0f)
            val newTrust = (state.trust + (affectionDelta * 0.5f)).coerceIn(0.1f, 1.0f)
            val newCuriosity = if (emotion == CharacterEmotion.CURIOUS) {
                (state.curiosity + 0.04f).coerceIn(0.1f, 1.0f)
            } else {
                state.curiosity
            }

            val newMood = when (emotion) {
                CharacterEmotion.HAPPY, CharacterEmotion.LAUGHING -> "CONTENT"
                CharacterEmotion.TEASING, CharacterEmotion.PLAYFUL -> "PLAYFUL"
                CharacterEmotion.AFFECTIONATE -> "WARM"
                CharacterEmotion.SERIOUS -> "ANALYTICAL"
                CharacterEmotion.ANNOYED, CharacterEmotion.ANGRY -> "IRRITATED"
                CharacterEmotion.JEALOUS -> "GUARDED"
                else -> "CALM"
            }

            val newSync = (state.syncRate + (if (newAffection > 0.6f) 0.1f else -0.05f)).coerceIn(90f, 99.9f)

            database.characterStateDao().saveState(
                state.copy(
                    affection = newAffection,
                    trust = newTrust,
                    curiosity = newCuriosity,
                    mood = newMood,
                    currentEmotion = emotion.name,
                    syncRate = newSync
                )
            )
        }
}

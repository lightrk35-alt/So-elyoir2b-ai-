package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.CharacterAssetEntity
import com.example.data.database.entity.CharacterStateEntity
import com.example.data.database.entity.ConversationEntity
import com.example.data.database.entity.MemoryEntity
import com.example.data.database.entity.MessageEntity
import com.example.data.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET characterName = :name WHERE id = 1")
    suspend fun updateCharacterName(name: String)

    @Query("UPDATE user_profile SET defaultImageUri = :uri WHERE id = 1")
    suspend fun updateDefaultImageUri(uri: String)

    @Query("UPDATE user_profile SET backgroundUri = :uri WHERE id = 1")
    suspend fun updateBackgroundUri(uri: String)

    @Query("UPDATE user_profile SET setupCompleted = :completed WHERE id = 1")
    suspend fun updateSetupCompleted(completed: Boolean)
}

@Dao
interface CharacterAssetDao {
    @Query("SELECT * FROM character_assets ORDER BY updatedAt DESC")
    fun getAllAssets(): Flow<List<CharacterAssetEntity>>

    @Query("SELECT * FROM character_assets WHERE emotion = :emotion LIMIT 1")
    suspend fun getAssetForEmotion(emotion: String): CharacterAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(asset: CharacterAssetEntity): Long

    @Query("DELETE FROM character_assets WHERE id = :id")
    suspend fun deleteAsset(id: Long)

    @Query("DELETE FROM character_assets WHERE emotion = :emotion")
    suspend fun deleteAssetForEmotion(emotion: String)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY date DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: Long, limit: Int = 20): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY CASE importance WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, updatedAt DESC LIMIT :limit")
    suspend fun getTopMemories(limit: Int = 8): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE `key` = :key LIMIT 1")
    suspend fun findByKey(key: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)
}

@Dao
interface CharacterStateDao {
    @Query("SELECT * FROM character_state WHERE id = 1 LIMIT 1")
    fun getState(): Flow<CharacterStateEntity?>

    @Query("SELECT * FROM character_state WHERE id = 1 LIMIT 1")
    suspend fun getStateSync(): CharacterStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: CharacterStateEntity)
}


package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.dao.CharacterAssetDao
import com.example.data.database.dao.CharacterStateDao
import com.example.data.database.dao.ConversationDao
import com.example.data.database.dao.MemoryDao
import com.example.data.database.dao.MessageDao
import com.example.data.database.dao.UserProfileDao
import com.example.data.database.entity.CharacterAssetEntity
import com.example.data.database.entity.CharacterStateEntity
import com.example.data.database.entity.ConversationEntity
import com.example.data.database.entity.MemoryEntity
import com.example.data.database.entity.MessageEntity
import com.example.data.database.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        CharacterAssetEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        CharacterStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun characterAssetDao(): CharacterAssetDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun characterStateDao(): CharacterStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elyoir_ai.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            prepopulateData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateData(db: AppDatabase) {
            // Default user profile
            db.userProfileDao().saveUserProfile(
                UserProfileEntity(
                    id = 1,
                    userName = "المستخدم",
                    characterName = "2B",
                    defaultImageUri = "",
                    backgroundUri = "",
                    setupCompleted = false
                )
            )

            // Initial conversation session
            val convId = db.conversationDao().insertConversation(
                ConversationEntity(
                    id = 1,
                    date = System.currentTimeMillis(),
                    title = "محادثتنا الأولى",
                    summary = "بداية التعارف"
                )
            )

            // Initial greetings from character
            db.messageDao().insertMessage(
                MessageEntity(
                    conversationId = convId,
                    sender = "ELYOR",
                    text = "أخيرًا التقينا... كنت أنتظرك 🖤",
                    timestamp = System.currentTimeMillis(),
                    emotion = "NORMAL",
                    intensity = 0.6f
                )
            )

            // Initial memory records
            db.memoryDao().insertMemory(
                MemoryEntity(
                    category = "PREFERENCE",
                    key = "tone",
                    value = "هدوء، ذكاء، مودة صادقة مع دعابة خفيفة",
                    importance = "HIGH"
                )
            )

            // Character state
            db.characterStateDao().saveState(
                CharacterStateEntity(
                    id = 1,
                    affection = 0.65f,
                    trust = 0.70f,
                    mood = "CALM",
                    stress = 0.05f,
                    anger = 0.02f,
                    curiosity = 0.80f,
                    currentEmotion = "NORMAL",
                    syncRate = 99.0f
                )
            )
        }
    }
}


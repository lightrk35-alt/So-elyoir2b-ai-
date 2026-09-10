package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AIEngine
import com.example.character.CharacterAssetManager
import com.example.data.database.entity.CharacterAssetEntity
import com.example.data.database.entity.ConversationEntity
import com.example.data.database.entity.MemoryEntity
import com.example.data.database.entity.UserProfileEntity
import com.example.data.repository.AppRepository
import com.example.model.CharacterEmotion
import com.example.model.CharacterState
import com.example.model.ChatMessage
import com.example.model.ConnectionStatus
import com.example.model.MemoryCandidate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DialogType {
    NONE,
    SETTINGS,
    CHARACTER_IMAGES,
    BACKGROUND_PICKER,
    MEMORIES
}

class MainViewModel(
    private val repository: AppRepository,
    private val aiEngine: AIEngine = AIEngine()
) : ViewModel() {

    val connectionStatus: StateFlow<ConnectionStatus> = aiEngine.connectionStatus

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val characterName: StateFlow<String> = repository.userProfile.map {
        it?.characterName?.ifBlank { "2B" } ?: "2B"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "2B"
    )

    val isSetupCompleted: StateFlow<Boolean> = repository.userProfile.map {
        it?.setupCompleted == true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val characterAssets: StateFlow<Map<String, String>> = repository.characterAssets.map { list ->
        list.associate { it.emotion to it.imageUri }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    val characterState: StateFlow<CharacterState> = repository.characterState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CharacterState()
    )

    // Current resolved image model (Coil URI string or Drawable Int resource)
    val currentCharacterImage: StateFlow<Any> = combine(
        characterState,
        characterAssets,
        userProfile
    ) { state, assets, profile ->
        CharacterAssetManager.resolveImageModel(
            emotion = state.currentEmotion,
            customEmotionAssets = assets,
            defaultImageUri = profile?.defaultImageUri
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = com.example.R.drawable.img_elyoir_normal
    )

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _activeConversationId = MutableStateFlow(1L)
    val activeConversationId: StateFlow<Long> = _activeConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _activeConversationId.flatMapLatest { convId ->
        repository.getMessagesForConversation(convId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeDialog = MutableStateFlow(DialogType.NONE)
    val activeDialog: StateFlow<DialogType> = _activeDialog.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure profile exists
            repository.getUserProfile()

            repository.conversations.collect { list ->
                if (list.isNotEmpty() && list.none { it.id == _activeConversationId.value }) {
                    _activeConversationId.value = list.first().id
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isGenerating.value) return

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            val convId = _activeConversationId.value

            // 1. Record user message in local Room database
            repository.insertMessage(
                convId = convId,
                sender = "USER",
                text = text,
                emotion = CharacterEmotion.NORMAL,
                intensity = 0.5f
            )

            // 2. Prepare contextual memories & history
            val relevantMemories = repository.getRelevantMemories().map {
                "${it.category} [${it.key}]: ${it.value}"
            }
            val recentEntities = repository.getRecentMessages(convId, limit = 8)
            val historyPairs = recentEntities.reversed().map { it.sender to it.text }

            // 3. Process with AI Engine
            val currentState = characterState.value
            val charName = characterName.value
            val aiResponse = aiEngine.processUserMessage(
                characterName = charName,
                userMessage = text,
                characterState = currentState,
                memories = relevantMemories,
                recentHistory = historyPairs
            )

            // 4. Save character response in Room database
            repository.insertMessage(
                convId = convId,
                sender = "ELYOR",
                text = aiResponse.dialogue,
                emotion = aiResponse.emotion,
                intensity = aiResponse.intensity
            )

            // 5. Update character dynamic emotion & internal state
            repository.updateCharacterEmotion(
                emotion = aiResponse.emotion,
                intensity = aiResponse.intensity
            )

            // 6. Save memory candidate if identified
            if (aiResponse.memoryCandidate != null && aiResponse.memoryCandidate.key.isNotBlank()) {
                repository.saveOrUpdateMemory(aiResponse.memoryCandidate)
            }

            _isGenerating.value = false
        }
    }

    // Setup completion
    fun completeSetup(name: String, imageUri: String, backgroundUri: String) {
        viewModelScope.launch {
            repository.completeSetup(
                characterName = name.ifBlank { "2B" },
                defaultImageUri = imageUri,
                backgroundUri = backgroundUri
            )
        }
    }

    // Settings actions
    fun updateCharacterName(name: String) {
        viewModelScope.launch {
            repository.updateCharacterName(name.ifBlank { "2B" })
        }
    }

    fun updateDefaultImage(uri: String) {
        viewModelScope.launch {
            repository.updateDefaultImageUri(uri)
        }
    }

    fun updateBackground(uri: String) {
        viewModelScope.launch {
            repository.updateBackgroundUri(uri)
        }
    }

    fun setEmotionAsset(emotion: CharacterEmotion, uri: String) {
        viewModelScope.launch {
            repository.setEmotionAsset(emotion.name, uri)
        }
    }

    fun removeEmotionAsset(emotion: CharacterEmotion) {
        viewModelScope.launch {
            repository.deleteEmotionAsset(emotion.name)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            val convId = _activeConversationId.value
            repository.createNewConversation("محادثة جديدة")
        }
    }

    fun openDialog(type: DialogType) {
        _activeDialog.value = type
    }

    fun dismissDialog() {
        _activeDialog.value = DialogType.NONE
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.ChatMessage
import com.example.ui.components.CharacterDisplay
import com.example.ui.components.ChatHistorySheet
import com.example.ui.components.ChatInputBar
import com.example.ui.components.DialogueCaption
import com.example.ui.components.MinimalBackground
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SimpleTopBar
import com.example.ui.theme.PureBlack
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val characterName by viewModel.characterName.collectAsState()
    val characterImage by viewModel.currentCharacterImage.collectAsState()
    val characterAssets by viewModel.characterAssets.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showChatHistoryDialog by remember { mutableStateOf(false) }

    Crossfade(
        targetState = isSetupCompleted,
        animationSpec = tween(durationMillis = 400),
        label = "setup_or_main_crossfade"
    ) { setupDone ->
        if (!setupDone) {
            OnboardingSetupScreen(
                onCompleteSetup = { name, imageUri, backgroundUri, customExpressions ->
                    viewModel.completeSetup(name, imageUri, backgroundUri)
                    customExpressions.forEach { (emotionName, uri) ->
                        val emotion = com.example.model.CharacterEmotion.fromString(emotionName)
                        viewModel.setEmotionAsset(emotion, uri)
                    }
                },
                modifier = modifier
            )
        } else {
            // OLED Pure Companion View
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(PureBlack)
                    .testTag("companion_main_screen")
            ) {
                // 1. Peaceful Background (OLED Black or user chosen quiet photo)
                MinimalBackground(backgroundUri = userProfile?.backgroundUri)

                // 2. Full-height Character Portrait with soft bottom gradient fade
                CharacterDisplay(imageModel = characterImage)

                // 3. Foreground UI
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Ultra-minimal Top Bar
                    SimpleTopBar(
                        characterName = characterName,
                        onOpenSettings = { showSettingsDialog = true }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Floating Living Dialogue Caption
                    val lastCharacterMsg = messages.lastOrNull { it.sender != "USER" }
                    val lastUserMsg = messages.lastOrNull { it.sender == "USER" }

                    DialogueCaption(
                        characterName = characterName,
                        lastCharacterMessage = lastCharacterMsg,
                        lastUserMessage = lastUserMsg,
                        isGenerating = isGenerating,
                        onOpenHistory = { showChatHistoryDialog = true }
                    )

                    // Minimal rounded chat input
                    ChatInputBar(
                        text = inputText,
                        onTextChanged = viewModel::onInputTextChanged,
                        onSend = viewModel::sendMessage,
                        isEnabled = !isGenerating
                    )
                }

                // Settings Dialog
                if (showSettingsDialog) {
                    SettingsDialog(
                        characterName = characterName,
                        currentDefaultImage = userProfile?.defaultImageUri,
                        currentBackground = userProfile?.backgroundUri,
                        customEmotionAssets = characterAssets,
                        memories = memories,
                        onUpdateCharacterName = viewModel::updateCharacterName,
                        onUpdateDefaultImage = viewModel::updateDefaultImage,
                        onUpdateBackground = viewModel::updateBackground,
                        onSetEmotionAsset = viewModel::setEmotionAsset,
                        onRemoveEmotionAsset = viewModel::removeEmotionAsset,
                        onDeleteMemory = viewModel::deleteMemory,
                        onClearChat = viewModel::clearMessages,
                        onDismiss = { showSettingsDialog = false }
                    )
                }

                // Chat History Dialog
                if (showChatHistoryDialog) {
                    ChatHistorySheet(
                        characterName = characterName,
                        messages = messages,
                        onDismiss = { showChatHistoryDialog = false }
                    )
                }
            }
        }
    }
}

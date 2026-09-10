package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CharacterEmotion
import com.example.ui.theme.AccentNeutral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class SetupStep {
    CHOOSE_CHARACTER,
    CHOOSE_NAME,
    CHOOSE_BACKGROUND,
    CUSTOM_EXPRESSIONS
}

@Composable
fun OnboardingSetupScreen(
    onCompleteSetup: (name: String, imageUri: String, backgroundUri: String, customExpressions: Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(SetupStep.CHOOSE_CHARACTER) }
    var selectedCharacterModel by remember { mutableStateOf<String>("preset_normal") }
    var characterNameInput by remember { mutableStateOf("2B") }
    var selectedBackgroundModel by remember { mutableStateOf<String>("pure_black") }
    val customExpressions = remember { mutableStateMapOf<String, String>() }

    // Gallery Picker for Main Character Image
    val characterPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedCharacterModel = uri.toString()
            }
        }
    )

    // Gallery Picker for Background Image
    val backgroundPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedBackgroundModel = uri.toString()
            }
        }
    )

    // Gallery Picker for specific emotion
    var targetEmotionForPicker by remember { mutableStateOf<CharacterEmotion?>(null) }
    val emotionPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val emotion = targetEmotionForPicker
            if (uri != null && emotion != null) {
                customExpressions[emotion.name] = uri.toString()
            }
            targetEmotionForPicker = null
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("onboarding_setup_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "setup_step_transition"
                ) { step ->
                    when (step) {
                        SetupStep.CHOOSE_CHARACTER -> {
                            StepChooseCharacter(
                                selectedModel = selectedCharacterModel,
                                onSelectPreset = { selectedCharacterModel = it },
                                onPickFromGallery = {
                                    characterPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                        SetupStep.CHOOSE_NAME -> {
                            StepChooseName(
                                characterImage = selectedCharacterModel,
                                name = characterNameInput,
                                onNameChanged = { characterNameInput = it },
                                onDone = { currentStep = SetupStep.CHOOSE_BACKGROUND }
                            )
                        }
                        SetupStep.CHOOSE_BACKGROUND -> {
                            StepChooseBackground(
                                selectedBackground = selectedBackgroundModel,
                                onSelectPreset = { selectedBackgroundModel = it },
                                onPickFromGallery = {
                                    backgroundPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                        SetupStep.CUSTOM_EXPRESSIONS -> {
                            StepCustomExpressions(
                                customMap = customExpressions,
                                onPickForEmotion = { emotion ->
                                    targetEmotionForPicker = emotion
                                    emotionPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Navigation controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (currentStep) {
                    SetupStep.CHOOSE_CHARACTER -> {
                        Button(
                            onClick = { currentStep = SetupStep.CHOOSE_NAME },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentNeutral,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("step_1_continue_button")
                        ) {
                            Text("متابعة", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.size(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    SetupStep.CHOOSE_NAME -> {
                        Button(
                            onClick = { currentStep = SetupStep.CHOOSE_BACKGROUND },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentNeutral,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("step_2_continue_button")
                        ) {
                            Text("متابعة", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    SetupStep.CHOOSE_BACKGROUND -> {
                        Button(
                            onClick = { currentStep = SetupStep.CUSTOM_EXPRESSIONS },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentNeutral,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("step_3_continue_button")
                        ) {
                            Text("متابعة", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    SetupStep.CUSTOM_EXPRESSIONS -> {
                        Button(
                            onClick = {
                                onCompleteSetup(
                                    characterNameInput.ifBlank { "2B" },
                                    selectedCharacterModel,
                                    selectedBackgroundModel,
                                    customExpressions
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentNeutral,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("step_finish_button")
                        ) {
                            Text("إكمال وبدء المحادثة", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        TextButton(
                            onClick = {
                                onCompleteSetup(
                                    characterNameInput.ifBlank { "2B" },
                                    selectedCharacterModel,
                                    selectedBackgroundModel,
                                    emptyMap()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("step_skip_button")
                        ) {
                            Text("تخطي والبدء فورًا", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: CHOOSE CHARACTER
// -------------------------------------------------------------
@Composable
private fun StepChooseCharacter(
    selectedModel: String,
    onSelectPreset: (String) -> Unit,
    onPickFromGallery: () -> Unit
) {
    val presets = listOf(
        "preset_normal" to R.drawable.img_elyoir_normal,
        "preset_happy" to R.drawable.img_elyoir_happy,
        "preset_serious" to R.drawable.img_elyoir_serious,
        "preset_teasing" to R.drawable.img_elyoir_teasing
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "لنتعرّف.",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "اختر صورة شخصيتك\nChoose your character",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        // Large Preview
        val previewModel: Any = when (selectedModel) {
            "preset_normal" -> R.drawable.img_elyoir_normal
            "preset_happy" -> R.drawable.img_elyoir_happy
            "preset_serious" -> R.drawable.img_elyoir_serious
            "preset_teasing" -> R.drawable.img_elyoir_teasing
            else -> selectedModel
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(2.dp, DarkBorder, CircleShape)
        ) {
            AsyncImage(
                model = previewModel,
                contentDescription = "Selected Character",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Row of presets + Gallery Pick
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { (presetKey, resId) ->
                val isSelected = selectedModel == presetKey
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AccentNeutral else DarkBorder,
                            shape = CircleShape
                        )
                        .clickable { onSelectPreset(presetKey) }
                ) {
                    AsyncImage(
                        model = resId,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Pick custom image button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable { onPickFromGallery() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Pick custom",
                    tint = AccentNeutral,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = "يمكنك اختيار أي صورة من ألبوم الصور بهاتفك",
            color = TextMuted,
            fontSize = 12.sp
        )
    }
}

// -------------------------------------------------------------
// STEP 2: CHOOSE NAME
// -------------------------------------------------------------
@Composable
private fun StepChooseName(
    characterImage: String,
    name: String,
    onNameChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    val imageModel: Any = when (characterImage) {
        "preset_normal" -> R.drawable.img_elyoir_normal
        "preset_happy" -> R.drawable.img_elyoir_happy
        "preset_serious" -> R.drawable.img_elyoir_serious
        "preset_teasing" -> R.drawable.img_elyoir_teasing
        else -> characterImage
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(1.dp, DarkBorder, CircleShape)
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "ماذا أسمّيها؟",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "What should I call her?",
            color = TextSecondary,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            placeholder = { Text("مثلاً: 2B", color = TextMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentNeutral,
                unfocusedBorderColor = DarkBorder
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("character_name_input")
        )
    }
}

// -------------------------------------------------------------
// STEP 3: CHOOSE BACKGROUND
// -------------------------------------------------------------
@Composable
private fun StepChooseBackground(
    selectedBackground: String,
    onSelectPreset: (String) -> Unit,
    onPickFromGallery: () -> Unit
) {
    val bgOptions = listOf(
        "pure_black" to "OLED أسود نقي",
        "preset_night" to "سماء هادئة",
        "preset_room" to "غرفة دافئة"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "اختر الخلفية",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Choose a background\nالخلفية ملك لك، يمكنك جعلها سوداء بالكامل أو صورة هادئة.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            bgOptions.forEach { (key, label) ->
                val isSelected = selectedBackground == key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AccentNeutral else DarkBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectPreset(key) }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = TextPrimary, fontSize = 15.sp)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentNeutral, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Pick from Gallery option
            val isCustom = !bgOptions.any { it.first == selectedBackground }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCustom) DarkSurfaceElevated else DarkSurface)
                .border(
                    width = if (isCustom) 1.5.dp else 1.dp,
                    color = if (isCustom) AccentNeutral else DarkBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onPickFromGallery() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentNeutral)
                    Text(
                        text = if (isCustom) "صورة مخصصة من ألبومك" else "اختيار صورة من المعرض",
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                }
                if (isCustom) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentNeutral, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: CUSTOM EMOTIONS (OPTIONAL)
// -------------------------------------------------------------
@Composable
private fun StepCustomExpressions(
    customMap: Map<String, String>,
    onPickForEmotion: (CharacterEmotion) -> Unit
) {
    val commonEmotions = listOf(
        CharacterEmotion.HAPPY,
        CharacterEmotion.SAD,
        CharacterEmotion.ANGRY,
        CharacterEmotion.AFFECTIONATE,
        CharacterEmotion.TEASING,
        CharacterEmotion.CURIOUS,
        CharacterEmotion.SLEEPY,
        CharacterEmotion.SURPRISED
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "تعبيرات الشخصية",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "يمكنك تعيين صور مختلفة لكل شعور (اختياري)\nأو البدء الآن وإدارتها لاحقًا من الإعدادات.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(commonEmotions) { emotion ->
                val hasCustom = customMap.containsKey(emotion.name)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (hasCustom) DarkSurfaceElevated else DarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (hasCustom) AccentNeutral else DarkBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onPickForEmotion(emotion) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = emotion.labelAr,
                            color = if (hasCustom) TextPrimary else TextSecondary,
                            fontSize = 13.sp
                        )
                        if (hasCustom) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentNeutral, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

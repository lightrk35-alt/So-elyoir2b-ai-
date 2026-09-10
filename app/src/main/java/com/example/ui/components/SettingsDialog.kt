package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.database.entity.MemoryEntity
import com.example.model.CharacterEmotion
import com.example.ui.theme.AccentNeutral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    characterName: String,
    currentDefaultImage: String?,
    currentBackground: String?,
    customEmotionAssets: Map<String, String>,
    memories: List<MemoryEntity>,
    onUpdateCharacterName: (String) -> Unit,
    onUpdateDefaultImage: (String) -> Unit,
    onUpdateBackground: (String) -> Unit,
    onSetEmotionAsset: (CharacterEmotion, String) -> Unit,
    onRemoveEmotionAsset: (CharacterEmotion) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("الشخصية", "التعبيرات", "الخلفية", "الذاكرة", "المحادثة")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(PureBlack)
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("settings_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الإعدادات",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tab row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PureBlack,
                    contentColor = AccentNeutral,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentNeutral,
                            height = 2.dp
                        )
                    },
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal,
                                    color = if (selectedTab == index) TextPrimary else TextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> CharacterIdentityTab(
                            characterName = characterName,
                            currentDefaultImage = currentDefaultImage,
                            onUpdateName = onUpdateCharacterName,
                            onUpdateImage = onUpdateDefaultImage
                        )
                        1 -> CharacterExpressionsTab(
                            customMap = customEmotionAssets,
                            onSetAsset = onSetEmotionAsset,
                            onRemoveAsset = onRemoveEmotionAsset
                        )
                        2 -> BackgroundSettingsTab(
                            currentBackground = currentBackground,
                            onUpdateBackground = onUpdateBackground
                        )
                        3 -> MemoriesSettingsTab(
                            memories = memories,
                            onDeleteMemory = onDeleteMemory
                        )
                        4 -> ChatManagementTab(
                            onClearChat = {
                                onClearChat()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: CHARACTER IDENTITY
// -------------------------------------------------------------
@Composable
private fun CharacterIdentityTab(
    characterName: String,
    currentDefaultImage: String?,
    onUpdateName: (String) -> Unit,
    onUpdateImage: (String) -> Unit
) {
    var nameState by remember(characterName) { mutableStateOf(characterName) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onUpdateImage(uri.toString())
            }
        }
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "اسم الشخصية",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentNeutral,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { onUpdateName(nameState.ifBlank { "2B" }) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentNeutral,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ")
                }
            }
        }

        item {
            Text(
                text = "الصورة الأساسية للشخصية",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            val presets = listOf(
                "preset_normal" to R.drawable.img_elyoir_normal,
                "preset_happy" to R.drawable.img_elyoir_happy,
                "preset_serious" to R.drawable.img_elyoir_serious,
                "preset_teasing" to R.drawable.img_elyoir_teasing
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { (key, resId) ->
                    val isSelected = currentDefaultImage == key
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) AccentNeutral else DarkBorder,
                                shape = CircleShape
                            )
                            .clickable { onUpdateImage(key) }
                    ) {
                        AsyncImage(
                            model = resId,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, CircleShape)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Gallery", tint = AccentNeutral)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: EXPRESSIONS (30+ EMOTIONS)
// -------------------------------------------------------------
@Composable
private fun CharacterExpressionsTab(
    customMap: Map<String, String>,
    onSetAsset: (CharacterEmotion, String) -> Unit,
    onRemoveAsset: (CharacterEmotion) -> Unit
) {
    var targetedEmotion by remember { mutableStateOf<CharacterEmotion?>(null) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val emotion = targetedEmotion
            if (uri != null && emotion != null) {
                onSetAsset(emotion, uri.toString())
            }
            targetedEmotion = null
        }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "إدارة صور التعبيرات (31 شعورًا)",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "اختر صورة لكل شعور من هاتفك، أو سيتم استخدام الصورة الافتراضية تلقائيًا دون توليد بالذكاء الاصطناعي.",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(CharacterEmotion.values()) { emotion ->
                val customUri = customMap[emotion.name]
                val hasCustom = !customUri.isNullOrBlank()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (hasCustom) AccentNeutral else DarkBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            targetedEmotion = emotion
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = emotion.labelAr,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = emotion.name,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        if (hasCustom) {
                            IconButton(
                                onClick = { onRemoveAsset(emotion) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: BACKGROUND
// -------------------------------------------------------------
@Composable
private fun BackgroundSettingsTab(
    currentBackground: String?,
    onUpdateBackground: (String) -> Unit
) {
    val bgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onUpdateBackground(uri.toString())
            }
        }
    )

    val options = listOf(
        "pure_black" to "OLED أسود نقي (#000000)",
        "preset_night" to "سماء هادئة (Preset)",
        "preset_room" to "غرفة دافئة (Preset)"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(options) { (key, label) ->
            val isSelected = currentBackground == key || (key == "pure_black" && (currentBackground.isNullOrBlank() || currentBackground == "black"))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) AccentNeutral else DarkBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onUpdateBackground(key) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = TextPrimary, fontSize = 14.sp)
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentNeutral)
                }
            }
        }

        item {
            val isCustom = currentBackground != null && !options.any { it.first == currentBackground }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCustom) DarkSurfaceElevated else DarkSurface)
                    .border(
                        width = 1.dp,
                        color = if (isCustom) AccentNeutral else DarkBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        bgPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentNeutral)
                    Text(text = "صورة مخصصة من معرض هاتفك", color = TextPrimary, fontSize = 14.sp)
                }
                if (isCustom) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentNeutral)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: MEMORIES
// -------------------------------------------------------------
@Composable
private fun MemoriesSettingsTab(
    memories: List<MemoryEntity>,
    onDeleteMemory: (Long) -> Unit
) {
    if (memories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لم تتعرف على تفاصيل كافية بعد.\nستتذكر اهتماماتك تلقائيًا أثناء الحديث.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(memories) { memory ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = memory.key, color = AccentNeutral, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = memory.value, color = TextSecondary, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = { onDeleteMemory(memory.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: CHAT RESET
// -------------------------------------------------------------
@Composable
private fun ChatManagementTab(
    onClearChat: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().padding(top = 10.dp)
    ) {
        Text(
            text = "إدارة المحادثة",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "بدء محادثة جديدة يتيح لك فتح صفحة جديدة مع شخصيتك، مع الاحتفاظ بذكرياتها عنك.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Button(
            onClick = onClearChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurfaceElevated,
                contentColor = AccentNeutral
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("بدء محادثة جديدة")
        }
    }
}

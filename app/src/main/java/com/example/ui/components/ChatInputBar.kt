package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentNeutral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Minimalist, soft-rounded Chat Input Bar.
 * Focused purely on comfort, legibility, and distraction-free communication.
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showQuickEmojis by remember { mutableStateOf(false) }
    val quickEmojis = listOf("🖤", "🤍", "😊", "✨", "😂", "👀", "☕", "🌸", "🌙", "💫")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_input_bar")
    ) {
        // Optional quick emoji picker bar
        AnimatedVisibility(
            visible = showQuickEmojis,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                items(quickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .clickable {
                                onTextChanged(text + emoji)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }
        }

        // Main input container: rounded, warm dark slate, clean border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(DarkSurfaceElevated)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji toggle button
            IconButton(
                onClick = { showQuickEmojis = !showQuickEmojis },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("emoji_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                    contentDescription = "Emoji Picker",
                    tint = if (showQuickEmojis) AccentNeutral else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text input area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "اكتب رسالة...",
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(AccentNeutral),
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("message_text_input")
                )
            }

            // Send button
            val canSend = text.isNotBlank() && isEnabled
            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (canSend) AccentNeutral else DarkBorder)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (canSend) PureBlack else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

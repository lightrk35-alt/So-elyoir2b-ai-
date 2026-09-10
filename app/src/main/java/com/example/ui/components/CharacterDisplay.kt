package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.PureBlack

/**
 * Calm, peaceful background for So Elyoir 2B.
 * Defaults to pure OLED #000000 black.
 * Supports custom photos from gallery or peaceful presets with subtle dimming.
 */
@Composable
fun MinimalBackground(
    backgroundUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (!backgroundUri.isNullOrBlank() && backgroundUri != "black" && backgroundUri != "pure_black") {
            val imageModel: Any = when (backgroundUri) {
                "preset_night" -> R.drawable.img_elyoir_serious // ambient dark tone
                "preset_room" -> R.drawable.img_elyoir_normal
                else -> backgroundUri
            }

            AsyncImage(
                model = imageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // Subtle OLED dimming so character and text always pop with pristine legibility
                        drawRect(
                            color = Color(0xCC000000),
                            blendMode = BlendMode.SrcOver
                        )
                    }
            )
        }
    }
}

/**
 * Large, natural character presence.
 * Occupies the screen elegantly with soft fade into the bottom black background.
 * No HUD, no neon rings, no futuristic cyber telemetry.
 */
@Composable
fun CharacterDisplay(
    imageModel: Any,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Crossfade(
            targetState = imageModel,
            animationSpec = tween(durationMillis = 350),
            label = "character_crossfade"
        ) { targetModel ->
            AsyncImage(
                model = targetModel,
                contentDescription = "Character Portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // Gentle gradient fade at the bottom into pure black so chat dialogue is effortlessly readable
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0x99000000),
                                    Color(0xFF000000)
                                ),
                                startY = size.height * 0.45f,
                                endY = size.height
                            )
                        )
                    }
            )
        }
    }
}

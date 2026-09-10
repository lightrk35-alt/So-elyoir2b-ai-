package com.example.character

import com.example.R
import com.example.model.CharacterEmotion

data class EmotionVisualData(
    val drawableResId: Int,
    val labelAr: String,
    val labelEn: String
)

/**
 * Character Asset Manager.
 * Resolves character emotion to the user's custom assigned image,
 * falling back to the user's chosen default character image,
 * or the bundled high-resolution portrait.
 * No AI image generation is performed.
 */
object CharacterAssetManager {

    // Default bundled fallback mappings for all 30+ emotions
    private val emotionMap: Map<CharacterEmotion, EmotionVisualData> = mapOf(
        CharacterEmotion.NORMAL to EmotionVisualData(R.drawable.img_elyoir_normal, "هادئة", "Normal"),
        CharacterEmotion.HAPPY to EmotionVisualData(R.drawable.img_elyoir_happy, "سعيدة", "Happy"),
        CharacterEmotion.VERY_HAPPY to EmotionVisualData(R.drawable.img_elyoir_happy, "سعيدة جدًا", "Very Happy"),
        CharacterEmotion.SMILE to EmotionVisualData(R.drawable.img_elyoir_happy, "مبتسمة", "Smile"),
        CharacterEmotion.LAUGHING to EmotionVisualData(R.drawable.img_elyoir_happy, "تضحك", "Laughing"),
        CharacterEmotion.PLAYFUL to EmotionVisualData(R.drawable.img_elyoir_teasing, "مرحة", "Playful"),
        CharacterEmotion.TEASING to EmotionVisualData(R.drawable.img_elyoir_teasing, "تستفز بمكر", "Teasing"),
        CharacterEmotion.CURIOUS to EmotionVisualData(R.drawable.img_elyoir_teasing, "فضولية", "Curious"),
        CharacterEmotion.SURPRISED to EmotionVisualData(R.drawable.img_elyoir_normal, "متفاجئة", "Surprised"),
        CharacterEmotion.CONFUSED to EmotionVisualData(R.drawable.img_elyoir_normal, "حائرة", "Confused"),
        CharacterEmotion.SERIOUS to EmotionVisualData(R.drawable.img_elyoir_serious, "جادة", "Serious"),
        CharacterEmotion.FOCUSED to EmotionVisualData(R.drawable.img_elyoir_serious, "مركّزة", "Focused"),
        CharacterEmotion.WORRIED to EmotionVisualData(R.drawable.img_elyoir_serious, "قلقة", "Worried"),
        CharacterEmotion.CONCERNED to EmotionVisualData(R.drawable.img_elyoir_serious, "مهتمة ومترقبة", "Concerned"),
        CharacterEmotion.SAD to EmotionVisualData(R.drawable.img_elyoir_serious, "حزينة", "Sad"),
        CharacterEmotion.CRYING to EmotionVisualData(R.drawable.img_elyoir_serious, "تبكي", "Crying"),
        CharacterEmotion.DISAPPOINTED to EmotionVisualData(R.drawable.img_elyoir_serious, "محبطة", "Disappointed"),
        CharacterEmotion.ANGRY to EmotionVisualData(R.drawable.img_elyoir_serious, "غاضبة", "Angry"),
        CharacterEmotion.VERY_ANGRY to EmotionVisualData(R.drawable.img_elyoir_serious, "غاضبة جدًا", "Very Angry"),
        CharacterEmotion.ANNOYED to EmotionVisualData(R.drawable.img_elyoir_serious, "منزعجة", "Annoyed"),
        CharacterEmotion.IRRITATED to EmotionVisualData(R.drawable.img_elyoir_serious, "مستاءة", "Irritated"),
        CharacterEmotion.EMBARRASSED to EmotionVisualData(R.drawable.img_elyoir_happy, "مرتبكة وخجولة", "Embarrassed"),
        CharacterEmotion.SHY to EmotionVisualData(R.drawable.img_elyoir_happy, "خجولة", "Shy"),
        CharacterEmotion.AFFECTIONATE to EmotionVisualData(R.drawable.img_elyoir_happy, "حنونة", "Affectionate"),
        CharacterEmotion.LOVING to EmotionVisualData(R.drawable.img_elyoir_happy, "محبّة ودافئة", "Loving"),
        CharacterEmotion.JEALOUS to EmotionVisualData(R.drawable.img_elyoir_teasing, "تغار", "Jealous"),
        CharacterEmotion.DISGUSTED to EmotionVisualData(R.drawable.img_elyoir_teasing, "مشمئزة", "Disgusted"),
        CharacterEmotion.AFRAID to EmotionVisualData(R.drawable.img_elyoir_serious, "خائفة", "Afraid"),
        CharacterEmotion.SMUG to EmotionVisualData(R.drawable.img_elyoir_teasing, "واثقة ومغرورة", "Smug"),
        CharacterEmotion.MISCHIEVOUS to EmotionVisualData(R.drawable.img_elyoir_teasing, "مشاكسة", "Mischievous"),
        CharacterEmotion.SLEEPY to EmotionVisualData(R.drawable.img_elyoir_normal, "نعسانة", "Sleepy")
    )

    /**
     * Resolves the visual model for Coil (String URI or Int Drawable Res)
     */
    fun resolveImageModel(
        emotion: CharacterEmotion,
        customEmotionAssets: Map<String, String>,
        defaultImageUri: String?
    ): Any {
        // 1. Check direct custom emotion match
        val customUri = customEmotionAssets[emotion.name]
        if (!customUri.isNullOrBlank()) {
            return customUri
        }

        // 2. Check similar emotion family match
        val familyUri = when (emotion) {
            CharacterEmotion.VERY_HAPPY, CharacterEmotion.SMILE, CharacterEmotion.LAUGHING ->
                customEmotionAssets[CharacterEmotion.HAPPY.name]
            CharacterEmotion.LOVING ->
                customEmotionAssets[CharacterEmotion.AFFECTIONATE.name]
            CharacterEmotion.IRRITATED, CharacterEmotion.VERY_ANGRY ->
                customEmotionAssets[CharacterEmotion.ANGRY.name] ?: customEmotionAssets[CharacterEmotion.ANNOYED.name]
            CharacterEmotion.CRYING, CharacterEmotion.DISAPPOINTED ->
                customEmotionAssets[CharacterEmotion.SAD.name]
            CharacterEmotion.MISCHIEVOUS, CharacterEmotion.SMUG ->
                customEmotionAssets[CharacterEmotion.TEASING.name] ?: customEmotionAssets[CharacterEmotion.PLAYFUL.name]
            CharacterEmotion.SHY ->
                customEmotionAssets[CharacterEmotion.EMBARRASSED.name]
            else -> null
        }
        if (!familyUri.isNullOrBlank()) {
            return familyUri
        }

        // 3. Fallback to user-chosen default character image
        if (!defaultImageUri.isNullOrBlank()) {
            // Check if default is a bundled preset name or custom URI
            return when (defaultImageUri) {
                "preset_normal" -> R.drawable.img_elyoir_normal
                "preset_happy" -> R.drawable.img_elyoir_happy
                "preset_serious" -> R.drawable.img_elyoir_serious
                "preset_teasing" -> R.drawable.img_elyoir_teasing
                else -> defaultImageUri
            }
        }

        // 4. Default bundled fallback for this emotion
        val visual = emotionMap[emotion] ?: emotionMap.getValue(CharacterEmotion.NORMAL)
        return visual.drawableResId
    }

    fun getLabel(emotion: CharacterEmotion): String = emotion.labelAr
}


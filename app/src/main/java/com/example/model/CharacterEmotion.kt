package com.example.model

/**
 * Character emotions supported by So Elyoir 2B AI.
 * Supports 30+ distinct emotional states with fallback resolution.
 */
enum class CharacterEmotion(val labelAr: String, val labelEn: String) {
    NORMAL("هادئة", "Normal"),
    HAPPY("سعيدة", "Happy"),
    VERY_HAPPY("سعيدة جدًا", "Very Happy"),
    SMILE("مبتسمة", "Smile"),
    LAUGHING("تضحك", "Laughing"),
    PLAYFUL("مرحة", "Playful"),
    TEASING("تستفز بمكر", "Teasing"),
    CURIOUS("فضولية", "Curious"),
    SURPRISED("متفاجئة", "Surprised"),
    CONFUSED("حائرة", "Confused"),
    SERIOUS("جادة", "Serious"),
    FOCUSED("مركّزة", "Focused"),
    WORRIED("قلقة", "Worried"),
    CONCERNED("مهتمة ومترقبة", "Concerned"),
    SAD("حزينة", "Sad"),
    CRYING("تبكي", "Crying"),
    DISAPPOINTED("محبطة", "Disappointed"),
    ANGRY("غاضبة", "Angry"),
    VERY_ANGRY("غاضبة جدًا", "Very Angry"),
    ANNOYED("منزعجة", "Annoyed"),
    IRRITATED("مستاءة", "Irritated"),
    EMBARRASSED("مرتبكة وخجولة", "Embarrassed"),
    SHY("خجولة", "Shy"),
    AFFECTIONATE("حنونة", "Affectionate"),
    LOVING("محبّة ودافئة", "Loving"),
    JEALOUS("تغار", "Jealous"),
    DISGUSTED("مشمئزة", "Disgusted"),
    AFRAID("خائفة", "Afraid"),
    SMUG("واثقة ومغرورة", "Smug"),
    MISCHIEVOUS("مشاكسة", "Mischievous"),
    SLEEPY("نعسانة", "Sleepy");

    companion object {
        fun fromString(value: String?): CharacterEmotion {
            if (value.isNullOrBlank()) return NORMAL
            val cleaned = value.trim().uppercase()
            return try {
                valueOf(cleaned)
            } catch (e: Exception) {
                // Approximate matching
                when {
                    cleaned.contains("LOVE") -> LOVING
                    cleaned.contains("AFFECT") -> AFFECTIONATE
                    cleaned.contains("JOY") || cleaned.contains("HAPP") -> HAPPY
                    cleaned.contains("LAUGH") -> LAUGHING
                    cleaned.contains("TEAS") -> TEASING
                    cleaned.contains("PLAY") -> PLAYFUL
                    cleaned.contains("ANGER") || cleaned.contains("ANGR") -> ANGRY
                    cleaned.contains("ANNOY") -> ANNOYED
                    cleaned.contains("SAD") || cleaned.contains("SORROW") -> SAD
                    cleaned.contains("CRY") -> CRYING
                    cleaned.contains("BLUSH") || cleaned.contains("SHY") -> SHY
                    cleaned.contains("EMBARRASS") -> EMBARRASSED
                    cleaned.contains("JEAL") -> JEALOUS
                    cleaned.contains("WORR") -> WORRIED
                    cleaned.contains("CURIO") -> CURIOUS
                    cleaned.contains("SURPRIS") -> SURPRISED
                    cleaned.contains("SERIO") -> SERIOUS
                    cleaned.contains("SMUG") -> SMUG
                    cleaned.contains("SLEEP") -> SLEEPY
                    else -> NORMAL
                }
            }
        }
    }
}


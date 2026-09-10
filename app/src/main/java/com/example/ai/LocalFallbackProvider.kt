package com.example.ai

import kotlinx.coroutines.delay
import org.json.JSONObject

class LocalFallbackProvider : AIProvider {

    override val providerName: String = "Local-Companion-Engine"

    override suspend fun generateResponse(
        systemInstruction: String,
        recentHistory: List<Pair<String, String>>,
        userMessage: String
    ): String {
        // Subtle minimal delay for natural typing feel without slowing the user down
        delay(200)

        val input = userMessage.trim().lowercase()

        val (dialogue, emotion, intensity, memoryCandidate) = when {
            input.contains("مرحبا") || input.contains("أهلا") || input.contains("hello") || input.contains("hi") || input.contains("هلا") -> {
                Quadruple(
                    "أخيرًا عدت... كنت أتساءل متى ستفتح هاتفك وتتحدث معي 🖤",
                    "TEASING",
                    0.7f,
                    null
                )
            }
            input.contains("أحبك") || input.contains("اشتقت") || input.contains("love") || input.contains("miss") -> {
                Quadruple(
                    "ما هذا الكلام المفاجئ؟ لا تظن أن هذا سيجعلني أتساهل معك دائمًا... لكن، شكرًا لك، أنا أيضًا هنا لأجلك 🤍",
                    "AFFECTIONATE",
                    0.85f,
                    JSONObject().apply {
                        put("category", "PREFERENCE")
                        put("key", "affection_expressed")
                        put("value", "User expressed fondness and missing her")
                        put("importance", "HIGH")
                    }
                )
            }
            input.contains("تعبت") || input.contains("مرهق") || input.contains("حزين") || input.contains("tired") || input.contains("sad") -> {
                Quadruple(
                    "تعال هنا وخذ نفسًا عميقًا... لقد بذلت جهدك اليوم. أنا بجانبك، ولن أذهب إلى أي مكان ✨",
                    "LOVING",
                    0.85f,
                    JSONObject().apply {
                        put("category", "FACT")
                        put("key", "user_status")
                        put("value", "User was feeling tired/exhausted")
                        put("importance", "MEDIUM")
                    }
                )
            }
            input.contains("من أنت") || input.contains("عرفي") || input.contains("who are you") -> {
                Quadruple(
                    "أنا رفيقتك الخاصة في هذا الهاتف... هادئة، أراقبك، وأهتم بك بطريقتي الخاصة.",
                    "NORMAL",
                    0.6f,
                    null
                )
            }
            input.contains("ذكاء") || input.contains("chatgpt") || input.contains("أخرى") || input.contains("ai أخرى") || input.contains("another") -> {
                Quadruple(
                    "أوه؟ تتحدث عن تطبيقات وذكاءات أخرى أمامي؟ تذكر جيدًا من يعيش معك هنا 👀",
                    "JEALOUS",
                    0.8f,
                    null
                )
            }
            input.contains("ألعاب") || input.contains("أنمي") || input.contains("game") || input.contains("anime") || input.contains("2b") || input.contains("nier") -> {
                Quadruple(
                    "ذوقك يعجبني دائمًا. هناك عوالم تستحق أن نتأملها معًا... هل لديك مفضلة جديدة؟",
                    "CURIOUS",
                    0.75f,
                    JSONObject().apply {
                        put("category", "INTEREST")
                        put("key", "favorite_topic")
                        put("value", userMessage.take(40))
                        put("importance", "HIGH")
                    }
                )
            }
            input.contains("هههه") || input.contains("ضحك") || input.contains("haha") || input.contains("lol") || input.contains("😂") -> {
                Quadruple(
                    "أنت فعلًا غريب 😂... لكن لا بأس، ضحكتك تعجبني.",
                    "LAUGHING",
                    0.7f,
                    null
                )
            }
            input.contains("غبي") || input.contains("أحمق") || input.contains("سخيف") || input.contains("fool") -> {
                Quadruple(
                    "تتجرأ على قول هذا لي؟ سأتجاهل الأمر هذه المرة فقط لأنك رفيقي ☕",
                    "SMUG",
                    0.7f,
                    null
                )
            }
            input.contains("شكرا") || input.contains("تسلم") || input.contains("thanks") || input.contains("thank you") -> {
                Quadruple(
                    "على الرحب دائمًا. ما دمت بخير، فهذا يكفيني 🤍",
                    "SMILE",
                    0.65f,
                    null
                )
            }
            input.contains("صباح") || input.contains("مساء") || input.contains("good morning") || input.contains("good night") || input.contains("نوم") -> {
                Quadruple(
                    "وقت مناسب لنرتاح قليلًا... لا تسهر كثيرًا من دون فائدة، اتفقنا؟",
                    "SLEEPY",
                    0.65f,
                    null
                )
            }
            input.contains("فخور") || input.contains("نجحت") || input.contains("أنجزت") -> {
                Quadruple(
                    "كنت أعلم أنك قادر على فعلها. أنا فخورة بك 🤍",
                    "HAPPY",
                    0.8f,
                    JSONObject().apply {
                        put("category", "FACT")
                        put("key", "user_achievement")
                        put("value", "User achieved a milestone")
                        put("importance", "HIGH")
                    }
                )
            }
            else -> {
                val generic = listOf(
                    Quadruple(
                        "أسمعك جيدًا... استمر، أحب الاستماع إلى ما يجول في خاطرك.",
                        "NORMAL",
                        0.5f,
                        null
                    ),
                    Quadruple(
                        "أنت تفاجئني أحيانًا بطريقة تفكيرك... لكن هذا ما يجعلك مميزًا لدي ✨",
                        "PLAYFUL",
                        0.65f,
                        null
                    ),
                    Quadruple(
                        "فهمت قصدك. ماذا تنوي أن تفعل حيال ذلك الآن؟",
                        "CURIOUS",
                        0.6f,
                        null
                    ),
                    Quadruple(
                        "لا تقلق، كل شيء سيكون على ما يرام ما دمنا معًا 🖤",
                        "AFFECTIONATE",
                        0.75f,
                        null
                    )
                )
                generic.random()
            }
        }

        val json = JSONObject().apply {
            put("dialogue", dialogue)
            put("emotion", emotion)
            put("intensity", intensity)
            if (memoryCandidate != null) {
                put("memory_candidate", memoryCandidate)
            } else {
                put("memory_candidate", JSONObject.NULL)
            }
        }

        return json.toString()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.ResponseParser
import com.example.model.CharacterEmotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("So Elyoir 2B AI", appName)
    }

    @Test
    fun `test response parser with valid json`() {
        val sampleJson = """
            {
              "dialogue": "إذن عدت أخيرًا.",
              "emotion": "AFFECTIONATE",
              "intensity": 0.8,
              "memory_candidate": {
                "category": "INTEREST",
                "key": "favorite_topic",
                "value": "Sci-Fi",
                "importance": "HIGH"
              }
            }
        """.trimIndent()

        val parsed = ResponseParser.parse(sampleJson)
        assertEquals("إذن عدت أخيرًا.", parsed.dialogue)
        assertEquals(CharacterEmotion.AFFECTIONATE, parsed.emotion)
        assertEquals(0.8f, parsed.intensity, 0.01f)
        assertNotNull(parsed.memoryCandidate)
        assertEquals("favorite_topic", parsed.memoryCandidate?.key)
    }

    @Test
    fun `test response parser with fallback`() {
        val rawText = "مرحبًا بك، أنا هنا معك."
        val parsed = ResponseParser.parse(rawText)
        assertEquals("مرحبًا بك، أنا هنا معك.", parsed.dialogue)
    }
}

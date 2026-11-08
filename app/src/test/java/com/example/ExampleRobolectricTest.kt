package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.EmotionTag
import com.example.data.model.ProjectEntity
import com.example.data.remote.GeminiApiService
import com.example.security.SecureKeyStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EmotiVoice Studio", appName)
    }

    @Test
    fun `test secure key storage masking and deletion`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = SecureKeyStorage(context)

        val testKey = "AIzaSyDummyKey1234567890SecretKeyBengali"
        val masked = storage.maskString(testKey)
        assertTrue(masked.startsWith("AIzaSy"))
        assertTrue(masked.endsWith("gali"))
        assertTrue(masked.contains("••••••••"))

        storage.deleteApiKey()
        assertFalse(storage.hasApiKey())
    }

    @Test
    fun `test local script analyzer splits speakers and detects emotions`() {
        val apiService = GeminiApiService()
        val script = """
            নাগ: শোনো নাগিনী, আজ আর কোনো ভয় নেই!
            নাগিনী: কিন্তু তুমি একা কোথায় গিয়েছিলে? আমার খুব কষ্ট হচ্ছিল!
            কালনাগ: হা হা হা! তোরা কেউ রেহাই পাবি না!
        """.trimIndent()

        val parsed = apiService.analyzeScriptLocally(script)
        assertEquals(3, parsed.size)
        assertEquals("নাগ", parsed[0].speakerName)
        assertEquals("নাগিনী", parsed[1].speakerName)
        assertEquals("কালনাগ", parsed[2].speakerName)
        assertNotNull(parsed[0].primaryEmotion)
    }

    @Test
    fun `test Room database insertion and retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val projectDao = db.projectDao()

        val project = ProjectEntity(
            id = "test_proj_1",
            title = "পরীক্ষামূলক নাটক",
            originalScript = "নাগ: জয় হোক!"
        )
        projectDao.insertProject(project)

        val retrieved = projectDao.getProjectById("test_proj_1")
        assertNotNull(retrieved)
        assertEquals("পরীক্ষামূলক নাটক", retrieved?.title)

        projectDao.deleteProjectById("test_proj_1")
        val afterDelete = projectDao.getProjectById("test_proj_1")
        assertEquals(null, afterDelete)
    }

    @Test
    fun `test EmotionTag helper mapping`() {
        val angry = EmotionTag.fromString("Angry")
        assertEquals(EmotionTag.ANGRY, angry)
        assertEquals("রাগ", angry.bengaliName)

        val hurt = EmotionTag.fromString("অভিমান")
        assertEquals(EmotionTag.HURT, hurt)
        assertEquals("Hurt", hurt.englishName)
    }
}

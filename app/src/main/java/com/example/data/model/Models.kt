package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalScript: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val selectedAnalysisModel: String = "gemini-2.5-flash",
    val selectedTtsModel: String = "gemini-2.5-flash",
    val status: String = "draft" // draft, analyzed, generated
)

@Entity(tableName = "speakers")
data class SpeakerEntity(
    @PrimaryKey val speakerId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val displayName: String,
    val voiceProfileId: String,
    val characterDescription: String = ""
)

@Entity(tableName = "dialogues")
data class DialogueEntity(
    @PrimaryKey val dialogueId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val sequenceNumber: Int,
    val speakerId: String,
    val speakerName: String,
    val exactText: String,
    val primaryEmotion: String = "Calm", // English key matching EmotionTag
    val intensity: Int = 50, // 0 - 100
    val deliveryStyle: String = "Normal",
    val paceDirection: String = "normal", // slow, normal, fast
    val pitchDirection: String = "normal",
    val pauseBeforeMs: Int = 200,
    val pauseAfterMs: Int = 300,
    val approved: Boolean = true,
    val analysisConfidence: Float = 0.95f,
    val explanation: String = "",
    val vocalEffects: String = "",
    val pronunciationNotes: String = "",
    val currentAudioPath: String? = null
)

@Entity(tableName = "voice_profiles")
data class VoiceProfileEntity(
    @PrimaryKey val voiceProfileId: String = UUID.randomUUID().toString(),
    val name: String,
    val bengaliLabel: String,
    val characterAgeDescription: String,
    val voiceDescription: String,
    val geminiVoiceName: String, // Puck, Charon, Kore, Fenrir, Aoede
    val languageCode: String = "bn-IN", // Bengali (India) or Bengali (Bangladesh) bn-BD
    val accentNotes: String = "Standard Bengali / প্রমিত বাংলা",
    val isFavourite: Boolean = false,
    val isPreset: Boolean = true
)

@Entity(tableName = "audio_versions")
data class AudioVersionEntity(
    @PrimaryKey val audioVersionId: String = UUID.randomUUID().toString(),
    val dialogueId: String,
    val projectId: String,
    val versionNumber: Int = 1,
    val localFilePath: String,
    val mimeType: String = "audio/wav",
    val modelName: String,
    val voiceName: String,
    val promptSnapshot: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val isCurrent: Boolean = true
)

enum class EmotionTag(
    val englishName: String,
    val bengaliName: String,
    val emoji: String,
    val description: String
) {
    ANGRY("Angry", "রাগ", "😡", "তীব্র ক্রোধ ও ক্ষোভ"),
    HURT("Hurt", "অভিমান", "🥺", "মনের কষ্ট ও অভিযোগপূর্ণ অভিমান"),
    SAD("Sad", "দুঃখ", "😢", "গভীর বিষাদ ও বেদনা"),
    CRYING("Crying", "কান্না", "😭", "কান্নাভেজা কণ্ঠ ও অশ্রুপাত"),
    HAPPY("Happy", "আনন্দ", "😊", "হাসিখুশি ও তৃপ্ত ভাব"),
    LOVE("Love", "ভালোবাসা", "❤️", "স্নেহ, প্রণয় ও হৃদয়ের টান"),
    FEAR("Fear", "ভয়", "😨", "ভীত ও সন্ত্রস্ত কণ্ঠ"),
    JEALOUS("Jealous", "হিংসা", "😒", "ঈর্ষা ও অন্তর্দ্বন্দ্ব"),
    SURPRISED("Surprised", "বিস্ময়", "😲", "হতবাক ও চমকে ওঠা"),
    CONFIDENT("Confident", "আত্মবিশ্বাস", "😎", "দৃঢ় ও তেজস্বী কণ্ঠ"),
    SARCASTIC("Sarcastic", "ব্যঙ্গ", "😏", "উপহাস ও বক্রোক্তি"),
    CALM("Calm", "শান্ত", "😌", "স্থির ও নির্লিপ্ত সুর"),
    EXCITED("Excited", "উত্তেজনা", "🤩", "উদ্বেলিত ও উল্লসিত"),
    SUSPICIOUS("Suspicious", "সন্দেহ", "🤨", "অবিশ্বাসী ও সন্দিহান"),
    DISAPPOINTED("Disappointed", "হতাশা", "😞", "নিরাশ ও অবসাদগ্রস্ত"),
    NERVOUS("Nervous", "উদ্বেগ", "😰", "ভীতু ও কাঁপাকাঁপা উৎকণ্ঠা");

    companion object {
        fun fromString(value: String): EmotionTag {
            return entries.find { 
                it.englishName.equals(value, ignoreCase = true) ||
                it.bengaliName.equals(value, ignoreCase = true) ||
                it.name.equals(value, ignoreCase = true)
            } ?: CALM
        }
    }
}

enum class DeliveryStyle(
    val englishName: String,
    val bengaliName: String,
    val description: String
) {
    NORMAL("Normal", "স্বাভাবিক", "স্বাভাবিক কথোপকথন"),
    SOFT("Soft", "কোমল", "ধীর ও স্নিগ্ধ কণ্ঠ"),
    FIRM("Firm", "দৃঢ়", "দৃঢ় ও আদেশসূচক"),
    SHOUT("Shout", "চিৎকার", "উচ্চস্বর ও তীব্র ডাক"),
    WHISPER("Whisper", "ফিসফিস", "গোপন ও নীচু স্বরে ফিসফিসানি"),
    TREMBLING("Trembling", "কাঁপা কাঁপা", "ভয়ে বা কান্নায় কাঁপমান কণ্ঠ"),
    BREATHY("Breathy", "শ্বাসরুদ্ধ", "উদ্বেগ বা আবেগে ভরা শ্বাস"),
    RESTRAINED("Restrained", "সংযত", "রাগ বা কষ্ট চেপে রেখে বলা");

    companion object {
        fun fromString(value: String): DeliveryStyle {
            return entries.find { 
                it.englishName.equals(value, ignoreCase = true) || 
                it.bengaliName.equals(value, ignoreCase = true)
            } ?: NORMAL
        }
    }
}

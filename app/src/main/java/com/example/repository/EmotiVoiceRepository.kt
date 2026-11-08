package com.example.repository

import android.content.Context
import com.example.audio.AudioEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AudioVersionEntity
import com.example.data.model.DialogueEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SpeakerEntity
import com.example.data.model.VoiceProfileEntity
import com.example.data.remote.ApiTestResult
import com.example.data.remote.GeminiApiService
import com.example.security.SecureKeyStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class EmotiVoiceRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val projectDao = db.projectDao()
    private val speakerDao = db.speakerDao()
    private val dialogueDao = db.dialogueDao()
    private val voiceProfileDao = db.voiceProfileDao()
    private val audioVersionDao = db.audioVersionDao()

    val secureKeyStorage = SecureKeyStorage(context)
    val apiService = GeminiApiService()
    val audioEngine = AudioEngine(context)

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allVoiceProfiles: Flow<List<VoiceProfileEntity>> = voiceProfileDao.getAllVoiceProfiles()

    fun getDialogues(projectId: String): Flow<List<DialogueEntity>> = dialogueDao.getDialoguesForProject(projectId)
    fun getSpeakers(projectId: String): Flow<List<SpeakerEntity>> = speakerDao.getSpeakersForProject(projectId)
    fun getAudioVersions(dialogueId: String): Flow<List<AudioVersionEntity>> = audioVersionDao.getVersionsForDialogue(dialogueId)
    fun getAllProjectAudio(projectId: String): Flow<List<AudioVersionEntity>> = audioVersionDao.getVersionsForProject(projectId)

    suspend fun initializePresetsIfEmpty() = withContext(Dispatchers.IO) {
        if (voiceProfileDao.getCount() == 0) {
            val presets = listOf(
                VoiceProfileEntity(
                    voiceProfileId = "vp_adult_male",
                    name = "Adult Male (রোহিত)",
                    bengaliLabel = "তরুণ পুরুষ (২৮ বছর)",
                    characterAgeDescription = "Around 28-year-old confident Bengali male",
                    voiceDescription = "উষ্ণ, বলিষ্ঠ ও আত্মবিশ্বাসী কণ্ঠ",
                    geminiVoiceName = "Puck",
                    accentNotes = "Standard Bengali / প্রমিত বাংলা",
                    isFavourite = true
                ),
                VoiceProfileEntity(
                    voiceProfileId = "vp_senior_male",
                    name = "Senior Male (দাদু/গুরুমশাই)",
                    bengaliLabel = "প্রবীণ পুরুষ (৬০ বছর)",
                    characterAgeDescription = "Around 60-year-old experienced elder",
                    voiceDescription = "ভারী, গম্ভীর ও রাশভারী স্নেহময় সুর",
                    geminiVoiceName = "Fenrir",
                    accentNotes = "Traditional Bengali elder",
                    isFavourite = true
                ),
                VoiceProfileEntity(
                    voiceProfileId = "vp_adult_female",
                    name = "Adult Female (অনন্যা)",
                    bengaliLabel = "তরুণী নারী (২৬ বছর)",
                    characterAgeDescription = "Configurable adult female character",
                    voiceDescription = "স্নিগ্ধ, আবেগময় ও স্পষ্ট অভিব্যক্তি",
                    geminiVoiceName = "Kore",
                    accentNotes = "Standard Bengali narration & acting",
                    isFavourite = true
                ),
                VoiceProfileEntity(
                    voiceProfileId = "vp_senior_female",
                    name = "Senior Female (দিদিমা/মা)",
                    bengaliLabel = "প্রবীণা নারী (৫৮ বছর)",
                    characterAgeDescription = "Elderly mother or grandmother",
                    voiceDescription = "স্নেহময়ী, মায়াবী ও কোমল কণ্ঠ",
                    geminiVoiceName = "Aoede",
                    accentNotes = "Warm rural/urban Bengali grandmother",
                    isFavourite = false
                ),
                VoiceProfileEntity(
                    voiceProfileId = "vp_cartoon_child",
                    name = "Playful Character (টুম্পা/ছোট্টু)",
                    bengaliLabel = "ছোট শিশু বা কার্টুন চরিত্র",
                    characterAgeDescription = "Lively, energetic cartoon character",
                    voiceDescription = "চঞ্চল, তীক্ষ্ণ ও আনন্দময় কার্টুন ভাব",
                    geminiVoiceName = "Charon",
                    accentNotes = "Animated cartoon expression",
                    isFavourite = false
                ),
                VoiceProfileEntity(
                    voiceProfileId = "vp_narrator",
                    name = "Narrator (বর্ণনাকারী)",
                    bengaliLabel = "গল্পের সূত্রধর / ন্যারেটর",
                    characterAgeDescription = "Professional neutral storyteller",
                    voiceDescription = "স্থির, ভারসাম্যপূর্ণ ও প্রামাণ্য সুর",
                    geminiVoiceName = "Zephyr",
                    accentNotes = "Clear storytelling articulation",
                    isFavourite = true
                )
            )
            voiceProfileDao.insertVoiceProfiles(presets)
        }

        // Also create a sample starter project if no projects exist
        val currentProjects = projectDao.getProjectById("sample_project_snake")
        if (currentProjects == null) {
            createSampleSnakeDramaProject()
        }
    }

    suspend fun createProject(title: String, script: String): ProjectEntity = withContext(Dispatchers.IO) {
        val project = ProjectEntity(
            title = title.ifBlank { "নতুন চিত্রনাট্য (${System.currentTimeMillis() % 1000})" },
            originalScript = script,
            status = "draft"
        )
        projectDao.insertProject(project)
        project
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        dialogueDao.deleteDialoguesByProject(projectId)
        speakerDao.deleteSpeakersByProject(projectId)
        projectDao.deleteProjectById(projectId)
    }

    suspend fun analyzeAndPopulateProject(
        projectId: String,
        scriptText: String,
        forceLocal: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        val apiKey = secureKeyStorage.getApiKey()
        val parsedList = if (!forceLocal && !apiKey.isNullOrBlank()) {
            val aiResult = apiService.analyzeScriptWithAi(apiKey = apiKey, scriptText = scriptText)
            if (aiResult.isSuccess) {
                aiResult.getOrThrow()
            } else {
                apiService.analyzeScriptLocally(scriptText)
            }
        } else {
            apiService.analyzeScriptLocally(scriptText)
        }

        if (parsedList.isEmpty()) {
            return@withContext Result.failure(Exception("চিত্রনাট্য থেকে কোনো সংলাপ পাওয়া যায়নি"))
        }

        // Extract speakers
        val uniqueSpeakerNames = parsedList.map { it.speakerName }.distinct()
        val existingSpeakers = speakerDao.getSpeakersForProjectSync(projectId).associateBy { it.displayName }
        val newSpeakers = mutableListOf<SpeakerEntity>()

        val voiceProfiles = voiceProfileDao.getAllVoiceProfiles()
        val allVp = mutableListOf<VoiceProfileEntity>()
        // Simple default profile matching
        for (name in uniqueSpeakerNames) {
            if (!existingSpeakers.containsKey(name)) {
                val assignedVpId = when {
                    name.contains("নারী") || name.contains("নাগিনী") || name.contains("মা") || name.contains("মেয়ে") -> "vp_adult_female"
                    name.contains("দাদু") || name.contains("গুরু") || name.contains("বুড়ো") -> "vp_senior_male"
                    name.contains("শিশু") || name.contains("বাচ্চা") -> "vp_cartoon_child"
                    name.contains("বর্ণনাকারী") || name.contains("Narrator") -> "vp_narrator"
                    else -> "vp_adult_male"
                }
                newSpeakers.add(
                    SpeakerEntity(
                        speakerId = UUID.randomUUID().toString(),
                        projectId = projectId,
                        displayName = name,
                        voiceProfileId = assignedVpId,
                        characterDescription = "চরিত্র: $name"
                    )
                )
            }
        }
        if (newSpeakers.isNotEmpty()) {
            speakerDao.insertSpeakers(newSpeakers)
        }

        val allSpeakersMap = speakerDao.getSpeakersForProjectSync(projectId).associateBy { it.displayName }

        // Clean old dialogues and insert new
        dialogueDao.deleteDialoguesByProject(projectId)
        val dialoguesToInsert = parsedList.map { item ->
            val speaker = allSpeakersMap[item.speakerName]
            DialogueEntity(
                projectId = projectId,
                sequenceNumber = item.sequenceNumber,
                speakerId = speaker?.speakerId ?: "unknown_spk",
                speakerName = item.speakerName,
                exactText = item.exactText,
                primaryEmotion = item.primaryEmotion,
                intensity = item.intensity,
                deliveryStyle = item.deliveryStyle,
                paceDirection = item.paceDirection,
                pitchDirection = item.pitchDirection,
                pauseBeforeMs = item.pauseBeforeMs,
                pauseAfterMs = item.pauseAfterMs,
                explanation = item.explanation,
                pronunciationNotes = item.pronunciationNotes,
                approved = true
            )
        }
        dialogueDao.insertDialogues(dialoguesToInsert)

        // Update project status
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(status = "analyzed", updatedAt = System.currentTimeMillis()))
        }

        Result.success(dialoguesToInsert.size)
    }

    suspend fun updateDialogue(dialogue: DialogueEntity) = withContext(Dispatchers.IO) {
        dialogueDao.updateDialogue(dialogue)
    }

    suspend fun updateSpeaker(speaker: SpeakerEntity) = withContext(Dispatchers.IO) {
        speakerDao.insertSpeaker(speaker)
    }

    suspend fun insertVoiceProfile(profile: VoiceProfileEntity) = withContext(Dispatchers.IO) {
        voiceProfileDao.insertVoiceProfile(profile)
    }

    suspend fun generateDialogueAudio(
        dialogue: DialogueEntity,
        preferDeviceTts: Boolean = false
    ): Result<File> = withContext(Dispatchers.IO) {
        val speaker = speakerDao.getSpeakersForProjectSync(dialogue.projectId)
            .find { it.speakerId == dialogue.speakerId || it.displayName == dialogue.speakerName }
        val voiceProfile = speaker?.voiceProfileId?.let { voiceProfileDao.getVoiceProfileById(it) }

        val audioDir = File(context.filesDir, "audio/${dialogue.projectId}")
        audioDir.mkdirs()

        val versionCount = audioVersionDao.getVersionCountForDialogue(dialogue.dialogueId) + 1
        val safeSpeaker = dialogue.speakerName.replace(Regex("[^a-zA-Z0-9_]"), "_").take(15)
        val fileName = "D_${dialogue.sequenceNumber}_${safeSpeaker}_v${versionCount}.wav"
        val outputFile = File(audioDir, fileName)

        val apiKey = secureKeyStorage.getApiKey()
        var generationSuccess = false
        var usedModel = "Device-TTS"

        // Attempt Gemini API Speech generation if key is present and not explicitly preferDeviceTts
        if (!preferDeviceTts && !apiKey.isNullOrBlank()) {
            val geminiVoice = voiceProfile?.geminiVoiceName ?: "Puck"
            val geminiResult = apiService.generateGeminiAudio(
                apiKey = apiKey,
                modelName = "gemini-2.5-flash",
                dialogue = dialogue,
                voiceName = geminiVoice,
                outputFile = outputFile
            )
            if (geminiResult.isSuccess) {
                generationSuccess = true
                usedModel = "gemini-2.5-flash ($geminiVoice)"
            }
        }

        // Fallback or explicit Device TTS
        if (!generationSuccess) {
            generationSuccess = audioEngine.synthesizeWithDeviceTts(
                dialogue = dialogue,
                voiceProfile = voiceProfile,
                outputFile = outputFile
            )
            usedModel = "Android-Native-TTS"
        }

        if (generationSuccess && outputFile.exists() && outputFile.length() > 0) {
            dialogueDao.updateAudioPath(dialogue.dialogueId, outputFile.absolutePath)
            val audioVersion = AudioVersionEntity(
                dialogueId = dialogue.dialogueId,
                projectId = dialogue.projectId,
                versionNumber = versionCount,
                localFilePath = outputFile.absolutePath,
                mimeType = "audio/wav",
                modelName = usedModel,
                voiceName = voiceProfile?.name ?: "Default",
                promptSnapshot = "ভাব: ${dialogue.primaryEmotion} (${dialogue.intensity}%), শৈলী: ${dialogue.deliveryStyle}",
                fileSizeBytes = outputFile.length(),
                isCurrent = true
            )
            audioVersionDao.insertAudioVersion(audioVersion)
            Result.success(outputFile)
        } else {
            Result.failure(Exception("অডিও রেকর্ডিং ব্যর্থ হয়েছে। অনুগ্রহ করে ইন্টারনেট বা ডিভাইসের TTS চেক করুন।"))
        }
    }

    suspend fun getProjectById(projectId: String): ProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(projectId)
    }

    suspend fun testGeminiApiKey(apiKey: String): ApiTestResult {
        return apiService.testApiKey(apiKey)
    }

    private suspend fun createSampleSnakeDramaProject() {
        val sampleScript = """
            নাগ: শোনো নাগিনী, আজ আর কোনো ভয় নেই! নাগমণি আজ আমাদের অধিকারে এসেছে!
            নাগিনী: (অভিমানভরা চোখে) কিন্তু তুমি আমাকে একা ফেলে কেন গেলে? আমি কতটা ভয় পেয়েছিলাম জানো?
            নাগ: (শান্ত ও স্নেহভরে) আমি তোমার সুরক্ষার জন্যই একাকী সেই গভীর গুহায় গিয়েছিলাম। আমায় ভুল বুঝো না।
            কালনাগ: (অট্টহাস্য করে চিৎকার) হা হা হা! ভাবিস না এত সহজে নাগমণি তোদের হবে! তোরা কেউ আজ জীবিত ফিরবি না!
            নাগিনী: (আতঙ্কিত হয়ে) নাগ, বাঁচাও! ও বিষ ছড়াচ্ছে!
            নাগ: (তীব্র গর্জনে) কালনাগ, তোর বিষ আজ তোকেই ভস্ম করবে! প্রস্তুত হ যুদ্ধের জন্য!
        """.trimIndent()

        val project = ProjectEntity(
            id = "sample_project_snake",
            title = "নাগ-নাগিনীর রহস্য ও প্রেম (সাপের নাটক)",
            originalScript = sampleScript,
            status = "analyzed"
        )
        projectDao.insertProject(project)

        val speakers = listOf(
            SpeakerEntity("spk_nag", "sample_project_snake", "নাগ", "vp_adult_male", "বীর ও সাহসী নায়ক সাপ"),
            SpeakerEntity("spk_nagini", "sample_project_snake", "নাগিনী", "vp_adult_female", "অভিমানী ও প্রেমময়ী সঙ্গিনী"),
            SpeakerEntity("spk_kalnag", "sample_project_snake", "কালনাগ", "vp_senior_male", "ভয়ানক দুষ্ট প্রতিপক্ষ")
        )
        speakerDao.insertSpeakers(speakers)

        val dialogues = listOf(
            DialogueEntity(
                dialogueId = "d_snake_1",
                projectId = "sample_project_snake",
                sequenceNumber = 1,
                speakerId = "spk_nag",
                speakerName = "নাগ",
                exactText = "শোনো নাগিনী, আজ আর কোনো ভয় নেই! নাগমণি আজ আমাদের অধিকারে এসেছে!",
                primaryEmotion = "Excited",
                intensity = 85,
                deliveryStyle = "Firm",
                explanation = "বিজয়ের আনন্দ ও বীরত্বব্যঞ্জক ঘোষণা"
            ),
            DialogueEntity(
                dialogueId = "d_snake_2",
                projectId = "sample_project_snake",
                sequenceNumber = 2,
                speakerId = "spk_nagini",
                speakerName = "নাগিনী",
                exactText = "কিন্তু তুমি আমাকে একা ফেলে কেন গেলে? আমি কতটা ভয় পেয়েছিলাম জানো?",
                primaryEmotion = "Hurt",
                intensity = 75,
                deliveryStyle = "Breathy",
                explanation = "প্রেমিকের প্রতি মিষ্টি অভিমান ও উদ্বেগের প্রকাশ"
            ),
            DialogueEntity(
                dialogueId = "d_snake_3",
                projectId = "sample_project_snake",
                sequenceNumber = 3,
                speakerId = "spk_nag",
                speakerName = "নাগ",
                exactText = "আমি তোমার সুরক্ষার জন্যই একাকী সেই গভীর গুহায় গিয়েছিলাম। আমায় ভুল বুঝো না।",
                primaryEmotion = "Love",
                intensity = 65,
                deliveryStyle = "Soft",
                explanation = "সঙ্গিনীকে আশ্বস্ত করা ও ভালোবাসার সান্ত্বনা"
            ),
            DialogueEntity(
                dialogueId = "d_snake_4",
                projectId = "sample_project_snake",
                sequenceNumber = 4,
                speakerId = "spk_kalnag",
                speakerName = "কালনাগ",
                exactText = "হা হা হা! ভাবিস না এত সহজে নাগমণি তোদের হবে! তোরা কেউ আজ জীবিত ফিরবি না!",
                primaryEmotion = "Angry",
                intensity = 95,
                deliveryStyle = "Shout",
                explanation = "ভিলেন চরিত্রের হুঙ্কার ও দাম্ভিক হুমকি"
            ),
            DialogueEntity(
                dialogueId = "d_snake_5",
                projectId = "sample_project_snake",
                sequenceNumber = 5,
                speakerId = "spk_nagini",
                speakerName = "নাগিনী",
                exactText = "নাগ, বাঁচাও! ও বিষ ছড়াচ্ছে!",
                primaryEmotion = "Fear",
                intensity = 90,
                deliveryStyle = "Trembling",
                explanation = "আকস্মিক বিপদ ও প্রাণভয়ে আর্তনাদ"
            ),
            DialogueEntity(
                dialogueId = "d_snake_6",
                projectId = "sample_project_snake",
                sequenceNumber = 6,
                speakerId = "spk_nag",
                speakerName = "নাগ",
                exactText = "কালনাগ, তোর বিষ আজ তোকেই ভস্ম করবে! প্রস্তুত হ যুদ্ধের জন্য!",
                primaryEmotion = "Confident",
                intensity = 90,
                deliveryStyle = "Firm",
                explanation = "প্রতিশোধের সংকল্প ও পাল্টা আক্রমণাত্মক হুঙ্কার"
            )
        )
        dialogueDao.insertDialogues(dialogues)
    }

    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val totalAudioBytes = audioVersionDao.getTotalAudioSizeBytes() ?: 0L
        val audioDir = File(context.filesDir, "audio")
        val fileCount = audioDir.walkTopDown().filter { it.isFile }.count()
        val freeSpaceBytes = context.filesDir.freeSpace
        StorageStats(
            totalAudioBytes = totalAudioBytes,
            fileCount = fileCount,
            freeSpaceBytes = freeSpaceBytes
        )
    }
}

data class StorageStats(
    val totalAudioBytes: Long,
    val fileCount: Int,
    val freeSpaceBytes: Long
)

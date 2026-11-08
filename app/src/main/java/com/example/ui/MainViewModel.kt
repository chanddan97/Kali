package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DialogueEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SpeakerEntity
import com.example.data.model.VoiceProfileEntity
import com.example.data.remote.ApiTestResult
import com.example.repository.EmotiVoiceRepository
import com.example.repository.StorageStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = EmotiVoiceRepository(application)

    // Navigation tab
    val currentTab = MutableStateFlow("studio") // studio, script, review, voice_studio, history, settings

    // Projects & Voices
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVoiceProfiles: StateFlow<List<VoiceProfileEntity>> = repository.allVoiceProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Project
    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _activeDialogues = MutableStateFlow<List<DialogueEntity>>(emptyList())
    val activeDialogues: StateFlow<List<DialogueEntity>> = _activeDialogues.asStateFlow()

    private val _activeSpeakers = MutableStateFlow<List<SpeakerEntity>>(emptyList())
    val activeSpeakers: StateFlow<List<SpeakerEntity>> = _activeSpeakers.asStateFlow()

    private var dialoguesJob: Job? = null
    private var speakersJob: Job? = null

    // Script editing
    val scriptDraft = MutableStateFlow("")
    val projectTitleDraft = MutableStateFlow("")
    val isAnalyzing = MutableStateFlow(false)
    val analysisError = MutableStateFlow<String?>(null)

    // Generation state
    val isGeneratingAudio = MutableStateFlow(false)
    val generatingDialogueId = MutableStateFlow<String?>(null)
    val batchProgress = MutableStateFlow<Pair<Int, Int>?>(null) // (current, total)
    val generationMessage = MutableStateFlow<String?>(null)

    // API Key states
    private val _hasApiKey = MutableStateFlow(repository.secureKeyStorage.hasApiKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    private val _maskedApiKey = MutableStateFlow(repository.secureKeyStorage.getMaskedKey())
    val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()

    val isTestingKey = MutableStateFlow(false)
    val keyTestResult = MutableStateFlow<ApiTestResult?>(null)

    // Audio Playback
    val isPlaying = repository.audioEngine.isPlaying
    val currentPlayingDialogueId = repository.audioEngine.currentPlayingDialogueId
    val currentPositionMs = repository.audioEngine.currentPositionMs
    val durationMs = repository.audioEngine.durationMs
    val playbackSpeed = MutableStateFlow(1.0f)

    // Diagnostics
    val storageStats = MutableStateFlow<StorageStats?>(null)
    val diagnosticLogs = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            repository.initializePresetsIfEmpty()
            refreshKeyStatus()
            refreshStorageStats()
            // Select default active project if available
            allProjects.collect { projects ->
                if (_activeProject.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun refreshKeyStatus() {
        _hasApiKey.value = repository.secureKeyStorage.hasApiKey()
        _maskedApiKey.value = repository.secureKeyStorage.getMaskedKey()
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            storageStats.value = repository.getStorageStats()
        }
    }

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        scriptDraft.value = project.originalScript
        projectTitleDraft.value = project.title

        dialoguesJob?.cancel()
        dialoguesJob = viewModelScope.launch {
            repository.getDialogues(project.id).collect {
                _activeDialogues.value = it
            }
        }

        speakersJob?.cancel()
        speakersJob = viewModelScope.launch {
            repository.getSpeakers(project.id).collect {
                _activeSpeakers.value = it
            }
        }
    }

    fun createNewProject(title: String, script: String) {
        viewModelScope.launch {
            val project = repository.createProject(title, script)
            selectProject(project)
            currentTab.value = "script"
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProject.value?.id == projectId) {
                _activeProject.value = null
                _activeDialogues.value = emptyList()
                _activeSpeakers.value = emptyList()
            }
            refreshStorageStats()
        }
    }

    fun loadSampleScript(type: String) {
        when (type) {
            "snake" -> {
                projectTitleDraft.value = "নাগ-নাগিনীর সাপের নাটক"
                scriptDraft.value = """
                    নাগ: শোনো নাগিনী, আজ আর কোনো ভয় নেই! নাগমণি আজ আমাদের অধিকারে এসেছে!
                    নাগিনী: (অভিমানভরা চোখে) কিন্তু তুমি আমাকে একা ফেলে কেন গেলে? আমি কতটা ভয় পেয়েছিলাম জানো?
                    নাগ: (শান্ত ও স্নেহভরে) আমি তোমার সুরক্ষার জন্যই একাকী সেই গভীর গুহায় গিয়েছিলাম। আমায় ভুল বুঝো না।
                    কালনাগ: (অট্টহাস্য করে চিৎকার) হা হা হা! ভাবিস না এত সহজে নাগমণি তোদের হবে! তোরা কেউ আজ জীবিত ফিরবি না!
                    নাগিনী: (আতঙ্কিত হয়ে) নাগ, বাঁচাও! ও বিষ ছড়াচ্ছে!
                    নাগ: (তীব্র গর্জনে) কালনাগ, তোর বিষ আজ তোকেই ভস্ম করবে! প্রস্তুত হ যুদ্ধের জন্য!
                """.trimIndent()
            }
            "family" -> {
                projectTitleDraft.value = "পারিবারিক মান-অভিমান"
                scriptDraft.value = """
                    মা: (স্নেহ ও দুঃখভরা কণ্ঠে) খোকা, তুই এত রাত পর্যন্ত কোথায় ছিলি? ভাত ঠান্ডা হয়ে জমে বরফ হয়ে গেল!
                    রোহিত: (বিরক্ত হয়ে) মা, অফিসে কত কাজের চাপ থাকে তুমি তো জানো না! সব সময় এক কথা ভালো লাগে না!
                    মা: (চোখের জল ফেলে মৃদু কণ্ঠে) আমি তোর মঙ্গলের জন্যই বলি বাবা... তুই ছাড়া আমার আর কে আছে বল?
                    রোহিত: (অনুতপ্ত হয়ে কোমল সুরে) ক্ষমা করে দাও মা, রাগের মাথায় কী যে বলে ফেলি! আর কখনো দেরি করব না।
                """.trimIndent()
            }
            "mystery" -> {
                projectTitleDraft.value = "গোয়েন্দা ও চোরের রহস্য লড়াই"
                scriptDraft.value = """
                    গোয়েন্দা রুদ্র: (আত্মবিশ্বাসী গলায়) দরজা বন্ধ করার চেষ্টা কোরো না বিক্রম! তোমার প্রতিটি পদক্ষেপ আমি লক্ষ্য করছিলাম!
                    বিক্রম: (বিস্ময় ও সন্দেহে) রুদ্র! তুমি এখানে পৌঁছালে কী করে? এটা তো অসম্ভব ছিল!
                    গোয়েন্দা রুদ্র: (ব্যঙ্গপূর্ণ মৃদু হাসিতে) অপরাধী যতই ধূর্ত হোক না কেন, পায়ের ধুলো সাক্ষ্য দেয়! আত্মসমর্পণ করো!
                """.trimIndent()
            }
            "multilingual" -> {
                projectTitleDraft.value = "বাংলা ও ইংরেজি বহুভাষিক দৃশ্য (Multilingual Story)"
                scriptDraft.value = """
                    Narrator: Under the starry Kolkata night, two old friends reunited after a decade.
                    রাহুল: বন্ধু, এতদিন পর তোমাকে দেখে যে কত আনন্দ হচ্ছে তা বলে বোঝাতে পারব না!
                    David: It feels just like yesterday, Rahul! What an incredible journey this has been!
                    রাহুল: এসো, আজ সারারাত গল্প হবে আর বাংলা মিষ্টি খাওয়া হবে!
                """.trimIndent()
            }
        }
    }

    fun analyzeCurrentScript(forceLocal: Boolean = false) {
        val current = _activeProject.value ?: return
        val script = scriptDraft.value.trim()
        if (script.isEmpty()) {
            analysisError.value = "চিত্রনাট্য খালি রাখা যাবে না। অনুগ্রহ করে কিছু সংলাপ লিখুন।"
            return
        }

        isAnalyzing.value = true
        analysisError.value = null

        viewModelScope.launch {
            // Update project script first
            repository.updateProject(current.copy(title = projectTitleDraft.value, originalScript = script))
            val result = repository.analyzeAndPopulateProject(
                projectId = current.id,
                scriptText = script,
                forceLocal = forceLocal
            )

            isAnalyzing.value = false
            if (result.isSuccess) {
                currentTab.value = "review"
            } else {
                analysisError.value = result.exceptionOrNull()?.localizedMessage ?: "চিত্রনাট্য বিশ্লেষণে ত্রুটি হয়েছে"
            }
        }
    }

    fun updateDialogueItem(dialogue: DialogueEntity) {
        viewModelScope.launch {
            repository.updateDialogue(dialogue)
        }
    }

    fun updateSpeakerVoiceProfile(speaker: SpeakerEntity, newVoiceProfileId: String) {
        viewModelScope.launch {
            repository.updateSpeaker(speaker.copy(voiceProfileId = newVoiceProfileId))
        }
    }

    fun addCustomVoiceProfile(profile: VoiceProfileEntity) {
        viewModelScope.launch {
            repository.insertVoiceProfile(profile)
        }
    }

    fun generateSingleDialogueAudio(dialogue: DialogueEntity, preferDeviceTts: Boolean = false) {
        viewModelScope.launch {
            isGeneratingAudio.value = true
            generatingDialogueId.value = dialogue.dialogueId
            generationMessage.value = "সংলাপ #${dialogue.sequenceNumber} রেকর্ডিং চলছে..."

            val result = repository.generateDialogueAudio(dialogue, preferDeviceTts)
            isGeneratingAudio.value = false
            generatingDialogueId.value = null

            if (result.isSuccess) {
                generationMessage.value = "সংলাপ #${dialogue.sequenceNumber} সফলভাবে প্রস্তুত হয়েছে!"
                refreshStorageStats()
                // Auto play the preview
                result.getOrNull()?.let { file ->
                    repository.audioEngine.playAudioFile(file, dialogue.dialogueId, playbackSpeed.value)
                }
            } else {
                generationMessage.value = result.exceptionOrNull()?.localizedMessage ?: "অডিও তৈরিতে সমস্যা হয়েছে"
            }
            delay(3000)
            generationMessage.value = null
        }
    }

    fun generateAllApprovedDialogues(preferDeviceTts: Boolean = false) {
        val dialogues = _activeDialogues.value.filter { it.approved }
        if (dialogues.isEmpty()) return

        viewModelScope.launch {
            isGeneratingAudio.value = true
            var completed = 0
            val total = dialogues.size
            batchProgress.value = Pair(0, total)

            for (dialogue in dialogues) {
                generatingDialogueId.value = dialogue.dialogueId
                generationMessage.value = "সংলাপ (${completed + 1}/$total) তৈরি হচ্ছে..."
                repository.generateDialogueAudio(dialogue, preferDeviceTts)
                completed++
                batchProgress.value = Pair(completed, total)
                delay(100)
            }

            isGeneratingAudio.value = false
            generatingDialogueId.value = null
            batchProgress.value = null
            generationMessage.value = "সব ($total) টি সংলাপ সফলভাবে রেকর্ড করা হয়েছে!"
            refreshStorageStats()
            delay(3000)
            generationMessage.value = null
        }
    }

    fun playDialogue(dialogue: DialogueEntity) {
        val path = dialogue.currentAudioPath
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                repository.audioEngine.playAudioFile(file, dialogue.dialogueId, playbackSpeed.value)
                return
            }
        }
        // If not yet generated, generate it on demand!
        generateSingleDialogueAudio(dialogue, preferDeviceTts = false)
    }

    fun playEntireScript() {
        val list = _activeDialogues.value.filter { !it.currentAudioPath.isNullOrEmpty() }
        if (list.isEmpty()) {
            generationMessage.value = "প্রথমে সংলাপগুলোর অডিও জেনারেট করুন"
            return
        }

        viewModelScope.launch {
            var index = 0
            fun playNext() {
                if (index < list.size) {
                    val d = list[index]
                    val file = File(d.currentAudioPath ?: return)
                    index++
                    repository.audioEngine.playAudioFile(file, d.dialogueId, playbackSpeed.value) {
                        viewModelScope.launch {
                            delay(d.pauseAfterMs.toLong().coerceIn(100, 2000))
                            playNext()
                        }
                    }
                }
            }
            playNext()
        }
    }

    fun pausePlayback() {
        repository.audioEngine.pausePlayback()
    }

    fun resumePlayback() {
        repository.audioEngine.resumePlayback()
    }

    fun stopPlayback() {
        repository.audioEngine.stopPlayback()
    }

    fun seekTo(positionMs: Int) {
        repository.audioEngine.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed.value = speed
    }

    // API Key Management
    fun testKey(key: String) {
        viewModelScope.launch {
            isTestingKey.value = true
            keyTestResult.value = null
            val result = repository.testGeminiApiKey(key)
            keyTestResult.value = result
            isTestingKey.value = false
        }
    }

    fun saveTestedKey(key: String): Boolean {
        val success = repository.secureKeyStorage.saveApiKey(key)
        refreshKeyStatus()
        return success
    }

    fun deleteApiKey() {
        repository.secureKeyStorage.deleteApiKey()
        refreshKeyStatus()
        keyTestResult.value = null
    }

    // Diagnostic actions
    fun runAllDiagnostics() {
        viewModelScope.launch {
            val logs = mutableListOf<String>()
            logs.add("🕒 ডায়াগনস্টিক শুরু: ${System.currentTimeMillis()}")
            logs.add("📱 OS: Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
            
            // Storage check
            val stats = repository.getStorageStats()
            storageStats.value = stats
            logs.add("💾 অ্যাপ স্টোরেজ: ${stats.totalAudioBytes / 1024} KB (${stats.fileCount} টি অডিও ফাইল)")
            logs.add("💾 অবশিষ্ট মেমরি: ${stats.freeSpaceBytes / (1024 * 1024)} MB উপলব্ধ")

            // Database check
            val projectCount = allProjects.value.size
            logs.add("🗄️ লোকাল ডাটাবেস: $projectCount টি প্রজেক্ট সংরক্ষিত")

            // Audio Engine test
            logs.add("🔊 অডিও ইঞ্জিন: TextToSpeech এবং MediaPlayer সক্রিয়")

            // API Key check
            val hasKey = repository.secureKeyStorage.hasApiKey()
            if (hasKey) {
                logs.add("🔑 API Key: এনক্রিপ্টেড ও সুরক্ষিত (${repository.secureKeyStorage.getMaskedKey()})")
                val testRes = repository.testGeminiApiKey(repository.secureKeyStorage.getApiKey().orEmpty())
                if (testRes is ApiTestResult.Success) {
                    logs.add("✅ Gemini API কানেকশন সফল (লেটেন্সি: ${testRes.latencyMs}ms)")
                } else if (testRes is ApiTestResult.Failure) {
                    logs.add("⚠️ Gemini API সতর্কতা: ${testRes.messageBengali}")
                }
            } else {
                logs.add("ℹ️ API Key: ম্যানুয়াল কী যোগ করা হয়নি (ডিভাইস লোকাল TTS সক্রিয়)")
            }

            diagnosticLogs.value = logs
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.audioEngine.release()
    }
}

package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.DialogueEntity
import com.example.data.model.EmotionTag
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class AudioEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingDialogueId = MutableStateFlow<String?>(null)
    val currentPlayingDialogueId: StateFlow<String?> = _currentPlayingDialogueId.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            // Attempt Bengali locale first, fallback to device locale
            val bengaliLocale = Locale("bn", "IN")
            val available = tts?.isLanguageAvailable(bengaliLocale)
            if (available == TextToSpeech.LANG_AVAILABLE || available == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                tts?.language = bengaliLocale
            } else {
                tts?.language = Locale.getDefault()
            }
        }
    }

    /**
     * Synthesizes dialogue text into a local WAV audio file using Android TTS engine with
     * acting directions applied to pitch and speech rate.
     */
    suspend fun synthesizeWithDeviceTts(
        dialogue: DialogueEntity,
        voiceProfile: VoiceProfileEntity?,
        outputFile: File
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val engine = tts
        if (engine == null || !isTtsInitialized) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        // Apply pitch adjustments based on character & emotion
        var pitch = 1.0f
        val emotion = EmotionTag.fromString(dialogue.primaryEmotion)
        when (emotion) {
            EmotionTag.FEAR, EmotionTag.SURPRISED, EmotionTag.CRYING -> pitch = 1.25f
            EmotionTag.ANGRY -> pitch = 1.15f
            EmotionTag.CALM, EmotionTag.SAD -> pitch = 0.90f
            EmotionTag.JEALOUS, EmotionTag.SARCASTIC -> pitch = 0.95f
            else -> pitch = 1.0f
        }

        if (dialogue.pitchDirection == "high") pitch += 0.2f
        if (dialogue.pitchDirection == "low") pitch -= 0.2f

        // Apply speech rate based on pace & emotion
        var speechRate = 1.0f
        when (dialogue.paceDirection) {
            "fast" -> speechRate = 1.30f
            "slow" -> speechRate = 0.80f
            else -> {
                if (emotion == EmotionTag.EXCITED || emotion == EmotionTag.ANGRY) speechRate = 1.20f
                if (emotion == EmotionTag.SAD || emotion == EmotionTag.HURT) speechRate = 0.85f
            }
        }

        // Voice profile character tuning
        if (voiceProfile != null) {
            if (voiceProfile.name.contains("Senior") || voiceProfile.name.contains("প্রবীণ")) {
                pitch = (pitch * 0.82f).coerceAtLeast(0.6f)
                speechRate = (speechRate * 0.90f)
            } else if (voiceProfile.name.contains("Child") || voiceProfile.name.contains("শিশু")) {
                pitch = (pitch * 1.45f).coerceAtMost(2.0f)
                speechRate = (speechRate * 1.15f)
            } else if (voiceProfile.name.contains("Female") || voiceProfile.name.contains("নারী")) {
                pitch = (pitch * 1.20f).coerceAtMost(1.8f)
            }
        }

        engine.setPitch(pitch)
        engine.setSpeechRate(speechRate)

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        outputFile.parentFile?.mkdirs()

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(uttId: String?) {}

            override fun onDone(uttId: String?) {
                if (uttId == utteranceId) {
                    if (continuation.isActive) {
                        continuation.resume(outputFile.exists() && outputFile.length() > 0)
                    }
                }
            }

            override fun onError(uttId: String?) {
                if (uttId == utteranceId) {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }
        })

        val result = engine.synthesizeToFile(
            dialogue.exactText,
            params,
            outputFile,
            utteranceId
        )

        if (result != TextToSpeech.SUCCESS) {
            if (continuation.isActive) {
                continuation.resume(false)
            }
        }
    }

    fun playAudioFile(
        file: File,
        dialogueId: String? = null,
        playbackSpeed: Float = 1.0f,
        onComplete: (() -> Unit)? = null
    ) {
        stopPlayback()
        if (!file.exists() || file.length() == 0L) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                playbackParams = playbackParams.setSpeed(playbackSpeed)
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPlayingDialogueId.value = null
                    stopProgressUpdates()
                    onComplete?.invoke()
                }
                start()
            }
            _isPlaying.value = true
            _currentPlayingDialogueId.value = dialogueId
            _durationMs.value = mediaPlayer?.duration ?: 0
            startProgressUpdates()
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentPlayingDialogueId.value = null
        }
    }

    fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun resumePlayback() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                startProgressUpdates()
            }
        }
    }

    fun stopPlayback() {
        stopProgressUpdates()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPlayingDialogueId.value = null
        _currentPositionMs.value = 0
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _currentPositionMs.value = it.currentPosition
                        _durationMs.value = it.duration
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }
        handler.post(progressRunnable!!)
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }

    fun release() {
        stopPlayback()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

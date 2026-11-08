package com.example.data.remote

import com.example.data.model.DialogueEntity
import com.example.data.model.EmotionTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class ApiTestResult {
    data class Success(val latencyMs: Long, val availableModelsCount: Int) : ApiTestResult()
    data class Failure(val messageBengali: String, val technicalDetail: String, val httpCode: Int? = null) : ApiTestResult()
}

data class ParsedDialogueItem(
    val sequenceNumber: Int,
    val speakerName: String,
    val exactText: String,
    val primaryEmotion: String,
    val intensity: Int,
    val deliveryStyle: String,
    val paceDirection: String,
    val pitchDirection: String,
    val pauseBeforeMs: Int,
    val pauseAfterMs: Int,
    val explanation: String,
    val pronunciationNotes: String
)

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testApiKey(apiKey: String): ApiTestResult = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            return@withContext ApiTestResult.Failure(
                messageBengali = "API কী খালি হতে পারে না। অনুগ্রহ করে একটি সঠিক Gemini API Key প্রদান করুন।",
                technicalDetail = "Empty API key provided"
            )
        }

        val startTime = System.currentTimeMillis()
        val url = "https://generativelanguage.googleapis.com/v1beta/models?pageSize=10"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", trimmed)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val code = response.code
                val bodyString = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val models = json.optJSONArray("models")
                    val count = models?.length() ?: 0
                    ApiTestResult.Success(latencyMs = latency, availableModelsCount = count)
                } else {
                    val errorMessage = when (code) {
                        400 -> "API কী-এর ফরম্যাট সঠিক নয় অথবা অনুরোধ বাতিল হয়েছে।"
                        403 -> "অননুমোদিত বা ভুল API কী! অনুগ্রহ করে Google AI Studio থেকে নতুন কী সংগ্রহ করুন।"
                        429 -> "API কোটা বা ব্যবহারের সীমা অতিক্রান্ত হয়েছে।"
                        else -> "API সংযোগ ব্যর্থ হয়েছে (HTTP $code)"
                    }
                    ApiTestResult.Failure(
                        messageBengali = errorMessage,
                        technicalDetail = "HTTP $code - ${bodyString.take(150)}",
                        httpCode = code
                    )
                }
            }
        } catch (e: Exception) {
            ApiTestResult.Failure(
                messageBengali = "ইন্টারনেট সংযোগ সমস্যা বা সার্ভার টাইমআউট। অনুগ্রহ করে নেটওয়ার্ক পরীক্ষা করুন।",
                technicalDetail = e.localizedMessage ?: "Unknown network error"
            )
        }
    }

    suspend fun analyzeScriptWithAi(
        apiKey: String,
        modelName: String = "gemini-2.5-flash",
        scriptText: String
    ): Result<List<ParsedDialogueItem>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("No Gemini API Key set"))
        }

        val systemInstruction = """
            You are a Bengali dialogue acting director. Analyse the supplied script without rewriting any dialogue. 
            Identify speakers, split dialogue units, infer scene context and propose appropriate emotion and delivery directions. 
            Return valid JSON matching the requested schema. Preserve every original dialogue character exactly. 
            Do not infer emotion from punctuation alone. Mark uncertain speaker or emotion assignments for user review. 
            Use Bengali labels and concise Bengali explanations. Emotion intensity is a subjective acting suggestion from 0 to 100, not a factual measurement.
            
            Supported emotion labels: Angry, Hurt, Sad, Crying, Happy, Love, Fear, Jealous, Surprised, Confident, Sarcastic, Calm, Excited, Suspicious, Disappointed, Nervous.
            Supported delivery styles: Normal, Soft, Firm, Shout, Whisper, Trembling, Breathy, Restrained.
            
            Return JSON in this exact structure:
            {
              "dialogues": [
                {
                  "sequenceNumber": 1,
                  "speakerName": "Speaker Name",
                  "exactText": "Preserved exact dialogue text",
                  "primaryEmotion": "Angry",
                  "intensity": 75,
                  "deliveryStyle": "Shout",
                  "paceDirection": "fast",
                  "pitchDirection": "high",
                  "pauseBeforeMs": 200,
                  "pauseAfterMs": 400,
                  "explanation": "সংক্ষিপ্ত অভিনয়ের কারণ",
                  "pronunciationNotes": ""
                }
              ]
            }
        """.trimIndent()

        val prompt = "Analyse this Bengali dramatic script:\n\n$scriptText"

        val requestJson = JSONObject().apply {
            put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            put("contents", JSONArray().put(
                JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            ))
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
                put("temperature", 0.2)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", trimmedKey)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Gemini API Error (${response.code}): ${body.take(200)}")
                    )
                }

                val jsonResponse = JSONObject(body)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResponse = parts?.optJSONObject(0)?.optString("text").orEmpty()

                val parsedList = parseAnalysisJson(textResponse)
                if (parsedList.isNotEmpty()) {
                    Result.success(parsedList)
                } else {
                    Result.failure(Exception("Could not parse dialogue units from AI response"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAnalysisJson(rawJson: String): List<ParsedDialogueItem> {
        val list = mutableListOf<ParsedDialogueItem>()
        try {
            val root = JSONObject(rawJson)
            val dialoguesArray = root.optJSONArray("dialogues") ?: return list
            for (i in 0 until dialoguesArray.length()) {
                val item = dialoguesArray.getJSONObject(i)
                list.add(
                    ParsedDialogueItem(
                        sequenceNumber = item.optInt("sequenceNumber", i + 1),
                        speakerName = item.optString("speakerName", "Unknown").trim().ifEmpty { "Speaker ${i + 1}" },
                        exactText = item.optString("exactText", "").trim(),
                        primaryEmotion = item.optString("primaryEmotion", "Calm"),
                        intensity = item.optInt("intensity", 50).coerceIn(0, 100),
                        deliveryStyle = item.optString("deliveryStyle", "Normal"),
                        paceDirection = item.optString("paceDirection", "normal"),
                        pitchDirection = item.optString("pitchDirection", "normal"),
                        pauseBeforeMs = item.optInt("pauseBeforeMs", 200),
                        pauseAfterMs = item.optInt("pauseAfterMs", 300),
                        explanation = item.optString("explanation", ""),
                        pronunciationNotes = item.optString("pronunciationNotes", "")
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }

    /**
     * Smart local rule-based script analyzer.
     * Splitting dialogue by lines/speakers and detecting emotion cues natively in Bengali.
     */
    fun analyzeScriptLocally(scriptText: String): List<ParsedDialogueItem> {
        val lines = scriptText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val result = mutableListOf<ParsedDialogueItem>()
        var seq = 1
        var currentSpeaker = "বর্ণনাকারী (Narrator)"

        for (line in lines) {
            val parts = if (line.contains(":") || line.contains("ঃ")) {
                line.split(Regex("[:ঃ]"), 2)
            } else if (line.contains(" - ")) {
                line.split(" - ", limit = 2)
            } else {
                null
            }

            val speaker: String
            val dialogueText: String

            if (parts != null && parts[0].length <= 25) {
                speaker = parts[0].trim()
                currentSpeaker = speaker
                dialogueText = parts[1].trim()
            } else {
                speaker = currentSpeaker
                dialogueText = line
            }

            if (dialogueText.isEmpty()) continue

            // Infer emotion from Bengali sentiment cues
            val emotion = inferEmotionFromText(dialogueText)
            val delivery = when (emotion) {
                EmotionTag.ANGRY -> "Shout"
                EmotionTag.CRYING, EmotionTag.HURT -> "Trembling"
                EmotionTag.FEAR, EmotionTag.NERVOUS -> "Breathy"
                EmotionTag.LOVE, EmotionTag.CALM -> "Soft"
                EmotionTag.CONFIDENT -> "Firm"
                EmotionTag.SARCASTIC -> "Restrained"
                else -> "Normal"
            }

            val intensity = when (emotion) {
                EmotionTag.ANGRY, EmotionTag.CRYING -> 85
                EmotionTag.EXCITED, EmotionTag.FEAR -> 80
                EmotionTag.LOVE, EmotionTag.HURT -> 70
                EmotionTag.CALM -> 35
                else -> 50
            }

            result.add(
                ParsedDialogueItem(
                    sequenceNumber = seq++,
                    speakerName = speaker,
                    exactText = dialogueText,
                    primaryEmotion = emotion.englishName,
                    intensity = intensity,
                    deliveryStyle = delivery,
                    paceDirection = if (emotion == EmotionTag.ANGRY || emotion == EmotionTag.EXCITED) "fast" else "normal",
                    pitchDirection = if (emotion == EmotionTag.FEAR || emotion == EmotionTag.CRYING) "high" else "normal",
                    pauseBeforeMs = 250,
                    pauseAfterMs = 350,
                    explanation = "স্থানীয় বিশ্লেষণ দ্বারা চিহ্নিত ভাব: ${emotion.bengaliName}",
                    pronunciationNotes = ""
                )
            )
        }

        if (result.isEmpty() && scriptText.isNotBlank()) {
            result.add(
                ParsedDialogueItem(
                    sequenceNumber = 1,
                    speakerName = "প্রধান চরিত্র",
                    exactText = scriptText.trim(),
                    primaryEmotion = "Calm",
                    intensity = 50,
                    deliveryStyle = "Normal",
                    paceDirection = "normal",
                    pitchDirection = "normal",
                    pauseBeforeMs = 200,
                    pauseAfterMs = 300,
                    explanation = "একক সংলাপ",
                    pronunciationNotes = ""
                )
            )
        }
        return result
    }

    private fun inferEmotionFromText(text: String): EmotionTag {
        val lower = text.lowercase()
        return when {
            lower.contains("রাগ") || lower.contains("খুন") || lower.contains("মরবি") || lower.contains("ধ্বংস") || text.contains("😡") -> EmotionTag.ANGRY
            lower.contains("অভিমান") || lower.contains("কেন এমন করলে") || lower.contains("কখনো বুঝলে না") -> EmotionTag.HURT
            lower.contains("কান্না") || lower.contains("চোখের জল") || lower.contains("হায়রে") || lower.contains("কাঁদ") -> EmotionTag.CRYING
            lower.contains("দুঃখ") || lower.contains("কষ্ট") || lower.contains("বেদন") || lower.contains("বেদনা") -> EmotionTag.SAD
            lower.contains("ভালোবাস") || lower.contains("প্রেম") || lower.contains("হৃদয়") || lower.contains("প্রিয়") -> EmotionTag.LOVE
            lower.contains("ভয়") || lower.contains("ভূত") || lower.contains("পালাও") || lower.contains("বাঁচাও") -> EmotionTag.FEAR
            lower.contains("আনন্দ") || lower.contains("খুশি") || lower.contains("দারুণ") || lower.contains("জিতে গেছি") -> EmotionTag.HAPPY
            lower.contains("হিংসা") || lower.contains("ওর এত অহংকার") || lower.contains("হিংসুটে") -> EmotionTag.JEALOUS
            lower.contains("অবাক") || lower.contains("কিভাবে সম্ভব") || lower.contains("অসম্ভব") || lower.contains("বাপরে") -> EmotionTag.SURPRISED
            lower.contains("পারব") || lower.contains("জয়") || lower.contains("সাহস") || lower.contains("শক্তি") -> EmotionTag.CONFIDENT
            lower.contains("ব্যঙ্গ") || lower.contains("খুব তো পন্ডিত") || lower.contains("মহারাজ") -> EmotionTag.SARCASTIC
            lower.contains("সন্দেহ") || lower.contains("কিছু একটা গোলমাল") || lower.contains("বিশ্বাস হয় না") -> EmotionTag.SUSPICIOUS
            lower.contains("উদ্বেগ") || lower.contains("কি হবে এখন") || lower.contains("চিন্তা") -> EmotionTag.NERVOUS
            lower.contains("হতাশ") || lower.contains("সব শেষ") -> EmotionTag.DISAPPOINTED
            lower.contains("উত্তেজনা") || lower.contains("জলদি") || lower.contains("চল চল") -> EmotionTag.EXCITED
            else -> EmotionTag.CALM
        }
    }

    /**
     * Generates Speech using Gemini Multimodal / Audio Generation API.
     * Returns true if audio file written successfully.
     */
    suspend fun generateGeminiAudio(
        apiKey: String,
        modelName: String,
        dialogue: DialogueEntity,
        voiceName: String,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("API key is empty"))
        }

        // Acting direction prompt builder
        val prompt = "Perform this dialogue with acting direction: Emotion=${dialogue.primaryEmotion}, Intensity=${dialogue.intensity}%, Delivery=${dialogue.deliveryStyle}. Bengali line: \"${dialogue.exactText}\""

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            ))
            put("generationConfig", JSONObject().apply {
                put("response_modalities", JSONArray().put("AUDIO"))
                put("speech_config", JSONObject().put(
                    "voice_config", JSONObject().put(
                        "prebuilt_voice_config", JSONObject().put("voice_name", voiceName)
                    )
                ))
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", trimmedKey)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Gemini TTS API returned HTTP ${response.code}: ${body.take(200)}")
                    )
                }

                val jsonResponse = JSONObject(body)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")

                var audioBase64: String? = null
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i)
                        val inlineData = part?.optJSONObject("inline_data")
                        if (inlineData != null) {
                            audioBase64 = inlineData.optString("data")
                            break
                        }
                    }
                }

                if (audioBase64.isNullOrEmpty()) {
                    return@withContext Result.failure(
                        Exception("No audio data returned in response. Model might not support direct audio output.")
                    )
                }

                val audioBytes = android.util.Base64.decode(audioBase64, android.util.Base64.DEFAULT)
                outputFile.parentFile?.mkdirs()
                FileOutputStream(outputFile).use { fos ->
                    fos.write(audioBytes)
                }
                Result.success(outputFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

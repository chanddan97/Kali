package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStyle
import com.example.data.model.DialogueEntity
import com.example.data.model.EmotionTag
import com.example.data.model.SpeakerEntity
import com.example.data.model.VoiceProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.EmotionAngry
import com.example.ui.theme.EmotionCalm
import com.example.ui.theme.EmotionCrying
import com.example.ui.theme.EmotionFear
import com.example.ui.theme.EmotionHappy
import com.example.ui.theme.EmotionHurt
import com.example.ui.theme.EmotionLove
import com.example.ui.theme.EmotionSad
import com.example.ui.theme.StudioAmberEmotion
import com.example.ui.theme.StudioCyanAccent
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioVioletLight
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DialogueReviewScreen(
    viewModel: MainViewModel,
    onNavigateToVoices: () -> Unit
) {
    val project = viewModel.activeProject.value
    val dialogues = viewModel.activeDialogues.value
    val speakers = viewModel.activeSpeakers.value
    val voiceProfiles = viewModel.allVoiceProfiles.value
    val isGenerating = viewModel.isGeneratingAudio.value
    val generatingId = viewModel.generatingDialogueId.value
    val batchProgress = viewModel.batchProgress.value
    val genMessage = viewModel.generationMessage.value
    val isPlaying = viewModel.isPlaying.value
    val playingDialogueId = viewModel.currentPlayingDialogueId.value

    var editingDialogue by remember { mutableStateOf<DialogueEntity?>(null) }
    var speakerToReassign by remember { mutableStateOf<SpeakerEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অভিনয় ও আবেগ টাইমলাইন",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = project?.title ?: "চিত্রনাট্য টাইমলাইন",
                            fontSize = 13.sp,
                            color = StudioVioletLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceHighlight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${dialogues.size} টি সংলাপ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCyanAccent
                        )
                    }
                }

                // Batch Progress or Status Message
                if (genMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceHighlight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = genMessage, fontSize = 12.sp, color = TextPrimary)
                            if (batchProgress != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { batchProgress.first.toFloat() / batchProgress.second.coerceAtLeast(1) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = StudioVioletPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar: Play Whole Scene & Batch Generate
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.generateAllApprovedDialogues() },
                    enabled = !isGenerating && dialogues.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("batch_generate_btn")
                ) {
                    if (isGenerating && batchProgress != null) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${batchProgress.first}/${batchProgress.second}")
                    } else {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সব রেকর্ড করুন", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (isPlaying) viewModel.pausePlayback() else viewModel.playEntireScript()
                    },
                    enabled = dialogues.any { !it.currentAudioPath.isNullOrEmpty() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("play_entire_script_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying && playingDialogueId == null) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPlaying && playingDialogueId == null) "থামান" else "সম্পূর্ণ দৃশ্য চালান")
                }
            }
        }

        // Dialogue Cards Timeline
        if (dialogues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "কোনো সংলাপ তৈরি করা হয়নি",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "চিত্রনাট্য ট্যাবে গিয়ে স্ক্রিপ্ট পেস্ট করুন ও 'বিশ্লেষণ' বাটনে চাপ দিন।",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(dialogues) { dialogue ->
                val speaker = speakers.find { it.speakerId == dialogue.speakerId || it.displayName == dialogue.speakerName }
                val voiceProfile = voiceProfiles.find { it.voiceProfileId == speaker?.voiceProfileId }
                val isCurrentPlaying = isPlaying && playingDialogueId == dialogue.dialogueId
                val isThisGenerating = isGenerating && generatingId == dialogue.dialogueId

                DialogueTimelineCard(
                    dialogue = dialogue,
                    speaker = speaker,
                    voiceProfile = voiceProfile,
                    isPlaying = isCurrentPlaying,
                    isGenerating = isThisGenerating,
                    onPlayClick = { viewModel.playDialogue(dialogue) },
                    onDialogueUpdate = { updated -> viewModel.updateDialogueItem(updated) },
                    onEditClick = { editingDialogue = dialogue },
                    onReassignSpeakerVoice = { speaker?.let { speakerToReassign = it } }
                )
            }
        }
    }

    // Edit Dialogue Text Dialog
    editingDialogue?.let { item ->
        var editedText by remember { mutableStateOf(item.exactText) }
        var editedSpeakerName by remember { mutableStateOf(item.speakerName) }

        AlertDialog(
            onDismissRequest = { editingDialogue = null },
            title = { Text("সংলাপ ও চরিত্র সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editedSpeakerName,
                        onValueChange = { editedSpeakerName = it },
                        label = { Text("চরিত্রের নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        label = { Text("সংলাপ (Exact Text)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateDialogueItem(
                        item.copy(exactText = editedText, speakerName = editedSpeakerName)
                    )
                    editingDialogue = null
                }) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDialogue = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Reassign Speaker Voice Dialog
    speakerToReassign?.let { spk ->
        AlertDialog(
            onDismissRequest = { speakerToReassign = null },
            title = { Text("'${spk.displayName}' চরিত্রের কণ্ঠ পরিবর্তন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("উপলব্ধ কণ্ঠ প্রোফাইল নির্বাচন করুন:", fontSize = 12.sp, color = TextSecondary)
                    voiceProfiles.forEach { vp ->
                        val isSelected = vp.voiceProfileId == spk.voiceProfileId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSpeakerVoiceProfile(spk, vp.voiceProfileId)
                                    speakerToReassign = null
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioVioletPrimary.copy(alpha = 0.2f) else StudioSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = vp.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(text = vp.bengaliLabel, fontSize = 11.sp, color = TextSecondary)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = StudioVioletPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { speakerToReassign = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
fun DialogueTimelineCard(
    dialogue: DialogueEntity,
    speaker: SpeakerEntity?,
    voiceProfile: VoiceProfileEntity?,
    isPlaying: Boolean,
    isGenerating: Boolean,
    onPlayClick: () -> Unit,
    onDialogueUpdate: (DialogueEntity) -> Unit,
    onEditClick: () -> Unit,
    onReassignSpeakerVoice: () -> Unit
) {
    var expandedControls by remember { mutableStateOf(false) }
    var emotionMenuOpen by remember { mutableStateOf(false) }
    var deliveryMenuOpen by remember { mutableStateOf(false) }

    val currentEmotion = EmotionTag.fromString(dialogue.primaryEmotion)
    val currentDelivery = DeliveryStyle.fromString(dialogue.deliveryStyle)
    val hasAudio = !dialogue.currentAudioPath.isNullOrEmpty()

    val emotionColor = when (currentEmotion) {
        EmotionTag.ANGRY -> EmotionAngry
        EmotionTag.HURT -> EmotionHurt
        EmotionTag.CRYING, EmotionTag.SAD -> EmotionSad
        EmotionTag.HAPPY -> EmotionHappy
        EmotionTag.LOVE -> EmotionLove
        EmotionTag.FEAR, EmotionTag.NERVOUS -> EmotionFear
        else -> EmotionCalm
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dialogue_card_${dialogue.sequenceNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Sequence, Speaker, Voice Profile, Approved Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(StudioSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dialogue.sequenceNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioVioletLight
                        )
                    }

                    // Speaker Name Chip
                    Text(
                        text = dialogue.speakerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Voice Tag Chip (tap to reassign)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurfaceHighlight)
                            .clickable(onClick = onReassignSpeakerVoice)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = StudioCyanAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = voiceProfile?.name?.take(14) ?: "Voice",
                                fontSize = 10.sp,
                                color = StudioCyanAccent
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit text", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Checkbox(
                        checked = dialogue.approved,
                        onCheckedChange = { onDialogueUpdate(dialogue.copy(approved = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = StudioVioletPrimary),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exact Dialogue Text
            Text(
                text = dialogue.exactText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Emotion & Directing Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emotion Chip
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(emotionColor.copy(alpha = 0.2f))
                            .clickable { emotionMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentEmotion.emoji} ${currentEmotion.bengaliName} (${dialogue.intensity}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = emotionColor
                        )
                    }

                    DropdownMenu(
                        expanded = emotionMenuOpen,
                        onDismissRequest = { emotionMenuOpen = false }
                    ) {
                        EmotionTag.entries.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text("${tag.emoji} ${tag.bengaliName} (${tag.englishName})") },
                                onClick = {
                                    onDialogueUpdate(dialogue.copy(primaryEmotion = tag.englishName))
                                    emotionMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Delivery Style Chip
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurfaceHighlight)
                            .clickable { deliveryMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "শৈলী: ${currentDelivery.bengaliName}",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = deliveryMenuOpen,
                        onDismissRequest = { deliveryMenuOpen = false }
                    ) {
                        DeliveryStyle.entries.forEach { style ->
                            DropdownMenuItem(
                                text = { Text("${style.bengaliName} (${style.englishName})") },
                                onClick = {
                                    onDialogueUpdate(dialogue.copy(deliveryStyle = style.englishName))
                                    deliveryMenuOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Toggle fine-tuning controls
                IconButton(
                    onClick = { expandedControls = !expandedControls },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expandedControls) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand controls",
                        tint = TextSecondary
                    )
                }

                // Play / Generate Audio Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (hasAudio) StudioVioletPrimary else StudioSurfaceHighlight)
                        .clickable(enabled = !isGenerating, onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else if (isPlaying) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = if (hasAudio) Icons.Default.PlayArrow else Icons.Default.Mic,
                            contentDescription = if (hasAudio) "Play" else "Record Preview",
                            tint = if (hasAudio) Color.White else StudioCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded Acting Directing Controls
            AnimatedVisibility(visible = expandedControls) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(StudioSurfaceHighlight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "আবেগের তীব্রতা (Emotion Intensity: ${dialogue.intensity}%):",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Slider(
                        value = dialogue.intensity.toFloat(),
                        onValueChange = { onDialogueUpdate(dialogue.copy(intensity = it.toInt())) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioVioletPrimary,
                            activeTrackColor = StudioVioletPrimary
                        )
                    )

                    if (dialogue.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 এআই পরিচালনা নোট: ${dialogue.explanation}",
                            fontSize = 11.sp,
                            color = StudioVioletLight,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.DialogueEntity
import com.example.data.model.VoiceProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.StudioAmberEmotion
import com.example.ui.theme.StudioCyanAccent
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioVioletLight
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VoiceStudioScreen(
    viewModel: MainViewModel
) {
    val voiceProfiles = viewModel.allVoiceProfiles.value
    val isPlaying = viewModel.isPlaying.value
    val playingDialogueId = viewModel.currentPlayingDialogueId.value

    var showAddDialog by remember { mutableStateOf(false) }
    var auditioningId by remember { mutableStateOf<String?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = StudioVioletPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "ভয়েস স্টুডিও (Voice Library)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "নাটকের বিভিন্ন চরিত্রের কণ্ঠস্বর ক্যাটালগ",
                            fontSize = 12.sp,
                            color = StudioVioletLight
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_custom_voice_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("কাস্টম ভয়েস", fontSize = 12.sp)
                }
            }
        }

        // Info Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceHighlight.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "💡 প্রতিটি চরিত্রের জন্য পৃথক ভয়েস প্রোফাইল নির্ধারিত থাকে। অডিশন বাটনে চাপ দিয়ে কণ্ঠের উচ্চারণ ও টোন শুনে নিন।",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Voice Profiles List
        items(voiceProfiles) { profile ->
            val isAuditioningThis = isPlaying && playingDialogueId == "audition_${profile.voiceProfileId}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_profile_card_${profile.voiceProfileId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(StudioSurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (profile.isFavourite) StudioAmberEmotion else StudioVioletLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (profile.isFavourite) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favourite",
                                        tint = StudioAmberEmotion,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = profile.bengaliLabel,
                                fontSize = 12.sp,
                                color = StudioCyanAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile.voiceDescription,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Audition Play Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isAuditioningThis) StudioVioletPrimary else StudioSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            if (isAuditioningThis) {
                                viewModel.pausePlayback()
                            } else {
                                auditionVoiceProfile(profile, viewModel)
                            }
                        }) {
                            Icon(
                                imageVector = if (isAuditioningThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Audition voice",
                                tint = if (isAuditioningThis) Color.White else StudioCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Custom Voice Dialog
    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var bengaliLabelInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন কাস্টম ভয়েস প্রোফাইল তৈরি") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("প্রোফাইল নাম (যেমন: গোয়েন্দা রুদ্র)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bengaliLabelInput,
                        onValueChange = { bengaliLabelInput = it },
                        label = { Text("বয়স ও চরিত্র (যেমন: যুবক পুরুষ ৩৫ বছর)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("কণ্ঠের বৈশিষ্ট্য ও ভাব") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            val newProfile = VoiceProfileEntity(
                                name = nameInput,
                                bengaliLabel = bengaliLabelInput.ifBlank { "কাস্টম চরিত্র" },
                                characterAgeDescription = bengaliLabelInput,
                                voiceDescription = descInput.ifBlank { "স্বাভাবিক প্রকাশভঙ্গি" },
                                geminiVoiceName = "Puck",
                                isFavourite = true,
                                isPreset = false
                            )
                            viewModel.addCustomVoiceProfile(newProfile)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary)
                ) {
                    Text("তৈরি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

private fun auditionVoiceProfile(profile: VoiceProfileEntity, viewModel: MainViewModel) {
    val sampleText = when {
        profile.name.contains("Senior") || profile.name.contains("দাদু") -> "শোনো হে বাছা, জীবনের অভিজ্ঞতা বলে ধৈর্য আর সত্যই শেষ পর্যন্ত জয়ী হয়।"
        profile.name.contains("Female") || profile.name.contains("অনন্যা") -> "নমস্কার! এই চরিত্রের প্রতিটি আবেগ ও অনুভূতি স্পষ্ট রূপ দিতে আমি প্রস্তুত।"
        profile.name.contains("Child") || profile.name.contains("ছোট্টু") -> "আরে মজা! চলো আমরা সবাই মিলে আজ আনন্দ আর খেলাধুলা করি!"
        else -> "নমস্কার! আমি এই চরিত্রের কণ্ঠস্বর হিসেবে সংলাপ পাঠ করতে প্রস্তুত।"
    }

    val dummyDialogue = DialogueEntity(
        dialogueId = "audition_${profile.voiceProfileId}",
        projectId = "audition_project",
        sequenceNumber = 1,
        speakerId = "spk_audition",
        speakerName = profile.name,
        exactText = sampleText,
        primaryEmotion = "Calm",
        intensity = 60,
        deliveryStyle = "Normal"
    )

    viewModel.generateSingleDialogueAudio(dummyDialogue, preferDeviceTts = true)
}

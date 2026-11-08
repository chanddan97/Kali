package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.DialogueEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.StudioCyanAccent
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioVioletLight
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.io.File

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val project = viewModel.activeProject.value
    val dialogues = viewModel.activeDialogues.value
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingDialogueId by viewModel.currentPlayingDialogueId.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()

    val generatedDialogues = dialogues.filter { !it.currentAudioPath.isNullOrEmpty() }
    val currentPlayingDialogue = dialogues.find { it.dialogueId == playingDialogueId }

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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = StudioVioletPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "অডিও প্লেয়ার ও হিস্ট্রি (Audio History)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "রেকর্ডকৃত সমস্ত সংলাপের অডিও ও এক্সপোর্ট",
                        fontSize = 12.sp,
                        color = StudioVioletLight
                    )
                }
            }
        }

        // Dedicated Studio Audio Player Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_player_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = StudioCyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "স্টুডিও মাস্টার প্লেয়ার",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioCyanAccent
                            )
                        }

                        if (currentPlayingDialogue?.currentAudioPath != null) {
                            IconButton(
                                onClick = { shareAudioFile(context, currentPlayingDialogue.currentAudioPath!!) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = StudioVioletLight, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentPlayingDialogue?.let { "${it.speakerName}: ${it.exactText}" }
                            ?: (project?.title ?: "প্লে করতে যেকোনো সংলাপ নির্বাচন করুন"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Slider
                    Slider(
                        value = if (durationMs > 0) currentPositionMs.toFloat() else 0f,
                        onValueChange = { viewModel.seekTo(it.toInt()) },
                        valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
                        colors = SliderDefaults.colors(
                            thumbColor = StudioVioletPrimary,
                            activeTrackColor = StudioVioletPrimary,
                            inactiveTrackColor = StudioSurfaceHighlight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatMs(currentPositionMs), fontSize = 11.sp, color = TextSecondary)
                        Text(text = formatMs(durationMs), fontSize = 11.sp, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Playback Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.seekTo((currentPositionMs - 5000).coerceAtLeast(0)) }) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = TextPrimary)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(StudioVioletPrimary)
                                .clickable {
                                    if (isPlaying) viewModel.pausePlayback()
                                    else if (currentPositionMs > 0) viewModel.resumePlayback()
                                    else viewModel.playEntireScript()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(onClick = { viewModel.seekTo((currentPositionMs + 5000).coerceAtMost(durationMs)) }) {
                            Icon(Icons.Default.FastForward, contentDescription = "Fast forward", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Speed controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "স্পিড: ", fontSize = 11.sp, color = TextSecondary)
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            FilterChip(
                                selected = playbackSpeed == speed,
                                onClick = { viewModel.setPlaybackSpeed(speed) },
                                label = { Text("${speed}x", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioVioletPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Generated Dialogue Audio List
        item {
            Text(
                text = "রেকর্ডকৃত সংলাপ অডিও ফাইলসমূহ (${generatedDialogues.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (generatedDialogues.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "কোনো অডিও ফাইল এখনো তৈরি করা হয়নি",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "টাইমলাইন ট্যাবে গিয়ে যেকোনো সংলাপে 'রেকর্ড' চাপুন।",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        } else {
            items(generatedDialogues) { item ->
                val isThisPlaying = isPlaying && playingDialogueId == item.dialogueId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playDialogue(item) }
                        .testTag("audio_history_item_${item.sequenceNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThisPlaying) StudioSurfaceHighlight else StudioSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isThisPlaying) StudioVioletPrimary else StudioSurfaceHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isThisPlaying) Color.White else StudioCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "#${item.sequenceNumber} [${item.speakerName}] ${item.exactText}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "আবেগ: ${item.primaryEmotion} (${item.intensity}%) | WAV",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Share action
                        IconButton(
                            onClick = { item.currentAudioPath?.let { shareAudioFile(context, it) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Export audio", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun shareAudioFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.emotivoice.kxmpzq.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "সংলাপ অডিও শেয়ার / সেভ করুন"))
    } catch (_: Exception) {
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

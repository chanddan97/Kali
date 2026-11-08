package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.StudioAmberEmotion
import com.example.ui.theme.StudioCyanAccent
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioVioletLight
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenApiKeyDialog: () -> Unit,
    onNavigateToScript: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val activeProject = viewModel.activeProject.value
    val dialogues = viewModel.activeDialogues.value
    val allProjects = viewModel.allProjects.value
    val hasApiKey = viewModel.hasApiKey.value
    val isPlaying = viewModel.isPlaying.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    StudioVioletPrimary.copy(alpha = 0.25f),
                                    StudioSurfaceVariant,
                                    StudioCyanAccent.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(StudioVioletPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Voice Studio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "EmotiVoice Studio",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "বাংলা এআই ভয়েস ও অ্যাক্টিং প্রোডাকশন",
                                    fontSize = 12.sp,
                                    color = StudioVioletLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "কার্টুন নাটক, গল্প ও রিলসের জন্য একাধিক চরিত্রের নিখুঁত অভিনয় ও অনুভূতির অভিব্যক্তি সম্পন্ন কণ্ঠস্বর তৈরি করুন।",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createNewProject("নতুন নাটক", "")
                                    onNavigateToScript()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("new_project_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নতুন প্রজেক্ট", fontWeight = FontWeight.Bold)
                            }

                            if (activeProject != null && dialogues.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = onNavigateToReview,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("review_timeline_btn")
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("টাইমলাইন (${dialogues.size})")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini API Key Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenApiKeyDialog() }
                    .testTag("api_key_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (hasApiKey) StudioVioletPrimary.copy(alpha = 0.2f)
                                    else StudioAmberEmotion.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasApiKey) Icons.Default.CheckCircle else Icons.Default.Key,
                                contentDescription = "API Status",
                                tint = if (hasApiKey) StudioVioletPrimary else StudioAmberEmotion,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (hasApiKey) "Gemini API সক্রিয়" else "Gemini API কী প্রয়োজন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasApiKey) viewModel.maskedApiKey.value else "ডিভাইস লোকাল TTS চালু রয়েছে (কী যোগ করতে ট্যাপ করুন)",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenApiKeyDialog,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("manage_key_btn")
                    ) {
                        Text(if (hasApiKey) "ম্যানেজ" else "যোগ করুন", fontSize = 12.sp)
                    }
                }
            }
        }

        // Quick Preset Scripts Row
        item {
            Text(
                text = "জনপ্রিয় চিত্রনাট্য প্রিসেট (১-ট্যাপ লোড)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    PresetChip(
                        title = "সাপের নাটক 🐍",
                        subtitle = "নাগ ও নাগিনীর প্রেম ও রাগ",
                        onClick = {
                            viewModel.loadSampleScript("snake")
                            viewModel.createNewProject("নাগ-নাগিনীর সাপের নাটক", viewModel.scriptDraft.value)
                            onNavigateToScript()
                        }
                    )
                }
                item {
                    PresetChip(
                        title = "পারিবারিক গল্প 🏡",
                        subtitle = "মা ও ছেলের মিষ্টি অভিমান",
                        onClick = {
                            viewModel.loadSampleScript("family")
                            viewModel.createNewProject("পারিবারিক মান-অভিমান", viewModel.scriptDraft.value)
                            onNavigateToScript()
                        }
                    )
                }
                item {
                    PresetChip(
                        title = "গোয়েন্দা নাটক 🕵️",
                        subtitle = "রহস্য, সন্দেহ ও আত্মবিশ্বাস",
                        onClick = {
                            viewModel.loadSampleScript("mystery")
                            viewModel.createNewProject("গোয়েন্দা ও চোরের রহস্য লড়াই", viewModel.scriptDraft.value)
                            onNavigateToScript()
                        }
                    )
                }
                item {
                    PresetChip(
                        title = "Multilingual 🌐",
                        subtitle = "বাংলা ও ইংরেজি কথপোকথন",
                        onClick = {
                            viewModel.loadSampleScript("multilingual")
                            viewModel.createNewProject("বাংলা ও ইংরেজি বহুভাষিক দৃশ্য", viewModel.scriptDraft.value)
                            onNavigateToScript()
                        }
                    )
                }
            }
        }

        // Active Project Overview
        if (activeProject != null) {
            item {
                Text(
                    text = "চলমান প্রজেক্ট",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_project_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeProject.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioSurfaceHighlight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${dialogues.size} টি সংলাপ",
                                    fontSize = 11.sp,
                                    color = StudioCyanAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeProject.originalScript.take(120).plus("..."),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToReview,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("অভিনয় রিভিউ করুন", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isPlaying) viewModel.pausePlayback() else viewModel.playEntireScript()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlaying) "থামান" else "সম্পূর্ণ নাটক চালান", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recent Projects List
        item {
            Text(
                text = "সংরক্ষিত প্রজেক্টসমূহ (${allProjects.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (allProjects.isEmpty()) {
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
                            text = "কোনো চিত্রনাট্য পাওয়া যায়নি",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.createNewProject("নতুন চিত্রনাট্য", "")
                                onNavigateToScript()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary)
                        ) {
                            Text("প্রথম চিত্রনাট্য তৈরি করুন")
                        }
                    }
                }
            }
        } else {
            items(allProjects) { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectProject(project)
                            onNavigateToReview()
                        }
                        .testTag("project_item_${project.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (project.id == activeProject?.id) StudioSurfaceHighlight else StudioSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = project.originalScript.take(60),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteProject(project.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete project",
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = StudioVioletLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

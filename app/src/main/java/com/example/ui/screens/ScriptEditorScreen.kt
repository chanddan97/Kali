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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScriptEditorScreen(
    viewModel: MainViewModel,
    onNavigateToReview: () -> Unit
) {
    val title = viewModel.projectTitleDraft.value
    val script = viewModel.scriptDraft.value
    val isAnalyzing = viewModel.isAnalyzing.value
    val error = viewModel.analysisError.value
    val hasApiKey = viewModel.hasApiKey.value

    val wordCount = if (script.isBlank()) 0 else script.trim().split(Regex("\\s+")).size
    val charCount = script.length

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = StudioVioletPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "চিত্রনাট্য সম্পাদক (Script Editor)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Project Title Input
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.projectTitleDraft.value = it },
                label = { Text("চিত্রনাট্যের শিরোনাম (Title)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_title_input"),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            )
        }

        // Quick Presets Row
        item {
            Column {
                Text(
                    text = "নমুনা ডায়লগ লোড করুন:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSampleScript("snake") },
                            label = { Text("সাপের নাটক 🐍") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSampleScript("family") },
                            label = { Text("পারিবারিক গল্প 🏡") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSampleScript("mystery") },
                            label = { Text("গোয়েন্দা নাটক 🕵️") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSampleScript("multilingual") },
                            label = { Text("Multilingual 🌐") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioSurface,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Main Script Input Area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = StudioVioletPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নাটকের সংলাপ ও চরিত্রসমূহ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        if (script.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.scriptDraft.value = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear script", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = script,
                        onValueChange = { viewModel.scriptDraft.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .testTag("script_text_input"),
                        placeholder = {
                            Text(
                                text = "এখানে বাংলা সংলাপ লিখুন, যেমন:\n\nনাগ: শোনো নাগিনী, আজ আর কোনো ভয় নেই!\nনাগিনী: কিন্তু তুমি একা কোথায় গিয়েছিলে?\nকালনাগ: হা হা হা! তোরা কেউ রেহাই পাবি না!\n\n(প্রতি লাইনে চরিত্র ও কোলন : ব্যবহার করুন)",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "শব্দ: $wordCount | অক্ষর: $charCount",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Bengali Script UTF-8",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Error Banner
        if (error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Analysis Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.analyzeCurrentScript(forceLocal = false) },
                    enabled = script.isNotBlank() && !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("analyze_script_btn")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("AI অ্যাক্টিং বিশ্লেষণ চলছে...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasApiKey) "Gemini AI অভিনয় ও ভাব বিশ্লেষণ" else "স্মার্ট বিশ্লেষণ ও চরিত্র বিভাজন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.analyzeCurrentScript(forceLocal = true) },
                    enabled = script.isNotBlank() && !isAnalyzing,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_analyze_btn")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("তাৎক্ষণিক অফলাইন বিশ্লেষণ (Local Rule-based)")
                }
            }
        }
    }
}

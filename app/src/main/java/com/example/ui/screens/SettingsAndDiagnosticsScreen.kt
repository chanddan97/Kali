package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiTestResult
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

@Composable
fun SettingsAndDiagnosticsScreen(
    viewModel: MainViewModel,
    onOpenApiKeyDialog: () -> Unit
) {
    val context = LocalContext.current
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val maskedKey by viewModel.maskedApiKey.collectAsState()
    val isTestingKey by viewModel.isTestingKey.collectAsState()
    val keyTestResult by viewModel.keyTestResult.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = StudioVioletPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "সেটিংস ও ডায়াগনস্টিকস",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "API Key নিরাপত্তা, স্টোরেজ ও সিস্টেম পরীক্ষা",
                        fontSize = 12.sp,
                        color = StudioVioletLight
                    )
                }
            }
        }

        // Section 1: Gemini API Key Security & Management
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_key_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = StudioVioletPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini API Key সুরক্ষা",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (hasApiKey) {
                            Text(
                                text = "সংরক্ষিত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioCyanAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "কী স্ট্যাটাস: $maskedKey",
                        fontSize = 13.sp,
                        color = if (hasApiKey) StudioVioletLight else StudioAmberEmotion,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "আপনার কী ডিভাইসের হার্ডওয়্যার-সমর্থিত Android Keystore দিয়ে AES-GCM পদ্ধতিতে এনক্রিপ্ট থাকে। কোনো সার্ভারে পাঠানো হয় না।",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenApiKeyDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioVioletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (hasApiKey) "কী বদলান" else "কী যোগ করুন", fontSize = 12.sp)
                        }

                        if (hasApiKey) {
                            OutlinedButton(
                                onClick = {
                                    val key = viewModel.repository.secureKeyStorage.getApiKey()
                                    if (key != null) viewModel.testKey(key)
                                },
                                enabled = !isTestingKey,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isTestingKey) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("কী টেস্ট", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Key test feedback
                    if (keyTestResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        when (val res = keyTestResult!!) {
                            is ApiTestResult.Success -> {
                                Text(
                                    text = "✅ সংযোগ সফল! লেটেন্সি: ${res.latencyMs}ms (${res.availableModelsCount} টি মডেল উপলব্ধ)",
                                    fontSize = 12.sp,
                                    color = StudioCyanAccent
                                )
                            }
                            is ApiTestResult.Failure -> {
                                Text(
                                    text = "❌ ব্যর্থতা: ${res.messageBengali}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Storage Manager
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_manager_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = StudioCyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিভাইস স্টোরেজ ও অডিও ফাইল",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    storageStats?.let { stats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "মোট অডিও সাইজ:", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "${stats.totalAudioBytes / 1024} KB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "রেকর্ডকৃত ফাইল সংখ্যা:", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "${stats.fileCount} টি ফাইল", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ডিভাইসের খালি স্পেস:", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "${stats.freeSpaceBytes / (1024 * 1024)} MB উপলব্ধ", fontSize = 12.sp, color = StudioCyanAccent)
                        }
                    }
                }
            }
        }

        // Section 3: App Diagnostics Runner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_diagnostics_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = StudioAmberEmotion, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "অ্যাপ ডায়াগনস্টিকস (Self-Test)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = { viewModel.runAllDiagnostics() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceHighlight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("টেস্ট রান করুন", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    if (diagnosticLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudioDarkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "ডায়াগনস্টিক রিপোর্ট:", fontSize = 11.sp, color = TextSecondary)
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Diagnostic Report", diagnosticLogs.joinToString("\n"))
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "রিপোর্ট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy report", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                diagnosticLogs.forEach { log ->
                                    Text(text = log, fontSize = 11.sp, color = TextPrimary, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Privacy & Local-first Statement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceHighlight.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = StudioVioletLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "গোপনীয়তা ও লোকাল-ফার্স্ট স্থাপত্য",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• EmotiVoice Studio কোনো ক্লাউড একাউন্ট বা অনলাইন ডেটাবেস ছাড়াই চলে।\n• চিত্রনাট্য, চরিত্রের বিবরণ ও তৈরি করা অডিও কেবল আপনার ফোনেই সংরক্ষিত থাকে।\n• স্ক্রিপ্ট অ্যানালাইসিস বা জেনারেটের সময় আপনার নিজস্ব Gemini API কী দিয়েই কেবল অনুরোধ প্রেরিত হয়।",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

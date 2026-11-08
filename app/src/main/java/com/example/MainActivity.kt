package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.ApiKeyDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DialogueReviewScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScriptEditorScreen
import com.example.ui.screens.SettingsAndDiagnosticsScreen
import com.example.ui.screens.VoiceStudioScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioVioletLight
import com.example.ui.theme.StudioVioletPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EmotiVoiceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EmotiVoiceApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val maskedApiKey by viewModel.maskedApiKey.collectAsState()
    val isTestingKey by viewModel.isTestingKey.collectAsState()
    val keyTestResult by viewModel.keyTestResult.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Handle back button: return to studio if on another sub-screen
    BackHandler(enabled = currentTab != "studio") {
        viewModel.currentTab.value = "studio"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = StudioSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "studio",
                    onClick = { viewModel.currentTab.value = "studio" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Studio", modifier = Modifier.size(22.dp)) },
                    label = { Text("স্টুডিও", fontSize = 10.sp, fontWeight = if (currentTab == "studio") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_studio")
                )

                NavigationBarItem(
                    selected = currentTab == "script",
                    onClick = { viewModel.currentTab.value = "script" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Script", modifier = Modifier.size(22.dp)) },
                    label = { Text("চিত্রনাট্য", fontSize = 10.sp, fontWeight = if (currentTab == "script") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_script")
                )

                NavigationBarItem(
                    selected = currentTab == "review",
                    onClick = { viewModel.currentTab.value = "review" },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Timeline", modifier = Modifier.size(22.dp)) },
                    label = { Text("টাইমলাইন", fontSize = 10.sp, fontWeight = if (currentTab == "review") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_review")
                )

                NavigationBarItem(
                    selected = currentTab == "voice_studio",
                    onClick = { viewModel.currentTab.value = "voice_studio" },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice Library", modifier = Modifier.size(22.dp)) },
                    label = { Text("ভয়েস", fontSize = 10.sp, fontWeight = if (currentTab == "voice_studio") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_voices")
                )

                NavigationBarItem(
                    selected = currentTab == "history",
                    onClick = { viewModel.currentTab.value = "history" },
                    icon = { Icon(Icons.Default.Headphones, contentDescription = "Audio History", modifier = Modifier.size(22.dp)) },
                    label = { Text("অডিও", fontSize = 10.sp, fontWeight = if (currentTab == "history") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_audio")
                )

                NavigationBarItem(
                    selected = currentTab == "settings",
                    onClick = { viewModel.currentTab.value = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(22.dp)) },
                    label = { Text("সেটিংস", fontSize = 10.sp, fontWeight = if (currentTab == "settings") FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = StudioVioletLight,
                        indicatorColor = StudioVioletPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "studio" -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenApiKeyDialog = { showApiKeyDialog = true },
                    onNavigateToScript = { viewModel.currentTab.value = "script" },
                    onNavigateToReview = { viewModel.currentTab.value = "review" }
                )
                "script" -> ScriptEditorScreen(
                    viewModel = viewModel,
                    onNavigateToReview = { viewModel.currentTab.value = "review" }
                )
                "review" -> DialogueReviewScreen(
                    viewModel = viewModel,
                    onNavigateToVoices = { viewModel.currentTab.value = "voice_studio" }
                )
                "voice_studio" -> VoiceStudioScreen(
                    viewModel = viewModel
                )
                "history" -> HistoryScreen(
                    viewModel = viewModel
                )
                "settings" -> SettingsAndDiagnosticsScreen(
                    viewModel = viewModel,
                    onOpenApiKeyDialog = { showApiKeyDialog = true }
                )
            }
        }

        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentMaskedKey = maskedApiKey,
                hasKey = hasApiKey,
                isTesting = isTestingKey,
                testResult = keyTestResult,
                onTestKey = { viewModel.testKey(it) },
                onSaveKey = { viewModel.saveTestedKey(it) },
                onDeleteKey = { viewModel.deleteApiKey() },
                onDismiss = { showApiKeyDialog = false }
            )
        }
    }
}

package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.ControlLayout
import com.example.model.GameUiState
import com.example.model.GraphicsQuality
import com.example.ui.theme.HazardRed
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSetSound: (Boolean) -> Unit,
    onSetGraphics: (GraphicsQuality) -> Unit,
    onSetControlLayout: (ControlLayout) -> Unit,
    onSaveManual: () -> Unit,
    onRestartLevel: (Int) -> Unit,
    onToggleSandboxDriving: () -> Unit,
    onSetAutoTooltips: (Boolean) -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = state.language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("settings_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JcbYellow)
            }
            Spacer(Modifier.width(8.dp))
            Text(AppStrings.settingsTitle.get(lang), style = MaterialTheme.typography.headlineMedium, color = JcbYellow)
        }

        // Language
        SettingCard(title = AppStrings.language.get(lang)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AppLanguage.entries.forEach { entry ->
                    val selected = state.language == entry
                    Button(
                        onClick = { if (!selected) onToggleLanguage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) JcbYellow else SteelSurfaceElevated,
                            contentColor = if (selected) TextOnYellow else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("lang_btn_${entry.name}")
                    ) {
                        Text(entry.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Sound
        SettingCard(title = AppStrings.soundFx.get(lang)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BiText("Diesel Engine, Tools & Reverse Alarm", "डीजल इंजन, औज़ार और रिवर्स अलार्म").get(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.soundEnabled,
                    onCheckedChange = onSetSound,
                    modifier = Modifier.testTag("settings_sound_switch")
                )
            }
        }

        // Graphics LOD
        SettingCard(title = AppStrings.graphicsQuality.get(lang)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GraphicsQuality.entries.forEach { q ->
                    val sel = state.graphicsQuality == q
                    Button(
                        onClick = { onSetGraphics(q) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sel) JcbYellow else SteelSurfaceElevated,
                            contentColor = if (sel) TextOnYellow else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("gfx_btn_${q.name}")
                    ) {
                        Text(q.label.get(lang), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }

        // Control Layout (Joystick vs 6-Lever)
        SettingCard(title = AppStrings.controlLayout.get(lang)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ControlLayout.entries.forEach { cl ->
                    val sel = state.controlLayout == cl
                    Button(
                        onClick = { onSetControlLayout(cl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sel) JcbYellow else SteelSurfaceElevated,
                            contentColor = if (sel) TextOnYellow else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("layout_btn_${cl.name}")
                    ) {
                        Text(cl.label.get(lang), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }

        // Save Progress & Free Drive Practice Mode
        SettingCard(title = BiText("Progress & Career", "प्रगति और करियर").get(lang)) {
            Button(
                onClick = onSaveManual,
                colors = ButtonDefaults.buttonColors(containerColor = HydraulicGreen, contentColor = TextOnYellow),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_save_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(AppStrings.saveProgress.get(lang), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BiText("Free-Drive Test Yard Access (Practice)", "फ्री-ड्राइव टेस्ट यार्ड (अभ्यास मोड)").get(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.sandboxDrivingUnlocked,
                    onCheckedChange = { onToggleSandboxDriving() },
                    modifier = Modifier.testTag("settings_sandbox_switch")
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = BiText("Contextual Assembly Tooltips (EN / HI)", "संदर्भित असेंबली टूलटिप गाइड (EN / हिन्दी)").get(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.tooltipGuide.autoContextualEnabled,
                    onCheckedChange = onSetAutoTooltips,
                    modifier = Modifier.testTag("settings_tooltips_switch")
                )
            }
        }

        // Restart Any Level
        SettingCard(title = AppStrings.restartLevel.get(lang)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    (1..5).forEach { lvl ->
                        OutlinedButton(
                            onClick = { onRestartLevel(lvl) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("restart_lvl_$lvl")
                        ) {
                            Text("L$lvl", style = MaterialTheme.typography.labelLarge, color = JcbYellow)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    (6..10).forEach { lvl ->
                        OutlinedButton(
                            onClick = { onRestartLevel(lvl) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("restart_lvl_$lvl")
                        ) {
                            Text("L$lvl", style = MaterialTheme.typography.labelLarge, color = JcbYellow)
                        }
                    }
                }
                OutlinedButton(
                    onClick = { onRestartLevel(1) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_all_btn")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = HazardRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        BiText("Reset Full Machine Build", "पूरी मशीन असेंबली रीसेट करें").get(lang),
                        color = HazardRed,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, SteelBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = JcbYellow)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

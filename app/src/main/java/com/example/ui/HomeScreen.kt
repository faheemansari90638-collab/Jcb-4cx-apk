package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.GameUiState
import com.example.model.MicroPartCatalog
import com.example.model.ScreenRoute
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigate: (ScreenRoute) -> Unit,
    onToggleLanguage: () -> Unit
) {
    val lang = state.language

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top bar with Language switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = JcbYellow,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "4CX SITEMASTER 3D",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnYellow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                OutlinedButton(
                    onClick = onToggleLanguage,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("home_language_toggle")
                ) {
                    Icon(Icons.Default.Translate, contentDescription = "Language", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "हिन्दी में बदलें" else "Switch to English",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Hero Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, JcbYellow.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_workshop_hero),
                    contentDescription = "JCB 4CX Industrial Workshop Bay",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.25f), SteelDarkBg.copy(alpha = 0.94f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = AppStrings.appTitle.get(lang),
                        style = MaterialTheme.typography.displaySmall,
                        color = JcbYellow
                    )
                    Text(
                        text = AppStrings.appSubtitle.get(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }

            // Telemetry Progress Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SteelBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.assemblyProgress.get(lang),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Text(
                                text = "${state.assemblyPercent}% • ${state.installedParts.size}/${state.totalAssemblyParts} ${if (lang == AppLanguage.HI) "पुर्जे" else "Parts"}",
                                style = MaterialTheme.typography.titleLarge,
                                color = JcbYellow
                            )
                        }
                        Surface(
                            color = SteelSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, BlueprintCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = JcbYellow, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${state.xp} XP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BlueprintCyan
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { state.assemblyPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (state.assemblyPercent >= 100) HydraulicGreen else JcbYellow,
                        trackColor = SteelSurfaceElevated
                    )

                    Text(
                        text = "${AppStrings.levelLabel.get(lang)} ${state.currentLevel}/10 • ${state.completedMissions.size}/7 ${AppStrings.missionsTitle.get(lang)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Primary Action: Workshop 3D Assembly
            Button(
                onClick = { onNavigate(ScreenRoute.WORKSHOP) },
                colors = ButtonDefaults.buttonColors(containerColor = JcbYellow, contentColor = TextOnYellow),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("home_workshop_button")
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = AppStrings.resumeWorkshop.get(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = BiText("Build Level ${state.currentLevel} in 3D", "3D वर्कशॉप लेवल ${state.currentLevel} शुरू करें").get(lang),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Extreme Micro-Assembly Bay (Engine Internals, Rings, Valves, Seals & Hydraulics)
            Button(
                onClick = { onNavigate(ScreenRoute.MICRO_ASSEMBLY) },
                colors = ButtonDefaults.buttonColors(containerColor = SteelSurfaceElevated, contentColor = BlueprintCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(1.5.dp, BlueprintCyan.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .testTag("home_micro_button")
            ) {
                Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(26.dp), tint = BlueprintCyan)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = BiText("Extreme Micro-Assembly Bay", "माइक्रो इंजन असेंबली बे").get(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan
                    )
                    Text(
                        text = BiText(
                            "Pistons, Rings, Valves, Seals, Torque & X-Ray • ${state.micro.installedMicroParts.size}/${MicroPartCatalog.parts.size}",
                            "पिस्टन रिंग, वाल्व, सील, टॉर्क व पारदर्शी इंजन • ${state.micro.installedMicroParts.size}/${MicroPartCatalog.parts.size}"
                        ).get(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Driving Simulator Button
            val unlocked = state.drivingUnlocked
            Button(
                onClick = { onNavigate(ScreenRoute.DRIVING) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unlocked) HydraulicGreen else SteelSurfaceElevated,
                    contentColor = if (unlocked) TextOnYellow else TextSecondary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("home_driving_button")
            ) {
                Icon(
                    imageVector = if (unlocked) Icons.Default.DirectionsCar else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = AppStrings.drivingSimulator.get(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (unlocked) {
                            BiText("Operate 4CX, Dig Trenches & Load Truck", "4CX चलाएं, खाई खोदें और ट्रक लोड करें").get(lang)
                        } else {
                            AppStrings.unlockHint.get(lang)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Secondary Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.MenuBook,
                    label = BiText("Service Manual", "सर्विस मैनुअल").get(lang),
                    badge = state.micro.selectedVariant.name.take(7),
                    onClick = { onNavigate(ScreenRoute.SERVICE_MANUAL) },
                    modifier = Modifier.weight(1f).testTag("home_manual_button")
                )
                MenuTileButton(
                    icon = Icons.Default.Flag,
                    label = AppStrings.missionsTitle.get(lang),
                    badge = "${state.completedMissions.size}/7",
                    onClick = { onNavigate(ScreenRoute.MISSIONS) },
                    modifier = Modifier.weight(1f).testTag("home_missions_button")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuTileButton(
                    icon = Icons.Default.Info,
                    label = AppStrings.specGuideTitle.get(lang),
                    badge = "4CX",
                    onClick = { onNavigate(ScreenRoute.SPEC_GUIDE) },
                    modifier = Modifier.weight(1f).testTag("home_spec_button")
                )
                MenuTileButton(
                    icon = Icons.Default.Settings,
                    label = AppStrings.settingsTitle.get(lang),
                    badge = "${state.graphicsQuality.name} • ${state.controlLayout.name}",
                    onClick = { onNavigate(ScreenRoute.SETTINGS) },
                    modifier = Modifier.weight(1f).testTag("home_settings_button")
                )
            }
        }
    }
}

@Composable
private fun MenuTileButton(
    icon: ImageVector,
    label: String,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = SteelSurface, contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(68.dp)
            .border(1.dp, SteelBorder, RoundedCornerShape(14.dp))
    ) {
        Icon(icon, contentDescription = label, tint = JcbYellow, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(text = badge, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
        }
    }
}

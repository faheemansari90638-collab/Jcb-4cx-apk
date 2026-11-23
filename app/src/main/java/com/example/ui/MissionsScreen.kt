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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.GameUiState
import com.example.model.MissionCatalog
import com.example.model.MissionId
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
fun MissionsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onSelectMission: (MissionId, ScreenRoute) -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = state.language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("missions_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JcbYellow)
            }
            Spacer(Modifier.width(8.dp))
            Text(AppStrings.missionsTitle.get(lang), style = MaterialTheme.typography.headlineMedium, color = JcbYellow)
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(MissionCatalog.all, key = { it.id.name }) { mission ->
                val done = mission.id in state.completedMissions
                val isLocked = mission.requiresDrivingMode && !state.drivingUnlocked
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (done) HydraulicGreen else SteelBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (done) HydraulicGreen else JcbYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${mission.number}. ${mission.title.get(lang)}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Surface(color = SteelSurfaceElevated, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "+${mission.xpReward} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueprintCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(mission.description.get(lang), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(
                            text = mission.objectiveHint.get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = JcbYellow
                        )

                        Button(
                            onClick = {
                                val target = if (mission.requiresDrivingMode) ScreenRoute.DRIVING else ScreenRoute.WORKSHOP
                                onSelectMission(mission.id, target)
                            },
                            enabled = !isLocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (done) SteelSurfaceElevated else JcbYellow,
                                contentColor = if (done) JcbYellow else TextOnYellow
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("mission_btn_${mission.number}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isLocked -> AppStrings.locked.get(lang)
                                    done -> BiText("Replay Mission", "पुनः खेलें").get(lang)
                                    else -> BiText("Start Mission", "मिशन शुरू करें").get(lang)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

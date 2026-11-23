package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine3d.Mat4
import com.example.model.ActivePartManipState
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.GameUiState
import com.example.model.JcbCatalog
import com.example.model.TooltipTargetId
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextSecondary

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SteelSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(48.dp)
            .border(1.dp, BlueprintCyan.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Translate, contentDescription = AppStrings.language.get(language), tint = BlueprintCyan, modifier = Modifier.size(16.dp))
            val enSelected = language == AppLanguage.EN
            Surface(
                color = if (enSelected) JcbYellow else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "EN",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enSelected) TextOnYellow else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Surface(
                color = if (!enSelected) JcbYellow else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "हिन्दी",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!enSelected) TextOnYellow else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun ActivePartInspectorCard(
    state: GameUiState,
    manip: ActivePartManipState,
    tooltipRegistry: TooltipAnchorRegistry,
    onMovePart: (Float, Float, Float) -> Unit,
    onRotatePart: (Float, Float, Float) -> Unit,
    onDropWithGravity: () -> Unit,
    onTogglePhysicsHold: () -> Unit,
    onToggleSnapAssist: () -> Unit,
    onLockPlacement: () -> Unit,
    onFastenBolt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val spec = JcbCatalog.findPart(manip.partId)
    Column(
        modifier = modifier
            .tooltipAnchor(TooltipTargetId.INSPECTOR_ALIGN, tooltipRegistry)
            .background(SteelSurface.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
            .border(1.dp, if (manip.isAligned) HydraulicGreen else JcbYellow, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (spec != null) {
            Text(spec.name.get(lang), style = MaterialTheme.typography.titleMedium, color = JcbYellow, maxLines = 1, overflow = TextOverflow.Ellipsis)
            spec.modelYearNote?.let { note ->
                Text(note.get(lang), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }

        if (!manip.placedInSocket) {
            val dist = if (spec != null) manip.position.distanceTo(spec.targetPos) else 0f
            val rotErr = if (spec != null) {
                maxOf(
                    Mat4.angleDiffDeg(manip.rotationDeg.x, spec.targetRot.x),
                    Mat4.angleDiffDeg(manip.rotationDeg.y, spec.targetRot.y),
                    Mat4.angleDiffDeg(manip.rotationDeg.z, spec.targetRot.z)
                )
            } else 0f

            Surface(
                color = if (manip.isAligned) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                shape = RoundedCornerShape(7.dp)
            ) {
                Text(
                    text = if (manip.isAligned) AppStrings.readyToBolt.get(lang)
                    else "Δ ${String.format("%.2f", dist)}m  •  ∠ ${rotErr.toInt()}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (manip.isAligned) HydraulicGreen else Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                StepMiniBtn("-X") { onMovePart(-0.18f, 0f, 0f) }
                StepMiniBtn("+X") { onMovePart(0.18f, 0f, 0f) }
                StepMiniBtn("-Y") { onMovePart(0f, -0.18f, 0f) }
                StepMiniBtn("+Y") { onMovePart(0f, 0.18f, 0f) }
                StepMiniBtn("-Z") { onMovePart(0f, 0f, -0.18f) }
                StepMiniBtn("+Z") { onMovePart(0f, 0f, 0.18f) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                StepMiniBtn("RX-") { onRotatePart(-45f, 0f, 0f) }
                StepMiniBtn("RX+") { onRotatePart(45f, 0f, 0f) }
                StepMiniBtn("RY-") { onRotatePart(0f, -45f, 0f) }
                StepMiniBtn("RY+") { onRotatePart(0f, 45f, 0f) }
                StepMiniBtn("RZ-") { onRotatePart(0f, 0f, -45f) }
                StepMiniBtn("RZ+") { onRotatePart(0f, 0f, 45f) }
            }

            val phys = state.physics
            val massLabel = if (phys.massKg >= 1f) "${phys.massKg.toInt()}kg" else "${(phys.massKg * 1000).toInt()}g"
            val badge = when {
                phys.isSnapping -> BiText("MAG-SNAP", "स्नैप").get(lang)
                !phys.isPartHeld -> BiText("FALL", "गिर रहा").get(lang)
                phys.isGrounded -> BiText("GROUND", "फर्श").get(lang)
                else -> BiText("HOLD", "होल्ड").get(lang)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = if (phys.isSnapping) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).clickable { onTogglePhysicsHold() }.padding(horizontal = 5.dp, vertical = 4.dp)
                ) {
                    Text("$massLabel • $badge", style = MaterialTheme.typography.labelSmall, color = if (phys.isSnapping) HydraulicGreen else BlueprintCyan, maxLines = 1)
                }
                Surface(
                    color = SteelSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, JcbYellow.copy(alpha = 0.6f), RoundedCornerShape(6.dp)).clickable { onDropWithGravity() }.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(BiText("Drop", "गिराएं").get(lang), style = MaterialTheme.typography.labelSmall, color = JcbYellow)
                }
                Surface(
                    color = if (phys.snapAssistEnabled) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, if (phys.snapAssistEnabled) HydraulicGreen else SteelBorder, RoundedCornerShape(6.dp)).clickable { onToggleSnapAssist() }.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(BiText("Snap", "स्नैप").get(lang), style = MaterialTheme.typography.labelSmall, color = if (phys.snapAssistEnabled) HydraulicGreen else TextSecondary)
                }
            }

            phys.lastCollision?.let { col ->
                Surface(color = AmberWarning.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${BiText("Collision", "टकराव").get(lang)}: ${col.otherName.get(lang)} (${String.format("%.1f", col.impactSpeedMps)} m/s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberWarning,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Button(
                onClick = onLockPlacement,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (manip.isAligned) HydraulicGreen else JcbYellow,
                    contentColor = TextOnYellow
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .tooltipAnchor(TooltipTargetId.LOCK_BTN, tooltipRegistry)
                    .testTag("lock_placement_btn")
            ) {
                Text(AppStrings.lockPlacement.get(lang), style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Text(
                text = BiText("Bolts Secured: ${manip.boltsSecured}/${manip.totalBolts}", "कसे गए बोल्ट: ${manip.boltsSecured}/${manip.totalBolts}").get(lang),
                style = MaterialTheme.typography.titleMedium,
                color = HydraulicGreen
            )
            Text(
                text = "${AppStrings.toolsTitle.get(lang)}: ${spec?.requiredTool?.title?.get(lang) ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = BlueprintCyan
            )
            Button(
                onClick = onFastenBolt,
                colors = ButtonDefaults.buttonColors(containerColor = JcbYellow, contentColor = TextOnYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .tooltipAnchor(TooltipTargetId.FASTEN_BTN, tooltipRegistry)
                    .testTag("fasten_bolt_btn")
            ) {
                Text(AppStrings.fastenBolt.get(lang), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepMiniBtn(label: String, onClick: () -> Unit) {
    Surface(
        color = SteelSurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
            .border(1.dp, SteelBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
fun HorizontalLevel10Checklist(
    state: GameUiState,
    onStep: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val c = state.commissioning
    val steps = listOf(
        Triple("engine_oil", BiText("1. Fill Engine Oil", "1. इंजन ऑयल भरें"), c.engineOilFilled),
        Triple("hyd_oil", BiText("2. Fill Hydraulic Oil", "2. हाइड्रोलिक ऑयल"), c.hydraulicOilFilled),
        Triple("coolant", BiText("3. Add Coolant", "3. कूलेंट डालें"), c.coolantAdded),
        Triple("diesel", BiText("4. Add Diesel", "4. डीजल भरें"), c.dieselAdded),
        Triple("battery", BiText("5. Scan Battery", "5. बैटरी जांचें"), c.batteryChecked),
        Triple("start_engine", BiText("6. Start Engine", "6. इंजन चालू करें"), c.engineStarted),
        Triple("warning_lights", BiText("7. Warning Lights", "7. वॉर्निंग लाइट्स"), c.warningLightsChecked),
        Triple("steering", BiText("8. Test Steering", "8. स्टीयरिंग टेस्ट"), c.steeringTested),
        Triple("gears", BiText("9. Test Gears", "9. गियर टेस्ट"), c.gearsTested),
        Triple("loader", BiText("10. Loader Bucket", "10. लोडर बकेट"), c.loaderTested),
        Triple("excavator", BiText("11. Rear Excavator", "11. रियर एक्स्कवेटर"), c.excavatorTested),
        Triple("stabilizers", BiText("12. Stabilizers", "12. स्टेबलाइज़र"), c.stabilizersTested),
        Triple("final_inspection", BiText("13. Final QC", "13. अंतिम निरीक्षण"), c.finalInspectionDone)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        items(steps, key = { it.first }) { (id, title, done) ->
            Surface(
                color = if (done) HydraulicGreen.copy(alpha = 0.18f) else SteelSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .width(170.dp)
                    .height(58.dp)
                    .border(1.dp, if (done) HydraulicGreen else SteelBorder, RoundedCornerShape(10.dp))
                    .clickable(enabled = !done) { onStep(id) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("comm_step_$id")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (done) HydraulicGreen else JcbYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(title.get(lang), style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun WorkshopPauseOverlay(
    state: GameUiState,
    onTogglePause: () -> Unit,
    onSaveGame: () -> Unit,
    onRestartLevel: (Int) -> Unit,
    onBackToMenu: () -> Unit
) {
    val lang = state.language
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(SteelSurface, RoundedCornerShape(16.dp))
                .border(1.5.dp, JcbYellow, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(AppStrings.pause.get(lang), style = MaterialTheme.typography.headlineMedium, color = JcbYellow)
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(containerColor = JcbYellow, contentColor = TextOnYellow),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(AppStrings.resume.get(lang))
            }
            Button(
                onClick = onSaveGame,
                colors = ButtonDefaults.buttonColors(containerColor = HydraulicGreen, contentColor = TextOnYellow),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(AppStrings.saveProgress.get(lang))
            }
            OutlinedButton(
                onClick = {
                    onRestartLevel(state.currentLevel)
                    onTogglePause()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("${AppStrings.restartLevel.get(lang)} (L${state.currentLevel})", color = JcbYellow)
            }
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(AppStrings.backToMenu.get(lang), color = Color.White)
            }
        }
    }
}

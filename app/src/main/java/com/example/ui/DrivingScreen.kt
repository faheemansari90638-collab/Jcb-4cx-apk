package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine3d.GlRenderer
import com.example.engine3d.Jcb3DViewport
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.CameraMode
import com.example.model.ControlLayout
import com.example.model.GameUiState
import com.example.model.GearMode
import com.example.model.MissionId
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.HazardRed
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun DrivingScreen(
    state: GameUiState,
    renderer: GlRenderer,
    onBackToMenu: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleCamera: () -> Unit,
    onResetCamera: () -> Unit,
    onZoomCamera: (Float) -> Unit,
    onToggleEngine: () -> Unit,
    onSetGear: (GearMode) -> Unit,
    onToggle4Wd: () -> Unit,
    onToggle4Ws: () -> Unit,
    onSetInputs: (throttle: Float?, brake: Float?, steer: Float?) -> Unit,
    onAdjustLoaderBoom: (Float) -> Unit,
    onAdjustLoaderBucket: (Float) -> Unit,
    onAdjustRearSwing: (Float) -> Unit,
    onAdjustRearBoom: (Float) -> Unit,
    onAdjustRearDipper: (Float) -> Unit,
    onAdjustRearBucket: (Float) -> Unit,
    onAdjustStabilizers: (Float) -> Unit,
    onToggleControlLayout: () -> Unit,
    onFinalInspection: () -> Unit
) {
    BackHandler(onBack = onBackToMenu)
    val lang = state.language
    val d = state.driving

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
    ) {
        Jcb3DViewport(
            state = state,
            renderer = renderer,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp)
        ) {
            // ---------- TOP CAB TELEMETRY & MISSION HUD ----------
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(SteelSurface.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onTogglePause, modifier = Modifier.size(44.dp).testTag("drive_pause_btn")) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = JcbYellow)
                }

                Column(modifier = Modifier.weight(1f)) {
                    val speedKmh = abs(d.speedMps * 3.6f).toInt()
                    Text(
                        text = "${d.rpm.toInt()} RPM • $speedKmh km/h • ${d.gear.label} • ${if (d.is4Wd) "4WD" else "2WD"} • ${if (d.is4Ws) "4WS" else "2WS"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = JcbYellow
                    )
                    Text(
                        text = BiText(
                            "Soil: ${d.soilMovedCount}/2 • Trench: ${(d.trenchProgress * 100).toInt()}% • Truck: ${(d.truckLoadProgress * 100).toInt()}% • Park: ${if (d.isInParkingBay) "IN BAY" else "---"}",
                            "मिट्टी: ${d.soilMovedCount}/2 • खाई: ${(d.trenchProgress * 100).toInt()}% • ट्रक: ${(d.truckLoadProgress * 100).toInt()}%"
                        ).get(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueprintCyan
                    )
                }

                Button(
                    onClick = onFinalInspection,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = TextOnYellow),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(40.dp).testTag("drive_inspect_btn")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(AppStrings.inspectMachine.get(lang), style = MaterialTheme.typography.labelSmall)
                }

                IconButton(onClick = onToggleControlLayout, modifier = Modifier.size(40.dp).testTag("drive_layout_toggle")) {
                    Icon(Icons.Default.Build, contentDescription = "Toggle Joystick/6-Lever", tint = JcbYellow)
                }
                IconButton(onClick = { onZoomCamera(-1.0f) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                }
                IconButton(onClick = { onZoomCamera(1.0f) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
                IconButton(onClick = onToggleCamera, modifier = Modifier.size(40.dp).testTag("drive_cam_btn")) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = if (state.cameraMode == CameraMode.FIRST_PERSON_CAB) HydraulicGreen else JcbYellow
                    )
                }
                IconButton(onClick = onResetCamera, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Cam", tint = Color.White)
                }
                IconButton(onClick = onToggleLanguage, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Translate, contentDescription = "Language", tint = BlueprintCyan)
                }
            }

            state.feedbackBanner?.let { (msg, isError) ->
                Surface(
                    color = if (isError) HazardRed.copy(alpha = 0.94f) else HydraulicGreen.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 58.dp)
                ) {
                    Text(msg.get(lang), style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(12.dp))
                }
            }

            // ---------- LEFT CLUSTER: IGNITION, GEARS, 4WD/4WS, STEERING ----------
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(235.dp)
                    .background(SteelSurface.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onToggleEngine,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (d.engineRunning) HazardRed else HydraulicGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("drive_engine_btn"),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (d.engineRunning) AppStrings.engineStop.get(lang) else AppStrings.engineStart.get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                    ModeChip(if (d.is4Wd) "4WD" else "2WD", d.is4Wd, onToggle4Wd)
                    ModeChip(if (d.is4Ws) "4WS" else "2WS", d.is4Ws, onToggle4Ws)
                }

                // Gear Selector Row
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    GearMode.entries.forEach { g ->
                        val active = d.gear == g
                        Surface(
                            color = if (active) (if (g == GearMode.R) HazardRed else JcbYellow) else SteelSurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { onSetGear(g) }
                                .testTag("gear_${g.label}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    g.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) TextOnYellow else Color.White
                                )
                            }
                        }
                    }
                }

                // Left / Right Steer Hold Pads
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    HoldActionPad(
                        label = "◀ ${BiText("STEER L", "बाएं").get(lang)}",
                        onPressChange = { pressed -> onSetInputs(null, null, if (pressed) -1f else 0f) },
                        modifier = Modifier.weight(1f).height(52.dp).testTag("steer_left_btn")
                    )
                    HoldActionPad(
                        label = "${BiText("STEER R", "दाएं").get(lang)} ▶",
                        onPressChange = { pressed -> onSetInputs(null, null, if (pressed) 1f else 0f) },
                        modifier = Modifier.weight(1f).height(52.dp).testTag("steer_right_btn")
                    )
                }
            }

            // ---------- RIGHT CLUSTER: HYDRAULICS (JOYSTICK OR 6-LEVER) + PEDALS ----------
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(292.dp)
                    .background(SteelSurface.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = state.controlLayout.label.get(lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = JcbYellow
                )

                if (state.controlLayout == ControlLayout.JOYSTICK) {
                    // Front Loader + Rear Backhoe Compact Dual-Pod Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        HydraulicBtn("ARM ▲", Modifier.weight(1f)) { onAdjustLoaderBoom(6f) }
                        HydraulicBtn("ARM ▼", Modifier.weight(1f)) { onAdjustLoaderBoom(-6f) }
                        HydraulicBtn("CURL", Modifier.weight(1f)) { onAdjustLoaderBucket(-8f) }
                        HydraulicBtn("DUMP", Modifier.weight(1f)) { onAdjustLoaderBucket(8f) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        HydraulicBtn("SWING ◀", Modifier.weight(1f)) { onAdjustRearSwing(-8f) }
                        HydraulicBtn("SWING ▶", Modifier.weight(1f)) { onAdjustRearSwing(8f) }
                        HydraulicBtn("BOOM ▲", Modifier.weight(1f)) { onAdjustRearBoom(6f) }
                        HydraulicBtn("BOOM ▼", Modifier.weight(1f)) { onAdjustRearBoom(-6f) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        HydraulicBtn("DIP ▲", Modifier.weight(1f)) { onAdjustRearDipper(8f) }
                        HydraulicBtn("DIP ▼", Modifier.weight(1f)) { onAdjustRearDipper(-8f) }
                        HydraulicBtn("DIG", Modifier.weight(1f)) { onAdjustRearBucket(-10f) }
                        HydraulicBtn("STAB ▼", Modifier.weight(1f)) { onAdjustStabilizers(if (d.stabilizerExt > 0.5f) -1f else 1f) }
                    }
                } else {
                    // Classic 6-Lever + Loader Bank
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        LeverColumn("LDR", { onAdjustLoaderBoom(7f) }, { onAdjustLoaderBoom(-7f) }, Modifier.weight(1f))
                        LeverColumn("BKT", { onAdjustLoaderBucket(-9f) }, { onAdjustLoaderBucket(9f) }, Modifier.weight(1f))
                        LeverColumn("SWG", { onAdjustRearSwing(-9f) }, { onAdjustRearSwing(9f) }, Modifier.weight(1f))
                        LeverColumn("BOM", { onAdjustRearBoom(7f) }, { onAdjustRearBoom(-7f) }, Modifier.weight(1f))
                        LeverColumn("DIP", { onAdjustRearDipper(9f) }, { onAdjustRearDipper(-9f) }, Modifier.weight(1f))
                        LeverColumn("EXC", { onAdjustRearBucket(-10f) }, { onAdjustRearBucket(10f) }, Modifier.weight(1f))
                        LeverColumn("STB", { onAdjustStabilizers(-0.5f) }, { onAdjustStabilizers(0.5f) }, Modifier.weight(1f))
                    }
                }

                // Accelerator & Brake Pedals
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    HoldActionPad(
                        label = BiText("BRAKE", "ब्रेक").get(lang),
                        activeColor = HazardRed,
                        onPressChange = { pressed -> onSetInputs(null, if (pressed) 1f else 0f, null) },
                        modifier = Modifier.weight(1f).height(52.dp).testTag("brake_pedal_btn")
                    )
                    HoldActionPad(
                        label = BiText("ACCEL", "एक्सीलरेटर").get(lang),
                        activeColor = HydraulicGreen,
                        onPressChange = { pressed -> onSetInputs(if (pressed) 1f else 0f, null, null) },
                        modifier = Modifier.weight(1.2f).height(52.dp).testTag("accel_pedal_btn")
                    )
                }
            }
        }

        if (state.isPaused) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .background(SteelSurface, RoundedCornerShape(16.dp))
                        .border(1.5.dp, JcbYellow, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(AppStrings.pause.get(lang), style = MaterialTheme.typography.headlineMedium, color = JcbYellow)
                    Text(
                        text = "${BiText("Completed Missions", "पूर्ण मिशन").get(lang)}: ${state.completedMissions.size}/7",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Button(
                        onClick = onTogglePause,
                        colors = ButtonDefaults.buttonColors(containerColor = JcbYellow, contentColor = TextOnYellow),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(AppStrings.resume.get(lang))
                    }
                    OutlinedButton(onClick = onBackToMenu, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(AppStrings.backToMenu.get(lang), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (active) BlueprintCyan else SteelSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) TextOnYellow else Color.White)
        }
    }
}

@Composable
private fun HydraulicBtn(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = SteelSurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .height(38.dp)
            .border(1.dp, SteelBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
private fun LeverColumn(label: String, onUp: () -> Unit, onDown: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        HydraulicBtn("▲", Modifier.fillMaxWidth(), onUp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
        HydraulicBtn("▼", Modifier.fillMaxWidth(), onDown)
    }
}

@Composable
private fun HoldActionPad(
    label: String,
    activeColor: Color = JcbYellow,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = activeColor.copy(alpha = 0.24f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.5.dp, activeColor, RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

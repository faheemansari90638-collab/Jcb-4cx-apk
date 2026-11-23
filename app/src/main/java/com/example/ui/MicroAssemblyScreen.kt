package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine3d.GlRenderer
import com.example.engine3d.Jcb3DViewport
import com.example.engine3d.Mat4
import com.example.model.AssemblyTooltipCatalog
import com.example.model.BiText
import com.example.model.GameUiState
import com.example.model.MicroCategory
import com.example.model.MicroPartCatalog
import com.example.model.MicroSimType
import com.example.model.ToolType
import com.example.model.TooltipTargetId
import com.example.ui.theme.AmberWarning
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

@Composable
fun MicroAssemblyScreen(
    state: GameUiState,
    renderer: GlRenderer,
    onBack: () -> Unit,
    onOpenManual: () -> Unit,
    onToggleLanguage: () -> Unit,
    onZoomCamera: (Float) -> Unit,
    onResetCamera: () -> Unit,
    onToggleExploded: () -> Unit,
    onExplodeFactor: (Float) -> Unit,
    onToggleTransparent: () -> Unit,
    onToggleInspect: () -> Unit,
    onSelectCategory: (MicroCategory) -> Unit,
    onSelectPart: (String) -> Unit,
    onMovePart: (Float, Float, Float) -> Unit,
    onRotatePart: (Float, Float, Float) -> Unit,
    onDropWithGravity: () -> Unit,
    onTogglePhysicsHold: () -> Unit,
    onToggleSnapAssist: () -> Unit,
    onAnimateStep: () -> Unit,
    onSetRingStagger: (Int) -> Unit,
    onSetValveSpring: (Float) -> Unit,
    onSetTorqueAndAngle: (Int, Int) -> Unit,
    onInstallPart: () -> Unit,
    onSelectTool: (ToolType) -> Unit,
    onCheckBearingClearance: () -> Unit,
    onCheckCrankRotation: () -> Unit,
    onOpenTooltipSpotlight: () -> Unit,
    onDismissTooltipSpotlight: () -> Unit,
    onNextTooltipStep: () -> Unit,
    onPrevTooltipStep: () -> Unit,
    onToggleAutoTooltips: (Boolean) -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = state.language
    val ms = state.micro
    val tooltipRegistry = rememberTooltipAnchorRegistry()
    val activeTooltip = remember(
        ms.activeMicro,
        ms.selectedCategory,
        ms.installedMicroParts.size,
        state.selectedTool,
        state.tooltipGuide.manualTourIndex
    ) {
        AssemblyTooltipCatalog.resolveMicroTooltip(state, state.tooltipGuide.manualTourIndex)
    }

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
            // ---------- TOP PRECISION BAR ----------
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .tooltipAnchor(TooltipTargetId.MICRO_TOOLBAR, tooltipRegistry)
                    .background(SteelSurface.copy(alpha = 0.90f), RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).testTag("micro_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JcbYellow)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${BiText("MICRO ENGINE BAY", "माइक्रो इंजन असेंबली").get(lang)} • ${ms.installedMicroParts.size}/${MicroPartCatalog.parts.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = JcbYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ms.selectedVariant.title.get(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueprintCyan,
                        maxLines = 1
                    )
                }

                ViewToggleChip(
                    icon = Icons.Default.Layers,
                    label = BiText("Explode", "एक्सप्लोड").get(lang),
                    active = ms.explodedMode,
                    onClick = onToggleExploded,
                    modifier = Modifier.testTag("micro_explode_btn")
                )
                ViewToggleChip(
                    icon = Icons.Default.Visibility,
                    label = BiText("X-Ray", "पारदर्शी").get(lang),
                    active = ms.transparentBlockMode,
                    onClick = onToggleTransparent,
                    modifier = Modifier.testTag("micro_xray_btn")
                )
                ViewToggleChip(
                    icon = Icons.Default.Visibility,
                    label = BiText("Inspect", "निरीक्षण").get(lang),
                    active = ms.inspectionMode,
                    onClick = onToggleInspect,
                    modifier = Modifier.testTag("micro_inspect_btn")
                )

                Button(
                    onClick = onOpenManual,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = TextOnYellow),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(38.dp).testTag("micro_manual_btn")
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(BiText("Manual", "मैनुअल").get(lang), style = MaterialTheme.typography.labelSmall)
                }

                IconButton(onClick = { onZoomCamera(-0.22f) }, modifier = Modifier.size(38.dp).testTag("micro_zoom_in")) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Macro Zoom In", tint = Color.White)
                }
                IconButton(onClick = { onZoomCamera(0.22f) }, modifier = Modifier.size(38.dp).testTag("micro_zoom_out")) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                }
                IconButton(onClick = onResetCamera, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Cam", tint = Color.White)
                }
                IconButton(onClick = onToggleLanguage, modifier = Modifier.size(38.dp).testTag("micro_lang_btn")) {
                    Icon(Icons.Default.Translate, contentDescription = "Lang", tint = BlueprintCyan)
                }
                IconButton(onClick = onOpenTooltipSpotlight, modifier = Modifier.size(38.dp).testTag("micro_help_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = BiText("Micro Assembly Guide", "माइक्रो असेंबली गाइड").get(lang),
                        tint = if (state.tooltipGuide.autoContextualEnabled) JcbYellow else TextSecondary
                    )
                }
            }

            // Feedback Toast Banner
            state.feedbackBanner?.let { (msg, isError) ->
                Surface(
                    color = if (isError) HazardRed.copy(alpha = 0.94f) else HydraulicGreen.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp)
                ) {
                    Text(msg.get(lang), style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }

            // ---------- LEFT PANEL: MICRO-COMPONENTS TREE + TESTS ----------
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(top = 58.dp)
                    .width(272.dp)
                    .fillMaxHeight()
                    .tooltipAnchor(TooltipTargetId.MICRO_PARTS, tooltipRegistry)
                    .background(SteelSurface.copy(alpha = 0.90f), RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (ms.explodedMode) {
                    Text(
                        text = "${BiText("Explode Spread", "फैलाव").get(lang)}: ${(ms.explodeFactor * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = JcbYellow
                    )
                    Slider(
                        value = ms.explodeFactor,
                        onValueChange = onExplodeFactor,
                        colors = SliderDefaults.colors(thumbColor = JcbYellow, activeTrackColor = JcbYellow),
                        modifier = Modifier.height(28.dp)
                    )
                }

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(MicroCategory.entries) { cat ->
                        val sel = ms.selectedCategory == cat
                        Surface(
                            color = if (sel) JcbYellow else SteelSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { onSelectCategory(cat) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat.title.get(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sel) TextOnYellow else Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Individual Micro-Parts List
                val catParts = MicroPartCatalog.byCategory(ms.selectedCategory)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(catParts, key = { it.id }) { part ->
                        val installed = part.id in ms.installedMicroParts
                        val active = ms.activeMicro?.partId == part.id
                        val locked = part.prerequisites.any { it !in ms.installedMicroParts }
                        Surface(
                            color = when {
                                active -> JcbYellow.copy(alpha = 0.22f)
                                installed -> HydraulicGreen.copy(alpha = 0.14f)
                                else -> SteelSurfaceElevated
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    when {
                                        active -> JcbYellow
                                        installed -> HydraulicGreen
                                        part.isInternalHidden && ms.inspectionMode -> BlueprintCyan
                                        else -> SteelBorder
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPart(part.id) }
                                .padding(8.dp)
                                .testTag("micro_part_${part.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when {
                                        installed -> Icons.Default.CheckCircle
                                        locked -> Icons.Default.Lock
                                        else -> Icons.Default.Build
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        installed -> HydraulicGreen
                                        locked -> TextSecondary
                                        else -> JcbYellow
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(7.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(part.name.dual(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${part.requiredTool.title.get(lang)} • ${part.targetDepthMm.toInt()}mm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Mechanical Clearance & Crank Rotation Simulations
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onCheckBearingClearance,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("check_bearing_btn")
                    ) {
                        Text(
                            text = if (ms.bearingClearanceVerified) "✓ Clear OK" else BiText("Plastigage", "क्लियरेंस जांच").get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ms.bearingClearanceVerified) HydraulicGreen else BlueprintCyan,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = onCheckCrankRotation,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("check_crank_btn")
                    ) {
                        Text(
                            text = if (ms.crankSpinVerified) "✓ 720° OK" else BiText("Spin Crank", "क्रैंक घुमाएं").get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ms.crankSpinVerified) HydraulicGreen else JcbYellow,
                            maxLines = 1
                        )
                    }
                }
            }

            // ---------- RIGHT PANEL: PRECISION ALIGNMENT, SIMS & TORQUE ----------
            val active = ms.activeMicro
            if (active != null) {
                val spec = MicroPartCatalog.findPart(active.partId)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 58.dp)
                        .width(276.dp)
                        .tooltipAnchor(TooltipTargetId.MICRO_INSPECTOR, tooltipRegistry)
                        .background(SteelSurface.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                        .border(1.dp, if (active.isAligned) HydraulicGreen else JcbYellow, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (spec != null) {
                        Text(spec.name.dual(), style = MaterialTheme.typography.titleMedium, color = JcbYellow)
                        Text(spec.stepInstruction.get(lang), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                        val distMm = (active.position.distanceTo(spec.targetPos) * 1000f).toInt()
                        val rotErr = maxOf(
                            Mat4.angleDiffDeg(active.rotationDeg.x, spec.targetRot.x),
                            Mat4.angleDiffDeg(active.rotationDeg.y, spec.targetRot.y),
                            Mat4.angleDiffDeg(active.rotationDeg.z, spec.targetRot.z)
                        ).toInt()

                        Surface(
                            color = if (active.isAligned) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(7.dp)) {
                                Text(
                                    text = "Δ ${distMm}mm • ∠ ${rotErr}° • ${BiText("Depth", "गहराई").get(lang)}: ${String.format("%.1f", active.currentDepthMm)}/${spec.targetDepthMm}mm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active.isAligned) HydraulicGreen else Color.White
                                )
                            }
                        }

                        // Move & Rotate Precision Controls
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            MicroStepBtn("-X") { onMovePart(-0.05f, 0f, 0f) }
                            MicroStepBtn("+X") { onMovePart(0.05f, 0f, 0f) }
                            MicroStepBtn("-Y") { onMovePart(0f, -0.05f, 0f) }
                            MicroStepBtn("+Y") { onMovePart(0f, 0.05f, 0f) }
                            MicroStepBtn("-Z") { onMovePart(0f, 0f, -0.05f) }
                            MicroStepBtn("+Z") { onMovePart(0f, 0f, 0.05f) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            MicroStepBtn("RX-") { onRotatePart(-45f, 0f, 0f) }
                            MicroStepBtn("RX+") { onRotatePart(45f, 0f, 0f) }
                            MicroStepBtn("RY-") { onRotatePart(0f, -45f, 0f) }
                            MicroStepBtn("RY+") { onRotatePart(0f, 45f, 0f) }
                            MicroStepBtn("RZ-") { onRotatePart(0f, 0f, -45f) }
                            MicroStepBtn("RZ+") { onRotatePart(0f, 0f, 45f) }
                        }

                        // Physics Telemetry, Gravity Drop & Magnetic Snap Assist
                        val phys = state.physics
                        val massLabel = if (phys.massKg >= 1f) "${String.format("%.1f", phys.massKg)}kg" else "${(phys.massKg * 1000).toInt()}g"
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = if (phys.isSnapping) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTogglePhysicsHold() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("micro_physics_hold_btn")
                            ) {
                                val badge = when {
                                    phys.isSnapping -> BiText("MAG-SNAP", "स्नैप").get(lang)
                                    !phys.isPartHeld -> BiText("FALL ${String.format("%.1f", phys.velocityMps)}m/s", "गिर रहा").get(lang)
                                    else -> BiText("HOLD", "होल्ड").get(lang)
                                }
                                Text("$massLabel • $badge", style = MaterialTheme.typography.labelSmall, color = if (phys.isSnapping) HydraulicGreen else BlueprintCyan, maxLines = 1)
                            }
                            Surface(
                                color = SteelSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .border(1.dp, JcbYellow.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { onDropWithGravity() }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                                    .testTag("micro_drop_gravity_btn")
                            ) {
                                Text(BiText("Drop", "गिराएं").get(lang), style = MaterialTheme.typography.labelSmall, color = JcbYellow)
                            }
                            Surface(
                                color = if (phys.snapAssistEnabled) HydraulicGreen.copy(alpha = 0.22f) else SteelSurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .border(1.dp, if (phys.snapAssistEnabled) HydraulicGreen else SteelBorder, RoundedCornerShape(6.dp))
                                    .clickable { onToggleSnapAssist() }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                                    .testTag("micro_snap_toggle_btn")
                            ) {
                                Text(BiText("Snap", "स्नैप").get(lang), style = MaterialTheme.typography.labelSmall, color = if (phys.snapAssistEnabled) HydraulicGreen else TextSecondary)
                            }
                        }

                        phys.lastCollision?.let { col ->
                            Surface(
                                color = AmberWarning.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${BiText("Contact", "टकराव").get(lang)}: ${col.otherName.get(lang)} (${String.format("%.1f", col.impactSpeedMps)} m/s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberWarning,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onAnimateStep,
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("micro_animate_btn"),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = BlueprintCyan)
                            Spacer(Modifier.width(4.dp))
                            Text(BiText("Animate Assembly Path", "असेंबली एनीमेशन देखें").get(lang), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
                        }

                        // Simulation 1: Piston Ring Stagger
                        if (spec.simType == MicroSimType.PISTON_RING_FIT) {
                            Text(BiText("Ring End-Gap Stagger (°)", "रिंग गैप कोण (°)").get(lang), style = MaterialTheme.typography.labelSmall, color = JcbYellow)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf(0, 120, 240).forEach { deg ->
                                    val sel = active.ringGapStaggerDeg == deg
                                    Surface(
                                        color = if (sel) JcbYellow else SteelSurfaceElevated,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).clickable { onSetRingStagger(deg) }.padding(vertical = 6.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${deg}°", style = MaterialTheme.typography.labelSmall, color = if (sel) TextOnYellow else Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Simulation 2: Valve Spring Compressor
                        if (spec.simType == MicroSimType.VALVE_SPRING_COMPRESS) {
                            Text(
                                "${BiText("Valve Spring Compressor", "वाल्व स्प्रिंग कम्प्रेसर").get(lang)}: ${(ms.valveSpringCompressed * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = JcbYellow
                            )
                            Slider(
                                value = ms.valveSpringCompressed,
                                onValueChange = onSetValveSpring,
                                colors = SliderDefaults.colors(thumbColor = JcbYellow, activeTrackColor = JcbYellow),
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        // Simulation 3: Torque Wrench & Angle Spec
                        if (spec.torqueSpecKey != null) {
                            val (reqNm, reqAng) = MicroPartCatalog.requiredTorqueHintNm(spec.torqueSpecKey, ms.selectedVariant)
                            val userSpec = ms.customManualSpecs[spec.torqueSpecKey]
                            if (reqNm != null) {
                                Text(
                                    "${BiText("Target Torque", "लक्षित टॉर्क").get(lang)}: $reqNm Nm + ${reqAng ?: 0}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HydraulicGreen
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    MicroStepBtn("-10 Nm") { onSetTorqueAndAngle(active.torqueInputNm - 10, active.angleInputDeg) }
                                    MicroStepBtn("${active.torqueInputNm} Nm") { }
                                    MicroStepBtn("+10 Nm") { onSetTorqueAndAngle(active.torqueInputNm + 10, active.angleInputDeg) }
                                    MicroStepBtn("+90°") { onSetTorqueAndAngle(active.torqueInputNm, (active.angleInputDeg + 90) % 270) }
                                }
                            } else {
                                Surface(
                                    color = if (userSpec.isNullOrBlank()) AmberWarning.copy(alpha = 0.22f) else HydraulicGreen.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { onOpenManual() }.padding(6.dp)
                                ) {
                                    Text(
                                        text = if (userSpec.isNullOrBlank()) {
                                            BiText("Verified Service-Manual Data Required — Tap to Open Manual", "सत्यापित सर्विस मैनुअल डेटा दर्ज करें (यहाँ दबाएं)").get(lang)
                                        } else {
                                            "Manual Spec Verified: $userSpec"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (userSpec.isNullOrBlank()) AmberWarning else HydraulicGreen
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onInstallPart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active.isAligned) HydraulicGreen else JcbYellow,
                                contentColor = TextOnYellow
                            ),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("micro_install_btn")
                        ) {
                            Text(
                                text = "${spec.requiredTool.title.get(lang)} • ${BiText("Install / Torque", "इंस्टॉल करें").get(lang)}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // ---------- BOTTOM 10-TOOL BELT ----------
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 280.dp, end = if (active != null) 284.dp else 0.dp)
                    .background(SteelSurface.copy(alpha = 0.88f), RoundedCornerShape(10.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(10.dp))
                    .padding(5.dp)
            ) {
                items(ToolType.entries) { tool ->
                    val sel = state.selectedTool == tool
                    Surface(
                        color = if (sel) JcbYellow else SteelSurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onSelectTool(tool) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(tool.title.get(lang), style = MaterialTheme.typography.labelSmall, color = if (sel) TextOnYellow else Color.White)
                    }
                }
            }

            // ---------- CONTEXTUAL TOOLTIP OVERLAY ----------
            AssemblyTooltipOverlay(
                tooltip = activeTooltip,
                language = lang,
                spotlightOpen = state.tooltipGuide.spotlightOpen,
                autoContextualEnabled = state.tooltipGuide.autoContextualEnabled,
                isPaused = state.isPaused,
                hideLivePill = state.feedbackBanner != null,
                registry = tooltipRegistry,
                onToggleLanguage = onToggleLanguage,
                onOpenSpotlight = onOpenTooltipSpotlight,
                onDismissSpotlight = onDismissTooltipSpotlight,
                onNextStep = onNextTooltipStep,
                onPrevStep = onPrevTooltipStep,
                onMuteAutoTips = { onToggleAutoTooltips(false) }
            )
        }
    }
}

@Composable
private fun ViewToggleChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (active) JcbYellow else SteelSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick)
            .border(1.dp, if (active) JcbYellow else SteelBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = if (active) TextOnYellow else Color.White, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) TextOnYellow else Color.White)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MicroStepBtn(label: String, onClick: () -> Unit) {
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

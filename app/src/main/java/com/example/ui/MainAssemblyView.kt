package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.engine3d.GlRenderer
import com.example.engine3d.Jcb3DViewport
import com.example.model.AppStrings
import com.example.model.AssemblyTooltipCatalog
import com.example.model.BiText
import com.example.model.CameraMode
import com.example.model.GameUiState
import com.example.model.JcbCatalog
import com.example.model.ToolType
import com.example.model.TooltipTargetId
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

/**
 * Main Compose assembly view featuring:
 * 1. Interactive framed 3D Preview Window (orbit, zoom, 1st-person cab, rigid-body physics)
 * 2. Scrollable horizontal parts inventory tray with L1-L10 tabs
 * 3. Dedicated English / Hindi language toggle pill
 */
@Composable
fun MainAssemblyView(
    state: GameUiState,
    renderer: GlRenderer,
    onTogglePause: () -> Unit,
    onBackToMenu: () -> Unit,
    onNavigateDriving: () -> Unit,
    onNavigateMicroAssembly: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleCamera: () -> Unit,
    onResetCamera: () -> Unit,
    onZoomCamera: (Float) -> Unit,
    onToggleInventory: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    onSelectPart: (String) -> Unit,
    onMovePart: (Float, Float, Float) -> Unit,
    onRotatePart: (Float, Float, Float) -> Unit,
    onLockPlacement: () -> Unit,
    onDropWithGravity: () -> Unit,
    onTogglePhysicsHold: () -> Unit,
    onToggleSnapAssist: () -> Unit,
    onSelectTool: (ToolType) -> Unit,
    onFastenBolt: () -> Unit,
    onCommissioningStep: (String) -> Unit,
    onSaveGame: () -> Unit,
    onRestartLevel: (Int) -> Unit,
    onOpenTooltipSpotlight: () -> Unit,
    onDismissTooltipSpotlight: () -> Unit,
    onNextTooltipStep: () -> Unit,
    onPrevTooltipStep: () -> Unit,
    onToggleAutoTooltips: (Boolean) -> Unit
) {
    BackHandler(onBack = onBackToMenu)
    val lang = state.language
    val tooltipRegistry = rememberTooltipAnchorRegistry()
    val activeTooltip = remember(
        state.currentLevel,
        state.activeManip,
        state.selectedTool,
        state.physics.lastCollision,
        state.commissioning,
        state.tooltipGuide.manualTourIndex,
        state.installedParts.size
    ) {
        AssemblyTooltipCatalog.resolveWorkshopTooltip(state, state.tooltipGuide.manualTourIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ==================== 1. TOP HEADER WITH EN / HI TOGGLE ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SteelSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(48.dp).testTag("workshop_pause_btn")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = AppStrings.pause.get(lang), tint = JcbYellow)
                }

                Column(modifier = Modifier.weight(1f)) {
                    val lvlSpec = JcbCatalog.levels.find { it.levelNumber == state.currentLevel }
                    Text(
                        text = lvlSpec?.title?.get(lang) ?: "${AppStrings.levelLabel.get(lang)} ${state.currentLevel}",
                        style = MaterialTheme.typography.titleMedium,
                        color = JcbYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { state.assemblyPercent / 100f },
                            modifier = Modifier.width(88.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (state.assemblyPercent >= 100) HydraulicGreen else JcbYellow,
                            trackColor = SteelSurfaceElevated
                        )
                        Text(
                            text = "${state.assemblyPercent}% • ${state.xp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNavigateMicroAssembly,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(40.dp).testTag("workshop_micro_btn")
                ) {
                    Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp), tint = BlueprintCyan)
                    Spacer(Modifier.width(4.dp))
                    Text(BiText("MICRO", "माइक्रो").get(lang), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
                }

                if (state.drivingUnlocked) {
                    Button(
                        onClick = onNavigateDriving,
                        colors = ButtonDefaults.buttonColors(containerColor = HydraulicGreen, contentColor = TextOnYellow),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(40.dp).testTag("workshop_drive_btn")
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(BiText("DRIVE", "चलाएं").get(lang), style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Explicit Bilingual English / Hindi Toggle Pill
                LanguageTogglePill(
                    language = lang,
                    onToggle = onToggleLanguage,
                    modifier = Modifier
                        .tooltipAnchor(TooltipTargetId.LANGUAGE_TOGGLE, tooltipRegistry)
                        .testTag("main_assembly_lang_toggle")
                )

                IconButton(
                    onClick = onOpenTooltipSpotlight,
                    modifier = Modifier.size(48.dp).testTag("workshop_help_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = BiText("Assembly Guide", "असेंबली गाइड").get(lang),
                        tint = if (state.tooltipGuide.autoContextualEnabled) JcbYellow else TextSecondary
                    )
                }

                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier.size(44.dp).testTag("workshop_cam_btn")
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = if (state.cameraMode == CameraMode.FIRST_PERSON_CAB) {
                            AppStrings.camera1st.get(lang)
                        } else {
                            AppStrings.camera3rd.get(lang)
                        },
                        tint = if (state.cameraMode == CameraMode.FIRST_PERSON_CAB) HydraulicGreen else JcbYellow
                    )
                }
                IconButton(
                    onClick = onSaveGame,
                    modifier = Modifier.size(44.dp).testTag("workshop_save_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = AppStrings.saveProgress.get(lang), tint = HydraulicGreen)
                }
            }

            // ==================== 2. 3D PREVIEW WINDOW ====================
            val previewBorderColor = when {
                state.physics.isSnapping || state.activeManip?.isAligned == true -> HydraulicGreen
                state.activeManip != null -> JcbYellow
                else -> BlueprintCyan.copy(alpha = 0.7f)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .tooltipAnchor(TooltipTargetId.VIEWPORT_3D, tooltipRegistry)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, previewBorderColor, RoundedCornerShape(14.dp))
                    .background(SteelSurfaceElevated)
                    .testTag("main_assembly_3d_preview")
            ) {
                Jcb3DViewport(
                    state = state,
                    renderer = renderer,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left 3D Preview Badge & Camera Zoom Strip
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(SteelSurface.copy(alpha = 0.86f), RoundedCornerShape(10.dp))
                        .border(1.dp, SteelBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = BiText("3D PREVIEW", "3D प्रीव्यू").get(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueprintCyan,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onZoomCamera(-0.8f) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomIn, contentDescription = BiText("Zoom In", "ज़ूम इन").get(lang), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onZoomCamera(0.8f) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomOut, contentDescription = BiText("Zoom Out", "ज़ूम आउट").get(lang), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onResetCamera, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = AppStrings.resetCam.get(lang), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Top-center Feedback Toast Banner
                state.feedbackBanner?.let { (msg, isError) ->
                    Surface(
                        color = if (isError) HazardRed.copy(alpha = 0.94f) else HydraulicGreen.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = msg.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Bottom-left Interactive 10-Tool Belt inside 3D Viewport
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 8.dp, end = if (state.activeManip != null) 256.dp else 8.dp)
                        .tooltipAnchor(TooltipTargetId.TOOL_BELT, tooltipRegistry)
                        .background(SteelSurface.copy(alpha = 0.88f), RoundedCornerShape(10.dp))
                        .border(1.dp, SteelBorder, RoundedCornerShape(10.dp))
                        .padding(5.dp)
                ) {
                    items(ToolType.entries) { tool ->
                        val sel = state.selectedTool == tool
                        Surface(
                            color = if (sel) JcbYellow else SteelSurfaceElevated,
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier
                                .clickable { onSelectTool(tool) }
                                .border(1.dp, if (sel) JcbYellow else SteelBorder, RoundedCornerShape(7.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("tool_btn_${tool.id}")
                        ) {
                            Text(
                                text = tool.title.get(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sel) TextOnYellow else Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Right-side Active Part 3D Alignment & Physics Controls
                val manip = state.activeManip
                if (manip != null) {
                    ActivePartInspectorCard(
                        state = state,
                        manip = manip,
                        tooltipRegistry = tooltipRegistry,
                        onMovePart = onMovePart,
                        onRotatePart = onRotatePart,
                        onDropWithGravity = onDropWithGravity,
                        onTogglePhysicsHold = onTogglePhysicsHold,
                        onToggleSnapAssist = onToggleSnapAssist,
                        onLockPlacement = onLockPlacement,
                        onFastenBolt = onFastenBolt,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                            .width(242.dp)
                            .fillMaxHeight()
                    )
                }
            }

            // ==================== 3. SCROLLABLE HORIZONTAL INVENTORY OF PARTS ====================
            HorizontalPartsInventoryTray(
                state = state,
                tooltipRegistry = tooltipRegistry,
                onToggleInventory = onToggleInventory,
                onSelectLevel = onSelectLevel,
                onSelectPart = onSelectPart,
                onCommissioningStep = onCommissioningStep
            )
        }

        // ==================== 4. BILINGUAL CONTEXTUAL TOOLTIP OVERLAY ====================
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

        // Pause Overlay
        if (state.isPaused) {
            WorkshopPauseOverlay(
                state = state,
                onTogglePause = onTogglePause,
                onSaveGame = onSaveGame,
                onRestartLevel = onRestartLevel,
                onBackToMenu = onBackToMenu
            )
        }
    }
}

@Composable
private fun HorizontalPartsInventoryTray(
    state: GameUiState,
    tooltipRegistry: TooltipAnchorRegistry,
    onToggleInventory: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    onSelectPart: (String) -> Unit,
    onCommissioningStep: (String) -> Unit
) {
    val lang = state.language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .tooltipAnchor(TooltipTargetId.PARTS_INVENTORY, tooltipRegistry)
            .background(SteelSurface.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
            .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("horizontal_parts_inventory")
    ) {
        // Level tabs + Inventory Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val levelParts = JcbCatalog.partsForLevel(state.currentLevel)
            val doneCount = if (state.currentLevel == 10) state.commissioning.completedCount() else levelParts.count { it.id in state.installedParts }
            val totalCount = if (state.currentLevel == 10) 13 else levelParts.size

            Text(
                text = "${AppStrings.partsInventory.get(lang)} ($doneCount/$totalCount)",
                style = MaterialTheme.typography.labelLarge,
                color = BlueprintCyan,
                maxLines = 1
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(JcbCatalog.levels) { lvl ->
                    val isSelected = lvl.levelNumber == state.currentLevel
                    val lvlParts = JcbCatalog.partsForLevel(lvl.levelNumber)
                    val lvlDone = if (lvl.levelNumber == 10) state.commissioning.allCompleted()
                    else lvlParts.isNotEmpty() && lvlParts.all { it.id in state.installedParts }
                    Surface(
                        color = when {
                            isSelected -> JcbYellow
                            lvlDone -> HydraulicGreen.copy(alpha = 0.25f)
                            else -> SteelSurfaceElevated
                        },
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier
                            .clickable { onSelectLevel(lvl.levelNumber) }
                            .border(1.dp, if (lvlDone) HydraulicGreen else SteelBorder, RoundedCornerShape(7.dp))
                            .testTag("level_tab_${lvl.levelNumber}")
                    ) {
                        Text(
                            text = "L${lvl.levelNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextOnYellow else if (lvlDone) HydraulicGreen else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleInventory,
                modifier = Modifier.size(32.dp).testTag("workshop_drawer_btn")
            ) {
                Icon(
                    imageVector = if (state.showInventoryDrawer) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = AppStrings.partsInventory.get(lang),
                    tint = JcbYellow
                )
            }
        }

        // Scrollable Horizontal Cards Carousel
        AnimatedVisibility(visible = state.showInventoryDrawer) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(4.dp))
                if (state.currentLevel < 10) {
                    val levelParts = JcbCatalog.partsForLevel(state.currentLevel)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("horizontal_parts_row")
                    ) {
                        items(levelParts, key = { it.id }) { part ->
                            val isInstalled = part.id in state.installedParts
                            val isActive = state.activeManip?.partId == part.id
                            val isLocked = part.prerequisites.any { it !in state.installedParts }

                            Surface(
                                color = when {
                                    isActive -> JcbYellow.copy(alpha = 0.22f)
                                    isInstalled -> HydraulicGreen.copy(alpha = 0.14f)
                                    else -> SteelSurfaceElevated
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .width(184.dp)
                                    .height(68.dp)
                                    .border(
                                        width = if (isActive) 1.5.dp else 1.dp,
                                        color = when {
                                            isActive -> JcbYellow
                                            isInstalled -> HydraulicGreen
                                            else -> SteelBorder
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectPart(part.id) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("part_item_${part.id}")
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when {
                                                isInstalled -> Icons.Default.CheckCircle
                                                isLocked -> Icons.Default.Lock
                                                else -> Icons.Default.Build
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isInstalled -> HydraulicGreen
                                                isLocked -> TextSecondary
                                                else -> JcbYellow
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = part.name.get(lang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isLocked) TextSecondary else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${part.requiredTool.title.get(lang)} • ${part.boltsCount}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) JcbYellow else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    HorizontalLevel10Checklist(
                        state = state,
                        onStep = onCommissioningStep,
                        modifier = Modifier.tooltipAnchor(TooltipTargetId.LEVEL_10_LIST, tooltipRegistry)
                    )
                }
            }
        }
    }
}

package com.example.ui

import androidx.compose.runtime.Composable
import com.example.engine3d.GlRenderer
import com.example.model.GameUiState
import com.example.model.ToolType

@Composable
fun WorkshopScreen(
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
    MainAssemblyView(
        state = state,
        renderer = renderer,
        onTogglePause = onTogglePause,
        onBackToMenu = onBackToMenu,
        onNavigateDriving = onNavigateDriving,
        onNavigateMicroAssembly = onNavigateMicroAssembly,
        onToggleLanguage = onToggleLanguage,
        onToggleCamera = onToggleCamera,
        onResetCamera = onResetCamera,
        onZoomCamera = onZoomCamera,
        onToggleInventory = onToggleInventory,
        onSelectLevel = onSelectLevel,
        onSelectPart = onSelectPart,
        onMovePart = onMovePart,
        onRotatePart = onRotatePart,
        onLockPlacement = onLockPlacement,
        onDropWithGravity = onDropWithGravity,
        onTogglePhysicsHold = onTogglePhysicsHold,
        onToggleSnapAssist = onToggleSnapAssist,
        onSelectTool = onSelectTool,
        onFastenBolt = onFastenBolt,
        onCommissioningStep = onCommissioningStep,
        onSaveGame = onSaveGame,
        onRestartLevel = onRestartLevel,
        onOpenTooltipSpotlight = onOpenTooltipSpotlight,
        onDismissTooltipSpotlight = onDismissTooltipSpotlight,
        onNextTooltipStep = onNextTooltipStep,
        onPrevTooltipStep = onPrevTooltipStep,
        onToggleAutoTooltips = onToggleAutoTooltips
    )
}

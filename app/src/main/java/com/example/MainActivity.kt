package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ControlLayout
import com.example.model.ScreenRoute
import com.example.ui.DrivingScreen
import com.example.ui.HomeScreen
import com.example.ui.MicroAssemblyScreen
import com.example.ui.MissionsScreen
import com.example.ui.ServiceManualScreen
import com.example.ui.SettingsScreen
import com.example.ui.SpecGuideScreen
import com.example.ui.WorkshopScreen
import com.example.ui.theme.JcbBuildMasterTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private var gameViewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GameViewModel = viewModel()
            gameViewModel = vm
            val state by vm.uiState.collectAsStateWithLifecycle()
            var manualReturnRoute by remember { mutableStateOf(ScreenRoute.HOME) }

            // Portrait Menu/Manual & Landscape 3D Gameplay
            LaunchedEffect(state.route) {
                requestedOrientation = when (state.route) {
                    ScreenRoute.WORKSHOP, ScreenRoute.DRIVING, ScreenRoute.MICRO_ASSEMBLY ->
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    else ->
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }

            JcbBuildMasterTheme {
                when (state.route) {
                    ScreenRoute.HOME -> HomeScreen(
                        state = state,
                        onNavigate = { target ->
                            if (target == ScreenRoute.SERVICE_MANUAL) {
                                manualReturnRoute = ScreenRoute.HOME
                            }
                            vm.navigate(target)
                        },
                        onToggleLanguage = vm::toggleLanguage
                    )
                    ScreenRoute.MISSIONS -> MissionsScreen(
                        state = state,
                        onBack = { vm.navigate(ScreenRoute.HOME) },
                        onSelectMission = { id, route ->
                            vm.selectActiveMission(id)
                            vm.navigate(route)
                        }
                    )
                    ScreenRoute.SPEC_GUIDE -> SpecGuideScreen(
                        state = state,
                        onBack = { vm.navigate(ScreenRoute.HOME) }
                    )
                    ScreenRoute.SERVICE_MANUAL -> ServiceManualScreen(
                        state = state,
                        onBack = { vm.navigate(manualReturnRoute) },
                        onSelectVariant = vm::selectEngineVariant,
                        onSaveCustomSpec = vm::saveCustomManualSpec
                    )
                    ScreenRoute.SETTINGS -> SettingsScreen(
                        state = state,
                        onBack = { vm.navigate(ScreenRoute.HOME) },
                        onToggleLanguage = vm::toggleLanguage,
                        onSetSound = vm::setSoundEnabled,
                        onSetGraphics = vm::setGraphicsQuality,
                        onSetControlLayout = vm::setControlLayout,
                        onSaveManual = vm::saveGameManual,
                        onRestartLevel = vm::restartLevel,
                        onToggleSandboxDriving = vm::toggleSandboxDrivingUnlock,
                        onSetAutoTooltips = vm::setAutoContextualTips
                    )
                    ScreenRoute.WORKSHOP -> WorkshopScreen(
                        state = state,
                        renderer = vm.renderer,
                        onTogglePause = vm::togglePause,
                        onBackToMenu = { vm.navigate(ScreenRoute.HOME) },
                        onNavigateDriving = { vm.navigate(ScreenRoute.DRIVING) },
                        onNavigateMicroAssembly = { vm.navigate(ScreenRoute.MICRO_ASSEMBLY) },
                        onToggleLanguage = vm::toggleLanguage,
                        onToggleCamera = vm::toggleCameraMode,
                        onResetCamera = vm::resetCamera,
                        onZoomCamera = vm::zoomCamera,
                        onToggleInventory = vm::toggleInventoryDrawer,
                        onSelectLevel = vm::selectLevel,
                        onSelectPart = vm::selectPartForAssembly,
                        onMovePart = vm::moveActivePart,
                        onRotatePart = vm::rotateActivePart,
                        onLockPlacement = vm::attemptLockPlacement,
                        onDropWithGravity = vm::dropActivePartWithGravity,
                        onTogglePhysicsHold = vm::togglePhysicsHold,
                        onToggleSnapAssist = vm::togglePhysicsSnapAssist,
                        onSelectTool = vm::selectTool,
                        onFastenBolt = vm::fastenBoltWithSelectedTool,
                        onCommissioningStep = vm::performCommissioningStep,
                        onSaveGame = vm::saveGameManual,
                        onRestartLevel = vm::restartLevel,
                        onOpenTooltipSpotlight = { vm.openTooltipSpotlight(false) },
                        onDismissTooltipSpotlight = vm::dismissTooltipSpotlight,
                        onNextTooltipStep = vm::nextTooltipStep,
                        onPrevTooltipStep = vm::prevTooltipStep,
                        onToggleAutoTooltips = vm::setAutoContextualTips
                    )
                    ScreenRoute.MICRO_ASSEMBLY -> MicroAssemblyScreen(
                        state = state,
                        renderer = vm.renderer,
                        onBack = { vm.navigate(ScreenRoute.HOME) },
                        onOpenManual = {
                            manualReturnRoute = ScreenRoute.MICRO_ASSEMBLY
                            vm.navigate(ScreenRoute.SERVICE_MANUAL)
                        },
                        onToggleLanguage = vm::toggleLanguage,
                        onZoomCamera = vm::zoomCamera,
                        onResetCamera = vm::resetCamera,
                        onToggleExploded = vm::toggleExplodedMode,
                        onExplodeFactor = vm::setExplodeFactor,
                        onToggleTransparent = vm::toggleTransparentBlock,
                        onToggleInspect = vm::toggleInspectionMode,
                        onSelectCategory = vm::selectMicroCategory,
                        onSelectPart = vm::selectMicroPart,
                        onMovePart = vm::moveMicroPart,
                        onRotatePart = vm::rotateMicroPart,
                        onDropWithGravity = vm::dropActivePartWithGravity,
                        onTogglePhysicsHold = vm::togglePhysicsHold,
                        onToggleSnapAssist = vm::togglePhysicsSnapAssist,
                        onAnimateStep = vm::animateMicroPartStep,
                        onSetRingStagger = vm::setMicroRingStagger,
                        onSetValveSpring = vm::setValveSpringCompression,
                        onSetTorqueAndAngle = vm::setMicroTorqueAndAngle,
                        onInstallPart = vm::installActiveMicroPart,
                        onSelectTool = vm::selectTool,
                        onCheckBearingClearance = vm::runBearingClearanceCheck,
                        onCheckCrankRotation = vm::runCrankshaftRotationCheck,
                        onOpenTooltipSpotlight = { vm.openTooltipSpotlight(false) },
                        onDismissTooltipSpotlight = vm::dismissTooltipSpotlight,
                        onNextTooltipStep = vm::nextTooltipStep,
                        onPrevTooltipStep = vm::prevTooltipStep,
                        onToggleAutoTooltips = vm::setAutoContextualTips
                    )
                    ScreenRoute.DRIVING -> DrivingScreen(
                        state = state,
                        renderer = vm.renderer,
                        onBackToMenu = { vm.navigate(ScreenRoute.HOME) },
                        onTogglePause = vm::togglePause,
                        onToggleLanguage = vm::toggleLanguage,
                        onToggleCamera = vm::toggleCameraMode,
                        onResetCamera = vm::resetCamera,
                        onZoomCamera = vm::zoomCamera,
                        onToggleEngine = vm::toggleDrivingEngine,
                        onSetGear = vm::setGear,
                        onToggle4Wd = vm::toggle4Wd,
                        onToggle4Ws = vm::toggle4Ws,
                        onSetInputs = vm::setDrivingInputs,
                        onAdjustLoaderBoom = vm::adjustLoaderBoom,
                        onAdjustLoaderBucket = vm::adjustLoaderBucket,
                        onAdjustRearSwing = vm::adjustRearSwing,
                        onAdjustRearBoom = vm::adjustRearBoom,
                        onAdjustRearDipper = vm::adjustRearDipper,
                        onAdjustRearBucket = vm::adjustRearBucket,
                        onAdjustStabilizers = vm::adjustStabilizers,
                        onToggleControlLayout = {
                            val next = if (state.controlLayout == ControlLayout.JOYSTICK) {
                                ControlLayout.SIX_LEVER
                            } else {
                                ControlLayout.JOYSTICK
                            }
                            vm.setControlLayout(next)
                        },
                        onFinalInspection = vm::runDrivingFinalInspection
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel?.soundEngine?.stopEngineLoop()
    }
}

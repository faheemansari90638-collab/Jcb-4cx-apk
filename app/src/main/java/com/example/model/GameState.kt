package com.example.model

import com.example.engine3d.CollisionContact
import com.example.engine3d.Vec3

enum class ScreenRoute {
    HOME,
    WORKSHOP,
    MICRO_ASSEMBLY,
    SERVICE_MANUAL,
    DRIVING,
    MISSIONS,
    SPEC_GUIDE,
    SETTINGS
}

enum class ControlLayout(val label: BiText) {
    JOYSTICK(BiText("ISO/SAE Joysticks", "जॉयस्टिक कंट्रोल")),
    SIX_LEVER(BiText("Classic 6-Lever", "क्लासिक 6-लीवर"))
}

enum class GraphicsQuality(val label: BiText, val circleSegments: Int) {
    LOW(BiText("Low (Fast)", "लो (तेज़)"), 10),
    MEDIUM(BiText("Medium (Balanced)", "मीडियम (संतुलित)"), 16),
    HIGH(BiText("High (Detailed)", "हाई (उच्च गुणवत्ता)"), 24)
}

enum class CameraMode(val label: BiText) {
    THIRD_PERSON_ORBIT(BiText("3rd Person Orbit", "तीसरा व्यक्ति")),
    FIRST_PERSON_CAB(BiText("1st Person Cab", "केबिन व्यू"))
}

enum class GearMode(val label: String, val speedFactor: Float) {
    F1("F1", 3.2f),
    F2("F2", 6.5f),
    F3("F3", 11.5f),
    N("N", 0f),
    R("R", -4.2f)
}

data class ActivePartManipState(
    val partId: String,
    val position: Vec3,
    val rotationDeg: Vec3,
    val isAligned: Boolean = false,
    val placedInSocket: Boolean = false,
    val boltsSecured: Int = 0,
    val totalBolts: Int = 2,
    val toolAnimProgress: Float = 0f,
    val animatingTool: ToolType? = null
)

data class MicroManipState(
    val partId: String,
    val position: Vec3,
    val rotationDeg: Vec3,
    val isAligned: Boolean = false,
    val currentDepthMm: Float = 0f,
    val ringGapStaggerDeg: Int = 0, // 0, 120, 240
    val torqueInputNm: Int = 0,
    val angleInputDeg: Int = 0,
    val animatingPathProgress: Float = 0f
)

data class MicroAssemblyState(
    val selectedVariant: EngineVariant = EngineVariant.JCB_444_DIESELMAX,
    val selectedCategory: MicroCategory = MicroCategory.BOTTOM_END,
    val installedMicroParts: Set<String> = emptySet(),
    val activeMicro: MicroManipState? = null,
    val explodedMode: Boolean = false,
    val explodeFactor: Float = 0f, // 0..1 animated or slider
    val transparentBlockMode: Boolean = true,
    val inspectionMode: Boolean = false,
    val valveSpringCompressed: Float = 0f, // 0..1
    val crankSpinDeg: Float = 0f, // 0..720
    val crankSpinVerified: Boolean = false,
    val bearingClearanceVerified: Boolean = false,
    val customManualSpecs: Map<String, String> = emptyMap()
)

data class CommissioningState(
    val engineOilFilled: Boolean = false,
    val hydraulicOilFilled: Boolean = false,
    val coolantAdded: Boolean = false,
    val dieselAdded: Boolean = false,
    val batteryChecked: Boolean = false,
    val engineStarted: Boolean = false,
    val warningLightsChecked: Boolean = false,
    val steeringTested: Boolean = false,
    val gearsTested: Boolean = false,
    val loaderTested: Boolean = false,
    val excavatorTested: Boolean = false,
    val stabilizersTested: Boolean = false,
    val finalInspectionDone: Boolean = false
) {
    fun completedCount(): Int = listOf(
        engineOilFilled, hydraulicOilFilled, coolantAdded, dieselAdded,
        batteryChecked, engineStarted, warningLightsChecked, steeringTested,
        gearsTested, loaderTested, excavatorTested, stabilizersTested, finalInspectionDone
    ).count { it }

    fun allCompleted(): Boolean = completedCount() == 13
}

data class DrivingSimState(
    val engineRunning: Boolean = false,
    val rpm: Float = 0f,
    val posX: Float = -6.0f,
    val posZ: Float = 2.0f,
    val headingDeg: Float = 90f,
    val speedMps: Float = 0f,
    val gear: GearMode = GearMode.N,
    val is4Wd: Boolean = true,
    val is4Ws: Boolean = true,
    val steerInput: Float = 0f,
    val wheelSpinDeg: Float = 0f,
    val throttleInput: Float = 0f,
    val brakeInput: Float = 0f,
    val loaderBoomDeg: Float = 8f,
    val loaderBucketDeg: Float = -10f,
    val rearSwingDeg: Float = 0f,
    val rearBoomDeg: Float = 38f,
    val rearDipperDeg: Float = -55f,
    val rearBucketDeg: Float = -35f,
    val stabilizerExt: Float = 0f,
    val frontBucketSoil: Float = 0f,
    val rearBucketSoil: Float = 0f,
    val soilMovedCount: Int = 0,
    val trenchProgress: Float = 0f,
    val truckLoadProgress: Float = 0f,
    val isInParkingBay: Boolean = false,
    val parkHoldProgress: Float = 0f,
    val finalInspectionDone: Boolean = false,
    val coolantTempC: Float = 78f,
    val oilPressureBar: Float = 0f,
    val fuelLevel: Float = 0.92f
)

data class PhysicsHudState(
    val gravityEnabled: Boolean = true,
    val snapAssistEnabled: Boolean = true,
    val isPartHeld: Boolean = true,
    val isSnapping: Boolean = false,
    val isGrounded: Boolean = false,
    val velocityMps: Float = 0f,
    val massKg: Float = 0f,
    val lastCollision: CollisionContact? = null,
    val collisionHighlightUntilMs: Long = 0L
)

data class TooltipGuideState(
    val spotlightOpen: Boolean = false,
    val autoContextualEnabled: Boolean = true,
    val manualTourIndex: Int? = null,
    val workshopIntroShown: Boolean = false,
    val microIntroShown: Boolean = false
)

data class GameUiState(
    val route: ScreenRoute = ScreenRoute.HOME,
    val language: AppLanguage = AppLanguage.EN,
    val soundEnabled: Boolean = true,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.MEDIUM,
    val controlLayout: ControlLayout = ControlLayout.JOYSTICK,
    val cameraMode: CameraMode = CameraMode.THIRD_PERSON_ORBIT,
    val currentLevel: Int = 1,
    val installedParts: Set<String> = emptySet(),
    val activeManip: ActivePartManipState? = null,
    val selectedTool: ToolType = ToolType.ENGINE_CRANE,
    val commissioning: CommissioningState = CommissioningState(),
    val micro: MicroAssemblyState = MicroAssemblyState(),
    val physics: PhysicsHudState = PhysicsHudState(),
    val tooltipGuide: TooltipGuideState = TooltipGuideState(),
    val completedMissions: Set<MissionId> = emptySet(),
    val activeMission: MissionId = MissionId.BUILD_JCB,
    val xp: Int = 0,
    val unlockedRewards: Set<String> = emptySet(),
    val driving: DrivingSimState = DrivingSimState(),
    val isPaused: Boolean = false,
    val feedbackBanner: Pair<BiText, Boolean>? = null,
    val showInventoryDrawer: Boolean = true,
    val showDiagnosticPopup: Boolean = false,
    val sandboxDrivingUnlocked: Boolean = false
) {
    val totalAssemblyParts: Int get() = JcbCatalog.totalBuildParts
    val assemblyPercent: Int
        get() {
            val partsPct = (installedParts.size.toFloat() / totalAssemblyParts.coerceAtLeast(1)) * 88f
            val testPct = (commissioning.completedCount().toFloat() / 13f) * 12f
            return (partsPct + testPct).toInt().coerceIn(0, 100)
        }
    val allPartsBuilt: Boolean get() = installedParts.size >= totalAssemblyParts
    val drivingUnlocked: Boolean get() = (allPartsBuilt && commissioning.allCompleted()) || sandboxDrivingUnlocked
}

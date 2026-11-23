package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.GamePersistence
import com.example.engine3d.GlRenderer
import com.example.engine3d.Mat4
import com.example.engine3d.MechanicalPhysicsEngine
import com.example.engine3d.RigidBodyState
import com.example.engine3d.Vec3
import com.example.model.ActivePartManipState
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.CameraMode
import com.example.model.ControlLayout
import com.example.model.GameUiState
import com.example.model.GearMode
import com.example.model.GraphicsQuality
import com.example.model.JcbCatalog
import com.example.model.MissionId
import com.example.model.ScreenRoute
import com.example.model.ToolType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val persistence = GamePersistence(application)
    val soundEngine = SoundEngine()
    val renderer = GlRenderer()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var drivingLoopJob: Job? = null
    private var physicsLoopJob: Job? = null
    private var toolAnimJob: Job? = null
    private var feedbackClearJob: Job? = null
    private var activeBody: RigidBodyState? = null
    private var lastCollisionSoundMs: Long = 0L

    init {
        viewModelScope.launch {
            val loaded = persistence.loadInto(GameUiState())
            soundEngine.setEnabled(loaded.soundEnabled)
            _uiState.value = syncMissionProgression(loaded)
            renderer.gameState = _uiState.value
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopEngineLoop()
        drivingLoopJob?.cancel()
        physicsLoopJob?.cancel()
    }

    fun navigate(route: ScreenRoute) {
        if (route == ScreenRoute.DRIVING && !_uiState.value.drivingUnlocked) {
            showFeedback(AppStrings.unlockHint, isError = true)
            soundEngine.playErrorBuzz()
            return
        }
        _uiState.update { st ->
            val guide = st.tooltipGuide
            val updatedGuide = when (route) {
                ScreenRoute.WORKSHOP -> {
                    val triggerIntro = guide.autoContextualEnabled && !guide.workshopIntroShown && st.installedParts.isEmpty()
                    if (triggerIntro) {
                        guide.copy(spotlightOpen = true, manualTourIndex = 0, workshopIntroShown = true)
                    } else {
                        guide.copy(spotlightOpen = false, manualTourIndex = null)
                    }
                }
                ScreenRoute.MICRO_ASSEMBLY -> {
                    val triggerIntro = guide.autoContextualEnabled && !guide.microIntroShown && st.micro.installedMicroParts.isEmpty()
                    if (triggerIntro) {
                        guide.copy(spotlightOpen = true, manualTourIndex = 0, microIntroShown = true)
                    } else {
                        guide.copy(spotlightOpen = false, manualTourIndex = null)
                    }
                }
                else -> guide.copy(spotlightOpen = false, manualTourIndex = null)
            }
            st.copy(route = route, isPaused = false, tooltipGuide = updatedGuide)
        }
        when (route) {
            ScreenRoute.DRIVING -> {
                stopPhysicsLoop()
                renderer.orbitDistance = 7.4f
                renderer.orbitPitchDeg = 24f
                startDrivingLoop()
            }
            ScreenRoute.MICRO_ASSEMBLY -> {
                stopDrivingLoop()
                renderer.focusTarget = Vec3(0f, 0.55f, 0f)
                renderer.orbitDistance = 1.45f
                renderer.orbitPitchDeg = 22f
                startPhysicsLoop()
            }
            ScreenRoute.WORKSHOP -> {
                stopDrivingLoop()
                val lvl = JcbCatalog.levels.find { it.levelNumber == _uiState.value.currentLevel }
                if (lvl != null) renderer.focusTarget = lvl.focusTarget
                renderer.orbitDistance = 5.0f
                startPhysicsLoop()
            }
            else -> {
                stopDrivingLoop()
                stopPhysicsLoop()
            }
        }
        syncRenderer()
        if (route == ScreenRoute.WORKSHOP || route == ScreenRoute.MICRO_ASSEMBLY) autoSave()
    }

    // ==================== CONTEXTUAL TOOLTIP GUIDE ====================
    fun openTooltipSpotlight(startFromTour: Boolean = false) {
        _uiState.update { st ->
            val maxTour = if (st.route == ScreenRoute.MICRO_ASSEMBLY) {
                com.example.model.AssemblyTooltipCatalog.MICRO_TOUR_COUNT
            } else {
                com.example.model.AssemblyTooltipCatalog.WORKSHOP_TOUR_COUNT
            }
            val nextIdx = if (startFromTour) 0 else st.tooltipGuide.manualTourIndex?.coerceIn(0, maxTour - 1)
            st.copy(
                tooltipGuide = st.tooltipGuide.copy(
                    spotlightOpen = true,
                    manualTourIndex = nextIdx
                )
            )
        }
    }

    fun dismissTooltipSpotlight() {
        _uiState.update { st ->
            st.copy(
                tooltipGuide = st.tooltipGuide.copy(
                    spotlightOpen = false,
                    manualTourIndex = null
                )
            )
        }
    }

    fun nextTooltipStep() {
        _uiState.update { st ->
            val maxCount = if (st.route == ScreenRoute.MICRO_ASSEMBLY) {
                com.example.model.AssemblyTooltipCatalog.MICRO_TOUR_COUNT
            } else {
                com.example.model.AssemblyTooltipCatalog.WORKSHOP_TOUR_COUNT
            }
            val cur = st.tooltipGuide.manualTourIndex ?: run {
                val resolved = if (st.route == ScreenRoute.MICRO_ASSEMBLY) {
                    com.example.model.AssemblyTooltipCatalog.resolveMicroTooltip(st, null)
                } else {
                    com.example.model.AssemblyTooltipCatalog.resolveWorkshopTooltip(st, null)
                }
                (resolved.stepIndex - 1).coerceIn(0, maxCount - 1)
            }
            if (cur + 1 >= maxCount) {
                st.copy(tooltipGuide = st.tooltipGuide.copy(spotlightOpen = false, manualTourIndex = null))
            } else {
                st.copy(tooltipGuide = st.tooltipGuide.copy(spotlightOpen = true, manualTourIndex = cur + 1))
            }
        }
    }

    fun prevTooltipStep() {
        _uiState.update { st ->
            val maxCount = if (st.route == ScreenRoute.MICRO_ASSEMBLY) {
                com.example.model.AssemblyTooltipCatalog.MICRO_TOUR_COUNT
            } else {
                com.example.model.AssemblyTooltipCatalog.WORKSHOP_TOUR_COUNT
            }
            val cur = st.tooltipGuide.manualTourIndex ?: run {
                val resolved = if (st.route == ScreenRoute.MICRO_ASSEMBLY) {
                    com.example.model.AssemblyTooltipCatalog.resolveMicroTooltip(st, null)
                } else {
                    com.example.model.AssemblyTooltipCatalog.resolveWorkshopTooltip(st, null)
                }
                (resolved.stepIndex - 1).coerceIn(0, maxCount - 1)
            }
            val prev = (cur - 1).coerceAtLeast(0)
            st.copy(tooltipGuide = st.tooltipGuide.copy(spotlightOpen = true, manualTourIndex = prev))
        }
    }

    fun setAutoContextualTips(enabled: Boolean) {
        _uiState.update { st ->
            st.copy(
                tooltipGuide = st.tooltipGuide.copy(
                    autoContextualEnabled = enabled,
                    spotlightOpen = if (!enabled) false else st.tooltipGuide.spotlightOpen,
                    manualTourIndex = if (!enabled) null else st.tooltipGuide.manualTourIndex
                )
            )
        }
        autoSave()
    }

    fun toggleLanguage() {
        _uiState.update {
            val next = if (it.language == AppLanguage.EN) AppLanguage.HI else AppLanguage.EN
            it.copy(language = next)
        }
        autoSave()
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEngine.setEnabled(enabled)
        _uiState.update { it.copy(soundEnabled = enabled) }
        autoSave()
    }

    fun setGraphicsQuality(quality: GraphicsQuality) {
        _uiState.update { it.copy(graphicsQuality = quality) }
        autoSave()
    }

    fun setControlLayout(layout: ControlLayout) {
        _uiState.update { it.copy(controlLayout = layout) }
        autoSave()
    }

    fun toggleCameraMode() {
        _uiState.update {
            val next = if (it.cameraMode == CameraMode.THIRD_PERSON_ORBIT) {
                CameraMode.FIRST_PERSON_CAB
            } else {
                CameraMode.THIRD_PERSON_ORBIT
            }
            it.copy(cameraMode = next)
        }
        syncRenderer()
    }

    fun resetCamera() {
        val st = _uiState.value
        when (st.route) {
            ScreenRoute.DRIVING -> {
                renderer.orbitYawDeg = st.driving.headingDeg + 180f
                renderer.orbitPitchDeg = 22f
                renderer.orbitDistance = 7.2f
            }
            ScreenRoute.MICRO_ASSEMBLY -> {
                val activeId = st.micro.activeMicro?.partId
                val spec = activeId?.let { com.example.model.MicroPartCatalog.findPart(it) }
                renderer.focusTarget = spec?.targetPos ?: Vec3(0f, 0.55f, 0f)
                renderer.orbitYawDeg = 35f
                renderer.orbitPitchDeg = 22f
                renderer.orbitDistance = if (spec != null) 0.70f else 1.45f
            }
            else -> {
                val lvl = JcbCatalog.levels.find { it.levelNumber == st.currentLevel }
                renderer.focusTarget = lvl?.focusTarget ?: Vec3(0f, 1.15f, 0f)
                renderer.orbitYawDeg = 38f
                renderer.orbitPitchDeg = 24f
                renderer.orbitDistance = 4.8f
            }
        }
    }

    fun zoomCamera(delta: Float) {
        val minZoom = if (_uiState.value.route == ScreenRoute.MICRO_ASSEMBLY) 0.22f else 2.0f
        renderer.orbitDistance = (renderer.orbitDistance + delta).coerceIn(minZoom, 15.0f)
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun toggleInventoryDrawer() {
        _uiState.update { it.copy(showInventoryDrawer = !it.showInventoryDrawer) }
    }

    fun saveGameManual() {
        viewModelScope.launch {
            persistence.save(_uiState.value)
            soundEngine.playSuccessChime()
            showFeedback(AppStrings.savedToast, isError = false)
        }
    }

    fun selectLevel(levelNumber: Int) {
        val maxUnlocked = highestUnlockedLevel(_uiState.value)
        if (levelNumber > maxUnlocked) {
            showFeedback(
                BiText(
                    "Complete Level ${levelNumber - 1} parts first!",
                    "पहले लेवल ${levelNumber - 1} के पुर्जे पूरे करें!"
                ),
                isError = true
            )
            soundEngine.playErrorBuzz()
            return
        }
        val lvl = JcbCatalog.levels.find { it.levelNumber == levelNumber }
        if (lvl != null) renderer.focusTarget = lvl.focusTarget
        _uiState.update { it.copy(currentLevel = levelNumber, activeManip = null) }
        syncRenderer()
    }

    fun restartLevel(levelNumber: Int) {
        val partsToRemove = JcbCatalog.parts.filter { it.level >= levelNumber }.map { it.id }.toSet()
        _uiState.update { st ->
            val updatedComm = if (levelNumber <= 10) com.example.model.CommissioningState() else st.commissioning
            st.copy(
                currentLevel = levelNumber,
                installedParts = st.installedParts - partsToRemove,
                activeManip = null,
                commissioning = updatedComm
            )
        }
        autoSave()
        syncRenderer()
        showFeedback(
            BiText("Level $levelNumber reset!", "लेवल $levelNumber रीसेट हो गया!"),
            isError = false
        )
    }

    fun selectPartForAssembly(partId: String) {
        val st = _uiState.value
        val spec = JcbCatalog.findPart(partId) ?: return
        if (partId in st.installedParts) {
            showFeedback(BiText("Already installed!", "पहले से इंस्टॉल है!"), isError = false)
            return
        }
        val missingPrereq = spec.prerequisites.firstOrNull { it !in st.installedParts }
        if (missingPrereq != null) {
            val prereqName = JcbCatalog.findPart(missingPrereq)?.name
            showFeedback(
                BiText(
                    "Missing: Install ${prereqName?.en ?: missingPrereq} first!",
                    "कमी: पहले ${prereqName?.hi ?: missingPrereq} लगाएं!"
                ),
                isError = true
            )
            soundEngine.playErrorBuzz()
            return
        }

        val startPos = spec.targetPos + spec.spawnOffset
        val physCfg = MechanicalPhysicsEngine.configForJcbPart(spec)
        activeBody = RigidBodyState(
            position = startPos,
            rotationDeg = spec.spawnRot,
            isHeld = true
        )
        val manip = ActivePartManipState(
            partId = partId,
            position = startPos,
            rotationDeg = spec.spawnRot,
            isAligned = isWithinTolerance(startPos, spec.spawnRot, spec.targetPos, spec.targetRot),
            placedInSocket = false,
            boltsSecured = 0,
            totalBolts = spec.boltsCount
        )
        renderer.focusTarget = spec.targetPos
        _uiState.update {
            it.copy(
                currentLevel = spec.level,
                activeManip = manip,
                selectedTool = spec.requiredTool,
                tooltipGuide = it.tooltipGuide.copy(manualTourIndex = null),
                physics = it.physics.copy(
                    isPartHeld = true,
                    isSnapping = false,
                    isGrounded = false,
                    velocityMps = 0f,
                    massKg = physCfg.massKg,
                    lastCollision = null
                )
            )
        }
        syncRenderer()
    }

    fun moveActivePart(dx: Float, dy: Float, dz: Float) {
        val st = _uiState.value
        val manip = st.activeManip ?: return
        if (manip.placedInSocket || manip.animatingTool != null) return
        val spec = JcbCatalog.findPart(manip.partId) ?: return

        val physCfg = MechanicalPhysicsEngine.configForJcbPart(spec)
        val obstacles = MechanicalPhysicsEngine.buildJcbObstacles(st.installedParts, spec, physCfg)
        val baseBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = manip.rotationDeg))
            .copy(position = manip.position, rotationDeg = manip.rotationDeg, isHeld = true)

        val resolved = MechanicalPhysicsEngine.applyManualNudge(
            body = baseBody,
            delta = Vec3(dx, dy, dz),
            config = physCfg,
            targetPos = spec.targetPos,
            targetRot = spec.targetRot,
            obstacles = obstacles,
            floorY = 0.0f,
            boundsMin = Vec3(-3.5f, 0.15f, -4.2f),
            boundsMax = Vec3(3.5f, 4.2f, 4.2f),
            snapAssistEnabled = st.physics.snapAssistEnabled
        )
        activeBody = resolved

        val nowMs = SystemClock.uptimeMillis()
        val collision = resolved.lastCollision
        if (collision != null && nowMs - lastCollisionSoundMs > 180L) {
            lastCollisionSoundMs = nowMs
            soundEngine.playCollisionThud(collision.impactSpeedMps)
        }

        val aligned = isWithinTolerance(resolved.position, resolved.rotationDeg, spec.targetPos, spec.targetRot)
        _uiState.update {
            it.copy(
                activeManip = manip.copy(position = resolved.position, isAligned = aligned),
                physics = it.physics.copy(
                    isPartHeld = true,
                    isSnapping = resolved.isSnapping,
                    isGrounded = resolved.isGrounded,
                    velocityMps = 0f,
                    lastCollision = collision,
                    collisionHighlightUntilMs = if (collision != null) nowMs + 380L else it.physics.collisionHighlightUntilMs
                )
            )
        }
        syncRenderer()
    }

    fun rotateActivePart(rx: Float, ry: Float, rz: Float) {
        val st = _uiState.value
        val manip = st.activeManip ?: return
        if (manip.placedInSocket || manip.animatingTool != null) return
        val spec = JcbCatalog.findPart(manip.partId) ?: return

        val nextRot = Vec3(
            (manip.rotationDeg.x + rx) % 360f,
            (manip.rotationDeg.y + ry) % 360f,
            (manip.rotationDeg.z + rz) % 360f
        )
        activeBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = nextRot))
            .copy(rotationDeg = nextRot, angularVelocityDeg = Vec3())
        val aligned = isWithinTolerance(manip.position, nextRot, spec.targetPos, spec.targetRot)
        _uiState.update {
            it.copy(activeManip = manip.copy(rotationDeg = nextRot, isAligned = aligned))
        }
        syncRenderer()
    }

    fun attemptLockPlacement() {
        val st = _uiState.value
        val manip = st.activeManip ?: return
        if (manip.placedInSocket) return
        val spec = JcbCatalog.findPart(manip.partId) ?: return

        val dist = manip.position.distanceTo(spec.targetPos)
        val rotErr = maxOf(
            Mat4.angleDiffDeg(manip.rotationDeg.x, spec.targetRot.x),
            Mat4.angleDiffDeg(manip.rotationDeg.y, spec.targetRot.y),
            Mat4.angleDiffDeg(manip.rotationDeg.z, spec.targetRot.z)
        )

        if (dist <= 0.28f && rotErr <= 25f) {
            soundEngine.playHydraulicSnap()
            _uiState.update {
                it.copy(
                    activeManip = manip.copy(
                        position = spec.targetPos,
                        rotationDeg = spec.targetRot,
                        isAligned = true,
                        placedInSocket = true
                    )
                )
            }
            showFeedback(
                BiText(
                    "Seated! Now use ${spec.requiredTool.title.en} to secure (${manip.totalBolts}x)",
                    "फिट हो गया! अब ${spec.requiredTool.title.hi} से कसें (${manip.totalBolts}x)"
                ),
                isError = false
            )
            syncRenderer()
        } else {
            renderer.errorFlashUntilMs = SystemClock.uptimeMillis() + 650L
            soundEngine.playErrorBuzz()
            val msg = if (rotErr > 25f) {
                BiText(
                    "Wrong orientation (${rotErr.toInt()}° off)! Rotate to match guide.",
                    "गलत कोण (${rotErr.toInt()}°)! गाइड के अनुसार घुमाएं।"
                )
            } else {
                BiText(
                    "Wrong position (${String.format("%.2f", dist)}m away)! Move into glowing guide.",
                    "गलत स्थान (${String.format("%.2f", dist)}m दूर)! चमकते गाइड में ले जाएं।"
                )
            }
            showFeedback(msg, isError = true)
        }
    }

    fun selectTool(tool: ToolType) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun fastenBoltWithSelectedTool() {
        val st = _uiState.value
        val manip = st.activeManip ?: return
        val spec = JcbCatalog.findPart(manip.partId) ?: return
        if (!manip.placedInSocket) {
            showFeedback(
                BiText("Place & lock the part into the guide first!", "पहले पुर्जे को गाइड में फिट करें!"),
                isError = true
            )
            soundEngine.playErrorBuzz()
            return
        }
        if (manip.animatingTool != null) return

        if (st.selectedTool != spec.requiredTool) {
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText(
                    "Wrong tool! Select ${spec.requiredTool.title.en}.",
                    "गलत औज़ार! ${spec.requiredTool.title.hi} चुनें।"
                ),
                isError = true
            )
            return
        }

        // Play tool sound
        when (st.selectedTool) {
            ToolType.HAMMER -> soundEngine.playHammer()
            ToolType.DIAGNOSTIC_SCANNER -> soundEngine.playScannerBeep()
            ToolType.ENGINE_CRANE, ToolType.JACK, ToolType.HYDRAULIC_WRENCH -> soundEngine.playHydraulicSnap()
            else -> soundEngine.playRatchet()
        }

        toolAnimJob?.cancel()
        toolAnimJob = viewModelScope.launch {
            val steps = 12
            for (s in 1..steps) {
                val progress = s.toFloat() / steps
                _uiState.update { cur ->
                    val m = cur.activeManip ?: return@update cur
                    cur.copy(activeManip = m.copy(toolAnimProgress = progress, animatingTool = st.selectedTool))
                }
                syncRenderer()
                delay(28)
            }

            _uiState.update { cur ->
                val m = cur.activeManip ?: return@update cur
                val nextBolts = m.boltsSecured + 1
                if (nextBolts >= m.totalBolts) {
                    val newInstalled = cur.installedParts + spec.id
                    val newXp = cur.xp + spec.xpReward
                    val levelParts = JcbCatalog.partsForLevel(spec.level)
                    val levelComplete = levelParts.all { it.id in newInstalled }
                    val nextLvl = if (levelComplete && cur.currentLevel == spec.level && spec.level < 10) spec.level + 1 else cur.currentLevel
                    val newRewards = if (levelComplete) {
                        val badge = JcbCatalog.levels.find { it.levelNumber == spec.level }?.rewardBadge?.en
                        if (badge != null) cur.unlockedRewards + badge else cur.unlockedRewards
                    } else cur.unlockedRewards

                    soundEngine.playSuccessChime()
                    showFeedback(
                        BiText(
                            "${spec.name.en} Installed! +${spec.xpReward} XP",
                            "${spec.name.hi} इंस्टॉल हो गया! +${spec.xpReward} XP"
                        ),
                        isError = false
                    )
                    syncMissionProgression(
                        cur.copy(
                            installedParts = newInstalled,
                            activeManip = null,
                            xp = newXp,
                            currentLevel = nextLvl,
                            unlockedRewards = newRewards
                        )
                    )
                } else {
                    cur.copy(
                        activeManip = m.copy(
                            boltsSecured = nextBolts,
                            toolAnimProgress = 0f,
                            animatingTool = null
                        )
                    )
                }
            }
            syncRenderer()
            autoSave()
        }
    }

    // ==================== LEVEL 10 COMMISSIONING ====================
    fun performCommissioningStep(stepId: String) {
        val st = _uiState.value
        if (!st.allPartsBuilt) {
            showFeedback(
                BiText("Install all Levels 1–9 parts first!", "पहले लेवल 1–9 के सभी पुर्जे लगाएं!"),
                isError = true
            )
            soundEngine.playErrorBuzz()
            return
        }
        val c = st.commissioning
        var next = c
        var ok = true
        var errMsg: BiText? = null

        when (stepId) {
            "engine_oil" -> next = c.copy(engineOilFilled = true)
            "hyd_oil" -> next = c.copy(hydraulicOilFilled = true)
            "coolant" -> next = c.copy(coolantAdded = true)
            "diesel" -> next = c.copy(dieselAdded = true)
            "battery" -> {
                if (st.selectedTool != ToolType.DIAGNOSTIC_SCANNER) {
                    ok = false
                    errMsg = BiText("Select Diagnostic Scanner to check 12V battery!", "बैटरी जांचने के लिए डायग्नोस्टिक स्कैनर चुनें!")
                } else {
                    soundEngine.playScannerBeep()
                    next = c.copy(batteryChecked = true)
                }
            }
            "start_engine" -> {
                if (!c.engineOilFilled || !c.hydraulicOilFilled || !c.coolantAdded || !c.dieselAdded || !c.batteryChecked) {
                    ok = false
                    errMsg = BiText("Fill all fluids & scan battery before starting engine!", "इंजन चालू करने से पहले सभी ऑयल, कूलेंट, डीजल और बैटरी जांचें!")
                } else {
                    soundEngine.playSuccessChime()
                    next = c.copy(engineStarted = true)
                }
            }
            "warning_lights" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playScannerBeep()
                    next = c.copy(warningLightsChecked = true)
                }
            }
            "steering" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playHydraulicSnap()
                    next = c.copy(steeringTested = true)
                }
            }
            "gears" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playRatchet()
                    next = c.copy(gearsTested = true)
                }
            }
            "loader" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playHydraulicSnap()
                    next = c.copy(loaderTested = true)
                }
            }
            "excavator" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playHydraulicSnap()
                    next = c.copy(excavatorTested = true)
                }
            }
            "stabilizers" -> {
                if (!c.engineStarted) {
                    ok = false; errMsg = BiText("Start engine first!", "पहले इंजन चालू करें!")
                } else {
                    soundEngine.playHydraulicSnap()
                    next = c.copy(stabilizersTested = true)
                }
            }
            "final_inspection" -> {
                if (!c.warningLightsChecked || !c.steeringTested || !c.gearsTested || !c.loaderTested || !c.excavatorTested || !c.stabilizersTested) {
                    ok = false
                    errMsg = BiText("Complete all functional tests before final sign-off!", "अंतिम निरीक्षण से पहले सभी टेस्ट पूरे करें!")
                } else {
                    soundEngine.playSuccessChime()
                    next = c.copy(finalInspectionDone = true)
                }
            }
        }

        if (!ok) {
            soundEngine.playErrorBuzz()
            errMsg?.let { showFeedback(it, isError = true) }
            return
        }

        _uiState.update { cur ->
            val updated = cur.copy(commissioning = next, xp = cur.xp + 50)
            syncMissionProgression(updated)
        }
        syncRenderer()
        autoSave()
    }

    // ==================== DRIVING MODE CONTROLS ====================
    fun toggleDrivingEngine() {
        _uiState.update { st ->
            val nextRun = !st.driving.engineRunning
            val next = st.copy(
                driving = st.driving.copy(
                    engineRunning = nextRun,
                    rpm = if (nextRun) 850f else 0f,
                    oilPressureBar = if (nextRun) 4.2f else 0f
                )
            )
            syncMissionProgression(next)
        }
        syncRenderer()
    }

    fun setGear(gear: GearMode) {
        _uiState.update { it.copy(driving = it.driving.copy(gear = gear)) }
    }

    fun toggle4Wd() {
        _uiState.update { it.copy(driving = it.driving.copy(is4Wd = !it.driving.is4Wd)) }
    }

    fun toggle4Ws() {
        _uiState.update { it.copy(driving = it.driving.copy(is4Ws = !it.driving.is4Ws)) }
    }

    fun setDrivingInputs(throttle: Float? = null, brake: Float? = null, steer: Float? = null) {
        _uiState.update { st ->
            val d = st.driving
            st.copy(
                driving = d.copy(
                    throttleInput = throttle ?: d.throttleInput,
                    brakeInput = brake ?: d.brakeInput,
                    steerInput = steer ?: d.steerInput
                )
            )
        }
    }

    fun adjustLoaderBoom(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(loaderBoomDeg = (d.loaderBoomDeg + deltaDeg).coerceIn(0f, 55f)))
        }
    }

    fun adjustLoaderBucket(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(loaderBucketDeg = (d.loaderBucketDeg + deltaDeg).coerceIn(-45f, 48f)))
        }
    }

    fun adjustRearSwing(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(rearSwingDeg = (d.rearSwingDeg + deltaDeg).coerceIn(-65f, 65f)))
        }
    }

    fun adjustRearBoom(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(rearBoomDeg = (d.rearBoomDeg + deltaDeg).coerceIn(8f, 74f)))
        }
    }

    fun adjustRearDipper(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(rearDipperDeg = (d.rearDipperDeg + deltaDeg).coerceIn(-110f, -15f)))
        }
    }

    fun adjustRearBucket(deltaDeg: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(rearBucketDeg = (d.rearBucketDeg + deltaDeg).coerceIn(-95f, 35f)))
        }
    }

    fun adjustStabilizers(delta: Float) {
        _uiState.update { st ->
            val d = st.driving
            if (!d.engineRunning) return@update st
            st.copy(driving = d.copy(stabilizerExt = (d.stabilizerExt + delta).coerceIn(0f, 1f)))
        }
    }

    fun runDrivingFinalInspection() {
        val st = _uiState.value
        val d = st.driving
        if (MissionId.PARK_MACHINE !in st.completedMissions && !d.isInParkingBay) {
            showFeedback(
                BiText("Park inside the yellow parking bay first!", "पहले मशीन को पीले पार्किंग बे में पार्क करें!"),
                isError = true
            )
            soundEngine.playErrorBuzz()
            return
        }
        soundEngine.playScannerBeep()
        _uiState.update { cur ->
            val updated = cur.copy(
                driving = cur.driving.copy(finalInspectionDone = true),
                xp = cur.xp + 200
            )
            syncMissionProgression(updated)
        }
        showFeedback(
            BiText("Inspection Passed! JCB 4CX Fleet Certified!", "निरीक्षण सफल! JCB 4CX प्रमाणित!"),
            isError = false
        )
        autoSave()
    }

    fun selectActiveMission(id: MissionId) {
        _uiState.update { it.copy(activeMission = id) }
    }

    fun toggleSandboxDrivingUnlock() {
        _uiState.update { it.copy(sandboxDrivingUnlocked = !it.sandboxDrivingUnlocked) }
        autoSave()
    }

    private fun startDrivingLoop() {
        drivingLoopJob?.cancel()
        drivingLoopJob = viewModelScope.launch {
            var lastTime = SystemClock.uptimeMillis()
            while (isActive) {
                val now = SystemClock.uptimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceIn(0.01f, 0.05f)
                lastTime = now

                val st = _uiState.value
                if (!st.isPaused) {
                    stepDrivingPhysics(dt)
                } else {
                    soundEngine.updateDrivingAudio(false, 0f, false, false)
                }
                delay(20)
            }
        }
    }

    private fun stopDrivingLoop() {
        drivingLoopJob?.cancel()
        drivingLoopJob = null
        soundEngine.updateDrivingAudio(false, 0f, false, false)
    }

    private fun stepDrivingPhysics(dt: Float) {
        _uiState.update { st ->
            val d = st.driving
            val isStabilized = d.stabilizerExt > 0.45f
            val targetSpeed = if (d.engineRunning && !isStabilized && d.gear != GearMode.N) {
                d.gear.speedFactor * d.throttleInput * (if (d.is4Wd) 1.0f else 0.78f)
            } else 0f

            val accelRate = if (d.brakeInput > 0.1f) 14f else 4.5f
            val nextSpeed = if (d.brakeInput > 0.1f) {
                if (abs(d.speedMps) < 0.15f) 0f else d.speedMps - kotlin.math.sign(d.speedMps) * accelRate * dt
            } else {
                d.speedMps + (targetSpeed - d.speedMps) * (accelRate * dt).coerceAtMost(1f)
            }

            val turnScale = if (d.is4Ws) 1.45f else 1.0f
            val nextHeading = (d.headingDeg + d.steerInput * nextSpeed * 18f * turnScale * dt) % 360f
            val rad = Math.toRadians(nextHeading.toDouble()).toFloat()
            val nextX = (d.posX + sin(rad) * nextSpeed * dt).coerceIn(-16.5f, 16.5f)
            val nextZ = (d.posZ + cos(rad) * nextSpeed * dt).coerceIn(-16.5f, 16.5f)
            val nextSpin = (d.wheelSpinDeg + (nextSpeed / 0.62f) * 57.3f * dt) % 360f
            val nextRpm = if (d.engineRunning) {
                (820f + d.throttleInput * 1350f + abs(nextSpeed) * 45f).coerceIn(780f, 2250f)
            } else 0f

            // Interactive Site Triggers
            val bucketWorldX = nextX + sin(rad) * 2.5f
            val bucketWorldZ = nextZ + cos(rad) * 2.5f
            val rearBucketX = nextX - sin(rad) * 2.6f
            val rearBucketZ = nextZ - cos(rad) * 2.6f

            var fSoil = d.frontBucketSoil
            var soilMoved = d.soilMovedCount
            var truckProg = d.truckLoadProgress
            var trenchProg = d.trenchProgress

            // 1. Scoop from Soil Mound (7, 7)
            val distMound = hypot(bucketWorldX - 7f, bucketWorldZ - 7f)
            if (d.engineRunning && distMound < 3.4f && d.loaderBoomDeg < 18f && d.loaderBucketDeg < 10f && fSoil < 0.95f) {
                fSoil = (fSoil + dt * 0.85f).coerceAtMost(1f)
            }

            // 2. Dump at Grading Zone (6, -9) -> Mission 3
            val distGrading = hypot(bucketWorldX - 6f, bucketWorldZ - (-9f))
            if (d.engineRunning && distGrading < 3.2f && fSoil > 0.6f && d.loaderBucketDeg > 14f) {
                fSoil = 0f
                soilMoved += 1
                soundEngine.playSuccessChime()
            }

            // 3. Load Dump Truck (11, -2) -> Mission 5
            val distTruck = hypot(bucketWorldX - 11f, bucketWorldZ - (-2f))
            if (d.engineRunning && distTruck < 3.6f && fSoil > 0.6f && d.loaderBoomDeg > 28f && d.loaderBucketDeg > 12f) {
                fSoil = 0f
                truckProg = (truckProg + 0.34f).coerceAtMost(1f)
                soundEngine.playSuccessChime()
            }

            // 4. Dig Trench (-2, -9.5) with Rear Backhoe -> Mission 4
            val distTrench = hypot(rearBucketX - (-2f), rearBucketZ - (-9.5f))
            if (d.engineRunning && isStabilized && distTrench < 3.8f && d.rearBoomDeg < 34f && d.rearBucketDeg < -20f) {
                val prev = trenchProg
                trenchProg = (trenchProg + dt * 0.25f).coerceAtMost(1f)
                if (prev < 1f && trenchProg >= 1f) soundEngine.playSuccessChime()
            }

            // 5. Parking Bay (-10, 9) -> Mission 6
            val inParkBay = hypot(nextX - (-10f), nextZ - 9f) < 2.6f
            val restingSafety = inParkBay && !d.engineRunning && d.loaderBoomDeg < 12f && d.stabilizerExt < 0.15f
            val parkProg = if (restingSafety) 1f else if (inParkBay) 0.5f else 0f

            val updatedDriving = d.copy(
                posX = nextX,
                posZ = nextZ,
                headingDeg = nextHeading,
                speedMps = nextSpeed,
                wheelSpinDeg = nextSpin,
                rpm = nextRpm,
                frontBucketSoil = fSoil,
                soilMovedCount = soilMoved,
                truckLoadProgress = truckProg,
                trenchProgress = trenchProg,
                isInParkingBay = inParkBay,
                parkHoldProgress = parkProg
            )

            soundEngine.updateDrivingAudio(
                running = updatedDriving.engineRunning,
                rpm = updatedDriving.rpm,
                hydraulicMoving = false,
                isReversing = updatedDriving.gear == GearMode.R && updatedDriving.engineRunning
            )

            syncMissionProgression(st.copy(driving = updatedDriving))
        }
        syncRenderer()
    }

    private fun syncMissionProgression(st: GameUiState): GameUiState {
        val completed = st.completedMissions.toMutableSet()
        var xp = st.xp

        fun complete(id: MissionId, bonus: Int) {
            if (id !in completed) {
                completed.add(id)
                xp += bonus
            }
        }

        if (st.allPartsBuilt) complete(MissionId.BUILD_JCB, 1000)
        if (st.commissioning.engineStarted || st.driving.engineRunning) complete(MissionId.START_ENGINE, 400)
        if (st.driving.soilMovedCount >= 2) complete(MissionId.MOVE_SOIL, 500)
        if (st.driving.trenchProgress >= 0.99f) complete(MissionId.DIG_TRENCH, 650)
        if (st.driving.truckLoadProgress >= 0.99f) complete(MissionId.LOAD_TRUCK, 700)
        if (st.driving.parkHoldProgress >= 0.99f) complete(MissionId.PARK_MACHINE, 450)
        if (st.driving.finalInspectionDone) complete(MissionId.FINAL_INSPECTION, 800)

        return st.copy(completedMissions = completed, xp = xp)
    }

    private fun highestUnlockedLevel(st: GameUiState): Int {
        for (lvl in 1..9) {
            val parts = JcbCatalog.partsForLevel(lvl)
            if (!parts.all { it.id in st.installedParts }) return lvl
        }
        return 10
    }

    private fun isWithinTolerance(pos: Vec3, rot: Vec3, targetPos: Vec3, targetRot: Vec3): Boolean {
        val d = pos.distanceTo(targetPos)
        val rx = Mat4.angleDiffDeg(rot.x, targetRot.x)
        val ry = Mat4.angleDiffDeg(rot.y, targetRot.y)
        val rz = Mat4.angleDiffDeg(rot.z, targetRot.z)
        return d <= 0.28f && rx <= 25f && ry <= 25f && rz <= 25f
    }

    private fun showFeedback(msg: BiText, isError: Boolean) {
        feedbackClearJob?.cancel()
        _uiState.update { it.copy(feedbackBanner = Pair(msg, isError)) }
        feedbackClearJob = viewModelScope.launch {
            delay(3200)
            _uiState.update { it.copy(feedbackBanner = null) }
        }
    }

    private fun autoSave() {
        viewModelScope.launch {
            persistence.save(_uiState.value)
        }
    }

    // ==================== EXTREME MICRO-PART ASSEMBLY SYSTEM ====================
    fun selectEngineVariant(variant: com.example.model.EngineVariant) {
        _uiState.update { it.copy(micro = it.micro.copy(selectedVariant = variant)) }
        autoSave()
    }

    fun selectMicroCategory(cat: com.example.model.MicroCategory) {
        _uiState.update { it.copy(micro = it.micro.copy(selectedCategory = cat)) }
    }

    fun toggleExplodedMode() {
        _uiState.update { st ->
            val next = !st.micro.explodedMode
            st.copy(micro = st.micro.copy(explodedMode = next, explodeFactor = if (next) 1.0f else 0.0f))
        }
        syncRenderer()
    }

    fun setExplodeFactor(factor: Float) {
        _uiState.update { st ->
            st.copy(micro = st.micro.copy(explodedMode = factor > 0.02f, explodeFactor = factor.coerceIn(0f, 1f)))
        }
        syncRenderer()
    }

    fun toggleTransparentBlock() {
        _uiState.update { it.copy(micro = it.micro.copy(transparentBlockMode = !it.micro.transparentBlockMode)) }
        syncRenderer()
    }

    fun toggleInspectionMode() {
        _uiState.update { it.copy(micro = it.micro.copy(inspectionMode = !it.micro.inspectionMode)) }
        syncRenderer()
    }

    fun selectMicroPart(partId: String) {
        val st = _uiState.value
        val spec = com.example.model.MicroPartCatalog.findPart(partId) ?: return
        if (partId in st.micro.installedMicroParts) {
            renderer.focusTarget = spec.targetPos
            renderer.orbitDistance = 0.55f
            showFeedback(BiText("Inspecting installed: ${spec.name.en}", "निरीक्षण: ${spec.name.hi}"), isError = false)
            return
        }
        val missing = spec.prerequisites.firstOrNull { it !in st.micro.installedMicroParts }
        if (missing != null) {
            val mSpec = com.example.model.MicroPartCatalog.findPart(missing)
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText(
                    "Missing prerequisite: Install ${mSpec?.name?.en ?: missing} first!",
                    "कमी: पहले ${mSpec?.name?.hi ?: missing} इंस्टॉल करें!"
                ),
                isError = true
            )
            return
        }

        val startPos = spec.targetPos + spec.spawnOffset
        val isTiny = spec.targetDepthMm < 20f || spec.id.contains("oring") || spec.id.contains("collet") || spec.id.contains("washer") || spec.id.contains("ring")
        renderer.focusTarget = spec.targetPos
        renderer.orbitDistance = if (isTiny) 0.45f else 0.85f

        val physCfg = MechanicalPhysicsEngine.configForMicroPart(spec)
        activeBody = RigidBodyState(
            position = startPos,
            rotationDeg = spec.spawnRot,
            isHeld = true
        )

        val (defaultNm, defaultAng) = com.example.model.MicroPartCatalog.requiredTorqueHintNm(
            spec.torqueSpecKey,
            st.micro.selectedVariant
        )
        val manip = com.example.model.MicroManipState(
            partId = spec.id,
            position = startPos,
            rotationDeg = spec.spawnRot,
            isAligned = isMicroAligned(startPos, spec.spawnRot, spec.targetPos, spec.targetRot),
            currentDepthMm = 0f,
            torqueInputNm = defaultNm ?: 0,
            angleInputDeg = defaultAng ?: 0
        )
        _uiState.update {
            it.copy(
                selectedTool = spec.requiredTool,
                tooltipGuide = it.tooltipGuide.copy(manualTourIndex = null),
                micro = it.micro.copy(activeMicro = manip, explodedMode = false, explodeFactor = 0f),
                physics = it.physics.copy(
                    isPartHeld = true,
                    isSnapping = false,
                    isGrounded = false,
                    velocityMps = 0f,
                    massKg = physCfg.massKg,
                    lastCollision = null
                )
            )
        }
        syncRenderer()
    }

    fun moveMicroPart(dx: Float, dy: Float, dz: Float) {
        val st = _uiState.value
        val manip = st.micro.activeMicro ?: return
        val spec = com.example.model.MicroPartCatalog.findPart(manip.partId) ?: return

        val physCfg = MechanicalPhysicsEngine.configForMicroPart(spec)
        val obstacles = MechanicalPhysicsEngine.buildMicroObstacles(st.micro.installedMicroParts, spec, physCfg)
        val baseBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = manip.rotationDeg))
            .copy(position = manip.position, rotationDeg = manip.rotationDeg, isHeld = true)

        val resolved = MechanicalPhysicsEngine.applyManualNudge(
            body = baseBody,
            delta = Vec3(dx, dy, dz),
            config = physCfg,
            targetPos = spec.targetPos,
            targetRot = spec.targetRot,
            obstacles = obstacles,
            floorY = 0.12f,
            boundsMin = Vec3(-1.5f, 0.05f, -1.5f),
            boundsMax = Vec3(1.5f, 2.2f, 1.5f),
            snapAssistEnabled = st.physics.snapAssistEnabled
        )
        activeBody = resolved

        val nowMs = SystemClock.uptimeMillis()
        val collision = resolved.lastCollision
        if (collision != null && nowMs - lastCollisionSoundMs > 180L) {
            lastCollisionSoundMs = nowMs
            soundEngine.playCollisionThud(collision.impactSpeedMps)
        }

        val dist = resolved.position.distanceTo(spec.targetPos)
        val depthRatio = (1f - (dist / 0.5f)).coerceIn(0f, 1f)
        val aligned = isMicroAligned(resolved.position, resolved.rotationDeg, spec.targetPos, spec.targetRot)
        _uiState.update {
            it.copy(
                micro = it.micro.copy(
                    activeMicro = manip.copy(
                        position = resolved.position,
                        isAligned = aligned,
                        currentDepthMm = depthRatio * spec.targetDepthMm
                    )
                ),
                physics = it.physics.copy(
                    isPartHeld = true,
                    isSnapping = resolved.isSnapping,
                    isGrounded = resolved.isGrounded,
                    velocityMps = 0f,
                    lastCollision = collision,
                    collisionHighlightUntilMs = if (collision != null) nowMs + 380L else it.physics.collisionHighlightUntilMs
                )
            )
        }
        syncRenderer()
    }

    fun rotateMicroPart(rx: Float, ry: Float, rz: Float) {
        val st = _uiState.value
        val manip = st.micro.activeMicro ?: return
        val spec = com.example.model.MicroPartCatalog.findPart(manip.partId) ?: return
        val nextRot = Vec3(
            (manip.rotationDeg.x + rx) % 360f,
            (manip.rotationDeg.y + ry) % 360f,
            (manip.rotationDeg.z + rz) % 360f
        )
        activeBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = nextRot))
            .copy(rotationDeg = nextRot, angularVelocityDeg = Vec3())
        val aligned = isMicroAligned(manip.position, nextRot, spec.targetPos, spec.targetRot)
        _uiState.update {
            it.copy(micro = it.micro.copy(activeMicro = manip.copy(rotationDeg = nextRot, isAligned = aligned)))
        }
        syncRenderer()
    }

    fun setMicroRingStagger(deg: Int) {
        _uiState.update { st ->
            val m = st.micro.activeMicro ?: return@update st
            st.copy(micro = st.micro.copy(activeMicro = m.copy(ringGapStaggerDeg = deg)))
        }
    }

    fun setValveSpringCompression(value: Float) {
        _uiState.update { st ->
            st.copy(micro = st.micro.copy(valveSpringCompressed = value.coerceIn(0f, 1f)))
        }
        syncRenderer()
    }

    fun setMicroTorqueAndAngle(nm: Int, angleDeg: Int) {
        _uiState.update { st ->
            val m = st.micro.activeMicro ?: return@update st
            st.copy(micro = st.micro.copy(activeMicro = m.copy(torqueInputNm = nm.coerceIn(0, 320), angleInputDeg = angleDeg.coerceIn(0, 360))))
        }
    }

    fun animateMicroPartStep() {
        val st = _uiState.value
        val manip = st.micro.activeMicro ?: return
        val spec = com.example.model.MicroPartCatalog.findPart(manip.partId) ?: return
        viewModelScope.launch {
            val startPos = manip.position
            val startRot = manip.rotationDeg
            val steps = 16
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val interpPos = Vec3(
                    startPos.x + (spec.targetPos.x - startPos.x) * t,
                    startPos.y + (spec.targetPos.y - startPos.y) * t,
                    startPos.z + (spec.targetPos.z - startPos.z) * t
                )
                val interpRot = Vec3(
                    startRot.x + (spec.targetRot.x - startRot.x) * t,
                    startRot.y + (spec.targetRot.y - startRot.y) * t,
                    startRot.z + (spec.targetRot.z - startRot.z) * t
                )
                _uiState.update { cur ->
                    val m = cur.micro.activeMicro ?: return@update cur
                    cur.copy(
                        micro = cur.micro.copy(
                            activeMicro = m.copy(
                                position = interpPos,
                                rotationDeg = interpRot,
                                isAligned = isMicroAligned(interpPos, interpRot, spec.targetPos, spec.targetRot),
                                currentDepthMm = t * spec.targetDepthMm,
                                animatingPathProgress = t
                            )
                        )
                    )
                }
                syncRenderer()
                delay(24)
            }
            soundEngine.playHydraulicSnap()
        }
    }

    fun runBearingClearanceCheck() {
        val st = _uiState.value
        if ("mp_main_bearing_shell_upper" !in st.micro.installedMicroParts && st.micro.activeMicro?.partId != "mp_main_bearing_shell_upper" && st.micro.activeMicro?.partId != "mp_conrod_bearing_shell") {
            soundEngine.playErrorBuzz()
            showFeedback(BiText("Install or select a bearing shell first!", "पहले बेयरिंग शेल चुनें या लगाएं!"), isError = true)
            return
        }
        val specVerified = st.micro.customManualSpecs["main_bearing_oil_clearance"]
        if (specVerified.isNullOrBlank()) {
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText(
                    "Service-Manual Data Required: Enter Main Bearing Oil Clearance in Digital Manual!",
                    "मैनुअल डेटा आवश्यक: डिजिटल मैनुअल में बेयरिंग ऑयल क्लियरेंस दर्ज करें!"
                ),
                isError = true
            )
            return
        }
        soundEngine.playScannerBeep()
        _uiState.update { it.copy(micro = it.micro.copy(bearingClearanceVerified = true), xp = it.xp + 30) }
        showFeedback(
            BiText("Plastigage Clearance Checked against $specVerified", "प्लास्टिगेज क्लियरेंस जांच पूर्ण ($specVerified)"),
            isError = false
        )
        autoSave()
    }

    fun runCrankshaftRotationCheck() {
        val st = _uiState.value
        if ("mp_crankshaft" !in st.micro.installedMicroParts) {
            soundEngine.playErrorBuzz()
            showFeedback(BiText("Install Crankshaft first!", "पहले क्रैंकशाफ्ट इंस्टॉल करें!"), isError = true)
            return
        }
        viewModelScope.launch {
            soundEngine.playRatchet()
            for (deg in 0..720 step 24) {
                _uiState.update { cur ->
                    cur.copy(micro = cur.micro.copy(crankSpinDeg = deg.toFloat()))
                }
                syncRenderer()
                delay(22)
            }
            _uiState.update { cur ->
                cur.copy(
                    micro = cur.micro.copy(crankSpinDeg = 0f, crankSpinVerified = true),
                    xp = cur.xp + 40
                )
            }
            soundEngine.playSuccessChime()
            showFeedback(
                BiText("720° Crankshaft Rotation Smooth — End Float OK!", "720° क्रैंकशाफ्ट घुमाव सुचारू — एंड फ्लोट सही है!"),
                isError = false
            )
            autoSave()
        }
    }

    fun installActiveMicroPart() {
        val st = _uiState.value
        val manip = st.micro.activeMicro ?: return
        val spec = com.example.model.MicroPartCatalog.findPart(manip.partId) ?: return

        val dist = manip.position.distanceTo(spec.targetPos)
        val rotErr = maxOf(
            Mat4.angleDiffDeg(manip.rotationDeg.x, spec.targetRot.x),
            Mat4.angleDiffDeg(manip.rotationDeg.y, spec.targetRot.y),
            Mat4.angleDiffDeg(manip.rotationDeg.z, spec.targetRot.z)
        )
        if (dist > 0.14f || rotErr > 25f) {
            renderer.errorFlashUntilMs = SystemClock.uptimeMillis() + 650L
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText(
                    "Incorrect Alignment! Offset ${(dist * 1000).toInt()}mm, Angle ${rotErr.toInt()}°",
                    "गलत संरेखण! दूरी ${(dist * 1000).toInt()}mm, कोण ${rotErr.toInt()}°"
                ),
                isError = true
            )
            return
        }

        if (st.selectedTool != spec.requiredTool) {
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText("Wrong tool! Use ${spec.requiredTool.title.en}", "गलत औज़ार! ${spec.requiredTool.title.hi} चुनें"),
                isError = true
            )
            return
        }

        // Simulation-specific validation
        if (spec.id == "mp_valve_collets_in" && st.micro.valveSpringCompressed < 0.75f) {
            soundEngine.playErrorBuzz()
            showFeedback(
                BiText("Compress valve spring >= 75% before fitting collets!", "कॉलेट्स लगाने से पहले वाल्व स्प्रिंग 75% दबाएं!"),
                isError = true
            )
            return
        }

        if (spec.simType == com.example.model.MicroSimType.PISTON_RING_FIT) {
            val expectedGap = when (spec.id) {
                "mp_ring_oil" -> 0
                "mp_ring_scraper" -> 120
                else -> 240
            }
            if (manip.ringGapStaggerDeg != expectedGap) {
                soundEngine.playErrorBuzz()
                showFeedback(
                    BiText("Set ring end-gap stagger to ${expectedGap}° (120° apart)!", "रिंग गैप को ${expectedGap}° पर सेट करें!"),
                    isError = true
                )
                return
            }
        }

        if (spec.torqueSpecKey != null) {
            val (reqNm, reqAng) = com.example.model.MicroPartCatalog.requiredTorqueHintNm(
                spec.torqueSpecKey,
                st.micro.selectedVariant
            )
            if (reqNm != null) {
                if (manip.torqueInputNm != reqNm || (reqAng != null && reqAng > 0 && manip.angleInputDeg != reqAng)) {
                    soundEngine.playErrorBuzz()
                    showFeedback(
                        BiText(
                            "Incorrect Torque! Set Wrench to $reqNm Nm + ${reqAng ?: 0}°",
                            "गलत टॉर्क! रिंच को $reqNm Nm + ${reqAng ?: 0}° पर सेट करें"
                        ),
                        isError = true
                    )
                    return
                }
            } else {
                // Unknown / serial-dependent torque: require verified manual data!
                val userSpec = st.micro.customManualSpecs[spec.torqueSpecKey]
                if (userSpec.isNullOrBlank()) {
                    soundEngine.playErrorBuzz()
                    showFeedback(
                        BiText(
                            "Manual Spec Required! Enter torque in Digital Service Manual first.",
                            "मैनुअल डेटा आवश्यक! पहले डिजिटल सर्विस मैनुअल में टॉर्क दर्ज करें।"
                        ),
                        isError = true
                    )
                    return
                }
            }
        }

        // All precision checks passed!
        when (spec.requiredTool) {
            ToolType.HAMMER -> soundEngine.playHammer()
            ToolType.TORQUE_WRENCH -> soundEngine.playRatchet()
            else -> soundEngine.playSuccessChime()
        }

        _uiState.update { cur ->
            val nextInstalled = cur.micro.installedMicroParts + spec.id
            val resetSpring = if (spec.id == "mp_valve_collets_in") 0f else cur.micro.valveSpringCompressed
            cur.copy(
                xp = cur.xp + spec.xpReward,
                micro = cur.micro.copy(
                    installedMicroParts = nextInstalled,
                    activeMicro = null,
                    valveSpringCompressed = resetSpring
                )
            )
        }
        showFeedback(
            BiText("${spec.name.en} Installed! +${spec.xpReward} XP", "${spec.name.hi} इंस्टॉल हो गया! +${spec.xpReward} XP"),
            isError = false
        )
        syncRenderer()
        autoSave()
    }

    fun saveCustomManualSpec(key: String, value: String) {
        _uiState.update { st ->
            val nextMap = st.micro.customManualSpecs.toMutableMap()
            if (value.isBlank()) nextMap.remove(key) else nextMap[key] = value.trim()
            st.copy(micro = st.micro.copy(customManualSpecs = nextMap))
        }
        soundEngine.playSuccessChime()
        autoSave()
    }

    fun resetMicroAssembly() {
        _uiState.update { st ->
            st.copy(
                micro = st.micro.copy(
                    installedMicroParts = emptySet(),
                    activeMicro = null,
                    crankSpinVerified = false,
                    bearingClearanceVerified = false,
                    valveSpringCompressed = 0f
                )
            )
        }
        syncRenderer()
        autoSave()
    }

    private fun isMicroAligned(pos: Vec3, rot: Vec3, targetPos: Vec3, targetRot: Vec3): Boolean {
        val d = pos.distanceTo(targetPos)
        val rx = Mat4.angleDiffDeg(rot.x, targetRot.x)
        val ry = Mat4.angleDiffDeg(rot.y, targetRot.y)
        val rz = Mat4.angleDiffDeg(rot.z, targetRot.z)
        return d <= 0.14f && rx <= 25f && ry <= 25f && rz <= 25f
    }

    // ==================== LIGHTWEIGHT PHYSICS ENGINE LOOP & CONTROLS ====================
    fun togglePhysicsGravity() {
        _uiState.update { st ->
            val next = !st.physics.gravityEnabled
            st.copy(physics = st.physics.copy(gravityEnabled = next))
        }
    }

    fun togglePhysicsSnapAssist() {
        _uiState.update { st ->
            st.copy(physics = st.physics.copy(snapAssistEnabled = !st.physics.snapAssistEnabled))
        }
    }

    fun togglePhysicsHold() {
        val st = _uiState.value
        val nextHold = !st.physics.isPartHeld
        activeBody = activeBody?.copy(
            isHeld = nextHold,
            isSleeping = false,
            velocity = if (nextHold) Vec3() else Vec3(0f, -0.05f, 0f)
        )
        _uiState.update {
            it.copy(physics = it.physics.copy(isPartHeld = nextHold))
        }
    }

    fun dropActivePartWithGravity() {
        val st = _uiState.value
        val hasTarget = when (st.route) {
            ScreenRoute.WORKSHOP -> st.activeManip != null && !st.activeManip.placedInSocket
            ScreenRoute.MICRO_ASSEMBLY -> st.micro.activeMicro != null
            else -> false
        }
        if (!hasTarget) return
        activeBody = activeBody?.copy(
            isHeld = false,
            isSleeping = false,
            isGrounded = false,
            velocity = Vec3(0f, -0.08f, 0f)
        )
        _uiState.update {
            it.copy(physics = it.physics.copy(gravityEnabled = true, isPartHeld = false))
        }
    }

    private fun startPhysicsLoop() {
        physicsLoopJob?.cancel()
        physicsLoopJob = viewModelScope.launch {
            var lastMs = SystemClock.uptimeMillis()
            while (isActive) {
                val nowMs = SystemClock.uptimeMillis()
                val dt = ((nowMs - lastMs) / 1000f).coerceIn(0.005f, 0.033f)
                lastMs = nowMs

                val st = _uiState.value
                if (!st.isPaused) {
                    stepAssemblyPhysics(dt, nowMs)
                }
                delay(20)
            }
        }
    }

    private fun stopPhysicsLoop() {
        physicsLoopJob?.cancel()
        physicsLoopJob = null
    }

    private fun stepAssemblyPhysics(dt: Float, nowMs: Long) {
        val st = _uiState.value
        when (st.route) {
            ScreenRoute.WORKSHOP -> {
                val manip = st.activeManip ?: return
                if (manip.placedInSocket || manip.animatingTool != null) return
                val spec = JcbCatalog.findPart(manip.partId) ?: return
                val cfg = MechanicalPhysicsEngine.configForJcbPart(spec)
                val obstacles = MechanicalPhysicsEngine.buildJcbObstacles(st.installedParts, spec, cfg)
                val curBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = manip.rotationDeg))
                    .copy(position = manip.position, rotationDeg = manip.rotationDeg, isHeld = st.physics.isPartHeld)

                val stepped = MechanicalPhysicsEngine.step(
                    dtSec = dt,
                    body = curBody,
                    config = cfg,
                    targetPos = spec.targetPos,
                    targetRot = spec.targetRot,
                    obstacles = obstacles,
                    floorY = 0.0f,
                    boundsMin = Vec3(-3.5f, 0.15f, -4.2f),
                    boundsMax = Vec3(3.5f, 4.2f, 4.2f),
                    gravityEnabled = st.physics.gravityEnabled,
                    snapAssistEnabled = st.physics.snapAssistEnabled,
                    seatPosTolerance = 0.015f,
                    seatAngleToleranceDeg = 3.0f
                )
                activeBody = stepped

                val contact = stepped.lastCollision
                if (contact != null && contact.impactSpeedMps > 0.55f && nowMs - lastCollisionSoundMs > 220L) {
                    lastCollisionSoundMs = nowMs
                    soundEngine.playCollisionThud(contact.impactSpeedMps)
                }

                if (stepped.justSnappedToSeat) {
                    soundEngine.playHydraulicSnap()
                    _uiState.update { cur ->
                        cur.copy(
                            activeManip = manip.copy(
                                position = spec.targetPos,
                                rotationDeg = spec.targetRot,
                                isAligned = true,
                                placedInSocket = true
                            ),
                            physics = cur.physics.copy(
                                isPartHeld = true,
                                isSnapping = false,
                                velocityMps = 0f
                            )
                        )
                    }
                    showFeedback(
                        BiText(
                            "Physics Snap Seated! Fasten bolts with ${spec.requiredTool.title.en}",
                            "स्नैप लॉक हो गया! अब ${spec.requiredTool.title.hi} से बोल्ट कसें"
                        ),
                        isError = false
                    )
                    syncRenderer()
                } else if (stepped.position != manip.position || stepped.rotationDeg != manip.rotationDeg || stepped.isSnapping != st.physics.isSnapping || stepped.lastCollision != null) {
                    val aligned = isWithinTolerance(stepped.position, stepped.rotationDeg, spec.targetPos, spec.targetRot)
                    _uiState.update { cur ->
                        cur.copy(
                            activeManip = manip.copy(
                                position = stepped.position,
                                rotationDeg = stepped.rotationDeg,
                                isAligned = aligned
                            ),
                            physics = cur.physics.copy(
                                isSnapping = stepped.isSnapping,
                                isGrounded = stepped.isGrounded,
                                velocityMps = stepped.velocity.length(),
                                lastCollision = contact,
                                collisionHighlightUntilMs = if (contact != null && contact.impactSpeedMps > 0.55f) nowMs + 380L else cur.physics.collisionHighlightUntilMs
                            )
                        )
                    }
                    syncRenderer()
                }
            }

            ScreenRoute.MICRO_ASSEMBLY -> {
                val manip = st.micro.activeMicro ?: return
                val spec = com.example.model.MicroPartCatalog.findPart(manip.partId) ?: return
                val cfg = MechanicalPhysicsEngine.configForMicroPart(spec)
                val obstacles = MechanicalPhysicsEngine.buildMicroObstacles(st.micro.installedMicroParts, spec, cfg)
                val curBody = (activeBody ?: RigidBodyState(manip.position, rotationDeg = manip.rotationDeg))
                    .copy(position = manip.position, rotationDeg = manip.rotationDeg, isHeld = st.physics.isPartHeld)

                val stepped = MechanicalPhysicsEngine.step(
                    dtSec = dt,
                    body = curBody,
                    config = cfg,
                    targetPos = spec.targetPos,
                    targetRot = spec.targetRot,
                    obstacles = obstacles,
                    floorY = 0.12f,
                    boundsMin = Vec3(-1.5f, 0.05f, -1.5f),
                    boundsMax = Vec3(1.5f, 2.2f, 1.5f),
                    gravityEnabled = st.physics.gravityEnabled,
                    snapAssistEnabled = st.physics.snapAssistEnabled,
                    seatPosTolerance = 0.008f,
                    seatAngleToleranceDeg = 3.0f
                )
                activeBody = stepped

                val contact = stepped.lastCollision
                if (contact != null && contact.impactSpeedMps > 0.45f && nowMs - lastCollisionSoundMs > 220L) {
                    lastCollisionSoundMs = nowMs
                    soundEngine.playCollisionThud(contact.impactSpeedMps)
                }

                if (stepped.justSnappedToSeat) {
                    soundEngine.playHydraulicSnap()
                    _uiState.update { cur ->
                        cur.copy(
                            micro = cur.micro.copy(
                                activeMicro = manip.copy(
                                    position = spec.targetPos,
                                    rotationDeg = spec.targetRot,
                                    isAligned = true,
                                    currentDepthMm = spec.targetDepthMm
                                )
                            ),
                            physics = cur.physics.copy(
                                isPartHeld = true,
                                isSnapping = false,
                                velocityMps = 0f
                            )
                        )
                    }
                    showFeedback(
                        BiText("Micro Snap Aligned (${spec.targetDepthMm}mm)! Apply tool/torque.", "सटीक स्नैप फिट! अब औज़ार/टॉर्क लगाएं।"),
                        isError = false
                    )
                    syncRenderer()
                } else if (stepped.position != manip.position || stepped.rotationDeg != manip.rotationDeg || stepped.isSnapping != st.physics.isSnapping) {
                    val dist = stepped.position.distanceTo(spec.targetPos)
                    val depthRatio = (1f - (dist / 0.5f)).coerceIn(0f, 1f)
                    val aligned = isMicroAligned(stepped.position, stepped.rotationDeg, spec.targetPos, spec.targetRot)
                    _uiState.update { cur ->
                        cur.copy(
                            micro = cur.micro.copy(
                                activeMicro = manip.copy(
                                    position = stepped.position,
                                    rotationDeg = stepped.rotationDeg,
                                    isAligned = aligned,
                                    currentDepthMm = depthRatio * spec.targetDepthMm
                                )
                            ),
                            physics = cur.physics.copy(
                                isSnapping = stepped.isSnapping,
                                isGrounded = stepped.isGrounded,
                                velocityMps = stepped.velocity.length(),
                                lastCollision = contact,
                                collisionHighlightUntilMs = if (contact != null && contact.impactSpeedMps > 0.45f) nowMs + 380L else cur.physics.collisionHighlightUntilMs
                            )
                        )
                    }
                    syncRenderer()
                }
            }
            else -> Unit
        }
    }

    private fun syncRenderer() {
        renderer.gameState = _uiState.value
    }
}

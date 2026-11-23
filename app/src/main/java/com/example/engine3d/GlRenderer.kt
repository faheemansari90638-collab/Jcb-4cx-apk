package com.example.engine3d

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.SystemClock
import com.example.model.CameraMode
import com.example.model.ControlLayout
import com.example.model.DrivingSimState
import com.example.model.GameUiState
import com.example.model.GraphicsQuality
import com.example.model.JcbCatalog
import com.example.model.MicroPartCatalog
import com.example.model.ScreenRoute
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GlRenderer : GLSurfaceView.Renderer {

    @Volatile
    var gameState: GameUiState = GameUiState()

    @Volatile
    var orbitYawDeg: Float = 38f
    @Volatile
    var orbitPitchDeg: Float = 24f
    @Volatile
    var orbitDistance: Float = 5.4f
    @Volatile
    var focusTarget: Vec3 = Vec3(0f, 1.15f, 0f)
    @Volatile
    var errorFlashUntilMs: Long = 0L

    private var program = 0
    private var uMvpLoc = 0
    private var uModelLoc = 0
    private var uViewPosLoc = 0
    private var uSpecularLoc = 0
    private var uShininessLoc = 0
    private var uAlphaLoc = 0
    private var uEmissiveLoc = 0
    private var uOverrideColorLoc = 0
    private var uUseOverrideLoc = 0

    private var currentQuality: GraphicsQuality = GraphicsQuality.MEDIUM
    private var currentLayout: ControlLayout = ControlLayout.JOYSTICK
    private lateinit var meshLib: MeshLibrary
    private lateinit var microMeshes: MicroPartMeshes
    private lateinit var envMeshes: EnvironmentMeshes

    private var widthPx = 1
    private var heightPx = 1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.06f, 0.08f, 0.11f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        program = compileProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        uMvpLoc = GLES30.glGetUniformLocation(program, "uMVP")
        uModelLoc = GLES30.glGetUniformLocation(program, "uModel")
        uViewPosLoc = GLES30.glGetUniformLocation(program, "uViewPos")
        uSpecularLoc = GLES30.glGetUniformLocation(program, "uSpecular")
        uShininessLoc = GLES30.glGetUniformLocation(program, "uShininess")
        uAlphaLoc = GLES30.glGetUniformLocation(program, "uAlpha")
        uEmissiveLoc = GLES30.glGetUniformLocation(program, "uEmissive")
        uOverrideColorLoc = GLES30.glGetUniformLocation(program, "uOverrideColor")
        uUseOverrideLoc = GLES30.glGetUniformLocation(program, "uUseOverride")

        currentQuality = gameState.graphicsQuality
        currentLayout = gameState.controlLayout
        meshLib = MeshLibrary(currentQuality, currentLayout)
        microMeshes = MicroPartMeshes(currentQuality)
        envMeshes = EnvironmentMeshes(currentQuality.circleSegments)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        widthPx = width.coerceAtLeast(1)
        heightPx = height.coerceAtLeast(1)
        GLES30.glViewport(0, 0, widthPx, heightPx)
    }

    override fun onDrawFrame(gl: GL10?) {
        val state = gameState
        if (state.graphicsQuality != currentQuality || state.controlLayout != currentLayout) {
            currentQuality = state.graphicsQuality
            currentLayout = state.controlLayout
            meshLib = MeshLibrary(currentQuality, currentLayout)
            microMeshes = MicroPartMeshes(currentQuality)
            envMeshes = EnvironmentMeshes(currentQuality.circleSegments)
        }

        val isDriving = state.route == ScreenRoute.DRIVING
        val isMicro = state.route == ScreenRoute.MICRO_ASSEMBLY
        if (isDriving) {
            GLES30.glClearColor(0.42f, 0.58f, 0.74f, 1.0f)
        } else {
            GLES30.glClearColor(0.06f, 0.08f, 0.11f, 1.0f)
        }

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(program)

        val aspect = widthPx.toFloat() / heightPx.toFloat()
        val nearPlane = if (isMicro) 0.03f else 0.1f
        val proj = Mat4.perspective(if (state.cameraMode == CameraMode.FIRST_PERSON_CAB) 64f else 48f, aspect, nearPlane, 120f)

        val (eye, target) = computeCamera(state, isDriving)
        val view = Mat4.lookAt(eye, target, Vec3(0f, 1f, 0f))
        val vp = Mat4.multiply(proj, view)
        GLES30.glUniform3f(uViewPosLoc, eye.x, eye.y, eye.z)

        val nowMs = SystemClock.uptimeMillis()

        when {
            isDriving -> renderDrivingWorld(state, vp, nowMs)
            isMicro -> renderMicroAssemblyWorld(state, vp, nowMs)
            else -> renderWorkshopWorld(state, vp, nowMs)
        }
    }

    private fun computeCamera(state: GameUiState, isDriving: Boolean): Pair<Vec3, Vec3> {
        if (state.cameraMode == CameraMode.FIRST_PERSON_CAB) {
            return if (isDriving) {
                val d = state.driving
                val rad = Math.toRadians(d.headingDeg.toDouble()).toFloat()
                val fwd = Vec3(sin(rad), 0f, cos(rad))
                val eye = Vec3(d.posX, 2.08f, d.posZ) - fwd * 0.42f
                val look = eye + fwd * 6.0f + Vec3(0f, -0.8f, 0f)
                Pair(eye, look)
            } else {
                val eye = Vec3(0f, 2.02f, -0.45f)
                val yawR = Math.toRadians(orbitYawDeg.toDouble()).toFloat()
                val look = eye + Vec3(sin(yawR), -0.15f, cos(yawR)) * 3.0f
                Pair(eye, look)
            }
        }

        val center = if (isDriving) {
            Vec3(state.driving.posX, 1.25f, state.driving.posZ)
        } else {
            focusTarget
        }
        val yawRad = Math.toRadians(orbitYawDeg.toDouble()).toFloat()
        val pitchRad = Math.toRadians(orbitPitchDeg.toDouble()).toFloat()
        val horiz = cos(pitchRad) * orbitDistance
        val eye = Vec3(
            center.x + sin(yawRad) * horiz,
            (center.y + sin(pitchRad) * orbitDistance).coerceAtLeast(0.12f),
            center.z + cos(yawRad) * horiz
        )
        return Pair(eye, center)
    }

    private fun renderMicroAssemblyWorld(state: GameUiState, vp: FloatArray, nowMs: Long) {
        drawSubGroup(microMeshes.engineStandMesh, Mat4.identity(), vp)
        val ms = state.micro
        val exp = if (ms.explodedMode) ms.explodeFactor.coerceIn(0f, 1f) else 0f
        val crankRad = Math.toRadians(ms.crankSpinDeg.toDouble()).toFloat()
        val pistonLift = (cos(crankRad) - 1f) * 0.055f

        for (part in MicroPartCatalog.parts) {
            val isInstalled = part.id in ms.installedMicroParts
            // Show installed parts, OR show all parts when Exploded View is enabled!
            if (!isInstalled && !ms.explodedMode) continue

            var pos = part.targetPos + (part.explodedOffset * exp)
            var rot = part.targetRot
            var scale = Vec3(1f, 1f, 1f)

            // Crankshaft rotation kinematics
            if (part.id == "mp_crankshaft" && exp < 0.05f) {
                rot = rot + Vec3(0f, 0f, ms.crankSpinDeg)
            }
            if (part.id in listOf("mp_piston", "mp_ring_oil", "mp_ring_scraper", "mp_ring_top") && exp < 0.05f) {
                pos = pos + Vec3(0f, pistonLift, 0f)
            }

            // Valve spring compression animation
            if (part.id == "mp_valve_spring_in" && ms.valveSpringCompressed > 0.01f) {
                val squash = 1f - 0.36f * ms.valveSpringCompressed
                scale = Vec3(1f, squash, 1f)
                pos = pos - Vec3(0f, 0.015f * ms.valveSpringCompressed, 0f)
            }
            if (part.id == "mp_valve_retainer_in" && ms.valveSpringCompressed > 0.01f) {
                pos = pos - Vec3(0f, 0.025f * ms.valveSpringCompressed, 0f)
            }

            val mesh = microMeshes.getMicroMesh(part.id)
            val model = Mat4.trs(pos, rot, scale)

            // Transparent engine block / head X-ray mode
            if (ms.transparentBlockMode && (part.id == "mp_engine_block" || part.id == "mp_cylinder_head")) {
                drawGhostSubGroup(mesh, model, vp, floatArrayOf(0.45f, 0.78f, 0.96f), 0.28f)
                continue
            }

            // Hidden-component inspection highlight mode
            if (ms.inspectionMode && part.isInternalHidden) {
                val pulse = 0.55f + 0.35f * sin(nowMs * 0.008f)
                drawGhostSubGroup(mesh, model, vp, floatArrayOf(0.18f, 0.95f, 0.65f), pulse)
                continue
            }

            if (isInstalled) {
                drawSubGroup(mesh, model, vp)
            } else {
                // Exploded preview of not-yet-installed part
                drawGhostSubGroup(mesh, model, vp, floatArrayOf(0.95f, 0.75f, 0.15f), 0.55f)
            }
        }

        // Active manipulated micro-part + Ghost target guide + Physics state glow
        val active = ms.activeMicro
        if (active != null) {
            val spec = MicroPartCatalog.findPart(active.partId)
            if (spec != null) {
                val mesh = microMeshes.getMicroMesh(spec.id)
                val isFlashingErr = nowMs < errorFlashUntilMs
                val isColliding = nowMs < state.physics.collisionHighlightUntilMs
                val pulse = 0.28f + 0.18f * sin(nowMs * 0.009f)
                val guideCol = when {
                    isFlashingErr -> floatArrayOf(1.0f, 0.18f, 0.18f)
                    state.physics.isSnapping || active.isAligned -> floatArrayOf(0.12f, 0.96f, 0.45f)
                    else -> floatArrayOf(0.22f, 0.80f, 1.0f)
                }
                drawGhostSubGroup(mesh, Mat4.trs(spec.targetPos, spec.targetRot), vp, guideCol, pulse)

                var activeScale = Vec3(1f, 1f, 1f)
                if (spec.id == "mp_valve_spring_in") {
                    activeScale = Vec3(1f, 1f - 0.36f * ms.valveSpringCompressed, 1f)
                }
                val activeModel = Mat4.trs(active.position, active.rotationDeg, activeScale)
                if (isColliding) {
                    drawGhostSubGroup(mesh, activeModel, vp, floatArrayOf(1.0f, 0.52f, 0.12f), 0.72f)
                } else {
                    drawSubGroup(mesh, activeModel, vp)
                }
            }
        }
    }

    private fun renderWorkshopWorld(state: GameUiState, vp: FloatArray, nowMs: Long) {
        // 1. Workshop Bay
        drawSubGroup(envMeshes.workshopScene, Mat4.identity(), vp)

        // 2. Installed parts (plus subtle Level 10 test animations if engine/hydraulics tested!)
        val comm = state.commissioning
        val engineVibration = if (comm.engineStarted) sin(nowMs * 0.07f) * 0.004f else 0f
        val fanSpin = if (comm.engineStarted) (nowMs * 0.6f) % 360f else 0f
        val steerTestDeg = if (comm.steeringTested && state.currentLevel == 10) sin(nowMs * 0.003f) * 18f else 0f
        val loaderTestDeg = if (comm.loaderTested && state.currentLevel == 10) 10f + sin(nowMs * 0.002f) * 10f else 0f

        for (part in JcbCatalog.parts) {
            if (part.id !in state.installedParts) continue
            val mesh = meshLib.getPartMesh(part.id)
            var pos = part.targetPos
            var rot = part.targetRot

            if (part.level == 2) {
                pos = pos + Vec3(0f, engineVibration, 0f)
                if (part.id == "l2_cooling_fan") {
                    rot = rot + Vec3(0f, 0f, fanSpin)
                }
            }
            if (part.id == "l5_front_wheels") {
                rot = rot + Vec3(0f, steerTestDeg, 0f)
            }
            if (part.id in listOf("l7_loader_arms", "l7_front_bucket", "l7_bucket_linkage", "l7_front_hyd_cylinders") && loaderTestDeg != 0f) {
                // Slight lift preview around loader shoulder (0, 1.48, 0.42)
                val pivot = Vec3(0f, 1.48f, 0.42f)
                val model = Mat4.multiply(
                    Mat4.translation(pivot.x, pivot.y, pivot.z),
                    Mat4.multiply(
                        Mat4.rotationX(-loaderTestDeg),
                        Mat4.translation(pos.x - pivot.x, pos.y - pivot.y, pos.z - pivot.z)
                    )
                )
                drawSubGroup(mesh, model, vp)
                continue
            }

            drawSubGroup(mesh, Mat4.trs(pos, rot), vp)
        }

        // 3. Active Manipulation Part + Ghost Fitting Guide + Bolts + Tool Animation
        val manip = state.activeManip
        if (manip != null) {
            val partSpec = JcbCatalog.findPart(manip.partId)
            if (partSpec != null) {
                val partMesh = meshLib.getPartMesh(partSpec.id)
                val isFlashingErr = nowMs < errorFlashUntilMs
                val isColliding = nowMs < state.physics.collisionHighlightUntilMs

                // Ghost guide at target
                if (!manip.placedInSocket) {
                    val pulse = 0.25f + 0.15f * sin(nowMs * 0.008f)
                    val guideColor = when {
                        isFlashingErr -> floatArrayOf(1.0f, 0.20f, 0.20f)
                        state.physics.isSnapping || manip.isAligned -> floatArrayOf(0.10f, 0.95f, 0.45f)
                        else -> floatArrayOf(0.22f, 0.78f, 1.0f)
                    }
                    drawGhostSubGroup(partMesh, Mat4.trs(partSpec.targetPos, partSpec.targetRot), vp, guideColor, pulse)
                }

                // Active part itself (amber highlight briefly on rigid-body collision)
                val activeModel = Mat4.trs(manip.position, manip.rotationDeg)
                if (isColliding && !manip.placedInSocket) {
                    drawGhostSubGroup(partMesh, activeModel, vp, floatArrayOf(1.0f, 0.52f, 0.12f), 0.78f)
                } else {
                    drawSubGroup(partMesh, activeModel, vp)
                }

                // Bolts around placed part
                if (manip.placedInSocket) {
                    val boltsTotal = manip.totalBolts.coerceAtLeast(1)
                    for (b in 0 until boltsTotal) {
                        val isDone = b < manip.boltsSecured
                        val angle = (b.toFloat() / boltsTotal) * (2f * PI.toFloat())
                        val bx = partSpec.targetPos.x + cos(angle) * 0.22f
                        val bz = partSpec.targetPos.z + sin(angle) * 0.22f
                        val lift = if (isDone) 0.0f else 0.14f
                        val by = partSpec.targetPos.y + 0.16f + lift
                        val spin = if (b == manip.boltsSecured && manip.animatingTool != null) manip.toolAnimProgress * 360f else 0f
                        drawSubGroup(meshLib.boltMesh, Mat4.trs(Vec3(bx, by - manip.toolAnimProgress * (if (b == manip.boltsSecured) 0.14f else 0f), bz), Vec3(0f, spin, 0f)), vp)
                    }
                }

                // Animated 3D Tool
                val animTool = manip.animatingTool
                if (animTool != null) {
                    val toolMesh = meshLib.getToolMesh(animTool)
                    val p = manip.toolAnimProgress
                    val toolPos = manip.position + Vec3(0.15f, 0.32f + sin(p * PI.toFloat() * 2f) * 0.12f, 0f)
                    val toolRot = Vec3(sin(p * PI.toFloat() * 4f) * 28f, p * 180f, 0f)
                    drawSubGroup(toolMesh, Mat4.trs(toolPos, toolRot), vp)
                }
            }
        }
    }

    private fun renderDrivingWorld(state: GameUiState, vp: FloatArray, nowMs: Long) {
        // 1. Yard base + soil mound + dump truck
        drawSubGroup(envMeshes.constructionYardBase, Mat4.identity(), vp)
        drawSubGroup(envMeshes.soilMoundMesh, Mat4.trs(Vec3(7f, 0f, 7f), Vec3()), vp)
        drawSubGroup(envMeshes.dumpTruckMesh, Mat4.trs(Vec3(11f, 0f, -2f), Vec3(0f, 0f, 0f)), vp)

        // Render rising soil load inside truck bed
        val d = state.driving
        if (d.truckLoadProgress > 0.02f) {
            val h = d.truckLoadProgress.coerceIn(0.05f, 1f)
            drawSubGroup(
                envMeshes.frontBucketSoilMesh,
                Mat4.trs(Vec3(11f, 1.85f + h * 0.35f, -2.85f), Vec3(), Vec3(1.0f, h * 2.2f, 4.2f)),
                vp
            )
        }

        // 2. Full Articulated JCB 4CX Vehicle Transform
        val bodyBase = Mat4.trs(Vec3(d.posX, 0f, d.posZ), Vec3(0f, d.headingDeg, 0f))
        val steerFront = d.steerInput * 28f
        val steerRear = if (d.is4Ws) -d.steerInput * 22f else 0f

        for (part in JcbCatalog.parts) {
            if (part.id !in state.installedParts && !state.sandboxDrivingUnlocked) continue
            val mesh = meshLib.getPartMesh(part.id)
            val model = computeDrivingPartMatrix(part.id, part.targetPos, part.targetRot, bodyBase, d, steerFront, steerRear, nowMs)
            drawSubGroup(mesh, model, vp)
        }

        // Soil inside front bucket
        if (d.frontBucketSoil > 0.05f) {
            val loaderMat = computeLoaderArmMatrix(bodyBase, d)
            val bucketMat = computeFrontBucketMatrix(loaderMat, d)
            drawSubGroup(envMeshes.frontBucketSoilMesh, bucketMat, vp)
        }
    }

    private fun computeLoaderArmMatrix(bodyBase: FloatArray, d: DrivingSimState): FloatArray {
        val shoulder = Vec3(0f, 1.48f, 0.42f)
        return Mat4.multiply(
            bodyBase,
            Mat4.multiply(
                Mat4.translation(shoulder.x, shoulder.y, shoulder.z),
                Mat4.rotationX(-d.loaderBoomDeg)
            )
        )
    }

    private fun computeFrontBucketMatrix(loaderMat: FloatArray, d: DrivingSimState): FloatArray {
        val bucketHingeRel = Vec3(0f, -1.04f, 2.06f) // relative to shoulder (0, 1.48, 0.42) -> (0, 0.44, 2.48)
        return Mat4.multiply(
            loaderMat,
            Mat4.multiply(
                Mat4.translation(bucketHingeRel.x, bucketHingeRel.y, bucketHingeRel.z),
                Mat4.rotationX(-d.loaderBucketDeg)
            )
        )
    }

    private fun computeDrivingPartMatrix(
        partId: String,
        targetPos: Vec3,
        targetRot: Vec3,
        bodyBase: FloatArray,
        d: DrivingSimState,
        steerFront: Float,
        steerRear: Float,
        nowMs: Long
    ): FloatArray {
        val vib = if (d.engineRunning && partId.startsWith("l2_")) sin(nowMs * 0.08f) * 0.004f else 0f
        if (partId == "l2_cooling_fan" && d.engineRunning) {
            return Mat4.multiply(bodyBase, Mat4.trs(targetPos, targetRot + Vec3(0f, 0f, (nowMs * 0.8f) % 360f)))
        }

        // Front & Rear equal wheels (spinning + 4WS steering!)
        if (partId == "l5_front_wheels") {
            return Mat4.multiply(bodyBase, Mat4.trs(targetPos, Vec3(d.wheelSpinDeg, steerFront, 0f)))
        }
        if (partId == "l5_rear_wheels") {
            return Mat4.multiply(bodyBase, Mat4.trs(targetPos, Vec3(d.wheelSpinDeg, steerRear, 0f)))
        }

        // Stabilizer legs extend down
        if (partId == "l8_stabilizer_legs") {
            val drop = d.stabilizerExt * 0.56f
            return Mat4.multiply(bodyBase, Mat4.trs(targetPos - Vec3(0f, drop, 0f), targetRot))
        }

        // Front Loader articulated sub-chain
        if (partId in listOf("l7_loader_arms", "l7_front_hyd_cylinders", "l7_bucket_linkage", "l7_pins", "l7_locking_clips")) {
            val shoulder = Vec3(0f, 1.48f, 0.42f)
            val loaderMat = computeLoaderArmMatrix(bodyBase, d)
            return Mat4.multiply(loaderMat, Mat4.trs(targetPos - shoulder, targetRot))
        }
        if (partId == "l7_front_bucket") {
            val loaderMat = computeLoaderArmMatrix(bodyBase, d)
            return computeFrontBucketMatrix(loaderMat, d)
        }

        // Rear Backhoe articulated kinematic chain: Kingpost -> Boom -> Dipper -> Bucket
        if (partId in listOf("l8_swing_frame", "l8_boom", "l8_dipper_arm", "l8_excavator_bucket", "l8_rear_hyd_cylinders", "l8_pins_hyd_conn")) {
            val kingPivot = Vec3(0f, 0.92f, -1.78f)
            val kingMat = Mat4.multiply(bodyBase, Mat4.multiply(Mat4.translation(kingPivot.x, kingPivot.y, kingPivot.z), Mat4.rotationY(d.rearSwingDeg)))
            if (partId == "l8_swing_frame") {
                return Mat4.multiply(kingMat, Mat4.trs(targetPos - kingPivot, targetRot))
            }

            // Boom pivots around kingpost (offset relative to kingpost)
            val boomDelta = d.rearBoomDeg - 38f
            val boomPivotRel = Vec3(0f, 0.35f, -0.15f)
            val boomMat = Mat4.multiply(kingMat, Mat4.multiply(Mat4.translation(boomPivotRel.x, boomPivotRel.y, boomPivotRel.z), Mat4.rotationX(boomDelta)))
            if (partId == "l8_boom" || partId == "l8_pins_hyd_conn") {
                return Mat4.multiply(boomMat, Mat4.trs(targetPos - (kingPivot + boomPivotRel), targetRot))
            }

            // Dipper pivots at boom tip
            val dipperPivotRel = Vec3(0f, 1.10f, -0.45f)
            val dipperDelta = d.rearDipperDeg + 55f
            val dipperMat = Mat4.multiply(boomMat, Mat4.multiply(Mat4.translation(dipperPivotRel.x, dipperPivotRel.y, dipperPivotRel.z), Mat4.rotationX(dipperDelta)))
            if (partId == "l8_dipper_arm" || partId == "l8_rear_hyd_cylinders") {
                return Mat4.multiply(dipperMat, Mat4.trs(targetPos - (kingPivot + boomPivotRel + dipperPivotRel), targetRot))
            }

            // Excavator Bucket curls at dipper tip
            val bucketPivotRel = Vec3(0f, -1.15f, -0.08f)
            val bucketDelta = d.rearBucketDeg + 35f
            val excMat = Mat4.multiply(dipperMat, Mat4.multiply(Mat4.translation(bucketPivotRel.x, bucketPivotRel.y, bucketPivotRel.z), Mat4.rotationX(bucketDelta)))
            return Mat4.multiply(excMat, Mat4.trs(targetPos - (kingPivot + boomPivotRel + dipperPivotRel + bucketPivotRel), targetRot))
        }

        return Mat4.multiply(bodyBase, Mat4.trs(targetPos + Vec3(0f, vib, 0f), targetRot))
    }

    private fun drawSubGroup(group: MeshSubGroup, model: FloatArray, vp: FloatArray) {
        val mvp = Mat4.multiply(vp, model)
        GLES30.glUniformMatrix4fv(uMvpLoc, 1, false, mvp, 0)
        GLES30.glUniformMatrix4fv(uModelLoc, 1, false, model, 0)
        GLES30.glUniform1i(uUseOverrideLoc, 0)

        for (mesh in group.meshes) {
            if (mesh.style.alpha < 0.99f) GLES30.glDepthMask(false)
            GLES30.glUniform1f(uSpecularLoc, mesh.style.specular)
            GLES30.glUniform1f(uShininessLoc, mesh.style.shininess)
            GLES30.glUniform1f(uAlphaLoc, mesh.style.alpha)
            GLES30.glUniform1f(uEmissiveLoc, mesh.style.emissive)
            bindAndDraw(mesh)
            if (mesh.style.alpha < 0.99f) GLES30.glDepthMask(true)
        }
    }

    private fun drawGhostSubGroup(group: MeshSubGroup, model: FloatArray, vp: FloatArray, rgb: FloatArray, alpha: Float) {
        val mvp = Mat4.multiply(vp, model)
        GLES30.glUniformMatrix4fv(uMvpLoc, 1, false, mvp, 0)
        GLES30.glUniformMatrix4fv(uModelLoc, 1, false, model, 0)
        GLES30.glUniform1i(uUseOverrideLoc, 1)
        GLES30.glUniform3f(uOverrideColorLoc, rgb[0], rgb[1], rgb[2])
        GLES30.glUniform1f(uSpecularLoc, 0.4f)
        GLES30.glUniform1f(uShininessLoc, 24f)
        GLES30.glUniform1f(uAlphaLoc, alpha)
        GLES30.glUniform1f(uEmissiveLoc, 0.55f)
        GLES30.glDepthMask(false)
        for (mesh in group.meshes) {
            bindAndDraw(mesh)
        }
        GLES30.glDepthMask(true)
    }

    private fun bindAndDraw(mesh: MeshData) {
        val stride = 9 * 4
        mesh.vertexBuffer.position(0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, mesh.vertexBuffer)

        mesh.vertexBuffer.position(3)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, mesh.vertexBuffer)

        mesh.vertexBuffer.position(6)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 3, GLES30.GL_FLOAT, false, stride, mesh.vertexBuffer)

        mesh.indexBuffer.position(0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mesh.indexCount, GLES30.GL_UNSIGNED_SHORT, mesh.indexBuffer)
    }

    private fun compileProgram(vsSrc: String, fsSrc: String): Int {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER, vsSrc)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fsSrc)
        val prog = GLES30.glCreateProgram()
        GLES30.glAttachShader(prog, vs)
        GLES30.glAttachShader(prog, fs)
        GLES30.glLinkProgram(prog)
        return prog
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, src)
        GLES30.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """#version 300 es
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 aColor;

uniform mat4 uMVP;
uniform mat4 uModel;

out vec3 vWorldPos;
out vec3 vNormal;
out vec3 vColor;

void main() {
    vec4 wp = uModel * vec4(aPos, 1.0);
    vWorldPos = wp.xyz;
    vNormal = normalize(mat3(uModel) * aNormal);
    vColor = aColor;
    gl_Position = uMVP * vec4(aPos, 1.0);
}
"""

        private const val FRAGMENT_SHADER = """#version 300 es
precision mediump float;

in vec3 vWorldPos;
in vec3 vNormal;
in vec3 vColor;

uniform vec3 uViewPos;
uniform float uSpecular;
uniform float uShininess;
uniform float uAlpha;
uniform float uEmissive;
uniform vec3 uOverrideColor;
uniform int uUseOverride;

out vec4 fragColor;

void main() {
    vec3 baseCol = (uUseOverride == 1) ? uOverrideColor : vColor;
    vec3 N = normalize(vNormal);
    vec3 V = normalize(uViewPos - vWorldPos);

    // Key warm overhead workshop / sun light
    vec3 L1 = normalize(vec3(0.45, 0.85, 0.55));
    float diff1 = max(dot(N, L1), 0.0);
    vec3 H1 = normalize(L1 + V);
    float spec1 = pow(max(dot(N, H1), 0.0), uShininess) * uSpecular;

    // Cool fill light from opposite side
    vec3 L2 = normalize(vec3(-0.6, 0.4, -0.5));
    float diff2 = max(dot(N, L2), 0.0) * 0.35;

    // Hemisphere ambient
    float hemi = 0.24 + 0.14 * (N.y * 0.5 + 0.5);
    // Fresnel rim highlight for metallic depth
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0) * uSpecular * 0.25;

    vec3 lit = baseCol * (hemi + diff1 * 0.88 + diff2) + vec3(1.0, 0.98, 0.92) * spec1 + baseCol * uEmissive + vec3(fresnel);
    fragColor = vec4(lit, uAlpha);
}
"""
    }
}

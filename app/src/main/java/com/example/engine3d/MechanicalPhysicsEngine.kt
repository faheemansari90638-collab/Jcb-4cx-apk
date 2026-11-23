package com.example.engine3d

import com.example.model.BiText
import com.example.model.JcbCatalog
import com.example.model.JcbPartSpec
import com.example.model.MicroCategory
import com.example.model.MicroPartCatalog
import com.example.model.MicroPartSpec
import kotlin.math.abs

data class PhysicsBodyConfig(
    val halfExtents: Vec3,
    val massKg: Float,
    val restitution: Float = 0.24f,
    val friction: Float = 0.72f,
    val snapRadius: Float = 0.34f,
    val snapAngleDeg: Float = 28f
)

data class StaticObstacle(
    val id: String,
    val name: BiText,
    val center: Vec3,
    val halfExtents: Vec3,
    val targetOverlapAllowed: Boolean = false
)

data class CollisionContact(
    val otherId: String,
    val otherName: BiText,
    val normal: Vec3,
    val penetrationDepthMm: Float,
    val impactSpeedMps: Float,
    val isFloor: Boolean
)

data class RigidBodyState(
    val position: Vec3,
    val velocity: Vec3 = Vec3(),
    val rotationDeg: Vec3 = Vec3(),
    val angularVelocityDeg: Vec3 = Vec3(),
    val isHeld: Boolean = true,
    val isSnapping: Boolean = false,
    val isGrounded: Boolean = false,
    val isSleeping: Boolean = false,
    val justSnappedToSeat: Boolean = false,
    val lastCollision: CollisionContact? = null
)

/**
 * Lightweight 3D rigid-body physics engine tailored for Android Jetpack Compose + OpenGL ES.
 * Manages AABB collision impulse resolution, 9.81 m/s² gravity, floor/stand support,
 * and damped spring-damper magnetic assembly snapping.
 */
object MechanicalPhysicsEngine {

    const val GRAVITY_MPS2 = 9.81f

    fun configForJcbPart(part: JcbPartSpec): PhysicsBodyConfig {
        val id = part.id
        return when {
            id == "l1_main_chassis" -> PhysicsBodyConfig(Vec3(0.68f, 0.24f, 1.55f), massKg = 920f, restitution = 0.14f, snapRadius = 0.38f)
            id.startsWith("l1_") -> PhysicsBodyConfig(Vec3(0.45f, 0.16f, 0.35f), massKg = 85f, restitution = 0.18f)
            id == "l2_engine_block" -> PhysicsBodyConfig(Vec3(0.32f, 0.34f, 0.44f), massKg = 420f, restitution = 0.15f)
            id == "l2_radiator" -> PhysicsBodyConfig(Vec3(0.36f, 0.34f, 0.12f), massKg = 34f, restitution = 0.22f)
            id == "l2_battery" -> PhysicsBodyConfig(Vec3(0.18f, 0.14f, 0.24f), massKg = 28f, restitution = 0.20f)
            id.startsWith("l2_") -> PhysicsBodyConfig(Vec3(0.18f, 0.16f, 0.20f), massKg = 14f, restitution = 0.25f)
            id.contains("wheel") -> PhysicsBodyConfig(Vec3(0.95f, 0.54f, 0.54f), massKg = 190f, restitution = 0.42f, friction = 0.85f)
            id.contains("axle") -> PhysicsBodyConfig(Vec3(0.88f, 0.18f, 0.20f), massKg = 260f, restitution = 0.16f)
            id == "l3_gearbox" || id == "l3_torque_converter" -> PhysicsBodyConfig(Vec3(0.28f, 0.24f, 0.36f), massKg = 185f, restitution = 0.18f)
            id == "l6_cabin_frame" -> PhysicsBodyConfig(Vec3(0.62f, 0.68f, 0.65f), massKg = 410f, restitution = 0.16f)
            id.startsWith("l6_") -> PhysicsBodyConfig(Vec3(0.30f, 0.25f, 0.30f), massKg = 24f, restitution = 0.26f)
            id == "l7_front_bucket" -> PhysicsBodyConfig(Vec3(0.88f, 0.34f, 0.38f), massKg = 340f, restitution = 0.18f)
            id == "l7_loader_arms" -> PhysicsBodyConfig(Vec3(0.75f, 0.36f, 0.92f), massKg = 310f, restitution = 0.18f)
            id == "l8_boom" || id == "l8_dipper_arm" -> PhysicsBodyConfig(Vec3(0.22f, 0.62f, 0.75f), massKg = 245f, restitution = 0.18f)
            id == "l8_excavator_bucket" -> PhysicsBodyConfig(Vec3(0.26f, 0.26f, 0.30f), massKg = 135f, restitution = 0.20f)
            id == "l9_counterweight" -> PhysicsBodyConfig(Vec3(0.55f, 0.28f, 0.22f), massKg = 380f, restitution = 0.12f)
            id == "l9_hood" -> PhysicsBodyConfig(Vec3(0.46f, 0.28f, 0.65f), massKg = 62f, restitution = 0.22f)
            else -> PhysicsBodyConfig(Vec3(0.24f, 0.20f, 0.28f), massKg = 22f, restitution = 0.25f)
        }
    }

    fun configForMicroPart(part: MicroPartSpec): PhysicsBodyConfig {
        val id = part.id
        return when {
            id == "mp_engine_block" -> PhysicsBodyConfig(Vec3(0.22f, 0.20f, 0.34f), massKg = 115f, restitution = 0.12f, snapRadius = 0.18f)
            id == "mp_cylinder_head" -> PhysicsBodyConfig(Vec3(0.20f, 0.10f, 0.32f), massKg = 48f, restitution = 0.14f, snapRadius = 0.18f)
            id == "mp_crankshaft" -> PhysicsBodyConfig(Vec3(0.10f, 0.10f, 0.34f), massKg = 38f, restitution = 0.16f, snapRadius = 0.16f)
            id == "mp_camshaft" -> PhysicsBodyConfig(Vec3(0.05f, 0.05f, 0.32f), massKg = 9.5f, restitution = 0.18f, snapRadius = 0.16f)
            id == "mp_piston" -> PhysicsBodyConfig(Vec3(0.06f, 0.10f, 0.06f), massKg = 2.4f, restitution = 0.22f, snapRadius = 0.16f)
            id.contains("ring") || id.contains("seal") || id.contains("gasket") -> PhysicsBodyConfig(
                halfExtents = Vec3(0.06f, 0.015f, 0.06f),
                massKg = 0.12f,
                restitution = 0.36f,
                friction = 0.80f,
                snapRadius = 0.15f
            )
            id.contains("collet") || id.contains("washer") || id.contains("nut") || id.contains("screw") || id.contains("bolt") -> PhysicsBodyConfig(
                halfExtents = Vec3(0.025f, 0.03f, 0.025f),
                massKg = 0.18f,
                restitution = 0.28f,
                snapRadius = 0.15f
            )
            id.contains("valve") || id.contains("injector") -> PhysicsBodyConfig(
                halfExtents = Vec3(0.035f, 0.07f, 0.035f),
                massKg = 0.45f,
                restitution = 0.24f,
                snapRadius = 0.15f
            )
            part.category == MicroCategory.HYDRAULIC_SPOOL -> PhysicsBodyConfig(
                halfExtents = Vec3(0.07f, 0.07f, 0.12f),
                massKg = 3.2f,
                restitution = 0.20f,
                snapRadius = 0.16f
            )
            else -> PhysicsBodyConfig(Vec3(0.08f, 0.06f, 0.08f), massKg = 1.8f, restitution = 0.22f, snapRadius = 0.16f)
        }
    }

    fun buildJcbObstacles(
        installedIds: Set<String>,
        activeSpec: JcbPartSpec,
        activeConfig: PhysicsBodyConfig
    ): List<StaticObstacle> {
        return JcbCatalog.parts
            .filter { it.id in installedIds && it.id != activeSpec.id }
            .map { obs ->
                val obsCfg = configForJcbPart(obs)
                val isPrereq = obs.id in activeSpec.prerequisites
                val overlapsTarget = aabbOverlaps(activeSpec.targetPos, activeConfig.halfExtents, obs.targetPos, obsCfg.halfExtents)
                StaticObstacle(
                    id = obs.id,
                    name = obs.name,
                    center = obs.targetPos,
                    halfExtents = obsCfg.halfExtents,
                    targetOverlapAllowed = isPrereq || overlapsTarget
                )
            }
    }

    fun buildMicroObstacles(
        installedIds: Set<String>,
        activeSpec: MicroPartSpec,
        activeConfig: PhysicsBodyConfig
    ): List<StaticObstacle> {
        return MicroPartCatalog.parts
            .filter { it.id in installedIds && it.id != activeSpec.id }
            .map { obs ->
                val obsCfg = configForMicroPart(obs)
                val isPrereq = obs.id in activeSpec.prerequisites
                val overlapsTarget = aabbOverlaps(activeSpec.targetPos, activeConfig.halfExtents, obs.targetPos, obsCfg.halfExtents)
                StaticObstacle(
                    id = obs.id,
                    name = obs.name,
                    center = obs.targetPos,
                    halfExtents = obsCfg.halfExtents,
                    targetOverlapAllowed = isPrereq || overlapsTarget
                )
            }
    }

    /**
     * Resolves a manual button nudge against static obstacles & floor, and detects entry into snap zone.
     */
    fun applyManualNudge(
        body: RigidBodyState,
        delta: Vec3,
        config: PhysicsBodyConfig,
        targetPos: Vec3,
        targetRot: Vec3,
        obstacles: List<StaticObstacle>,
        floorY: Float,
        boundsMin: Vec3,
        boundsMax: Vec3,
        snapAssistEnabled: Boolean
    ): RigidBodyState {
        val subSteps = 4
        val stepDelta = delta * (1f / subSteps)
        var cur = body.copy(
            velocity = Vec3(),
            angularVelocityDeg = Vec3(),
            isSleeping = false,
            justSnappedToSeat = false,
            lastCollision = null
        )
        repeat(subSteps) {
            val candidate = Vec3(
                (cur.position.x + stepDelta.x).coerceIn(boundsMin.x, boundsMax.x),
                (cur.position.y + stepDelta.y).coerceIn(floorY + config.halfExtents.y, boundsMax.y),
                (cur.position.z + stepDelta.z).coerceIn(boundsMin.z, boundsMax.z)
            )
            cur = resolveCollisionsAndSnapCheck(
                body = cur.copy(position = candidate),
                config = config,
                targetPos = targetPos,
                targetRot = targetRot,
                obstacles = obstacles,
                floorY = floorY,
                snapAssistEnabled = snapAssistEnabled,
                impactVelocityHint = delta * 8f
            )
        }
        return cur
    }

    /**
     * Advances rigid body physics by dtSec (2x sub-stepped) with gravity, AABB collision response,
     * floor contact, and critically-damped spring-damper magnetic snapping.
     */
    fun step(
        dtSec: Float,
        body: RigidBodyState,
        config: PhysicsBodyConfig,
        targetPos: Vec3,
        targetRot: Vec3,
        obstacles: List<StaticObstacle>,
        floorY: Float,
        boundsMin: Vec3,
        boundsMax: Vec3,
        gravityEnabled: Boolean,
        snapAssistEnabled: Boolean,
        seatPosTolerance: Float,
        seatAngleToleranceDeg: Float
    ): RigidBodyState {
        val clampedDt = dtSec.coerceIn(0.001f, 0.033f)
        val subDt = clampedDt * 0.5f
        var cur = body.copy(justSnappedToSeat = false)

        repeat(2) {
            val dist = cur.position.distanceTo(targetPos)
            val rotErr = maxAngleDiffDeg(cur.rotationDeg, targetRot)
            val inSnapEnvelope = snapAssistEnabled && dist <= config.snapRadius && rotErr <= config.snapAngleDeg

            if (inSnapEnvelope) {
                // Critically damped 3D spring-damper pulling part into mechanical socket
                val k = 115f
                val c = 21f
                val disp = targetPos - cur.position
                val accel = (disp * k) - (cur.velocity * c)
                var nextVel = cur.velocity + accel * subDt
                var nextPos = cur.position + nextVel * subDt

                val dxRot = shortestSignedAngleDeg(cur.rotationDeg.x, targetRot.x)
                val dyRot = shortestSignedAngleDeg(cur.rotationDeg.y, targetRot.y)
                val dzRot = shortestSignedAngleDeg(cur.rotationDeg.z, targetRot.z)
                val rotGain = (14f * subDt).coerceAtMost(1f)
                var nextRot = Vec3(
                    wrap360(cur.rotationDeg.x + dxRot * rotGain),
                    wrap360(cur.rotationDeg.y + dyRot * rotGain),
                    wrap360(cur.rotationDeg.z + dzRot * rotGain)
                )

                val postDist = nextPos.distanceTo(targetPos)
                val postRotErr = maxAngleDiffDeg(nextRot, targetRot)
                if (postDist <= seatPosTolerance && postRotErr <= seatAngleToleranceDeg && nextVel.length() <= 0.18f) {
                    nextPos = targetPos
                    nextRot = targetRot
                    nextVel = Vec3()
                    return cur.copy(
                        position = nextPos,
                        velocity = Vec3(),
                        rotationDeg = nextRot,
                        angularVelocityDeg = Vec3(),
                        isSnapping = false,
                        isSleeping = true,
                        justSnappedToSeat = true
                    )
                }

                cur = cur.copy(
                    position = nextPos,
                    velocity = nextVel,
                    rotationDeg = nextRot,
                    angularVelocityDeg = Vec3(),
                    isSnapping = true,
                    isSleeping = false
                )
            } else {
                // Held vs Free-Fall under 9.81 m/s² gravity
                if (cur.isHeld || cur.isSleeping || !gravityEnabled) {
                    cur = cur.copy(isSnapping = false)
                } else {
                    var nextVel = Vec3(
                        cur.velocity.x * (1f - 0.35f * subDt).coerceAtLeast(0f),
                        cur.velocity.y - GRAVITY_MPS2 * subDt,
                        cur.velocity.z * (1f - 0.35f * subDt).coerceAtLeast(0f)
                    )
                    var nextPos = cur.position + nextVel * subDt
                    var nextRot = Vec3(
                        wrap360(cur.rotationDeg.x + cur.angularVelocityDeg.x * subDt),
                        wrap360(cur.rotationDeg.y + cur.angularVelocityDeg.y * subDt),
                        wrap360(cur.rotationDeg.z + cur.angularVelocityDeg.z * subDt)
                    )

                    // Clamp bounds
                    if (nextPos.x < boundsMin.x || nextPos.x > boundsMax.x) {
                        nextPos = nextPos.copy(x = nextPos.x.coerceIn(boundsMin.x, boundsMax.x))
                        nextVel = nextVel.copy(x = -nextVel.x * config.restitution)
                    }
                    if (nextPos.z < boundsMin.z || nextPos.z > boundsMax.z) {
                        nextPos = nextPos.copy(z = nextPos.z.coerceIn(boundsMin.z, boundsMax.z))
                        nextVel = nextVel.copy(z = -nextVel.z * config.restitution)
                    }
                    if (nextPos.y > boundsMax.y) {
                        nextPos = nextPos.copy(y = boundsMax.y)
                        nextVel = nextVel.copy(y = 0f)
                    }

                    cur = resolveCollisionsAndSnapCheck(
                        body = cur.copy(
                            position = nextPos,
                            velocity = nextVel,
                            rotationDeg = nextRot,
                            isSnapping = false
                        ),
                        config = config,
                        targetPos = targetPos,
                        targetRot = targetRot,
                        obstacles = obstacles,
                        floorY = floorY,
                        snapAssistEnabled = snapAssistEnabled,
                        impactVelocityHint = nextVel
                    )

                    if (cur.isGrounded && cur.velocity.length() < 0.045f && cur.angularVelocityDeg.length() < 1.5f) {
                        cur = cur.copy(velocity = Vec3(), angularVelocityDeg = Vec3(), isSleeping = true)
                    }
                }
            }
        }
        return cur
    }

    private fun resolveCollisionsAndSnapCheck(
        body: RigidBodyState,
        config: PhysicsBodyConfig,
        targetPos: Vec3,
        targetRot: Vec3,
        obstacles: List<StaticObstacle>,
        floorY: Float,
        snapAssistEnabled: Boolean,
        impactVelocityHint: Vec3
    ): RigidBodyState {
        var pos = body.position
        var vel = body.velocity
        var angVel = body.angularVelocityDeg
        var grounded = false
        var strongestContact: CollisionContact? = null

        val minY = floorY + config.halfExtents.y
        if (pos.y <= minY) {
            val pen = minY - pos.y
            pos = pos.copy(y = minY)
            grounded = true
            val impactDown = abs(minOf(vel.y, impactVelocityHint.y))
            if (vel.y < 0f) {
                vel = vel.copy(
                    x = vel.x * (1f - config.friction * 0.4f).coerceAtLeast(0f),
                    y = if (impactDown > 0.45f) -vel.y * config.restitution else 0f,
                    z = vel.z * (1f - config.friction * 0.4f).coerceAtLeast(0f)
                )
                angVel = angVel * 0.5f
            }
            if (pen > 0.002f || impactDown > 0.45f) {
                strongestContact = CollisionContact(
                    otherId = "env_floor",
                    otherName = BiText("Workshop Floor / Stand", "वर्कशॉप फर्श / स्टैंड"),
                    normal = Vec3(0f, 1f, 0f),
                    penetrationDepthMm = pen * 1000f,
                    impactSpeedMps = impactDown,
                    isFloor = true
                )
            }
        }

        val distTarget = pos.distanceTo(targetPos)
        val rotErr = maxAngleDiffDeg(body.rotationDeg, targetRot)
        val nearTargetMatingZone = distTarget <= config.snapRadius * 1.45f && rotErr <= config.snapAngleDeg * 1.25f

        for (obs in obstacles) {
            if (obs.targetOverlapAllowed && (body.isSnapping || nearTargetMatingZone)) continue

            val dx = pos.x - obs.center.x
            val dy = pos.y - obs.center.y
            val dz = pos.z - obs.center.z
            val ox = (config.halfExtents.x + obs.halfExtents.x) - abs(dx)
            val oy = (config.halfExtents.y + obs.halfExtents.y) - abs(dy)
            val oz = (config.halfExtents.z + obs.halfExtents.z) - abs(dz)

            if (ox > 0f && oy > 0f && oz > 0f) {
                val normal: Vec3
                val pen: Float
                when {
                    oy <= ox && oy <= oz -> {
                        pen = oy
                        normal = Vec3(0f, if (dy >= 0f) 1f else -1f, 0f)
                    }
                    ox <= oz -> {
                        pen = ox
                        normal = Vec3(if (dx >= 0f) 1f else -1f, 0f, 0f)
                    }
                    else -> {
                        pen = oz
                        normal = Vec3(0f, 0f, if (dz >= 0f) 1f else -1f)
                    }
                }

                pos += normal * pen
                if (normal.y > 0.7f) grounded = true

                val vn = vel.dot(normal)
                val hintVn = impactVelocityHint.dot(normal)
                val impactSpeed = maxOf(if (vn < 0f) -vn else 0f, if (hintVn < 0f) -hintVn else 0f)

                if (vn < 0f) {
                    vel -= normal * ((1f + config.restitution) * vn)
                    // Tangential friction
                    val tangentVel = vel - normal * vel.dot(normal)
                    vel -= tangentVel * (config.friction * 0.25f)
                    if (impactSpeed > 0.6f && abs(normal.y) > 0.5f) {
                        angVel = Vec3(
                            (dx * 35f).coerceIn(-45f, 45f),
                            angVel.y * 0.7f,
                            (-dz * 35f).coerceIn(-45f, 45f)
                        )
                    }
                }

                if (strongestContact == null || impactSpeed > strongestContact.impactSpeedMps) {
                    strongestContact = CollisionContact(
                        otherId = obs.id,
                        otherName = obs.name,
                        normal = normal,
                        penetrationDepthMm = pen * 1000f,
                        impactSpeedMps = impactSpeed,
                        isFloor = false
                    )
                }
            }
        }

        val finalDist = pos.distanceTo(targetPos)
        val snapReady = snapAssistEnabled && finalDist <= config.snapRadius && rotErr <= config.snapAngleDeg

        return body.copy(
            position = pos,
            velocity = vel,
            angularVelocityDeg = angVel,
            isGrounded = grounded,
            isSnapping = snapReady || body.isSnapping,
            lastCollision = strongestContact ?: body.lastCollision
        )
    }

    fun aabbOverlaps(c1: Vec3, h1: Vec3, c2: Vec3, h2: Vec3): Boolean {
        return abs(c1.x - c2.x) < (h1.x + h2.x) &&
            abs(c1.y - c2.y) < (h1.y + h2.y) &&
            abs(c1.z - c2.z) < (h1.z + h2.z)
    }

    fun maxAngleDiffDeg(a: Vec3, b: Vec3): Float = maxOf(
        Mat4.angleDiffDeg(a.x, b.x),
        Mat4.angleDiffDeg(a.y, b.y),
        Mat4.angleDiffDeg(a.z, b.z)
    )

    fun shortestSignedAngleDeg(current: Float, target: Float): Float {
        var diff = ((target - current) % 360f + 540f) % 360f - 180f
        if (diff < -180f) diff += 360f
        return diff
    }

    private fun wrap360(deg: Float): Float {
        val m = deg % 360f
        return if (m < 0f) m + 360f else m
    }
}

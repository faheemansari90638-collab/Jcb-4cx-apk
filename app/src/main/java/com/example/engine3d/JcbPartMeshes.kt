package com.example.engine3d

import com.example.model.ControlLayout
import com.example.model.GraphicsQuality
import com.example.model.ToolType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MeshLibrary(quality: GraphicsQuality, val controlLayout: ControlLayout) {
    private val seg = quality.circleSegments
    private val partCache = HashMap<String, MeshSubGroup>()
    private val toolCache = HashMap<ToolType, MeshSubGroup>()
    val boltMesh: MeshSubGroup = buildSingleBolt()

    // Signature JCB & Industrial Colors
    private val yR = 0.95f; private val yG = 0.71f; private val yB = 0.02f
    private val blkR = 0.12f; private val blkG = 0.13f; private val blkB = 0.16f
    private val ironR = 0.34f; private val ironG = 0.37f; private val ironB = 0.42f
    private val chrR = 0.84f; private val chrG = 0.88f; private val chrB = 0.93f
    private val brsR = 0.80f; private val brsG = 0.58f; private val brsB = 0.22f
    private val rubR = 0.09f; private val rubG = 0.10f; private val rubB = 0.11f

    fun getPartMesh(partId: String): MeshSubGroup {
        return partCache.getOrPut(partId) { buildPartMesh(partId) }
    }

    fun getToolMesh(tool: ToolType): MeshSubGroup {
        return toolCache.getOrPut(tool) { buildToolMesh(tool) }
    }

    private fun buildSingleBolt(): MeshSubGroup {
        val m = MeshBuilder(8)
        m.addBolt(0f, 0f, 0f, 0.028f, 0.08f, 1)
        return MeshSubGroup(listOf(m.build(MaterialStyle.CHROME_METAL)))
    }

    private fun buildWheel(mbRubber: MeshBuilder, mbRim: MeshBuilder, mbHub: MeshBuilder, cx: Float, cy: Float, cz: Float) {
        // Outer tyre carcass
        mbRubber.addCylinder(cx, cy, cz, 0.62f, 0.62f, 0.40f, 0, rubR, rubG, rubB, seg)
        // Heavy agricultural tractor lugs around circumference
        val lugCount = (seg.coerceAtLeast(12))
        for (i in 0 until lugCount) {
            val a = (2.0 * PI * i / lugCount).toFloat()
            val ly = cy + cos(a) * 0.61f
            val lz = cz + sin(a) * 0.61f
            val deg = Math.toDegrees(a.toDouble()).toFloat()
            val sideOffset = if (i % 2 == 0) 0.09f else -0.09f
            mbRubber.addRotatedBox(cx + sideOffset, ly, lz, 0.18f, 0.055f, 0.085f, -deg, 0f, 0f, rubR, rubG, rubB)
        }
        // Bright JCB yellow dish rim
        mbRim.addCylinder(cx, cy, cz, 0.36f, 0.36f, 0.42f, 0, yR, yG, yB, seg)
        // Dark planetary hub center + lug bolts
        mbHub.addCylinder(cx, cy, cz, 0.15f, 0.15f, 0.46f, 0, blkR, blkG, blkB, seg)
    }

    private fun buildPartMesh(id: String): MeshSubGroup {
        val y = MeshBuilder(seg)
        val blk = MeshBuilder(seg)
        val iron = MeshBuilder(seg)
        val chr = MeshBuilder(seg)
        val brs = MeshBuilder(seg)
        val rub = MeshBuilder(seg)
        val glass = MeshBuilder(seg)
        val lamp = MeshBuilder(seg)

        when (id) {
            // ---------- LEVEL 1: CHASSIS ----------
            "l1_main_chassis" -> {
                // Twin heavy box-section side rails + crossmembers
                blk.addBox(-0.42f, 0f, 0f, 0.14f, 0.28f, 3.30f, blkR, blkG, blkB)
                blk.addBox(0.42f, 0f, 0f, 0.14f, 0.28f, 3.30f, blkR, blkG, blkB)
                blk.addBox(0f, -0.02f, 1.45f, 0.96f, 0.22f, 0.22f, blkR, blkG, blkB)
                blk.addBox(0f, -0.02f, 0.45f, 0.90f, 0.16f, 0.20f, blkR, blkG, blkB)
                blk.addBox(0f, 0.05f, -0.45f, 1.06f, 0.12f, 0.85f, blkR, blkG, blkB) // cab floor deck
                blk.addBox(0f, -0.02f, -1.45f, 0.98f, 0.24f, 0.24f, blkR, blkG, blkB)
                // Loader tower pedestals
                y.addBox(-0.46f, 0.32f, 0.42f, 0.12f, 0.56f, 0.32f, yR, yG, yB)
                y.addBox(0.46f, 0.32f, 0.42f, 0.12f, 0.56f, 0.32f, yR, yG, yB)
            }
            "l1_front_brackets" -> {
                blk.addBox(-0.42f, 0f, 0f, 0.16f, 0.32f, 0.30f, blkR, blkG, blkB)
                blk.addBox(0.42f, 0f, 0f, 0.16f, 0.32f, 0.30f, blkR, blkG, blkB)
                y.addBox(0f, -0.06f, 0.06f, 0.98f, 0.20f, 0.18f, yR, yG, yB)
                chr.addCylinder(0f, 0.04f, 0.08f, 0.04f, 0.04f, 0.98f, 0, chrR, chrG, chrB)
            }
            "l1_rear_brackets" -> {
                // PowerSlide hydraclamp twin rails
                blk.addBox(0f, 0.18f, 0f, 1.48f, 0.12f, 0.20f, blkR, blkG, blkB)
                blk.addBox(0f, -0.18f, 0f, 1.48f, 0.12f, 0.20f, blkR, blkG, blkB)
                y.addBox(-0.68f, 0f, 0f, 0.14f, 0.46f, 0.22f, yR, yG, yB)
                y.addBox(0.68f, 0f, 0f, 0.14f, 0.46f, 0.22f, yR, yG, yB)
            }
            "l1_engine_mounts" -> {
                iron.addBox(-0.30f, 0f, 0.25f, 0.14f, 0.10f, 0.18f, ironR, ironG, ironB)
                iron.addBox(0.30f, 0f, 0.25f, 0.14f, 0.10f, 0.18f, ironR, ironG, ironB)
                iron.addBox(-0.30f, 0f, -0.25f, 0.14f, 0.10f, 0.18f, ironR, ironG, ironB)
                iron.addBox(0.30f, 0f, -0.25f, 0.14f, 0.10f, 0.18f, ironR, ironG, ironB)
                rub.addCylinder(-0.30f, 0.06f, 0.25f, 0.05f, 0.05f, 0.05f, 1, rubR, rubG, rubB)
                rub.addCylinder(0.30f, 0.06f, 0.25f, 0.05f, 0.05f, 0.05f, 1, rubR, rubG, rubB)
                rub.addCylinder(-0.30f, 0.06f, -0.25f, 0.05f, 0.05f, 0.05f, 1, rubR, rubG, rubB)
                rub.addCylinder(0.30f, 0.06f, -0.25f, 0.05f, 0.05f, 0.05f, 1, rubR, rubG, rubB)
            }
            "l1_axle_mounts" -> {
                iron.addBox(0f, 0f, 1.11f, 0.72f, 0.14f, 0.24f, ironR, ironG, ironB)
                iron.addBox(0f, 0f, -1.11f, 0.84f, 0.14f, 0.24f, ironR, ironG, ironB)
                chr.addCylinder(0f, -0.02f, 1.11f, 0.045f, 0.045f, 0.34f, 2, chrR, chrG, chrB)
            }

            // ---------- LEVEL 2: ENGINE ----------
            "l2_engine_block" -> {
                iron.addBox(0f, 0f, 0f, 0.46f, 0.42f, 0.72f, ironR, ironG, ironB)
                blk.addBox(0f, -0.25f, 0f, 0.42f, 0.12f, 0.66f, blkR, blkG, blkB) // oil sump
                iron.addCylinder(0f, -0.05f, -0.38f, 0.22f, 0.22f, 0.08f, 2, ironR, ironG, ironB) // flywheel housing
            }
            "l2_pistons_crank" -> {
                chr.addCylinder(0f, -0.08f, 0f, 0.055f, 0.055f, 0.68f, 2, chrR, chrG, chrB)
                for (i in 0..3) {
                    val pz = -0.24f + i * 0.16f
                    chr.addCylinder(0f, 0.10f, pz, 0.055f, 0.055f, 0.11f, 1, chrR, chrG, chrB)
                    iron.addBox(0f, 0.01f, pz, 0.03f, 0.16f, 0.03f, ironR, ironG, ironB)
                }
            }
            "l2_cylinder_head" -> {
                iron.addBox(0f, 0f, 0f, 0.44f, 0.16f, 0.70f, ironR, ironG, ironB)
                blk.addBox(0f, 0.11f, 0f, 0.36f, 0.09f, 0.64f, blkR, blkG, blkB) // rocker cover
                for (i in 0..3) {
                    brs.addCylinder(-0.08f, 0.17f, -0.22f + i * 0.15f, 0.02f, 0.02f, 0.07f, 1, brsR, brsG, brsB)
                }
            }
            "l2_oil_filter" -> {
                y.addCylinder(0f, 0f, 0f, 0.058f, 0.058f, 0.18f, 1, yR, yG, yB)
                iron.addCylinder(0f, 0.10f, 0f, 0.064f, 0.064f, 0.04f, 1, ironR, ironG, ironB)
            }
            "l2_fuel_filter" -> {
                chr.addCylinder(0f, 0.03f, 0f, 0.052f, 0.052f, 0.16f, 1, chrR, chrG, chrB)
                glass.addCylinder(0f, -0.07f, 0f, 0.048f, 0.044f, 0.07f, 1, 0.85f, 0.65f, 0.25f)
            }
            "l2_air_filter" -> {
                blk.addCylinder(0f, 0f, 0f, 0.13f, 0.13f, 0.44f, 2, blkR, blkG, blkB)
                blk.addTube(listOf(Vec3(0f, 0f, 0.22f), Vec3(0.18f, -0.06f, 0.25f), Vec3(0.32f, -0.10f, 0.18f)), 0.042f, blkR, blkG, blkB)
            }
            "l2_turbocharger" -> {
                iron.addCylinder(0f, 0f, -0.06f, 0.11f, 0.11f, 0.09f, 2, ironR, ironG, ironB)
                chr.addCylinder(0f, 0f, 0.06f, 0.10f, 0.10f, 0.09f, 2, chrR, chrG, chrB)
                iron.addBox(0f, -0.04f, 0f, 0.08f, 0.08f, 0.18f, ironR, ironG, ironB)
            }
            "l2_radiator" -> {
                blk.addBox(0f, 0f, 0f, 0.64f, 0.54f, 0.12f, blkR, blkG, blkB)
                brs.addBox(0f, 0f, 0f, 0.56f, 0.46f, 0.13f, 0.55f, 0.58f, 0.62f)
                brs.addCylinder(0.22f, 0.30f, 0f, 0.04f, 0.04f, 0.05f, 1, brsR, brsG, brsB) // cap
            }
            "l2_cooling_fan" -> {
                blk.addCylinder(0f, 0f, 0f, 0.08f, 0.08f, 0.06f, 2, blkR, blkG, blkB)
                for (i in 0..7) {
                    val a = (i * 45f)
                    val rad = Math.toRadians(a.toDouble()).toFloat()
                    blk.addRotatedBox(cos(rad) * 0.14f, sin(rad) * 0.14f, 0f, 0.16f, 0.06f, 0.02f, 24f, 0f, a, blkR, blkG, blkB)
                }
            }
            "l2_alternator" -> {
                chr.addCylinder(0f, 0f, 0f, 0.085f, 0.085f, 0.18f, 2, chrR, chrG, chrB)
                iron.addCylinder(0f, 0f, 0.11f, 0.04f, 0.04f, 0.04f, 2, ironR, ironG, ironB)
                brs.addBox(0f, 0.09f, -0.04f, 0.04f, 0.04f, 0.04f, brsR, brsG, brsB)
            }
            "l2_belts_pulleys" -> {
                iron.addCylinder(0f, -0.10f, 0f, 0.09f, 0.09f, 0.04f, 2, ironR, ironG, ironB)
                iron.addCylinder(0f, 0.12f, 0f, 0.06f, 0.06f, 0.04f, 2, ironR, ironG, ironB)
                iron.addCylinder(0.26f, 0.06f, 0f, 0.04f, 0.04f, 0.04f, 2, ironR, ironG, ironB)
                rub.addTube(listOf(Vec3(0f, -0.17f, 0f), Vec3(-0.07f, 0.12f, 0f), Vec3(0.28f, 0.09f, 0f), Vec3(0.08f, -0.15f, 0f), Vec3(0f, -0.17f, 0f)), 0.015f, rubR, rubG, rubB)
            }
            "l2_battery" -> {
                blk.addBox(0f, 0f, 0f, 0.24f, 0.22f, 0.34f, blkR, blkG, blkB)
                lamp.addBox(0f, 0.12f, 0.10f, 0.05f, 0.03f, 0.05f, 0.9f, 0.15f, 0.1f) // + terminal
                chr.addBox(0f, 0.12f, -0.10f, 0.05f, 0.03f, 0.05f, chrR, chrG, chrB) // - terminal
            }

            // ---------- LEVEL 3: TRANSMISSION & AXLES ----------
            "l3_torque_converter" -> {
                iron.addCylinder(0f, 0f, 0f, 0.22f, 0.20f, 0.18f, 2, ironR, ironG, ironB)
                chr.addCylinder(0f, 0f, -0.12f, 0.06f, 0.06f, 0.12f, 2, chrR, chrG, chrB)
            }
            "l3_gearbox" -> {
                iron.addBox(0f, 0f, 0f, 0.42f, 0.36f, 0.54f, ironR, ironG, ironB)
                iron.addCylinder(0f, 0.04f, 0.30f, 0.18f, 0.18f, 0.12f, 2, ironR, ironG, ironB)
                blk.addBox(0f, -0.20f, -0.05f, 0.30f, 0.10f, 0.32f, blkR, blkG, blkB)
            }
            "l3_4wd_components" -> {
                iron.addBox(0f, 0f, 0f, 0.26f, 0.24f, 0.28f, ironR, ironG, ironB)
                chr.addCylinder(0f, -0.06f, 0f, 0.05f, 0.05f, 0.38f, 2, chrR, chrG, chrB)
            }
            "l3_front_axle" -> {
                blk.addCylinder(0f, 0f, 0f, 0.09f, 0.09f, 1.56f, 0, blkR, blkG, blkB)
                iron.addCylinder(0f, 0f, 0f, 0.18f, 0.18f, 0.24f, 2, ironR, ironG, ironB)
                iron.addBox(-0.78f, 0f, 0f, 0.14f, 0.18f, 0.18f, ironR, ironG, ironB)
                iron.addBox(0.78f, 0f, 0f, 0.14f, 0.18f, 0.18f, ironR, ironG, ironB)
            }
            "l3_rear_axle" -> {
                blk.addCylinder(0f, 0f, 0f, 0.10f, 0.10f, 1.56f, 0, blkR, blkG, blkB)
                iron.addBox(-0.78f, 0f, 0f, 0.15f, 0.19f, 0.19f, ironR, ironG, ironB)
                iron.addBox(0.78f, 0f, 0f, 0.15f, 0.19f, 0.19f, ironR, ironG, ironB)
            }
            "l3_differential" -> {
                iron.addCylinder(0f, 0f, 0f, 0.21f, 0.21f, 0.28f, 0, ironR, ironG, ironB)
                chr.addCylinder(0f, 0f, 0.16f, 0.07f, 0.07f, 0.12f, 2, chrR, chrG, chrB)
            }
            "l3_drive_shafts" -> {
                chr.addCylinder(0f, 0f, 0.55f, 0.042f, 0.042f, 0.92f, 2, chrR, chrG, chrB)
                chr.addCylinder(0f, 0f, -0.55f, 0.042f, 0.042f, 0.92f, 2, chrR, chrG, chrB)
                iron.addBox(0f, 0f, 0.12f, 0.09f, 0.09f, 0.09f, ironR, ironG, ironB)
                iron.addBox(0f, 0f, -0.12f, 0.09f, 0.09f, 0.09f, ironR, ironG, ironB)
            }
            "l3_trans_connections" -> {
                brs.addTube(listOf(Vec3(-0.14f, 0f, -0.1f), Vec3(-0.14f, 0.12f, 0.2f), Vec3(0.05f, 0.12f, 0.35f)), 0.018f, brsR, brsG, brsB)
                chr.addTube(listOf(Vec3(0.14f, 0f, -0.1f), Vec3(0.14f, 0.06f, 0.3f)), 0.018f, chrR, chrG, chrB)
            }

            // ---------- LEVEL 4: HYDRAULICS ----------
            "l4_hyd_pump" -> {
                iron.addBox(0f, 0f, 0f, 0.18f, 0.18f, 0.24f, ironR, ironG, ironB)
                brs.addCylinder(0.09f, 0.04f, 0f, 0.035f, 0.035f, 0.06f, 0, brsR, brsG, brsB)
            }
            "l4_hyd_tank" -> {
                blk.addBox(0f, 0f, 0f, 0.34f, 0.44f, 0.62f, blkR, blkG, blkB)
                chr.addCylinder(0f, 0.24f, 0.16f, 0.04f, 0.04f, 0.05f, 1, chrR, chrG, chrB)
                glass.addBox(-0.175f, 0f, 0.12f, 0.02f, 0.18f, 0.04f, 0.9f, 0.7f, 0.2f) // sight gauge
            }
            "l4_hyd_filters" -> {
                chr.addCylinder(0f, 0f, -0.09f, 0.06f, 0.06f, 0.22f, 1, chrR, chrG, chrB)
                chr.addCylinder(0f, 0f, 0.09f, 0.06f, 0.06f, 0.22f, 1, chrR, chrG, chrB)
                iron.addBox(0f, 0.12f, 0f, 0.15f, 0.06f, 0.30f, ironR, ironG, ironB)
            }
            "l4_main_valve" -> {
                iron.addBox(0f, 0f, 0f, 0.46f, 0.20f, 0.26f, ironR, ironG, ironB)
                for (i in -2..2) {
                    brs.addCylinder(i * 0.08f, 0.12f, 0f, 0.022f, 0.022f, 0.08f, 1, brsR, brsG, brsB)
                }
            }
            "l4_hyd_pipes" -> {
                chr.addTube(listOf(Vec3(-0.22f, 0f, 0.8f), Vec3(-0.22f, 0f, -0.8f)), 0.018f, chrR, chrG, chrB)
                chr.addTube(listOf(Vec3(0.22f, 0f, 0.8f), Vec3(0.22f, 0f, -0.8f)), 0.018f, chrR, chrG, chrB)
            }
            "l4_hyd_hoses" -> {
                rub.addTube(listOf(Vec3(-0.35f, 0f, 0.5f), Vec3(-0.25f, 0.14f, 0f), Vec3(-0.12f, -0.05f, -0.6f)), 0.024f, rubR, rubG, rubB)
                rub.addTube(listOf(Vec3(0.25f, 0f, 0.5f), Vec3(0.20f, 0.16f, 0f), Vec3(0.12f, -0.05f, -0.6f)), 0.024f, rubR, rubG, rubB)
            }
            "l4_loader_cylinders" -> {
                for (sx in listOf(-0.56f, 0.56f)) {
                    y.addCylinder(sx, 0f, -0.22f, 0.052f, 0.052f, 0.58f, 2, yR, yG, yB)
                    chr.addCylinder(sx, 0f, 0.22f, 0.030f, 0.030f, 0.52f, 2, chrR, chrG, chrB)
                }
            }
            "l4_boom_cylinders" -> {
                y.addCylinder(0f, -0.12f, 0.18f, 0.065f, 0.065f, 0.64f, 1, yR, yG, yB)
                chr.addCylinder(0f, 0.25f, 0.04f, 0.036f, 0.036f, 0.56f, 1, chrR, chrG, chrB)
            }
            "l4_dipper_cylinders" -> {
                y.addCylinder(0f, 0.06f, 0.16f, 0.056f, 0.056f, 0.60f, 2, yR, yG, yB)
                chr.addCylinder(0f, 0.06f, -0.22f, 0.032f, 0.032f, 0.52f, 2, chrR, chrG, chrB)
            }
            "l4_stabilizer_cylinders" -> {
                for (sx in listOf(-0.66f, 0.66f)) {
                    blk.addCylinder(sx, 0.12f, 0f, 0.050f, 0.050f, 0.42f, 1, blkR, blkG, blkB)
                    chr.addCylinder(sx, -0.14f, 0f, 0.030f, 0.030f, 0.36f, 1, chrR, chrG, chrB)
                }
            }
            "l4_hyd_connections" -> {
                for (sx in listOf(-0.18f, 0f, 0.18f)) {
                    brs.addCylinder(sx, 0f, 0f, 0.032f, 0.032f, 0.14f, 2, brsR, brsG, brsB)
                }
            }

            // ---------- LEVEL 5: WHEELS & BRAKES ----------
            "l5_brake_components" -> {
                for (sx in listOf(-0.56f, 0.56f)) {
                    iron.addCylinder(sx, 0f, 0f, 0.18f, 0.18f, 0.09f, 0, ironR, ironG, ironB)
                }
            }
            "l5_steering_components" -> {
                chr.addCylinder(0f, 0f, -0.10f, 0.036f, 0.036f, 1.12f, 0, chrR, chrG, chrB)
                blk.addCylinder(0f, 0f, -0.10f, 0.052f, 0.052f, 0.44f, 0, blkR, blkG, blkB)
            }
            "l5_wheel_hubs" -> {
                for (sx in listOf(-0.82f, 0.82f)) {
                    for (sz in listOf(-1.11f, 1.11f)) {
                        iron.addCylinder(sx, 0f, sz, 0.16f, 0.16f, 0.12f, 0, ironR, ironG, ironB)
                    }
                }
            }
            "l5_front_wheels" -> {
                buildWheel(rub, y, blk, -0.92f, 0f, 0f)
                buildWheel(rub, y, blk, 0.92f, 0f, 0f)
            }
            "l5_rear_wheels" -> {
                buildWheel(rub, y, blk, -0.92f, 0f, 0f)
                buildWheel(rub, y, blk, 0.92f, 0f, 0f)
            }
            "l5_nuts_bolts" -> {
                for (sx in listOf(-1.12f, 1.12f)) {
                    for (sz in listOf(-1.11f, 1.11f)) {
                        for (k in 0..5) {
                            val a = (k * 60.0 * PI / 180.0).toFloat()
                            chr.addBolt(sx, cos(a) * 0.11f, sz + sin(a) * 0.11f, 0.016f, 0.04f, 0)
                        }
                    }
                }
            }

            // ---------- LEVEL 6: CABIN ----------
            "l6_cabin_frame" -> {
                // 4 ROPS pillars + roof canopy
                for (sx in listOf(-0.56f, 0.56f)) {
                    blk.addRotatedBox(sx, 0f, 0.52f, 0.08f, 1.48f, 0.08f, -8f, 0f, 0f, blkR, blkG, blkB)
                    blk.addBox(sx, 0f, -0.52f, 0.08f, 1.48f, 0.08f, blkR, blkG, blkB)
                    blk.addBox(sx, 0.72f, 0f, 0.08f, 0.08f, 1.12f, blkR, blkG, blkB)
                }
                y.addBox(0f, 0.78f, -0.02f, 1.24f, 0.10f, 1.24f, yR, yG, yB) // signature yellow roof
                blk.addBox(0f, -0.72f, 0f, 1.16f, 0.08f, 1.10f, blkR, blkG, blkB)
            }
            "l6_doors_windows" -> {
                glass.addRotatedBox(0f, 0.14f, 0.54f, 1.02f, 1.10f, 0.02f, -8f, 0f, 0f, 0.45f, 0.68f, 0.80f) // front glass
                glass.addBox(0f, 0.14f, -0.54f, 1.02f, 1.10f, 0.02f, 0.45f, 0.68f, 0.80f) // rear glass
                glass.addBox(-0.58f, 0.14f, 0f, 0.02f, 1.10f, 0.96f, 0.45f, 0.68f, 0.80f) // left door glass
                glass.addBox(0.58f, 0.14f, 0f, 0.02f, 1.10f, 0.96f, 0.45f, 0.68f, 0.80f) // right door glass
            }
            "l6_seat" -> {
                blk.addBox(0f, -0.10f, 0f, 0.44f, 0.12f, 0.44f, blkR, blkG, blkB) // cushion
                blk.addRotatedBox(0f, 0.24f, -0.18f, 0.42f, 0.56f, 0.10f, -10f, 0f, 0f, blkR, blkG, blkB) // backrest
                y.addBox(0f, 0.26f, -0.23f, 0.26f, 0.14f, 0.02f, yR, yG, yB) // JCB seat back trim
            }
            "l6_dashboard" -> {
                blk.addBox(0f, 0f, 0f, 0.72f, 0.24f, 0.26f, blkR, blkG, blkB)
                lamp.addBox(-0.14f, 0.10f, -0.10f, 0.22f, 0.08f, 0.04f, 0.15f, 0.75f, 0.95f) // digital cluster
            }
            "l6_steering_wheel" -> {
                blk.addRotatedBox(0f, -0.10f, 0.06f, 0.07f, 0.28f, 0.07f, -26f, 0f, 0f, blkR, blkG, blkB)
                blk.addCylinder(0f, 0.04f, -0.02f, 0.18f, 0.18f, 0.03f, 1, blkR, blkG, blkB)
            }
            "l6_control_levers" -> {
                if (controlLayout == ControlLayout.JOYSTICK) {
                    blk.addBox(-0.34f, 0f, 0f, 0.14f, 0.12f, 0.28f, blkR, blkG, blkB)
                    blk.addBox(0.34f, 0f, 0f, 0.14f, 0.12f, 0.28f, blkR, blkG, blkB)
                    chr.addCylinder(-0.34f, 0.16f, 0f, 0.024f, 0.024f, 0.24f, 1, chrR, chrG, chrB)
                    chr.addCylinder(0.34f, 0.16f, 0f, 0.024f, 0.024f, 0.24f, 1, chrR, chrG, chrB)
                } else {
                    blk.addBox(0f, -0.04f, 0f, 0.62f, 0.14f, 0.22f, blkR, blkG, blkB)
                    for (i in 0..5) {
                        val lx = -0.22f + i * 0.088f
                        chr.addCylinder(lx, 0.18f, 0f, 0.015f, 0.015f, 0.34f, 1, chrR, chrG, chrB)
                        blk.addCylinder(lx, 0.36f, 0f, 0.026f, 0.026f, 0.05f, 1, blkR, blkG, blkB)
                    }
                }
            }
            "l6_pedals" -> {
                chr.addBox(-0.12f, 0f, 0f, 0.08f, 0.03f, 0.14f, chrR, chrG, chrB)
                chr.addBox(0f, 0f, 0f, 0.08f, 0.03f, 0.14f, chrR, chrG, chrB)
                chr.addBox(0.14f, 0.02f, 0f, 0.06f, 0.03f, 0.16f, chrR, chrG, chrB)
            }
            "l6_electrical_wiring" -> {
                brs.addTube(listOf(Vec3(-0.32f, -0.1f, 0.3f), Vec3(-0.32f, 0.1f, 0f), Vec3(0.28f, 0.1f, -0.2f)), 0.016f, 0.85f, 0.25f, 0.15f)
                blk.addBox(0.24f, 0.05f, -0.15f, 0.16f, 0.12f, 0.12f, blkR, blkG, blkB)
            }
            "l6_lights" -> {
                for (sx in listOf(-0.44f, 0.44f)) {
                    lamp.addBox(sx, 0f, 0.56f, 0.14f, 0.08f, 0.06f, 1.0f, 0.96f, 0.80f)
                    lamp.addBox(sx, 0f, -0.56f, 0.14f, 0.08f, 0.06f, 1.0f, 0.96f, 0.80f)
                }
                lamp.addCylinder(0f, 0.14f, -0.15f, 0.065f, 0.075f, 0.14f, 1, 1.0f, 0.62f, 0.05f) // amber beacon
            }
            "l6_mirrors" -> {
                blk.addBox(-0.76f, 0f, 0f, 0.10f, 0.28f, 0.05f, blkR, blkG, blkB)
                blk.addBox(0.76f, 0f, 0f, 0.10f, 0.28f, 0.05f, blkR, blkG, blkB)
                chr.addBox(-0.66f, 0f, 0f, 0.16f, 0.03f, 0.03f, chrR, chrG, chrB)
                chr.addBox(0.66f, 0f, 0f, 0.16f, 0.03f, 0.03f, chrR, chrG, chrB)
            }
            "l6_wipers" -> {
                blk.addRotatedBox(0f, 0f, 0f, 0.03f, 0.54f, 0.02f, 0f, 0f, 22f, blkR, blkG, blkB)
            }

            // ---------- LEVEL 7: FRONT LOADER ----------
            "l7_loader_arms" -> {
                for (sx in listOf(-0.56f, 0.56f)) {
                    y.addRotatedBox(sx, 0.12f, -0.35f, 0.11f, 0.18f, 1.15f, 22f, 0f, 0f, yR, yG, yB)
                    y.addRotatedBox(sx, -0.32f, 0.52f, 0.11f, 0.16f, 1.05f, 42f, 0f, 0f, yR, yG, yB)
                }
                y.addBox(0f, 0.04f, 0.08f, 1.12f, 0.14f, 0.16f, yR, yG, yB) // cross torsion tube
            }
            "l7_front_hyd_cylinders" -> {
                for (sx in listOf(-0.38f, 0.38f)) {
                    y.addRotatedBox(sx, 0.06f, -0.15f, 0.08f, 0.08f, 0.56f, 28f, 0f, 0f, yR, yG, yB)
                    chr.addRotatedBox(sx, -0.14f, 0.24f, 0.045f, 0.045f, 0.48f, 28f, 0f, 0f, chrR, chrG, chrB)
                }
            }
            "l7_bucket_linkage" -> {
                for (sx in listOf(-0.38f, 0.38f)) {
                    blk.addBox(sx, 0f, 0f, 0.06f, 0.32f, 0.10f, blkR, blkG, blkB)
                    chr.addRotatedBox(sx, -0.12f, 0.18f, 0.05f, 0.05f, 0.38f, 25f, 0f, 0f, chrR, chrG, chrB)
                }
            }
            "l7_front_bucket" -> {
                // Wide 2.3m JCB 4CX front shovel bucket with teeth
                blk.addBox(0f, -0.18f, 0.10f, 2.24f, 0.06f, 0.68f, blkR, blkG, blkB) // floor
                blk.addRotatedBox(0f, 0.12f, -0.24f, 2.24f, 0.62f, 0.06f, 18f, 0f, 0f, yR, yG, yB) // backplate
                blk.addBox(-1.10f, 0.06f, 0.05f, 0.06f, 0.52f, 0.62f, yR, yG, yB) // left cheek
                blk.addBox(1.10f, 0.06f, 0.05f, 0.06f, 0.52f, 0.62f, yR, yG, yB) // right cheek
                for (i in 0..7) {
                    val tx = -0.96f + i * 0.274f
                    chr.addBox(tx, -0.19f, 0.48f, 0.05f, 0.04f, 0.14f, chrR, chrG, chrB) // forged teeth
                }
            }
            "l7_pins" -> {
                for (sx in listOf(-0.56f, 0.56f)) {
                    chr.addCylinder(sx, 0.12f, 0f, 0.032f, 0.032f, 0.22f, 0, chrR, chrG, chrB)
                    chr.addCylinder(sx, -0.12f, 0.12f, 0.032f, 0.032f, 0.22f, 0, chrR, chrG, chrB)
                }
            }
            "l7_locking_clips" -> {
                for (sx in listOf(-0.68f, 0.68f)) {
                    brs.addBox(sx, 0f, 0f, 0.03f, 0.09f, 0.09f, brsR, brsG, brsB)
                }
            }

            // ---------- LEVEL 8: REAR BACKHOE ----------
            "l8_swing_frame" -> {
                iron.addBox(0f, 0f, 0.08f, 0.48f, 0.52f, 0.34f, ironR, ironG, ironB)
                y.addBox(0f, 0.14f, -0.14f, 0.28f, 0.48f, 0.28f, yR, yG, yB) // kingpost pivot
            }
            "l8_stabilizer_legs" -> {
                for (sx in listOf(-0.72f, 0.72f)) {
                    y.addBox(sx, 0.16f, 0f, 0.14f, 0.56f, 0.14f, yR, yG, yB)
                    blk.addBox(sx, -0.16f, 0f, 0.30f, 0.07f, 0.32f, blkR, blkG, blkB) // foot pad
                }
            }
            "l8_boom" -> {
                // Iconic curved JCB boom
                y.addRotatedBox(0f, -0.12f, 0.26f, 0.20f, 0.26f, 1.15f, -55f, 0f, 0f, yR, yG, yB)
                y.addRotatedBox(0f, 0.45f, -0.28f, 0.18f, 0.22f, 0.96f, -15f, 0f, 0f, yR, yG, yB)
            }
            "l8_dipper_arm" -> {
                y.addRotatedBox(0f, 0f, 0f, 0.16f, 1.48f, 0.18f, -12f, 0f, 0f, yR, yG, yB)
                blk.addRotatedBox(0f, -0.35f, -0.05f, 0.12f, 0.90f, 0.13f, -12f, 0f, 0f, blkR, blkG, blkB) // Extradig inner slide
            }
            "l8_excavator_bucket" -> {
                blk.addBox(0f, 0.12f, 0.16f, 0.56f, 0.36f, 0.06f, blkR, blkG, blkB)
                blk.addBox(0f, -0.12f, -0.04f, 0.56f, 0.06f, 0.44f, blkR, blkG, blkB)
                y.addBox(-0.27f, 0f, 0f, 0.05f, 0.34f, 0.40f, yR, yG, yB)
                y.addBox(0.27f, 0f, 0f, 0.05f, 0.34f, 0.40f, yR, yG, yB)
                for (i in 0..3) {
                    chr.addBox(-0.19f + i * 0.125f, -0.13f, -0.30f, 0.045f, 0.04f, 0.14f, chrR, chrG, chrB)
                }
            }
            "l8_rear_hyd_cylinders" -> {
                y.addRotatedBox(0f, 0.18f, 0.12f, 0.09f, 0.56f, 0.09f, -12f, 0f, 0f, yR, yG, yB)
                chr.addRotatedBox(0f, -0.28f, 0.02f, 0.048f, 0.48f, 0.048f, -12f, 0f, 0f, chrR, chrG, chrB)
            }
            "l8_pins_hyd_conn" -> {
                chr.addCylinder(0f, 0f, 0f, 0.036f, 0.036f, 0.32f, 0, chrR, chrG, chrB)
                rub.addTube(listOf(Vec3(-0.08f, 0.3f, 0.2f), Vec3(-0.14f, 0f, 0f), Vec3(-0.08f, -0.3f, -0.15f)), 0.022f, rubR, rubG, rubB)
                rub.addTube(listOf(Vec3(0.08f, 0.3f, 0.2f), Vec3(0.14f, 0f, 0f), Vec3(0.08f, -0.3f, -0.15f)), 0.022f, rubR, rubG, rubB)
            }

            // ---------- LEVEL 9: BODY & FINAL ASSEMBLY ----------
            "l9_fuel_tank" -> {
                y.addBox(0f, 0f, 0f, 0.34f, 0.44f, 0.62f, yR, yG, yB)
                blk.addCylinder(0.08f, 0.24f, 0.18f, 0.045f, 0.045f, 0.05f, 1, blkR, blkG, blkB)
            }
            "l9_steps" -> {
                for (sx in listOf(-0.64f, 0.64f)) {
                    blk.addBox(sx, 0.06f, 0f, 0.18f, 0.04f, 0.34f, blkR, blkG, blkB)
                    blk.addBox(sx, -0.14f, 0f, 0.18f, 0.04f, 0.34f, blkR, blkG, blkB)
                    blk.addBox(sx, -0.04f, -0.16f, 0.04f, 0.34f, 0.04f, blkR, blkG, blkB)
                    blk.addBox(sx, -0.04f, 0.16f, 0.04f, 0.34f, 0.04f, blkR, blkG, blkB)
                }
            }
            "l9_mudguards" -> {
                for (sx in listOf(-0.92f, 0.92f)) {
                    blk.addBox(sx, 0.12f, 0f, 0.44f, 0.05f, 0.88f, blkR, blkG, blkB)
                    blk.addRotatedBox(sx, 0.02f, 0.48f, 0.44f, 0.05f, 0.28f, 35f, 0f, 0f, blkR, blkG, blkB)
                    blk.addRotatedBox(sx, 0.02f, -0.48f, 0.44f, 0.05f, 0.28f, -35f, 0f, 0f, blkR, blkG, blkB)
                }
            }
            "l9_exhaust" -> {
                blk.addCylinder(0f, -0.25f, 0f, 0.09f, 0.09f, 0.44f, 1, blkR, blkG, blkB)
                blk.addCylinder(0f, 0.20f, 0f, 0.045f, 0.045f, 0.56f, 1, blkR, blkG, blkB)
            }
            "l9_engine_hood" -> {
                y.addRotatedBox(0f, 0.04f, 0f, 0.78f, 0.16f, 1.12f, 8f, 0f, 0f, yR, yG, yB)
                blk.addBox(0f, -0.08f, 0.54f, 0.72f, 0.38f, 0.06f, blkR, blkG, blkB) // front grille
            }
            "l9_side_panels" -> {
                y.addBox(-0.40f, 0f, 0f, 0.04f, 0.42f, 0.98f, yR, yG, yB)
                y.addBox(0.40f, 0f, 0f, 0.04f, 0.42f, 0.98f, yR, yG, yB)
                blk.addBox(-0.425f, 0f, 0.10f, 0.02f, 0.22f, 0.42f, blkR, blkG, blkB)
                blk.addBox(0.425f, 0f, 0.10f, 0.02f, 0.22f, 0.42f, blkR, blkG, blkB)
            }
            "l9_counterweight" -> {
                iron.addBox(0f, 0f, 0f, 1.06f, 0.32f, 0.24f, blkR, blkG, blkB)
                y.addBox(0f, 0.04f, 0.13f, 0.96f, 0.18f, 0.03f, yR, yG, yB)
            }
            "l9_decals" -> {
                blk.addBox(-0.43f, 0.06f, 0f, 0.015f, 0.14f, 0.52f, blkR, blkG, blkB)
                blk.addBox(0.43f, 0.06f, 0f, 0.015f, 0.14f, 0.52f, blkR, blkG, blkB)
                y.addBox(-0.44f, 0.06f, 0f, 0.01f, 0.08f, 0.42f, yR, yG, yB)
                y.addBox(0.44f, 0.06f, 0f, 0.01f, 0.08f, 0.42f, yR, yG, yB)
            }
            "l9_final_paint" -> {
                lamp.addBox(0f, 0f, 0f, 0.24f, 0.04f, 0.24f, 0.1f, 0.9f, 0.5f)
            }

            else -> {
                y.addBox(0f, 0f, 0f, 0.3f, 0.3f, 0.3f, yR, yG, yB)
            }
        }

        val list = ArrayList<MeshData>()
        fun addIfNonEmpty(mb: MeshBuilder, style: MaterialStyle) {
            val built = mb.build(style)
            if (built.indexCount > 0) list.add(built)
        }
        addIfNonEmpty(y, MaterialStyle.JCB_YELLOW)
        addIfNonEmpty(blk, MaterialStyle.MATTE_BLACK)
        addIfNonEmpty(iron, MaterialStyle.CAST_IRON)
        addIfNonEmpty(chr, MaterialStyle.CHROME_METAL)
        addIfNonEmpty(brs, MaterialStyle.BRASS_FITTING)
        addIfNonEmpty(rub, MaterialStyle.RUBBER_TYRE)
        addIfNonEmpty(glass, MaterialStyle.CAB_GLASS)
        addIfNonEmpty(lamp, MaterialStyle.EMISSIVE_LAMP)
        return MeshSubGroup(list)
    }

    private fun buildToolMesh(tool: ToolType): MeshSubGroup {
        val chr = MeshBuilder(10)
        val blk = MeshBuilder(10)
        val y = MeshBuilder(10)
        val iron = MeshBuilder(10)
        val brs = MeshBuilder(10)
        val lamp = MeshBuilder(10)

        when (tool) {
            ToolType.SPANNER -> {
                chr.addBox(0f, 0.14f, 0f, 0.045f, 0.32f, 0.018f, chrR, chrG, chrB)
                chr.addCylinder(0f, 0.30f, 0f, 0.055f, 0.055f, 0.022f, 2, chrR, chrG, chrB)
            }
            ToolType.SOCKET_WRENCH -> {
                chr.addBox(0f, 0.16f, 0f, 0.036f, 0.34f, 0.025f, chrR, chrG, chrB)
                chr.addCylinder(0f, 0.02f, 0f, 0.035f, 0.035f, 0.09f, 1, chrR, chrG, chrB)
                blk.addCylinder(0f, 0.28f, 0f, 0.026f, 0.026f, 0.14f, 1, blkR, blkG, blkB)
            }
            ToolType.SCREWDRIVER -> {
                chr.addCylinder(0f, 0.10f, 0f, 0.012f, 0.012f, 0.22f, 1, chrR, chrG, chrB)
                y.addCylinder(0f, 0.26f, 0f, 0.032f, 0.028f, 0.14f, 1, yR, yG, yB)
            }
            ToolType.HAMMER -> {
                y.addCylinder(0f, 0.20f, 0f, 0.022f, 0.022f, 0.38f, 1, yR, yG, yB)
                iron.addBox(0f, 0.38f, 0f, 0.16f, 0.075f, 0.075f, ironR, ironG, ironB)
            }
            ToolType.PLIERS -> {
                chr.addRotatedBox(-0.02f, 0.14f, 0f, 0.022f, 0.22f, 0.02f, 0f, 0f, 10f, chrR, chrG, chrB)
                chr.addRotatedBox(0.02f, 0.14f, 0f, 0.022f, 0.22f, 0.02f, 0f, 0f, -10f, chrR, chrG, chrB)
                y.addRotatedBox(-0.04f, 0.25f, 0f, 0.026f, 0.14f, 0.026f, 0f, 0f, -12f, yR, yG, yB)
                y.addRotatedBox(0.04f, 0.25f, 0f, 0.026f, 0.14f, 0.026f, 0f, 0f, 12f, yR, yG, yB)
            }
            ToolType.HYDRAULIC_WRENCH -> {
                blk.addBox(0f, 0.14f, 0f, 0.055f, 0.34f, 0.024f, blkR, blkG, blkB)
                brs.addCylinder(0f, 0.02f, 0f, 0.05f, 0.05f, 0.03f, 2, brsR, brsG, brsB)
            }
            ToolType.TORQUE_WRENCH -> {
                chr.addCylinder(0f, 0.22f, 0f, 0.024f, 0.024f, 0.44f, 1, chrR, chrG, chrB)
                blk.addCylinder(0f, 0.36f, 0f, 0.032f, 0.032f, 0.14f, 1, blkR, blkG, blkB)
                lamp.addBox(0f, 0.22f, 0.028f, 0.036f, 0.07f, 0.01f, 0.2f, 0.85f, 1.0f)
            }
            ToolType.ENGINE_CRANE -> {
                y.addBox(0f, 0.65f, 0f, 0.16f, 0.12f, 0.90f, yR, yG, yB)
                chr.addCylinder(0f, 0.34f, 0f, 0.014f, 0.014f, 0.56f, 1, chrR, chrG, chrB)
                iron.addBox(0f, 0.04f, 0f, 0.09f, 0.10f, 0.09f, ironR, ironG, ironB)
            }
            ToolType.JACK -> {
                y.addBox(0f, -0.05f, 0f, 0.28f, 0.08f, 0.52f, yR, yG, yB)
                chr.addCylinder(0f, 0.06f, 0f, 0.055f, 0.055f, 0.14f, 1, chrR, chrG, chrB)
                chr.addRotatedBox(0f, 0.22f, -0.32f, 0.03f, 0.46f, 0.03f, -35f, 0f, 0f, chrR, chrG, chrB)
            }
            ToolType.DIAGNOSTIC_SCANNER -> {
                blk.addBox(0f, 0.18f, 0f, 0.18f, 0.26f, 0.05f, blkR, blkG, blkB)
                lamp.addBox(0f, 0.22f, 0.03f, 0.13f, 0.11f, 0.01f, 0.15f, 0.95f, 0.65f)
                lamp.addCylinder(0f, 0.02f, 0.10f, 0.01f, 0.07f, 0.24f, 2, 0.15f, 0.95f, 0.65f)
            }
        }

        val list = ArrayList<MeshData>()
        fun addIf(mb: MeshBuilder, style: MaterialStyle) {
            val b = mb.build(style)
            if (b.indexCount > 0) list.add(b)
        }
        addIf(chr, MaterialStyle.CHROME_METAL)
        addIf(blk, MaterialStyle.MATTE_BLACK)
        addIf(y, MaterialStyle.JCB_YELLOW)
        addIf(iron, MaterialStyle.CAST_IRON)
        addIf(brs, MaterialStyle.BRASS_FITTING)
        addIf(lamp, MaterialStyle.EMISSIVE_LAMP)
        return MeshSubGroup(list)
    }
}

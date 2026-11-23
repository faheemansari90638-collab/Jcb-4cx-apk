package com.example.engine3d

import com.example.model.GraphicsQuality

class MicroPartMeshes(quality: GraphicsQuality) {
    private val seg = quality.circleSegments.coerceAtLeast(14)
    private val cache = HashMap<String, MeshSubGroup>()
    val transparentBlockOverlay: MeshSubGroup = buildBlockXrayMesh()
    val engineStandMesh: MeshSubGroup = buildEngineStand()

    private val castR = 0.36f; private val castG = 0.39f; private val castB = 0.44f
    private val chrR = 0.86f; private val chrG = 0.90f; private val chrB = 0.94f
    private val brsR = 0.84f; private val brsG = 0.62f; private val brsB = 0.24f
    private val vitonR = 0.14f; private val vitonG = 0.15f; private val vitonB = 0.18f
    private val redSealR = 0.72f; private val redSealG = 0.18f; private val redSealB = 0.14f
    private val yR = 0.95f; private val yG = 0.71f; private val yB = 0.02f

    fun getMicroMesh(id: String): MeshSubGroup = cache.getOrPut(id) { buildMesh(id) }

    private fun buildEngineStand(): MeshSubGroup {
        val blk = MeshBuilder(seg)
        val y = MeshBuilder(seg)
        blk.addBox(0f, -0.02f, 0f, 1.8f, 0.04f, 1.6f, 0.16f, 0.18f, 0.22f)
        y.addBox(-0.48f, 0.30f, -0.45f, 0.06f, 0.60f, 0.06f, yR, yG, yB)
        y.addBox(0.48f, 0.30f, -0.45f, 0.06f, 0.60f, 0.06f, yR, yG, yB)
        y.addBox(0.46f, 0.28f, -0.22f, 0.24f, 0.48f, 0.34f, 0.22f, 0.25f, 0.29f) // hyd bench pedestal
        return MeshSubGroup(listOf(blk.build(MaterialStyle.MATTE_BLACK), y.build(MaterialStyle.JCB_YELLOW)))
    }

    private fun buildBlockXrayMesh(): MeshSubGroup {
        val glass = MeshBuilder(seg)
        glass.addBox(-0.16f, 0f, 0f, 0.08f, 0.38f, 0.72f, 0.45f, 0.72f, 0.90f)
        glass.addBox(0.16f, 0f, 0f, 0.08f, 0.38f, 0.72f, 0.45f, 0.72f, 0.90f)
        glass.addBox(0f, 0.14f, 0f, 0.36f, 0.08f, 0.72f, 0.45f, 0.72f, 0.90f)
        return MeshSubGroup(listOf(glass.build(MaterialStyle.CAB_GLASS)))
    }

    private fun buildMesh(id: String): MeshSubGroup {
        val cast = MeshBuilder(seg)
        val chr = MeshBuilder(seg)
        val brs = MeshBuilder(seg)
        val blk = MeshBuilder(seg)
        val seal = MeshBuilder(seg)
        val y = MeshBuilder(seg)
        val lamp = MeshBuilder(seg)

        when (id) {
            "mp_engine_block" -> {
                // Hollowed block walls & 4 cylinder deck sleeves so internal parts show clearly
                cast.addBox(-0.17f, 0f, 0f, 0.06f, 0.36f, 0.72f, castR, castG, castB)
                cast.addBox(0.17f, 0f, 0f, 0.06f, 0.36f, 0.72f, castR, castG, castB)
                cast.addBox(0f, 0.14f, 0.34f, 0.36f, 0.10f, 0.05f, castR, castG, castB)
                cast.addBox(0f, 0.14f, -0.34f, 0.36f, 0.10f, 0.05f, castR, castG, castB)
                cast.addBox(0f, -0.15f, 0f, 0.34f, 0.06f, 0.70f, castR, castG, castB)
            }
            "mp_main_bearing_shell_upper" -> {
                brs.addHalfShell(0f, 0f, 0f, 0.052f, 0.044f, 0.008f, true, brsR, brsG, brsB)
            }
            "mp_main_bearing_shell_lower" -> {
                brs.addHalfShell(0f, 0f, 0f, 0.052f, 0.044f, 0.008f, false, brsR, brsG, brsB)
            }
            "mp_crankshaft" -> {
                chr.addCylinder(0f, 0f, 0f, 0.042f, 0.042f, 0.72f, 2, chrR, chrG, chrB)
                // 4 crank throws + counterweights
                for (i in 0..3) {
                    val pz = -0.22f + i * 0.145f
                    val sign = if (i == 0 || i == 3) 1f else -1f
                    cast.addBox(0f, sign * 0.04f, pz - 0.04f, 0.09f, 0.14f, 0.025f, castR, castG, castB)
                    cast.addBox(0f, sign * 0.04f, pz + 0.04f, 0.09f, 0.14f, 0.025f, castR, castG, castB)
                    chr.addCylinder(0f, sign * 0.065f, pz, 0.032f, 0.032f, 0.055f, 2, chrR, chrG, chrB)
                }
            }
            "mp_main_bearing_cap" -> {
                cast.addBox(0f, 0f, 0f, 0.22f, 0.06f, 0.055f, castR, castG, castB)
            }
            "mp_main_bearing_bolt_1", "mp_main_bearing_bolt_2" -> {
                chr.addBolt(0f, 0f, 0f, 0.014f, 0.11f, 1)
                brs.addTorusRing(0f, -0.02f, 0f, 0.020f, 0.004f, 1, brsR, brsG, brsB)
            }
            "mp_front_crank_oil_seal", "mp_rear_crank_oil_seal" -> {
                seal.addTorusRing(0f, 0f, 0f, 0.058f, 0.012f, 2, redSealR, redSealG, redSealB)
                chr.addTorusRing(0f, 0f, 0f, 0.052f, 0.004f, 2, chrR, chrG, chrB)
            }
            "mp_piston" -> {
                chr.addCylinder(0f, 0.02f, 0f, 0.052f, 0.052f, 0.095f, 1, chrR, chrG, chrB)
                // Connecting rod beam down to journal
                cast.addBox(0f, -0.12f, 0f, 0.028f, 0.20f, 0.024f, castR, castG, castB)
                cast.addCylinder(0f, -0.23f, 0f, 0.042f, 0.042f, 0.035f, 2, castR, castG, castB)
            }
            "mp_ring_oil" -> {
                blk.addTorusRing(0f, 0f, 0f, 0.055f, 0.005f, 1, 0.25f, 0.28f, 0.32f, gapDeg = 12f)
            }
            "mp_ring_scraper" -> {
                chr.addTorusRing(0f, 0f, 0f, 0.055f, 0.005f, 1, 0.75f, 0.78f, 0.82f, gapDeg = 12f)
            }
            "mp_ring_top" -> {
                brs.addTorusRing(0f, 0f, 0f, 0.055f, 0.005f, 1, chrR, chrG, chrB, gapDeg = 12f)
            }
            "mp_conrod_bearing_shell" -> {
                brs.addHalfShell(0f, 0f, 0f, 0.038f, 0.034f, 0.006f, false, brsR, brsG, brsB)
            }
            "mp_conrod_cap" -> {
                cast.addBox(0f, 0f, 0f, 0.13f, 0.042f, 0.036f, castR, castG, castB)
            }
            "mp_conrod_bolt_1", "mp_conrod_bolt_2" -> {
                chr.addBolt(0f, 0f, 0f, 0.011f, 0.075f, 1)
            }
            "mp_oil_pump_rotor" -> {
                cast.addCylinder(0f, 0f, 0f, 0.068f, 0.068f, 0.048f, 2, castR, castG, castB)
                brs.addCylinder(0f, 0f, 0.01f, 0.040f, 0.040f, 0.052f, 2, brsR, brsG, brsB)
            }
            "mp_oil_pump_oring" -> {
                blk.addTorusRing(0f, 0f, 0f, 0.028f, 0.006f, 1, vitonR, vitonG, vitonB)
            }
            "mp_oil_pump_bolt" -> {
                chr.addBolt(0f, 0f, 0f, 0.009f, 0.055f, 2)
                brs.addTorusRing(0f, 0f, 0.015f, 0.015f, 0.003f, 2, brsR, brsG, brsB)
            }
            "mp_sump_gasket" -> {
                seal.addBox(-0.16f, 0f, 0f, 0.025f, 0.008f, 0.68f, 0.18f, 0.55f, 0.72f)
                seal.addBox(0.16f, 0f, 0f, 0.025f, 0.008f, 0.68f, 0.18f, 0.55f, 0.72f)
                seal.addBox(0f, 0f, 0.33f, 0.34f, 0.008f, 0.025f, 0.18f, 0.55f, 0.72f)
                seal.addBox(0f, 0f, -0.33f, 0.34f, 0.008f, 0.025f, 0.18f, 0.55f, 0.72f)
            }
            "mp_camshaft" -> {
                chr.addCylinder(0f, 0f, 0f, 0.022f, 0.022f, 0.70f, 2, chrR, chrG, chrB)
                for (i in 0..7) {
                    val lz = -0.26f + i * 0.075f
                    chr.addRotatedBox(0f, 0.014f, lz, 0.036f, 0.052f, 0.022f, 0f, 0f, i * 45f, chrR, chrG, chrB)
                }
            }
            "mp_head_gasket" -> {
                chr.addBox(0f, 0f, 0f, 0.36f, 0.006f, 0.70f, 0.62f, 0.66f, 0.70f)
                for (i in 0..3) {
                    brs.addTorusRing(0f, 0.004f, -0.22f + i * 0.145f, 0.052f, 0.004f, 1, brsR, brsG, brsB)
                }
            }
            "mp_cylinder_head" -> {
                cast.addBox(0f, 0f, 0f, 0.34f, 0.13f, 0.68f, castR, castG, castB)
            }
            "mp_head_bolt_1", "mp_head_bolt_2" -> {
                chr.addBolt(0f, 0f, 0f, 0.013f, 0.14f, 1)
            }
            "mp_intake_valve", "mp_exhaust_valve" -> {
                val headR = if (id == "mp_intake_valve") 0.026f else 0.022f
                chr.addCylinder(0f, -0.045f, 0f, 0.008f, headR, 0.012f, 1, chrR, chrG, chrB)
                chr.addCylinder(0f, 0.020f, 0f, 0.005f, 0.005f, 0.12f, 1, chrR, chrG, chrB)
            }
            "mp_valve_stem_seal_in", "mp_valve_stem_seal_ex" -> {
                seal.addCylinder(0f, 0f, 0f, 0.013f, 0.015f, 0.018f, 1, 0.15f, 0.65f, 0.35f)
            }
            "mp_valve_spring_in" -> {
                chr.addHelixCoil(0f, 0f, 0f, 0.020f, 0.004f, 0.064f, 5.5f, chrR, chrG, chrB)
            }
            "mp_valve_retainer_in" -> {
                cast.addCylinder(0f, 0f, 0f, 0.022f, 0.024f, 0.012f, 1, castR, castG, castB)
            }
            "mp_valve_collets_in" -> {
                brs.addBox(-0.007f, 0f, 0f, 0.006f, 0.014f, 0.010f, brsR, brsG, brsB)
                brs.addBox(0.007f, 0f, 0f, 0.006f, 0.014f, 0.010f, brsR, brsG, brsB)
            }
            "mp_rocker_arm_in" -> {
                cast.addBox(0f, 0f, 0f, 0.14f, 0.024f, 0.026f, castR, castG, castB)
                chr.addBolt(-0.055f, 0.015f, 0f, 0.006f, 0.032f, 1)
            }
            "mp_rocker_nut" -> {
                chr.addCylinder(0f, 0f, 0f, 0.011f, 0.011f, 0.012f, 1, chrR, chrG, chrB, 6)
                brs.addTorusRing(0f, -0.007f, 0f, 0.012f, 0.003f, 1, brsR, brsG, brsB)
            }
            "mp_injector_washer" -> {
                brs.addTorusRing(0f, 0f, 0f, 0.013f, 0.004f, 1, 0.90f, 0.45f, 0.15f)
            }
            "mp_injector_oring" -> {
                blk.addTorusRing(0f, 0f, 0f, 0.014f, 0.004f, 1, vitonR, vitonG, vitonB)
            }
            "mp_injector" -> {
                blk.addCylinder(0f, 0.03f, 0f, 0.016f, 0.016f, 0.08f, 1, 0.16f, 0.18f, 0.22f)
                chr.addCylinder(0f, -0.035f, 0f, 0.008f, 0.012f, 0.065f, 1, chrR, chrG, chrB)
                brs.addBox(0.02f, 0.055f, 0f, 0.032f, 0.012f, 0.022f, brsR, brsG, brsB)
            }
            "mp_rocker_cover_gasket" -> {
                blk.addBox(-0.16f, 0f, 0f, 0.02f, 0.010f, 0.64f, vitonR, vitonG, vitonB)
                blk.addBox(0.16f, 0f, 0f, 0.02f, 0.010f, 0.64f, vitonR, vitonG, vitonB)
                blk.addBox(0f, 0f, 0.31f, 0.34f, 0.010f, 0.02f, vitonR, vitonG, vitonB)
                blk.addBox(0f, 0f, -0.31f, 0.34f, 0.010f, 0.02f, vitonR, vitonG, vitonB)
            }
            "mp_rocker_screw_1", "mp_rocker_screw_2" -> {
                chr.addBolt(0f, 0f, 0f, 0.007f, 0.042f, 1)
                brs.addTorusRing(0f, 0.010f, 0f, 0.012f, 0.0025f, 1, brsR, brsG, brsB)
            }
            "mp_hyd_spool_body" -> {
                cast.addBox(0f, -0.04f, 0f, 0.16f, 0.07f, 0.30f, castR, castG, castB)
                cast.addBox(-0.055f, 0.02f, 0f, 0.04f, 0.08f, 0.30f, castR, castG, castB)
                cast.addBox(0.055f, 0.02f, 0f, 0.04f, 0.08f, 0.30f, castR, castG, castB)
            }
            "mp_hyd_spool_oring_1", "mp_hyd_spool_oring_2" -> {
                blk.addTorusRing(0f, 0f, 0f, 0.024f, 0.005f, 2, vitonR, vitonG, vitonB)
            }
            "mp_hyd_spool_backup_ring" -> {
                lamp.addTorusRing(0f, 0f, 0f, 0.025f, 0.0045f, 2, 0.92f, 0.94f, 0.98f)
            }
            "mp_hyd_spool_plunger" -> {
                chr.addCylinder(0f, 0f, 0f, 0.018f, 0.018f, 0.32f, 2, chrR, chrG, chrB)
                chr.addCylinder(0f, 0f, 0.07f, 0.023f, 0.023f, 0.05f, 2, chrR, chrG, chrB)
                chr.addCylinder(0f, 0f, -0.07f, 0.023f, 0.023f, 0.05f, 2, chrR, chrG, chrB)
            }
            else -> {
                cast.addBox(0f, 0f, 0f, 0.06f, 0.06f, 0.06f, castR, castG, castB)
            }
        }

        val list = ArrayList<MeshData>()
        fun addIf(mb: MeshBuilder, style: MaterialStyle) {
            val built = mb.build(style)
            if (built.indexCount > 0) list.add(built)
        }
        addIf(cast, MaterialStyle.CAST_IRON)
        addIf(chr, MaterialStyle.CHROME_METAL)
        addIf(brs, MaterialStyle.BRASS_FITTING)
        addIf(blk, MaterialStyle.RUBBER_TYRE)
        addIf(seal, MaterialStyle.MATTE_BLACK)
        addIf(y, MaterialStyle.JCB_YELLOW)
        addIf(lamp, MaterialStyle.EMISSIVE_LAMP)
        return MeshSubGroup(list)
    }
}

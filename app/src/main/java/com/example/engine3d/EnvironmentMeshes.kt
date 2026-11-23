package com.example.engine3d

class EnvironmentMeshes(segments: Int = 12) {
    val workshopScene: MeshSubGroup = buildWorkshop(segments)
    val constructionYardBase: MeshSubGroup = buildConstructionYardBase(segments)
    val soilMoundMesh: MeshSubGroup = buildSoilMound(segments)
    val dumpTruckMesh: MeshSubGroup = buildDumpTruck(segments)
    val frontBucketSoilMesh: MeshSubGroup = buildBucketSoilLoad()

    private fun buildWorkshop(seg: Int): MeshSubGroup {
        val env = MeshBuilder(seg)
        val y = MeshBuilder(seg)
        val blk = MeshBuilder(seg)
        val lamp = MeshBuilder(seg)

        // Concrete garage floor
        env.addBox(0f, -0.05f, 0f, 16f, 0.10f, 16f, 0.24f, 0.26f, 0.29f)
        // Raised inspection bay pad
        env.addBox(0f, 0.01f, 0f, 3.6f, 0.02f, 6.2f, 0.18f, 0.20f, 0.23f)
        // Yellow hazard bay perimeter stripes
        y.addBox(-1.90f, 0.015f, 0f, 0.12f, 0.02f, 6.4f, 0.95f, 0.72f, 0.04f)
        y.addBox(1.90f, 0.015f, 0f, 0.12f, 0.02f, 6.4f, 0.95f, 0.72f, 0.04f)
        y.addBox(0f, 0.015f, 3.14f, 3.92f, 0.02f, 0.12f, 0.95f, 0.72f, 0.04f)
        y.addBox(0f, 0.015f, -3.14f, 3.92f, 0.02f, 0.12f, 0.95f, 0.72f, 0.04f)

        // Workshop walls & structural steel columns
        env.addBox(0f, 3.0f, -7.8f, 16f, 6.0f, 0.25f, 0.17f, 0.19f, 0.22f)
        env.addBox(-7.8f, 3.0f, 0f, 0.25f, 6.0f, 16f, 0.15f, 0.17f, 0.20f)
        env.addBox(7.8f, 3.0f, 0f, 0.25f, 6.0f, 16f, 0.15f, 0.17f, 0.20f)
        for (sx in listOf(-4.5f, 4.5f)) {
            for (sz in listOf(-4.5f, 0f, 4.5f)) {
                blk.addBox(sx, 2.8f, sz, 0.28f, 5.6f, 0.28f, 0.14f, 0.16f, 0.19f)
            }
        }
        // Overhead 2-ton yellow gantry bridge beam
        y.addBox(0f, 4.6f, 0f, 9.2f, 0.28f, 0.30f, 0.92f, 0.68f, 0.04f)
        // Red tool cabinet + workbench along back wall
        env.addBox(-3.6f, 0.55f, -5.8f, 2.2f, 1.1f, 0.8f, 0.72f, 0.16f, 0.14f)
        blk.addBox(2.8f, 0.48f, -5.8f, 2.6f, 0.96f, 0.85f, 0.22f, 0.24f, 0.27f)
        // Overhead LED bay lights
        for (lz in listOf(-2.0f, 2.0f)) {
            lamp.addBox(0f, 4.95f, lz, 2.2f, 0.06f, 0.24f, 1.0f, 0.96f, 0.85f)
        }

        return MeshSubGroup(
            listOf(
                env.build(MaterialStyle.WORKSHOP_ENV),
                y.build(MaterialStyle.JCB_YELLOW),
                blk.build(MaterialStyle.MATTE_BLACK),
                lamp.build(MaterialStyle.EMISSIVE_LAMP)
            )
        )
    }

    private fun buildConstructionYardBase(seg: Int): MeshSubGroup {
        val soil = MeshBuilder(seg)
        val env = MeshBuilder(seg)
        val y = MeshBuilder(seg)
        val blk = MeshBuilder(seg)

        // Expansive terrain ground
        soil.addBox(0f, -0.08f, 0f, 54f, 0.16f, 54f, 0.36f, 0.29f, 0.21f)
        // Compacted concrete haul road
        env.addBox(0f, 0.01f, 0f, 34f, 0.03f, 8f, 0.30f, 0.32f, 0.34f)
        // Grading dump zone (around X = +6, Z = -7)
        y.addBox(6f, 0.02f, -7f, 4.2f, 0.03f, 0.15f, 0.95f, 0.72f, 0.04f)
        y.addBox(6f, 0.02f, -11f, 4.2f, 0.03f, 0.15f, 0.95f, 0.72f, 0.04f)
        env.addBox(6f, 0.015f, -9f, 4.0f, 0.025f, 3.8f, 0.26f, 0.28f, 0.30f)

        // Trench excavation marker box (around X = -2, Z = -9)
        y.addBox(-2f, 0.02f, -6.5f, 1.5f, 0.03f, 0.14f, 0.95f, 0.72f, 0.04f)
        y.addBox(-2f, 0.02f, -12.5f, 1.5f, 0.03f, 0.14f, 0.95f, 0.72f, 0.04f)
        blk.addBox(-2f, 0.015f, -9.5f, 1.25f, 0.03f, 5.8f, 0.22f, 0.16f, 0.12f)

        // Parking Bay (around X = -10, Z = +9)
        env.addBox(-10f, 0.02f, 9f, 4.2f, 0.03f, 6.6f, 0.22f, 0.24f, 0.27f)
        y.addBox(-12.1f, 0.03f, 9f, 0.15f, 0.03f, 6.8f, 0.95f, 0.72f, 0.04f)
        y.addBox(-7.9f, 0.03f, 9f, 0.15f, 0.03f, 6.8f, 0.95f, 0.72f, 0.04f)
        y.addBox(-10f, 0.03f, 12.3f, 4.35f, 0.03f, 0.15f, 0.95f, 0.72f, 0.04f)
        // Safety bollards at parking corners
        for (bx in listOf(-12.1f, -7.9f)) {
            y.addCylinder(bx, 0.45f, 12.3f, 0.10f, 0.10f, 0.90f, 1, 0.95f, 0.72f, 0.04f, seg)
        }

        // Site perimeter concrete jersey barriers
        for (zPos in listOf(-18f, 18f)) {
            env.addBox(0f, 0.45f, zPos, 36f, 0.9f, 0.5f, 0.48f, 0.50f, 0.52f)
        }

        return MeshSubGroup(
            listOf(
                soil.build(MaterialStyle.SOIL_EARTH),
                env.build(MaterialStyle.WORKSHOP_ENV),
                y.build(MaterialStyle.JCB_YELLOW),
                blk.build(MaterialStyle.MATTE_BLACK)
            )
        )
    }

    private fun buildSoilMound(seg: Int): MeshSubGroup {
        val soil = MeshBuilder(seg)
        // Tiered earth mound at (X = +7, Z = +7)
        soil.addCylinder(0f, 0.45f, 0f, 1.4f, 2.8f, 0.90f, 1, 0.48f, 0.34f, 0.20f, seg)
        soil.addCylinder(0.3f, 1.05f, -0.2f, 0.4f, 1.4f, 0.55f, 1, 0.52f, 0.37f, 0.22f, seg)
        return MeshSubGroup(listOf(soil.build(MaterialStyle.SOIL_EARTH)))
    }

    private fun buildDumpTruck(seg: Int): MeshSubGroup {
        // Parked dump truck at (X = +11, Z = -2)
        val env = MeshBuilder(seg)
        val y = MeshBuilder(seg)
        val blk = MeshBuilder(seg)
        val glass = MeshBuilder(seg)

        // Chassis & cab
        blk.addBox(0f, 0.78f, 0f, 1.6f, 0.24f, 5.4f, 0.14f, 0.15f, 0.18f)
        env.addBox(0f, 1.65f, 1.95f, 2.0f, 1.5f, 1.4f, 0.85f, 0.87f, 0.90f)
        glass.addBox(0f, 1.85f, 2.66f, 1.72f, 0.65f, 0.04f, 0.40f, 0.65f, 0.78f)
        // Tipper dump bed
        y.addBox(0f, 1.55f, -0.85f, 2.25f, 1.10f, 3.45f, 0.88f, 0.52f, 0.10f)
        // 6 heavy truck tyres
        for (sx in listOf(-0.98f, 0.98f)) {
            for (sz in listOf(1.9f, -0.6f, -1.9f)) {
                blk.addCylinder(sx, 0.54f, sz, 0.54f, 0.54f, 0.34f, 0, 0.10f, 0.10f, 0.12f, seg)
            }
        }
        return MeshSubGroup(
            listOf(
                blk.build(MaterialStyle.RUBBER_TYRE),
                env.build(MaterialStyle.WORKSHOP_ENV),
                y.build(MaterialStyle.JCB_YELLOW),
                glass.build(MaterialStyle.CAB_GLASS)
            )
        )
    }

    private fun buildBucketSoilLoad(): MeshSubGroup {
        val soil = MeshBuilder(8)
        soil.addBox(0f, 0f, 0.10f, 1.95f, 0.28f, 0.52f, 0.48f, 0.34f, 0.20f)
        return MeshSubGroup(listOf(soil.build(MaterialStyle.SOIL_EARTH)))
    }
}

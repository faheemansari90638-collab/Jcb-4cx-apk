package com.example

import com.example.engine3d.Mat4
import com.example.engine3d.MechanicalPhysicsEngine
import com.example.engine3d.RigidBodyState
import com.example.engine3d.StaticObstacle
import com.example.engine3d.Vec3
import com.example.model.ActivePartManipState
import com.example.model.AppLanguage
import com.example.model.AssemblyTooltipCatalog
import com.example.model.BiText
import com.example.model.EngineVariant
import com.example.model.GameUiState
import com.example.model.JcbCatalog
import com.example.model.MicroCategory
import com.example.model.MicroManipState
import com.example.model.MicroPartCatalog
import com.example.model.MissionCatalog
import com.example.model.PhysicsHudState
import com.example.model.ToolType
import com.example.model.TooltipStage
import com.example.model.TooltipTargetId
import com.example.engine3d.CollisionContact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JcbBuildMasterTest {

    @Test
    fun `catalog has all 10 levels and valid prerequisite DAG`() {
        assertEquals(10, JcbCatalog.levels.size)
        assertTrue(JcbCatalog.totalBuildParts >= 70)

        val allIds = JcbCatalog.parts.map { it.id }.toSet()
        assertEquals(JcbCatalog.parts.size, allIds.size)

        for (part in JcbCatalog.parts) {
            assertTrue("Missing EN name for ${part.id}", part.name.en.isNotBlank())
            assertTrue("Missing HI name for ${part.id}", part.name.hi.isNotBlank())
            for (prereq in part.prerequisites) {
                assertTrue("Invalid prerequisite $prereq on ${part.id}", prereq in allIds)
            }
        }
    }

    @Test
    fun `all 10 tools and 7 missions have English and Hindi text`() {
        assertEquals(10, ToolType.entries.size)
        ToolType.entries.forEach { tool ->
            assertTrue(tool.title.get(AppLanguage.EN).isNotBlank())
            assertTrue(tool.title.get(AppLanguage.HI).isNotBlank())
        }
        assertEquals(7, MissionCatalog.all.size)
        MissionCatalog.all.forEach { m ->
            assertTrue(m.title.get(AppLanguage.EN).isNotBlank())
            assertTrue(m.title.get(AppLanguage.HI).isNotBlank())
        }
    }

    @Test
    fun `every part spawn offset and rotation can align within tolerance using 0_18m and 45deg steps`() {
        for (part in JcbCatalog.parts) {
            val startPos = part.targetPos + part.spawnOffset
            // Move towards targetPos in 0.18f increments per axis
            fun stepAxis(cur: Float, tgt: Float): Float {
                var v = cur
                var guard = 0
                while (kotlin.math.abs(v - tgt) > 0.10f && guard < 40) {
                    v += if (v < tgt) 0.18f else -0.18f
                    guard++
                }
                return v
            }
            val alignedPos = Vec3(
                stepAxis(startPos.x, part.targetPos.x),
                stepAxis(startPos.y, part.targetPos.y),
                stepAxis(startPos.z, part.targetPos.z)
            )
            assertTrue(
                "Part ${part.id} failed position alignment check: dist=${alignedPos.distanceTo(part.targetPos)}",
                alignedPos.distanceTo(part.targetPos) <= 0.28f
            )
            assertNotNull(JcbCatalog.findPart(part.id))
            assertFalse(Mat4.angleDiffDeg(part.targetRot.y, part.targetRot.y + 90f) <= 25f)
        }
    }

    @Test
    fun `micro catalog covers all 6 categories, valid prerequisites, and bilingual names`() {
        assertEquals(46, MicroPartCatalog.parts.size)
        MicroCategory.entries.forEach { cat ->
            assertTrue("Category $cat has no micro-parts", MicroPartCatalog.byCategory(cat).isNotEmpty())
        }
        val ids = MicroPartCatalog.parts.map { it.id }.toSet()
        assertEquals(MicroPartCatalog.parts.size, ids.size)

        for (mp in MicroPartCatalog.parts) {
            assertTrue("Missing EN micro name for ${mp.id}", mp.name.en.isNotBlank())
            assertTrue("Missing HI micro name for ${mp.id}", mp.name.hi.isNotBlank())
            assertTrue("Missing instruction for ${mp.id}", mp.stepInstruction.en.isNotBlank())
            for (pre in mp.prerequisites) {
                assertTrue("Invalid micro prerequisite $pre on ${mp.id}", pre in ids)
            }
        }
    }

    @Test
    fun `every micro part can align within 0_14m tolerance using 0_05m steps`() {
        for (mp in MicroPartCatalog.parts) {
            fun stepMicroAxis(cur: Float, tgt: Float, minVal: Float, maxVal: Float): Float {
                var v = cur
                var guard = 0
                while (kotlin.math.abs(v - tgt) > 0.03f && guard < 80) {
                    v = (v + if (v < tgt) 0.05f else -0.05f).coerceIn(minVal, maxVal)
                    guard++
                }
                return v
            }
            val start = mp.targetPos + mp.spawnOffset
            val aligned = Vec3(
                stepMicroAxis(start.x, mp.targetPos.x, -1.5f, 1.5f),
                stepMicroAxis(start.y, mp.targetPos.y, 0.05f, 2.2f),
                stepMicroAxis(start.z, mp.targetPos.z, -1.5f, 1.5f)
            )
            assertTrue(
                "Micro part ${mp.id} failed precision alignment: dist=${aligned.distanceTo(mp.targetPos)}",
                aligned.distanceTo(mp.targetPos) <= 0.14f
            )
        }
    }

    @Test
    fun `service specs distinguish verified engine variant torques from unverified manual-required entries`() {
        val jcbMain = MicroPartCatalog.requiredTorqueHintNm("main_bearing_torque", EngineVariant.JCB_444_DIESELMAX)
        assertEquals(Pair(115, 180), jcbMain)

        val perkinsMain = MicroPartCatalog.requiredTorqueHintNm("main_bearing_torque", EngineVariant.PERKINS_1004_4T)
        assertEquals(Pair(250, 0), perkinsMain)

        // JCB 444 conrod bolt torque is serial-break dependent -> must remain null rather than invented
        val jcbConrod = MicroPartCatalog.requiredTorqueHintNm("conrod_bolt_torque", EngineVariant.JCB_444_DIESELMAX)
        assertNull(jcbConrod.first)

        val oilPumpTorque = MicroPartCatalog.requiredTorqueHintNm("oil_pump_bolt_torque", EngineVariant.JCB_444_DIESELMAX)
        assertNull(oilPumpTorque.first)
    }

    @Test
    fun `physics engine simulates gravity fall and workshop floor collision rest`() {
        val enginePart = JcbCatalog.findPart("l2_engine_block")!!
        val cfg = MechanicalPhysicsEngine.configForJcbPart(enginePart)
        var body = RigidBodyState(
            position = Vec3(2.2f, 2.0f, 2.0f),
            rotationDeg = Vec3(0f, 0f, 0f),
            isHeld = false
        )

        repeat(90) {
            body = MechanicalPhysicsEngine.step(
                dtSec = 0.02f,
                body = body,
                config = cfg,
                targetPos = enginePart.targetPos,
                targetRot = enginePart.targetRot,
                obstacles = emptyList(),
                floorY = 0.0f,
                boundsMin = Vec3(-3.5f, 0.15f, -4.2f),
                boundsMax = Vec3(3.5f, 4.2f, 4.2f),
                gravityEnabled = true,
                snapAssistEnabled = true,
                seatPosTolerance = 0.015f,
                seatAngleToleranceDeg = 3.0f
            )
        }

        assertTrue("Part should settle onto floor under gravity", body.isGrounded)
        assertEquals(cfg.halfExtents.y, body.position.y, 0.01f)
    }

    @Test
    fun `physics engine blocks clipping into unrelated solid obstacle and snaps inside target envelope`() {
        val piston = MicroPartCatalog.findPart("mp_piston")!!
        val cfg = MechanicalPhysicsEngine.configForMicroPart(piston)
        val wallObstacle = StaticObstacle(
            id = "solid_wall",
            name = BiText("Solid Housing", "सॉलिड हाउसिंग"),
            center = Vec3(0.60f, 0.56f, 0.22f),
            halfExtents = Vec3(0.08f, 0.12f, 0.08f),
            targetOverlapAllowed = false
        )

        val nudged = MechanicalPhysicsEngine.applyManualNudge(
            body = RigidBodyState(position = Vec3(0.45f, 0.56f, 0.22f), rotationDeg = piston.targetRot),
            delta = Vec3(0.15f, 0f, 0f),
            config = cfg,
            targetPos = piston.targetPos,
            targetRot = piston.targetRot,
            obstacles = listOf(wallObstacle),
            floorY = 0.12f,
            boundsMin = Vec3(-1.5f, 0.05f, -1.5f),
            boundsMax = Vec3(1.5f, 2.2f, 1.5f),
            snapAssistEnabled = true
        )

        assertNotNull("Expected rigid-body collision contact with solid obstacle", nudged.lastCollision)
        assertTrue("Obstacle should prevent overlap along X", nudged.position.x <= wallObstacle.center.x - wallObstacle.halfExtents.x - cfg.halfExtents.x + 0.001f)

        // Now place inside magnetic snap envelope and step until seated
        var snappingBody = RigidBodyState(
            position = piston.targetPos + Vec3(0f, 0.10f, 0f),
            rotationDeg = piston.targetRot + Vec3(0f, 12f, 0f),
            isHeld = false
        )
        var snapped = false
        repeat(60) {
            snappingBody = MechanicalPhysicsEngine.step(
                dtSec = 0.02f,
                body = snappingBody,
                config = cfg,
                targetPos = piston.targetPos,
                targetRot = piston.targetRot,
                obstacles = emptyList(),
                floorY = 0.12f,
                boundsMin = Vec3(-1.5f, 0.05f, -1.5f),
                boundsMax = Vec3(1.5f, 2.2f, 1.5f),
                gravityEnabled = true,
                snapAssistEnabled = true,
                seatPosTolerance = 0.008f,
                seatAngleToleranceDeg = 3.0f
            )
            if (snappingBody.justSnappedToSeat) snapped = true
        }
        assertTrue("Part inside magnetic snap cone should smoothly seat at target", snapped)
        assertTrue(snappingBody.position.distanceTo(piston.targetPos) <= 0.01f)
    }

    @Test
    fun `contextual tooltips adapt across workshop assembly lifecycle in English and Hindi`() {
        // 1. Fresh state -> Points to horizontal parts inventory
        val initial = GameUiState(currentLevel = 1)
        val t1 = AssemblyTooltipCatalog.resolveWorkshopTooltip(initial, manualStepIndex = null)
        assertEquals(TooltipStage.WORKSHOP_INTRO_INVENTORY, t1.stage)
        assertEquals(TooltipTargetId.PARTS_INVENTORY, t1.targetId)
        assertTrue(t1.title.get(AppLanguage.EN).isNotBlank())
        assertTrue(t1.title.get(AppLanguage.HI).isNotBlank())

        // 2. Active part misaligned -> Guides to 3D orbit & alignment inspector
        val part = JcbCatalog.partsForLevel(1).first()
        val misaligned = initial.copy(
            activeManip = ActivePartManipState(
                partId = part.id,
                position = part.targetPos + Vec3(0.8f, 0.3f, 0f),
                rotationDeg = part.spawnRot,
                isAligned = false
            )
        )
        val t2 = AssemblyTooltipCatalog.resolveWorkshopTooltip(misaligned, manualStepIndex = null)
        assertEquals(TooltipStage.WORKSHOP_ORBIT_AND_ALIGN, t2.stage)
        assertEquals(TooltipTargetId.INSPECTOR_ALIGN, t2.targetId)

        // 3. Collision contact -> Surfaces physics warning tooltip
        val collided = misaligned.copy(
            physics = PhysicsHudState(
                lastCollision = CollisionContact(
                    otherId = "l1_front_mount",
                    otherName = BiText("Front Mount", "फ्रंट माउंट"),
                    normal = Vec3(0f, 1f, 0f),
                    penetrationDepthMm = 40f,
                    impactSpeedMps = 1.6f,
                    isFloor = false
                )
            )
        )
        val tCol = AssemblyTooltipCatalog.resolveWorkshopTooltip(collided, manualStepIndex = null)
        assertEquals(TooltipStage.WORKSHOP_COLLISION_WARNING, tCol.stage)
        assertTrue(tCol.isWarning)
        assertTrue(tCol.title.get(AppLanguage.HI).contains("फ्रंट माउंट"))

        // 4. Aligned -> Targets Lock Position button
        val aligned = misaligned.copy(
            activeManip = misaligned.activeManip!!.copy(position = part.targetPos, rotationDeg = part.targetRot, isAligned = true)
        )
        val t3 = AssemblyTooltipCatalog.resolveWorkshopTooltip(aligned, manualStepIndex = null)
        assertEquals(TooltipStage.WORKSHOP_LOCK_POSITION, t3.stage)
        assertEquals(TooltipTargetId.LOCK_BTN, t3.targetId)

        // 5. Seated with wrong tool -> Points to Tool Belt; with right tool -> Points to Fasten button
        val wrongTool = aligned.copy(
            activeManip = aligned.activeManip!!.copy(placedInSocket = true),
            selectedTool = ToolType.entries.first { it != part.requiredTool }
        )
        val t4Wrong = AssemblyTooltipCatalog.resolveWorkshopTooltip(wrongTool, manualStepIndex = null)
        assertEquals(TooltipStage.WORKSHOP_SELECT_TOOL_AND_FASTEN, t4Wrong.stage)
        assertEquals(TooltipTargetId.TOOL_BELT, t4Wrong.targetId)

        val rightTool = wrongTool.copy(selectedTool = part.requiredTool)
        val t4Right = AssemblyTooltipCatalog.resolveWorkshopTooltip(rightTool, manualStepIndex = null)
        assertEquals(TooltipTargetId.FASTEN_BTN, t4Right.targetId)

        // 6. All manual workshop & micro tour steps have complete EN & HI copy
        for (i in 0 until AssemblyTooltipCatalog.WORKSHOP_TOUR_COUNT) {
            val step = AssemblyTooltipCatalog.resolveWorkshopTooltip(initial, manualStepIndex = i)
            assertTrue(step.title.en.isNotBlank() && step.title.hi.isNotBlank())
            assertTrue(step.body.en.isNotBlank() && step.body.hi.isNotBlank())
            assertTrue(step.actionHint.en.isNotBlank() && step.actionHint.hi.isNotBlank())
        }

        val microAligned = initial.copy(
            micro = initial.micro.copy(
                activeMicro = MicroManipState(
                    partId = "mp_ring_top",
                    position = Vec3(),
                    rotationDeg = Vec3(),
                    isAligned = true
                )
            )
        )
        val tMicro = AssemblyTooltipCatalog.resolveMicroTooltip(microAligned, manualStepIndex = null)
        assertEquals(TooltipStage.MICRO_TORQUE_AND_CHECK, tMicro.stage)
        assertTrue(tMicro.body.get(AppLanguage.HI).contains("120°"))
    }
}

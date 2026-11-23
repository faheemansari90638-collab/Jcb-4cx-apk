package com.example.model

import com.example.engine3d.Vec3

enum class EngineVariant(val title: BiText, val code: String, val years: String) {
    JCB_444_DIESELMAX(
        title = BiText("JCB 444 Dieselmax (4.4L 16V)", "JCB 444 डीजलमैक्स (4.4L 16V)"),
        code = "JCB 444 (Pub 9806/3000)",
        years = "2005–2014 (SA/SB/SC/SD/SF)"
    ),
    PERKINS_1004_4T(
        title = BiText("Perkins 1004-4T (4.0L 8V)", "पर्किन्स 1004-4T (4.0L 8V)"),
        code = "Perkins 1000 Series T4.40",
        years = "1991–2004 Classic 4CX"
    )
}

enum class MicroCategory(val title: BiText) {
    BOTTOM_END(BiText("Block, Crank & Bearings", "ब्लॉक, क्रैंक और बेयरिंग")),
    PISTON_CONROD(BiText("Pistons, Rings & Conrod", "पिस्टन, रिंग और कॉनरॉड")),
    OIL_PUMP(BiText("Oil Pump & Sump", "ऑयल पंप और गैस्केट")),
    VALVETRAIN_HEAD(BiText("Head, Valves & Cam", "हेड, वाल्व और कैमशाफ्ट")),
    INJECTOR_COVERS(BiText("Injectors & Fasteners", "इंजेक्टर, स्क्रू और वॉशर")),
    HYDRAULIC_SPOOL(BiText("Hydraulic Spool & Seals", "हाइड्रोलिक स्पूल और सील"))
}

enum class MicroSimType {
    NONE,
    PISTON_RING_FIT,
    VALVE_SPRING_COMPRESS,
    BEARING_CLEARANCE,
    CRANK_ROTATION,
    GASKET_SEAL,
    HYDRAULIC_SPOOL_SEAL,
    INJECTOR_STACK,
    TORQUE_WRENCH_SPEC
}

data class ServiceSpecEntry(
    val key: String,
    val label: BiText,
    val jcb444VerifiedValue: String?, // null = unknown/serial-dependent -> prompt user!
    val perkins1004VerifiedValue: String?,
    val unit: String,
    val sourceNote: BiText
) {
    fun verifiedValueFor(variant: EngineVariant): String? = when (variant) {
        EngineVariant.JCB_444_DIESELMAX -> jcb444VerifiedValue
        EngineVariant.PERKINS_1004_4T -> perkins1004VerifiedValue
    }
}

data class MicroPartSpec(
    val id: String,
    val category: MicroCategory,
    val name: BiText,
    val stepInstruction: BiText,
    val targetPos: Vec3,
    val targetRot: Vec3 = Vec3(0f, 0f, 0f),
    val explodedOffset: Vec3,
    val spawnOffset: Vec3,
    val spawnRot: Vec3 = Vec3(0f, 90f, 0f),
    val targetDepthMm: Float,
    val requiredTool: ToolType,
    val torqueSpecKey: String? = null,
    val simType: MicroSimType = MicroSimType.NONE,
    val isInternalHidden: Boolean = false,
    val prerequisites: List<String> = emptyList(),
    val xpReward: Int = 25
)

object MicroPartCatalog {

    val serviceSpecs: List<ServiceSpecEntry> = listOf(
        ServiceSpecEntry(
            key = "bore_stroke",
            label = BiText("Cylinder Bore x Stroke", "सिलेंडर बोर x स्ट्रोक"),
            jcb444VerifiedValue = "103.00–103.02 mm x 132 mm",
            perkins1004VerifiedValue = "100.00 mm x 127 mm",
            unit = "mm",
            sourceNote = BiText("Verified: JCB 444 Manual / Perkins 1000 Spec", "सत्यापित: JCB 444 / पर्किन्स मैनुअल")
        ),
        ServiceSpecEntry(
            key = "firing_order",
            label = BiText("Firing Order", "फायरिंग ऑर्डर"),
            jcb444VerifiedValue = "1-3-4-2",
            perkins1004VerifiedValue = "1-3-4-2",
            unit = "seq",
            sourceNote = BiText("Verified: 4-cylinder inline standard", "सत्यापित: 4-सिलेंडर मानक")
        ),
        ServiceSpecEntry(
            key = "main_bearing_torque",
            label = BiText("Main Bearing Bolt Torque", "मेन बेयरिंग बोल्ट टॉर्क"),
            jcb444VerifiedValue = "50 Nm -> 115 Nm + 180°",
            perkins1004VerifiedValue = "250 Nm (185 lb-ft)",
            unit = "Nm / deg",
            sourceNote = BiText("Verified: JCB 444 bedplate / Perkins main caps", "सत्यापित: मैनुअल टॉर्क डेटा")
        ),
        ServiceSpecEntry(
            key = "head_bolt_torque",
            label = BiText("Cylinder Head Bolt Torque", "सिलेंडर हेड बोल्ट टॉर्क"),
            jcb444VerifiedValue = "40 -> 75 -> 75 Nm + 90° + 180°",
            perkins1004VerifiedValue = "110 Nm + 150°/180°/210°",
            unit = "Nm / deg",
            sourceNote = BiText("Verified: Center-out spiral sequence", "सत्यापित: केंद्र से बाहर क्रम")
        ),
        ServiceSpecEntry(
            key = "conrod_bolt_torque",
            label = BiText("Connecting Rod Bolt Torque", "कनेक्टिंग रॉड बोल्ट टॉर्क"),
            jcb444VerifiedValue = null, // Serial-break dependent on split-fracture JCB 444 rods!
            perkins1004VerifiedValue = "155 Nm (114 lb-ft) bolt / 125 Nm nut",
            unit = "Nm",
            sourceNote = BiText("JCB 444 split-fracture rods vary by serial break — enter verified manual data", "JCB 444 सीरियल अनुसार भिन्न — मैनुअल डेटा दर्ज करें")
        ),
        ServiceSpecEntry(
            key = "valve_clearance_cold",
            label = BiText("Valve Tip Clearance (Cold)", "वाल्व क्लियरेंस (ठंडा इंजन)"),
            jcb444VerifiedValue = "In: 0.19–0.27mm (0.35 SD/SF) | Ex: 0.56–0.64mm",
            perkins1004VerifiedValue = "In: 0.20 mm | Ex: 0.45–0.46 mm",
            unit = "mm",
            sourceNote = BiText("Verified: SA/SB/SC vs SD/SF / Perkins 1004", "सत्यापित: कोल्ड वाल्व क्लियरेंस")
        ),
        ServiceSpecEntry(
            key = "crank_end_float",
            label = BiText("Crankshaft End Float", "क्रैंकशाफ्ट एंड फ्लोट"),
            jcb444VerifiedValue = "0.05–0.28 mm",
            perkins1004VerifiedValue = null,
            unit = "mm",
            sourceNote = BiText("Verified for JCB 444; verify Perkins thrust spec", "JCB 444 हेतु सत्यापित")
        ),
        ServiceSpecEntry(
            key = "conrod_side_clearance",
            label = BiText("Conrod Side Clearance", "कॉनरॉड साइड क्लियरेंस"),
            jcb444VerifiedValue = "0.05–0.40 mm",
            perkins1004VerifiedValue = null,
            unit = "mm",
            sourceNote = BiText("Verified for JCB 444", "JCB 444 हेतु सत्यापित")
        ),
        ServiceSpecEntry(
            key = "main_bearing_oil_clearance",
            label = BiText("Main Bearing Oil Clearance", "मेन बेयरिंग ऑयल क्लियरेंस"),
            jcb444VerifiedValue = null,
            perkins1004VerifiedValue = null,
            unit = "mm",
            sourceNote = BiText("Verify exact journal grade in engine serial manual", "इंजन सीरियल मैनुअल से सत्यापित करें")
        ),
        ServiceSpecEntry(
            key = "piston_ring_end_gap",
            label = BiText("Piston Ring End Gap", "पिस्टन रिंग एंड गैप"),
            jcb444VerifiedValue = null,
            perkins1004VerifiedValue = null,
            unit = "mm",
            sourceNote = BiText("Verify ring pack spec from piston supplier/manual", "पिस्टन मैनुअल से सत्यापित करें")
        ),
        ServiceSpecEntry(
            key = "oil_pump_bolt_torque",
            label = BiText("Oil Pump & Sump Fastener Torque", "ऑयल पंप बोल्ट टॉर्क"),
            jcb444VerifiedValue = null,
            perkins1004VerifiedValue = null,
            unit = "Nm",
            sourceNote = BiText("Enter verified manual torque for M8/M10 fasteners", "मैनुअल टॉर्क मान दर्ज करें")
        ),
        ServiceSpecEntry(
            key = "injector_clamp_torque",
            label = BiText("Injector Clamp & Rocker Nut Torque", "इंजेक्टर क्लैंप और रॉकर नट टॉर्क"),
            jcb444VerifiedValue = null,
            perkins1004VerifiedValue = null,
            unit = "Nm",
            sourceNote = BiText("Enter verified manual torque before final tightening", "सत्यापित मैनुअल मान दर्ज करें")
        )
    )

    // Numeric target values ONLY where verified from JCB 444 / Perkins 1004 manuals
    fun requiredTorqueHintNm(key: String?, variant: EngineVariant): Pair<Int?, Int?> {
        return when (key) {
            "main_bearing_torque" -> if (variant == EngineVariant.JCB_444_DIESELMAX) Pair(115, 180) else Pair(250, 0)
            "head_bolt_torque" -> if (variant == EngineVariant.JCB_444_DIESELMAX) Pair(75, 180) else Pair(110, 180)
            "conrod_bolt_torque" -> if (variant == EngineVariant.PERKINS_1004_4T) Pair(155, 0) else Pair(null, null)
            else -> Pair(null, null)
        }
    }

    val parts: List<MicroPartSpec> = listOf(
        // ==================== 1. BLOCK, CRANK & BEARINGS ====================
        MicroPartSpec(
            id = "mp_engine_block",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Cylinder Block Casting", "सिलेंडर ब्लॉक कास्टिंग"),
            stepInstruction = BiText("Mount the bare 4-cylinder block onto the workshop engine stand.", "वर्कशॉप इंजन स्टैंड पर 4-सिलेंडर ब्लॉक लगाएं।"),
            targetPos = Vec3(0f, 0.55f, 0f),
            explodedOffset = Vec3(0f, 0f, 0f),
            spawnOffset = Vec3(0f, 0.45f, 0f),
            spawnRot = Vec3(0f, 45f, 0f),
            targetDepthMm = 0f,
            requiredTool = ToolType.ENGINE_CRANE,
            xpReward = 40
        ),
        MicroPartSpec(
            id = "mp_main_bearing_shell_upper",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Upper Main Bearing Shell (Grooved)", "ऊपरी मेन बेयरिंग शेल"),
            stepInstruction = BiText("Align oil feed hole with block gallery and seat upper bearing shell into saddle.", "ऑयल होल मिलाकर ऊपरी बेयरिंग शेल को ब्लॉक सैडल में बैठाएं।"),
            targetPos = Vec3(0f, 0.34f, 0f),
            explodedOffset = Vec3(0f, -0.25f, 0f),
            spawnOffset = Vec3(0.45f, -0.15f, 0.2f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 18f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.BEARING_CLEARANCE,
            isInternalHidden = true,
            prerequisites = listOf("mp_engine_block")
        ),
        MicroPartSpec(
            id = "mp_crankshaft",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Forged Steel Crankshaft", "फोर्ज्ड स्टील क्रैंकशाफ्ट"),
            stepInstruction = BiText("Hoist crankshaft gently into upper main bearings and verify free rotation.", "क्रैंकशाफ्ट को बेयरिंग में रखें और घुमाव की जांच करें।"),
            targetPos = Vec3(0f, 0.30f, 0f),
            explodedOffset = Vec3(0f, -0.55f, 0f),
            spawnOffset = Vec3(0f, -0.45f, 0.5f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 42f,
            requiredTool = ToolType.ENGINE_CRANE,
            simType = MicroSimType.CRANK_ROTATION,
            isInternalHidden = true,
            prerequisites = listOf("mp_main_bearing_shell_upper"),
            xpReward = 50
        ),
        MicroPartSpec(
            id = "mp_main_bearing_shell_lower",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Lower Main Bearing Shell", "निचला मेन बेयरिंग शेल"),
            stepInstruction = BiText("Seat lower bearing shell into main bearing cap and check Plastigage clearance.", "निचले बेयरिंग शेल को कैप में बैठाएं और क्लियरेंस जांचें।"),
            targetPos = Vec3(0f, 0.25f, 0f),
            explodedOffset = Vec3(0f, -0.78f, 0f),
            spawnOffset = Vec3(-0.45f, -0.20f, 0.2f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 18f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.BEARING_CLEARANCE,
            isInternalHidden = true,
            prerequisites = listOf("mp_crankshaft")
        ),
        MicroPartSpec(
            id = "mp_main_bearing_cap",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Main Bearing Cap", "मेन बेयरिंग कैप"),
            stepInstruction = BiText("Position numbered main bearing cap squarely over crankshaft journal.", "नंबर वाला मेन बेयरिंग कैप क्रैंकशाफ्ट जर्नल पर बैठाएं।"),
            targetPos = Vec3(0f, 0.20f, 0f),
            explodedOffset = Vec3(0f, -0.95f, 0f),
            spawnOffset = Vec3(0.50f, -0.25f, 0.1f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 26f,
            requiredTool = ToolType.HAMMER,
            isInternalHidden = true,
            prerequisites = listOf("mp_main_bearing_shell_lower")
        ),
        MicroPartSpec(
            id = "mp_main_bearing_bolt_1",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Main Bearing Bolt (Left)", "मेन बेयरिंग बोल्ट (बायां)"),
            stepInstruction = BiText("Thread left main bearing bolt and torque to verified manual specification.", "बायां मेन बेयरिंग बोल्ट लगाएं और टॉर्क रिंच से कसें।"),
            targetPos = Vec3(-0.09f, 0.17f, 0f),
            explodedOffset = Vec3(-0.09f, -1.15f, 0f),
            spawnOffset = Vec3(-0.45f, -0.35f, 0.2f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 48f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "main_bearing_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            isInternalHidden = true,
            prerequisites = listOf("mp_main_bearing_cap")
        ),
        MicroPartSpec(
            id = "mp_main_bearing_bolt_2",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Main Bearing Bolt (Right)", "मेन बेयरिंग बोल्ट (दायां)"),
            stepInstruction = BiText("Thread right main bearing bolt and torque to verified manual specification.", "दायां मेन बेयरिंग बोल्ट लगाएं और टॉर्क रिंच से कसें।"),
            targetPos = Vec3(0.09f, 0.17f, 0f),
            explodedOffset = Vec3(0.09f, -1.15f, 0f),
            spawnOffset = Vec3(0.45f, -0.35f, 0.2f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 48f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "main_bearing_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            isInternalHidden = true,
            prerequisites = listOf("mp_main_bearing_bolt_1")
        ),
        MicroPartSpec(
            id = "mp_front_crank_oil_seal",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Front Crankshaft Oil Seal", "फ्रंट क्रैंकशाफ्ट ऑयल सील"),
            stepInstruction = BiText("Press front Viton oil seal squarely into timing cover bore (10 mm depth).", "फ्रंट ऑयल सील को 10 mm गहराई तक सीधा प्रेस करें।"),
            targetPos = Vec3(0f, 0.30f, 0.36f),
            explodedOffset = Vec3(0f, 0f, 0.35f),
            spawnOffset = Vec3(0.45f, 0.1f, 0.45f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 10f,
            requiredTool = ToolType.HAMMER,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_main_bearing_bolt_2")
        ),
        MicroPartSpec(
            id = "mp_rear_crank_oil_seal",
            category = MicroCategory.BOTTOM_END,
            name = BiText("Rear Crankshaft Oil Seal", "रियर क्रैंकशाफ्ट ऑयल सील"),
            stepInstruction = BiText("Press rear flywheel oil seal without nicking lip.", "रियर क्रैंक ऑयल सील को सावधानी से फिट करें।"),
            targetPos = Vec3(0f, 0.30f, -0.36f),
            explodedOffset = Vec3(0f, 0f, -0.35f),
            spawnOffset = Vec3(-0.45f, 0.1f, -0.45f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 10f,
            requiredTool = ToolType.HAMMER,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_main_bearing_bolt_2")
        ),

        // ==================== 2. PISTONS, RINGS & CONROD ====================
        MicroPartSpec(
            id = "mp_piston",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Cylinder #1 Aluminum Piston & Rod", "सिलेंडर #1 पिस्टन और रॉड"),
            stepInstruction = BiText("Insert piston & connecting rod assembly aligned with cylinder bore #1.", "पिस्टन और कनेक्टिंग रॉड को सिलेंडर बोर #1 में रखें।"),
            targetPos = Vec3(0f, 0.56f, 0.22f),
            explodedOffset = Vec3(0f, 0.45f, 0f),
            spawnOffset = Vec3(0.55f, 0.40f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 65f,
            requiredTool = ToolType.HAMMER,
            isInternalHidden = true,
            prerequisites = listOf("mp_crankshaft")
        ),
        MicroPartSpec(
            id = "mp_ring_oil",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Oil Control Piston Ring (Groove 3)", "ऑयल कंट्रोल रिंग (खांचा 3)"),
            stepInstruction = BiText("Fit oil control ring into bottom groove and set 120° gap stagger.", "सबसे निचले खांचे में ऑयल रिंग लगाएं और गैप 120° पर रखें।"),
            targetPos = Vec3(0f, 0.57f, 0.22f),
            explodedOffset = Vec3(0.28f, 0.52f, 0f),
            spawnOffset = Vec3(0.45f, 0.45f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 3.5f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.PISTON_RING_FIT,
            isInternalHidden = true,
            prerequisites = listOf("mp_piston")
        ),
        MicroPartSpec(
            id = "mp_ring_scraper",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Scraper Compression Ring (Groove 2)", "स्क्रैपर रिंग (खांचा 2)"),
            stepInstruction = BiText("Install 2nd tapered scraper ring with 'TOP' mark facing upward.", "'TOP' निशान ऊपर रखते हुए दूसरी स्क्रैपर रिंग फिट करें।"),
            targetPos = Vec3(0f, 0.59f, 0.22f),
            explodedOffset = Vec3(-0.28f, 0.62f, 0f),
            spawnOffset = Vec3(-0.45f, 0.50f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 3.2f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.PISTON_RING_FIT,
            isInternalHidden = true,
            prerequisites = listOf("mp_ring_oil")
        ),
        MicroPartSpec(
            id = "mp_ring_top",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Top Compression Ring (Groove 1)", "टॉप कम्प्रेशन रिंग (खांचा 1)"),
            stepInstruction = BiText("Fit top keystone compression ring and compress ring pack into cylinder.", "ऊपरी कम्प्रेशन रिंग फिट करके रिंग पैक को बोर में बैठाएं।"),
            targetPos = Vec3(0f, 0.61f, 0.22f),
            explodedOffset = Vec3(0.28f, 0.72f, 0f),
            spawnOffset = Vec3(0.45f, 0.55f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 3.0f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.PISTON_RING_FIT,
            isInternalHidden = true,
            prerequisites = listOf("mp_ring_scraper")
        ),
        MicroPartSpec(
            id = "mp_conrod_bearing_shell",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Big-End Conrod Bearing Shell", "कॉनरॉड बिग-एंड बेयरिंग शेल"),
            stepInstruction = BiText("Snap tang-locked bearing shell into conrod cap and check journal clearance.", "लॉक टैंग मिलाकर कॉनरॉड बेयरिंग शेल बैठाएं और क्लियरेंस जांचें।"),
            targetPos = Vec3(0f, 0.31f, 0.22f),
            explodedOffset = Vec3(0f, -0.45f, 0.22f),
            spawnOffset = Vec3(-0.40f, -0.15f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 14f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.BEARING_CLEARANCE,
            isInternalHidden = true,
            prerequisites = listOf("mp_ring_top")
        ),
        MicroPartSpec(
            id = "mp_conrod_cap",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Match-Fractured Conrod Cap", "कॉनरॉड कैप"),
            stepInstruction = BiText("Seat matched split-fracture cap onto conrod big end (do NOT mix caps!).", "सही जोड़ी वाला कॉनरॉड कैप बैठाएं।"),
            targetPos = Vec3(0f, 0.26f, 0.22f),
            explodedOffset = Vec3(0f, -0.65f, 0.22f),
            spawnOffset = Vec3(0.45f, -0.25f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 20f,
            requiredTool = ToolType.HAMMER,
            isInternalHidden = true,
            prerequisites = listOf("mp_conrod_bearing_shell")
        ),
        MicroPartSpec(
            id = "mp_conrod_bolt_1",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Connecting Rod Bolt #1", "कॉनरॉड बोल्ट #1"),
            stepInstruction = BiText("Tighten conrod bolt #1 using Torque Wrench per verified manual spec.", "कॉनरॉड बोल्ट #1 को सत्यापित टॉर्क पर कसें।"),
            targetPos = Vec3(-0.05f, 0.23f, 0.22f),
            explodedOffset = Vec3(-0.05f, -0.82f, 0.22f),
            spawnOffset = Vec3(-0.40f, -0.35f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 38f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "conrod_bolt_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            isInternalHidden = true,
            prerequisites = listOf("mp_conrod_cap")
        ),
        MicroPartSpec(
            id = "mp_conrod_bolt_2",
            category = MicroCategory.PISTON_CONROD,
            name = BiText("Connecting Rod Bolt #2", "कॉनरॉड बोल्ट #2"),
            stepInstruction = BiText("Tighten conrod bolt #2 and perform crankshaft spin check.", "कॉनरॉड बोल्ट #2 कसें और क्रैंकशाफ्ट घुमाव जांचें।"),
            targetPos = Vec3(0.05f, 0.23f, 0.22f),
            explodedOffset = Vec3(0.05f, -0.82f, 0.22f),
            spawnOffset = Vec3(0.40f, -0.35f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 38f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "conrod_bolt_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            isInternalHidden = true,
            prerequisites = listOf("mp_conrod_bolt_1")
        ),

        // ==================== 3. OIL PUMP & SUMP ====================
        MicroPartSpec(
            id = "mp_oil_pump_rotor",
            category = MicroCategory.OIL_PUMP,
            name = BiText("Gerotor Oil Pump Assembly", "गियर-रोटर ऑयल पंप"),
            stepInstruction = BiText("Align drive tang and mount oil pump body onto front block face.", "ड्राइव टैंग मिलाकर ऑयल पंप को ब्लॉक पर लगाएं।"),
            targetPos = Vec3(0.14f, 0.25f, 0.30f),
            explodedOffset = Vec3(0.28f, -0.28f, 0.32f),
            spawnOffset = Vec3(0.55f, 0.1f, 0.45f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 24f,
            requiredTool = ToolType.SOCKET_WRENCH,
            isInternalHidden = true,
            prerequisites = listOf("mp_crankshaft")
        ),
        MicroPartSpec(
            id = "mp_oil_pump_oring",
            category = MicroCategory.OIL_PUMP,
            name = BiText("Oil Pump Pickup O-Ring", "ऑयल पंप O-रिंग"),
            stepInstruction = BiText("Seat lubricated Viton O-ring into oil pump suction groove.", "ऑयल पंप सक्शन खांचे में O-रिंग फिट करें।"),
            targetPos = Vec3(0.14f, 0.17f, 0.30f),
            explodedOffset = Vec3(0.28f, -0.45f, 0.32f),
            spawnOffset = Vec3(0.45f, -0.15f, 0.45f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 3.0f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.GASKET_SEAL,
            isInternalHidden = true,
            prerequisites = listOf("mp_oil_pump_rotor")
        ),
        MicroPartSpec(
            id = "mp_oil_pump_bolt",
            category = MicroCategory.OIL_PUMP,
            name = BiText("Oil Pump Bolt & Spring Washer", "ऑयल पंप बोल्ट और वॉशर"),
            stepInstruction = BiText("Fasten oil pump bolt with washer using verified manual torque.", "ऑयल पंप बोल्ट और वॉशर को सत्यापित टॉर्क से कसें।"),
            targetPos = Vec3(0.20f, 0.25f, 0.32f),
            explodedOffset = Vec3(0.42f, -0.28f, 0.45f),
            spawnOffset = Vec3(0.55f, 0.2f, 0.45f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 22f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "oil_pump_bolt_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            isInternalHidden = true,
            prerequisites = listOf("mp_oil_pump_oring")
        ),
        MicroPartSpec(
            id = "mp_sump_gasket",
            category = MicroCategory.OIL_PUMP,
            name = BiText("Molded Oil Sump Gasket", "ऑयल सम्प गैस्केट"),
            stepInstruction = BiText("Lay sump gasket flat along bottom block rail aligning dowel pins.", "ब्लॉक के निचले रेल पर ऑयल सम्प गैस्केट सीधा बैठाएं।"),
            targetPos = Vec3(0f, 0.14f, 0f),
            explodedOffset = Vec3(0f, -1.32f, 0f),
            spawnOffset = Vec3(-0.55f, -0.30f, 0f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 2.5f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_main_bearing_bolt_2", "mp_conrod_bolt_2", "mp_oil_pump_bolt")
        ),

        // ==================== 4. VALVETRAIN, CAMSHAFT & HEAD ====================
        MicroPartSpec(
            id = "mp_camshaft",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Lobed Steel Camshaft", "कैमशाफ्ट"),
            stepInstruction = BiText("Slide lubricated camshaft into block cam bearings aligning timing mark.", "टाइमिंग मार्क मिलाकर कैमशाफ्ट को ब्लॉक में डालें।"),
            targetPos = Vec3(-0.14f, 0.56f, 0f),
            explodedOffset = Vec3(-0.45f, 0.15f, 0.45f),
            spawnOffset = Vec3(-0.55f, 0.45f, 0.45f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 320f,
            requiredTool = ToolType.SPANNER,
            isInternalHidden = true,
            prerequisites = listOf("mp_crankshaft")
        ),
        MicroPartSpec(
            id = "mp_head_gasket",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("MLS Cylinder Head Gasket", "मल्टी-लेयर हेड गैस्केट"),
            stepInstruction = BiText("Place multi-layer steel head gasket dry over block deck dowels.", "डowel पिन के ऊपर मल्टी-लेयर हेड गैस्केट सीधा रखें।"),
            targetPos = Vec3(0f, 0.72f, 0f),
            explodedOffset = Vec3(0f, 0.90f, 0f),
            spawnOffset = Vec3(0.55f, 0.85f, 0f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 1.6f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_camshaft", "mp_ring_top")
        ),
        MicroPartSpec(
            id = "mp_cylinder_head",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Crossflow Cylinder Head", "सिलेंडर हेड"),
            stepInstruction = BiText("Lower cylinder head squarely onto MLS gasket and locating dowels.", "हेड गैस्केट के ऊपर सिलेंडर हेड को सावधानी से रखें।"),
            targetPos = Vec3(0f, 0.80f, 0f),
            explodedOffset = Vec3(0f, 1.15f, 0f),
            spawnOffset = Vec3(0f, 1.15f, 0.45f),
            spawnRot = Vec3(0f, 45f, 0f),
            targetDepthMm = 0f,
            requiredTool = ToolType.ENGINE_CRANE,
            prerequisites = listOf("mp_head_gasket"),
            xpReward = 45
        ),
        MicroPartSpec(
            id = "mp_head_bolt_1",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Cylinder Head Bolt (Center-L)", "सिलेंडर हेड बोल्ट #1"),
            stepInstruction = BiText("Torque head bolt #1 in center-out spiral sequence per manual.", "मैनुअल क्रम अनुसार हेड बोल्ट #1 को टॉर्क रिंच से कसें।"),
            targetPos = Vec3(-0.10f, 0.89f, 0.08f),
            explodedOffset = Vec3(-0.10f, 1.48f, 0.08f),
            spawnOffset = Vec3(-0.45f, 1.05f, 0.15f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 92f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "head_bolt_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            prerequisites = listOf("mp_cylinder_head")
        ),
        MicroPartSpec(
            id = "mp_head_bolt_2",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Cylinder Head Bolt (Center-R)", "सिलेंडर हेड बोल्ट #2"),
            stepInstruction = BiText("Torque head bolt #2 matching stage & angle specification.", "हेड बोल्ट #2 को निर्धारित टॉर्क और कोण पर कसें।"),
            targetPos = Vec3(0.10f, 0.89f, -0.08f),
            explodedOffset = Vec3(0.10f, 1.48f, -0.08f),
            spawnOffset = Vec3(0.45f, 1.05f, -0.15f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 92f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "head_bolt_torque",
            simType = MicroSimType.TORQUE_WRENCH_SPEC,
            prerequisites = listOf("mp_head_bolt_1")
        ),
        MicroPartSpec(
            id = "mp_intake_valve",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Intake Valve", "इनटेक वाल्व"),
            stepInstruction = BiText("Lubricate valve stem and seat intake valve into cylinder #1 port.", "वाल्व स्टेम पर ऑयल लगाकर इनटेक वाल्व गाइड में डालें।"),
            targetPos = Vec3(-0.06f, 0.83f, 0.22f),
            explodedOffset = Vec3(-0.06f, 1.38f, 0.22f),
            spawnOffset = Vec3(-0.45f, 0.95f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 42f,
            requiredTool = ToolType.PLIERS,
            isInternalHidden = true,
            prerequisites = listOf("mp_head_bolt_2")
        ),
        MicroPartSpec(
            id = "mp_exhaust_valve",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Exhaust Valve", "एग्जॉस्ट वाल्व"),
            stepInstruction = BiText("Seat heat-resistant exhaust valve into exhaust guide.", "एग्जॉस्ट वाल्व को गाइड में बैठाएं।"),
            targetPos = Vec3(0.06f, 0.83f, 0.22f),
            explodedOffset = Vec3(0.06f, 1.38f, 0.22f),
            spawnOffset = Vec3(0.45f, 0.95f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 42f,
            requiredTool = ToolType.PLIERS,
            isInternalHidden = true,
            prerequisites = listOf("mp_intake_valve")
        ),
        MicroPartSpec(
            id = "mp_valve_stem_seal_in",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Intake Valve Stem Seal", "इनटेक वाल्व स्टेम सील"),
            stepInstruction = BiText("Press intake valve stem seal firmly onto valve guide boss.", "इनटेक वाल्व गाइड पर स्टेम सील प्रेस करें।"),
            targetPos = Vec3(-0.06f, 0.86f, 0.22f),
            explodedOffset = Vec3(-0.06f, 1.54f, 0.22f),
            spawnOffset = Vec3(-0.40f, 1.05f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 7.5f,
            requiredTool = ToolType.SOCKET_WRENCH,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_intake_valve")
        ),
        MicroPartSpec(
            id = "mp_valve_stem_seal_ex",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Exhaust Valve Stem Seal", "एग्जॉस्ट वाल्व स्टेम सील"),
            stepInstruction = BiText("Press exhaust valve stem seal cleanly onto guide boss.", "एग्जॉस्ट वाल्व स्टेम सील को गाइड पर फिट करें।"),
            targetPos = Vec3(0.06f, 0.86f, 0.22f),
            explodedOffset = Vec3(0.06f, 1.54f, 0.22f),
            spawnOffset = Vec3(0.40f, 1.05f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 7.5f,
            requiredTool = ToolType.SOCKET_WRENCH,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_exhaust_valve", "mp_valve_stem_seal_in")
        ),
        MicroPartSpec(
            id = "mp_valve_spring_in",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Intake Valve Spring", "इनटेक वाल्व स्प्रिंग"),
            stepInstruction = BiText("Place helical valve spring over stem seal and compress with tool.", "वाल्व स्प्रिंग रखकर स्प्रिंग कम्प्रेसर से दबाएं।"),
            targetPos = Vec3(-0.06f, 0.89f, 0.22f),
            explodedOffset = Vec3(-0.06f, 1.66f, 0.22f),
            spawnOffset = Vec3(-0.45f, 1.12f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 18f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.VALVE_SPRING_COMPRESS,
            prerequisites = listOf("mp_valve_stem_seal_ex")
        ),
        MicroPartSpec(
            id = "mp_valve_retainer_in",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Valve Spring Retainer", "वाल्व रिटेनर"),
            stepInstruction = BiText("Set steel spring retainer atop valve spring prior to compression.", "स्प्रिंग के ऊपर रिटेनर कैप रखें।"),
            targetPos = Vec3(-0.06f, 0.92f, 0.22f),
            explodedOffset = Vec3(-0.06f, 1.80f, 0.22f),
            spawnOffset = Vec3(-0.38f, 1.18f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 4.0f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.VALVE_SPRING_COMPRESS,
            prerequisites = listOf("mp_valve_spring_in")
        ),
        MicroPartSpec(
            id = "mp_valve_collets_in",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Split Valve Locks / Collets (Pair)", "वाल्व लॉक और कॉलेट्स"),
            stepInstruction = BiText("Compress valve spring first! Drop twin tapered collets into stem groove.", "पहले स्प्रिंग दबाएं! फिर दोनों कॉलेट्स ग्रूव में लॉक करें।"),
            targetPos = Vec3(-0.06f, 0.93f, 0.22f),
            explodedOffset = Vec3(-0.06f, 1.94f, 0.22f),
            spawnOffset = Vec3(0.38f, 1.20f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 3.0f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.VALVE_SPRING_COMPRESS,
            prerequisites = listOf("mp_valve_retainer_in")
        ),
        MicroPartSpec(
            id = "mp_rocker_arm_in",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Forged Rocker Arm & Tappet Screw", "रॉकर आर्म"),
            stepInstruction = BiText("Mount rocker arm onto shaft and set cold valve tip clearance.", "रॉकर आर्म लगाएं और कोल्ड वाल्व क्लियरेंस सेट करें।"),
            targetPos = Vec3(0f, 0.96f, 0.22f),
            explodedOffset = Vec3(0f, 2.08f, 0.22f),
            spawnOffset = Vec3(-0.50f, 1.25f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 12f,
            requiredTool = ToolType.SCREWDRIVER,
            prerequisites = listOf("mp_valve_collets_in")
        ),
        MicroPartSpec(
            id = "mp_rocker_nut",
            category = MicroCategory.VALVETRAIN_HEAD,
            name = BiText("Rocker Tappet Locknut & Washer", "रॉकर लॉकनट और वॉशर"),
            stepInstruction = BiText("Hold tappet screw and tighten locknut with spanner.", "टैपेट स्क्रू पकड़कर लॉकनट को पाने से कसें।"),
            targetPos = Vec3(-0.06f, 0.98f, 0.22f),
            explodedOffset = Vec3(-0.06f, 2.22f, 0.22f),
            spawnOffset = Vec3(0.45f, 1.28f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 6.0f,
            requiredTool = ToolType.SPANNER,
            torqueSpecKey = "injector_clamp_torque",
            prerequisites = listOf("mp_rocker_arm_in")
        ),

        // ==================== 5. INJECTORS & COVERS ====================
        MicroPartSpec(
            id = "mp_injector_washer",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("Copper Injector Sealing Washer", "तांबे का इंजेक्टर वॉशर"),
            stepInstruction = BiText("Drop new annealed copper flame washer into injector bore seat first!", "सबसे पहले इंजेक्टर बोर में तांबे का वॉशर बैठाएं!"),
            targetPos = Vec3(0.11f, 0.86f, 0.22f),
            explodedOffset = Vec3(0.28f, 1.50f, 0.22f),
            spawnOffset = Vec3(0.50f, 1.02f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 38f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.INJECTOR_STACK,
            isInternalHidden = true,
            prerequisites = listOf("mp_head_bolt_2")
        ),
        MicroPartSpec(
            id = "mp_injector_oring",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("Injector Body Dust O-Ring", "इंजेक्टर O-रिंग"),
            stepInstruction = BiText("Slide upper Viton O-ring onto injector sleeve groove.", "इंजेक्टर स्लीव खांचे में O-रिंग लगाएं।"),
            targetPos = Vec3(0.11f, 0.91f, 0.22f),
            explodedOffset = Vec3(0.28f, 1.68f, 0.22f),
            spawnOffset = Vec3(0.50f, 1.15f, 0.22f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 14f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.INJECTOR_STACK,
            prerequisites = listOf("mp_injector_washer")
        ),
        MicroPartSpec(
            id = "mp_injector",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("High-Pressure Diesel Injector", "डीजल फ्यूल इंजेक्टर"),
            stepInstruction = BiText("Seat injector squarely onto copper washer and torque clamp screw.", "तांबे के वॉशर पर इंजेक्टर बैठाकर क्लैंप कसें।"),
            targetPos = Vec3(0.11f, 0.96f, 0.22f),
            explodedOffset = Vec3(0.28f, 1.90f, 0.22f),
            spawnOffset = Vec3(0.55f, 1.28f, 0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 40f,
            requiredTool = ToolType.TORQUE_WRENCH,
            torqueSpecKey = "injector_clamp_torque",
            simType = MicroSimType.INJECTOR_STACK,
            prerequisites = listOf("mp_injector_oring")
        ),
        MicroPartSpec(
            id = "mp_rocker_cover_gasket",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("Rocker Cover Perimeter Gasket", "रॉकर कवर गैस्केट"),
            stepInstruction = BiText("Fit perimeter elastomer gasket onto cylinder head flange.", "सिलेंडर हेड के ऊपर रॉकर कवर गैस्केट बैठाएं।"),
            targetPos = Vec3(0f, 0.99f, 0f),
            explodedOffset = Vec3(0f, 2.35f, 0f),
            spawnOffset = Vec3(-0.55f, 1.25f, 0f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 3.0f,
            requiredTool = ToolType.PLIERS,
            simType = MicroSimType.GASKET_SEAL,
            prerequisites = listOf("mp_rocker_nut", "mp_injector")
        ),
        MicroPartSpec(
            id = "mp_rocker_screw_1",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("M6 Flange Screw & Washer #1", "M6 स्क्रू और वॉशर #1"),
            stepInstruction = BiText("Fasten M6 cover screw and captive washer with Screwdriver.", "पेचकस से M6 स्क्रू और वॉशर कसें।"),
            targetPos = Vec3(-0.16f, 1.02f, 0.26f),
            explodedOffset = Vec3(-0.16f, 2.52f, 0.26f),
            spawnOffset = Vec3(-0.48f, 1.32f, 0.26f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 16f,
            requiredTool = ToolType.SCREWDRIVER,
            prerequisites = listOf("mp_rocker_cover_gasket")
        ),
        MicroPartSpec(
            id = "mp_rocker_screw_2",
            category = MicroCategory.INJECTOR_COVERS,
            name = BiText("M6 Flange Screw & Nut #2", "M6 स्क्रू और नट #2"),
            stepInstruction = BiText("Fasten opposite flange screw, nut & flat washer evenly.", "दूसरी तरफ का स्क्रू, नट और वॉशर बराबर कसें।"),
            targetPos = Vec3(0.16f, 1.02f, -0.26f),
            explodedOffset = Vec3(0.16f, 2.52f, -0.26f),
            spawnOffset = Vec3(0.48f, 1.32f, -0.26f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 16f,
            requiredTool = ToolType.SCREWDRIVER,
            prerequisites = listOf("mp_rocker_screw_1")
        ),

        // ==================== 6. HYDRAULIC CONTROL SPOOL SEALS ====================
        MicroPartSpec(
            id = "mp_hyd_spool_body",
            category = MicroCategory.HYDRAULIC_SPOOL,
            name = BiText("Hydraulic Spool Valve Bore Block", "हाइड्रोलिक स्पूल वाल्व ब्लॉक"),
            stepInstruction = BiText("Position precision-honed hydraulic control valve bore block on bench.", "बेंच पर हाइड्रोलिक स्पूल वाल्व ब्लॉक रखें।"),
            targetPos = Vec3(0.46f, 0.55f, -0.22f),
            explodedOffset = Vec3(0.75f, 0f, -0.22f),
            spawnOffset = Vec3(0.85f, 0.55f, -0.22f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 0f,
            requiredTool = ToolType.SPANNER
        ),
        MicroPartSpec(
            id = "mp_hyd_spool_oring_1",
            category = MicroCategory.HYDRAULIC_SPOOL,
            name = BiText("Spool Bore O-Ring Seal (Front)", "स्पूल बोर O-रिंग सील #1"),
            stepInstruction = BiText("Seat nitrile 90-durometer O-ring into front spool land groove.", "आगे के स्पूल खांचे में O-रिंग सील बैठाएं।"),
            targetPos = Vec3(0.46f, 0.55f, -0.10f),
            explodedOffset = Vec3(0.75f, 0f, 0.12f),
            spawnOffset = Vec3(0.85f, 0.72f, 0.10f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 5.0f,
            requiredTool = ToolType.HYDRAULIC_WRENCH,
            simType = MicroSimType.HYDRAULIC_SPOOL_SEAL,
            isInternalHidden = true,
            prerequisites = listOf("mp_hyd_spool_body")
        ),
        MicroPartSpec(
            id = "mp_hyd_spool_backup_ring",
            category = MicroCategory.HYDRAULIC_SPOOL,
            name = BiText("PTFE Anti-Extrusion Backup Ring", "PTFE बैकअप रिंग"),
            stepInstruction = BiText("Place PTFE backup ring behind O-ring to prevent 250-bar blow-out.", "250-बार दबाव से बचाव हेतु O-रिंग के पीछे बैकअप रिंग लगाएं।"),
            targetPos = Vec3(0.46f, 0.55f, -0.14f),
            explodedOffset = Vec3(0.75f, 0f, 0.24f),
            spawnOffset = Vec3(0.85f, 0.78f, 0.18f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 6.5f,
            requiredTool = ToolType.HYDRAULIC_WRENCH,
            simType = MicroSimType.HYDRAULIC_SPOOL_SEAL,
            isInternalHidden = true,
            prerequisites = listOf("mp_hyd_spool_oring_1")
        ),
        MicroPartSpec(
            id = "mp_hyd_spool_oring_2",
            category = MicroCategory.HYDRAULIC_SPOOL,
            name = BiText("Spool Bore O-Ring Seal (Rear)", "स्पूल बोर O-रिंग सील #2"),
            stepInstruction = BiText("Install rear spool wiper & pressure O-ring seal into housing.", "पीछे की स्पूल O-रिंग सील को खांचे में फिट करें।"),
            targetPos = Vec3(0.46f, 0.55f, -0.34f),
            explodedOffset = Vec3(0.75f, 0f, -0.45f),
            spawnOffset = Vec3(0.85f, 0.72f, -0.45f),
            spawnRot = Vec3(90f, 0f, 0f),
            targetDepthMm = 5.0f,
            requiredTool = ToolType.HYDRAULIC_WRENCH,
            simType = MicroSimType.HYDRAULIC_SPOOL_SEAL,
            isInternalHidden = true,
            prerequisites = listOf("mp_hyd_spool_backup_ring")
        ),
        MicroPartSpec(
            id = "mp_hyd_spool_plunger",
            category = MicroCategory.HYDRAULIC_SPOOL,
            name = BiText("Hard-Chrome Spool Plunger", "क्रोम स्पूल प्लंजर"),
            stepInstruction = BiText("Slide lubricated spool plunger gently through O-rings & backup ring (55 mm).", "O-रिंग को बिना नुकसान पहुंचाए क्रोम स्पूल प्लंजर अंदर स्लाइड करें।"),
            targetPos = Vec3(0.46f, 0.55f, -0.22f),
            explodedOffset = Vec3(0.75f, 0f, 0.48f),
            spawnOffset = Vec3(0.85f, 0.65f, 0.35f),
            spawnRot = Vec3(0f, 90f, 0f),
            targetDepthMm = 55f,
            requiredTool = ToolType.HYDRAULIC_WRENCH,
            simType = MicroSimType.HYDRAULIC_SPOOL_SEAL,
            prerequisites = listOf("mp_hyd_spool_oring_2"),
            xpReward = 45
        )
    )

    fun findPart(id: String): MicroPartSpec? = parts.find { it.id == id }
    fun byCategory(cat: MicroCategory): List<MicroPartSpec> = parts.filter { it.category == cat }
}

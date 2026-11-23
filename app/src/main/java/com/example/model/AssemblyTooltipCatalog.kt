package com.example.model

import com.example.engine3d.Mat4

enum class TooltipTargetId {
    NONE,
    PARTS_INVENTORY,
    VIEWPORT_3D,
    INSPECTOR_ALIGN,
    LOCK_BTN,
    TOOL_BELT,
    FASTEN_BTN,
    LANGUAGE_TOGGLE,
    LEVEL_10_LIST,
    MICRO_TOOLBAR,
    MICRO_PARTS,
    MICRO_INSPECTOR
}

enum class TooltipStage {
    WORKSHOP_INTRO_INVENTORY,
    WORKSHOP_ORBIT_AND_ALIGN,
    WORKSHOP_COLLISION_WARNING,
    WORKSHOP_LOCK_POSITION,
    WORKSHOP_SELECT_TOOL_AND_FASTEN,
    WORKSHOP_LEVEL10_COMMISSIONING,
    WORKSHOP_LANGUAGE_SWITCH,
    MICRO_EXPLORE_MODES,
    MICRO_PICK_PART,
    MICRO_ALIGN_AND_SIM,
    MICRO_TORQUE_AND_CHECK
}

data class ContextualTooltip(
    val stage: TooltipStage,
    val targetId: TooltipTargetId,
    val stepIndex: Int,
    val totalSteps: Int,
    val badgeText: BiText,
    val title: BiText,
    val body: BiText,
    val actionHint: BiText,
    val isWarning: Boolean = false
)

object AssemblyTooltipCatalog {
    const val WORKSHOP_TOUR_COUNT = 6
    const val MICRO_TOUR_COUNT = 4

    fun resolveWorkshopTooltip(state: GameUiState, manualStepIndex: Int?): ContextualTooltip {
        if (manualStepIndex != null) {
            return workshopTourStep(manualStepIndex.coerceIn(0, WORKSHOP_TOUR_COUNT - 1), state)
        }

        // Live contextual awareness based on exact workshop progression
        if (state.currentLevel == 10) {
            return commissioningTooltip(state)
        }

        val manip = state.activeManip
        if (manip == null) {
            val nextPart = nextAvailablePart(state)
            val partHint = if (nextPart != null) {
                BiText(
                    "Suggested next: ${nextPart.name.en} (${nextPart.requiredTool.title.en})",
                    "अगला सुझाया गया पुर्जा: ${nextPart.name.hi} (${nextPart.requiredTool.title.hi})"
                )
            } else {
                BiText(
                    "Use L1–L10 tabs to review completed levels or proceed to L10.",
                    "पूर्ण लेवल देखने या L10 पर जाने के लिए L1–L10 टैब का उपयोग करें।"
                )
            }
            return ContextualTooltip(
                stage = TooltipStage.WORKSHOP_INTRO_INVENTORY,
                targetId = TooltipTargetId.PARTS_INVENTORY,
                stepIndex = 1,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("STEP 1 • PICK A PART", "चरण 1 • पुर्जा चुनें"),
                title = BiText(
                    "Select a Part from the Inventory",
                    "नीचे की सूची से एक पुर्जा चुनें"
                ),
                body = BiText(
                    "Scroll the horizontal parts tray below and tap any unlocked part (yellow wrench icon). Locked parts show which prerequisite must be installed first.",
                    "नीचे दी गई हॉरिजॉन्टल सूची को स्क्रॉल करें और किसी अनलॉक पुर्जे (पीला रिंच आइकन) पर टैप करें। लॉक किए गए पुर्जे पहले ज़रूरी भाग दिखाते हैं।"
                ),
                actionHint = partHint
            )
        }

        val spec = JcbCatalog.findPart(manip.partId)

        // Placed in socket -> Tool selection & bolt fastening guidance
        if (manip.placedInSocket) {
            val reqTool = spec?.requiredTool ?: ToolType.SPANNER
            val wrongToolSelected = state.selectedTool != reqTool
            val remaining = (manip.totalBolts - manip.boltsSecured).coerceAtLeast(1)
            return ContextualTooltip(
                stage = TooltipStage.WORKSHOP_SELECT_TOOL_AND_FASTEN,
                targetId = if (wrongToolSelected) TooltipTargetId.TOOL_BELT else TooltipTargetId.FASTEN_BTN,
                stepIndex = 5,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("STEP 4 • FASTEN BOLTS", "चरण 4 • बोल्ट कसें"),
                title = if (wrongToolSelected) {
                    BiText(
                        "Select ${reqTool.title.en} from Tool Belt",
                        "टूल बेल्ट से ${reqTool.title.hi} चुनें"
                    )
                } else {
                    BiText(
                        "Fasten ${remaining}x Bolt(s) with ${reqTool.title.en}",
                        "${reqTool.title.hi} से ${remaining}x बोल्ट कसें"
                    )
                },
                body = if (wrongToolSelected) {
                    BiText(
                        "The part is seated! Before tightening, pick ${reqTool.title.en} from the bottom-left interactive tool bar.",
                        "पुर्जा सीट पर बैठ गया है! कसने से पहले नीचे-बाएं टूल बार से ${reqTool.title.hi} चुनें।"
                    )
                } else {
                    BiText(
                        "Right tool selected! Tap 'Secure / Tighten' repeatedly until all ${manip.totalBolts} fasteners lock in place.",
                        "सही औज़ार चुना गया है! सभी ${manip.totalBolts} बोल्ट लॉक होने तक 'कसें / फिट करें' बटन दबाएं।"
                    )
                },
                actionHint = BiText(
                    "Remaining: $remaining of ${manip.totalBolts} • Tool: ${reqTool.title.en}",
                    "शेष: ${manip.totalBolts} में से $remaining • औज़ार: ${reqTool.title.hi}"
                )
            )
        }

        // Collision physics warning
        val collision = state.physics.lastCollision
        if (collision != null && !manip.isAligned) {
            return ContextualTooltip(
                stage = TooltipStage.WORKSHOP_COLLISION_WARNING,
                targetId = TooltipTargetId.INSPECTOR_ALIGN,
                stepIndex = 3,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("PHYSICS ALERT • COLLISION", "फिजिक्स चेतावनी • टकराव"),
                title = BiText(
                    "Obstacle Contact: ${collision.otherName.en}",
                    "पुर्जा टकराया: ${collision.otherName.hi}"
                ),
                body = BiText(
                    "Rigid-body collision blocked movement around ${collision.otherName.en}. Lift along +Y or shift around the frame before guiding into the glowing cyan slot.",
                    "${collision.otherName.hi} से टकराव के कारण रास्ता रुका है। +Y से थोड़ा ऊपर उठाएं या बगल से घुमाकर चमकते गाइड में ले जाएं।"
                ),
                actionHint = BiText(
                    "Impact ${String.format("%.1f", collision.impactSpeedMps)} m/s • Keep 'Snap' ON for smooth seating",
                    "टकराव गति ${String.format("%.1f", collision.impactSpeedMps)} m/s • आसान फिटिंग के लिए 'स्नैप' चालू रखें"
                ),
                isWarning = true
            )
        }

        // Aligned & ready to lock
        if (manip.isAligned) {
            return ContextualTooltip(
                stage = TooltipStage.WORKSHOP_LOCK_POSITION,
                targetId = TooltipTargetId.LOCK_BTN,
                stepIndex = 4,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("STEP 3 • ALIGNED!", "चरण 3 • सही संरेखण!"),
                title = BiText(
                    "Lock ${spec?.name?.en ?: "Part"} Into Seat",
                    "${spec?.name?.hi ?: "पुर्जे"} को सीट में लॉक करें"
                ),
                body = BiText(
                    "Position and rotation match the JCB 4CX fitting guide. Tap 'Lock Position' now to seat it before bolting.",
                    "स्थिति और कोण JCB 4CX गाइड से मेल खाते हैं। बोल्ट कसने से पहले 'स्थान लॉक करें' पर टैप करें।"
                ),
                actionHint = BiText(
                    "Tip: Green border means tolerance ≤ 0.28m & ≤ 25°",
                    "सुझाव: हरा बॉर्डर मतलब सटीक फिट (≤ 0.28m व ≤ 25°)"
                )
            )
        }

        // Active part needs movement/rotation alignment
        val dist = if (spec != null) manip.position.distanceTo(spec.targetPos) else 0f
        val rotErr = if (spec != null) {
            maxOf(
                Mat4.angleDiffDeg(manip.rotationDeg.x, spec.targetRot.x),
                Mat4.angleDiffDeg(manip.rotationDeg.y, spec.targetRot.y),
                Mat4.angleDiffDeg(manip.rotationDeg.z, spec.targetRot.z)
            ).toInt()
        } else 0

        return ContextualTooltip(
            stage = TooltipStage.WORKSHOP_ORBIT_AND_ALIGN,
            targetId = TooltipTargetId.INSPECTOR_ALIGN,
            stepIndex = 2,
            totalSteps = WORKSHOP_TOUR_COUNT,
            badgeText = BiText("STEP 2 • ALIGN IN 3D", "चरण 2 • 3D में संरेखित करें"),
            title = BiText(
                "Move & Rotate ${spec?.name?.en ?: "Part"} to Guide",
                "${spec?.name?.hi ?: "पुर्जे"} को चमकते गाइड में ले जाएं"
            ),
            body = BiText(
                "Drag the 3D window to orbit 360° or pinch to zoom. Use X/Y/Z buttons to slide into the glowing outline and RX/RY/RZ to match orientation.",
                "360° घुमाने के लिए 3D विंडो पर उंगली घुमाएं और ज़ूम करें। चमकते गाइड में लाने के लिए X/Y/Z व कोण मिलाने के लिए RX/RY/RZ दबाएं।"
            ),
            actionHint = BiText(
                "Offset: Δ ${String.format("%.2f", dist)}m • Angle: ∠ ${rotErr}° • Try 'Snap' near target",
                "दूरी: Δ ${String.format("%.2f", dist)}m • कोण: ∠ ${rotErr}° • पास आने पर 'स्नैप' आज़माएं"
            )
        )
    }

    fun resolveMicroTooltip(state: GameUiState, manualStepIndex: Int?): ContextualTooltip {
        if (manualStepIndex != null) {
            return microTourStep(manualStepIndex.coerceIn(0, MICRO_TOUR_COUNT - 1))
        }

        val active = state.micro.activeMicro
        if (active == null) {
            return ContextualTooltip(
                stage = TooltipStage.MICRO_PICK_PART,
                targetId = TooltipTargetId.MICRO_PARTS,
                stepIndex = 1,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO GUIDE • SELECT PART", "माइक्रो गाइड • पुर्जा चुनें"),
                title = BiText(
                    "Pick a Precision Engine Component",
                    "सूक्ष्म इंजन पुर्जा चुनें"
                ),
                body = BiText(
                    "Switch categories at the top-left (Bottom End, Piston/Conrod, Valvetrain, Fuel/Hyd) and pick any unlocked part. Use Explode or X-Ray to inspect internals.",
                    "ऊपर-बाएं श्रेणी बदलें (बॉटम एंड, पिस्टन, वाल्वट्रेन, फ्यूल) और अनलॉक पुर्जा चुनें। अंदरूनी भाग देखने के लिए एक्सप्लोड या पारदर्शी मोड उपयोग करें।"
                ),
                actionHint = BiText(
                    "46 micro-components • Tap 'Manual' anytime for factory torque specs",
                    "46 सूक्ष्म पुर्जे • टॉर्क मान देखने के लिए 'मैनुअल' खोलें"
                )
            )
        }

        val spec = MicroPartCatalog.findPart(active.partId)
        if (!active.isAligned) {
            val distMm = if (spec != null) (active.position.distanceTo(spec.targetPos) * 1000f).toInt() else 0
            return ContextualTooltip(
                stage = TooltipStage.MICRO_ALIGN_AND_SIM,
                targetId = TooltipTargetId.MICRO_INSPECTOR,
                stepIndex = 3,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO STEP • 0.05M PRECISION", "माइक्रो चरण • सूक्ष्म संरेखण"),
                title = BiText(
                    "Seat ${spec?.name?.en ?: "Component"} (${distMm}mm away)",
                    "${spec?.name?.hi ?: "पुर्जे"} को गाइड में बैठाएं (${distMm}mm दूर)"
                ),
                body = BiText(
                    "Nudge with ±0.05m controls, zoom right in with Macro Zoom (+), or press 'Animate Assembly Path' to preview the exact installation vector.",
                    "±0.05m बटन से पुर्जा खिसकाएं, मैक्रो ज़ूम (+) से पास देखें, या सही मार्ग देखने के लिए 'असेंबली एनीमेशन' दबाएं।"
                ),
                actionHint = BiText(
                    "Required depth: ${spec?.targetDepthMm ?: 0f}mm • Physics Snap assists near seat",
                    "आवश्यक गहराई: ${spec?.targetDepthMm ?: 0f}mm • पास आने पर स्नैप मदद करता है"
                )
            )
        }

        return ContextualTooltip(
            stage = TooltipStage.MICRO_TORQUE_AND_CHECK,
            targetId = TooltipTargetId.MICRO_INSPECTOR,
            stepIndex = 4,
            totalSteps = MICRO_TOUR_COUNT,
            badgeText = BiText("MICRO READY • TORQUE & FIT", "माइक्रो तैयार • टॉर्क व फिट"),
            title = BiText(
                "Verify Sim Rule & Torque Setting",
                "सिमुलेशन नियम और टॉर्क सेटिंग जांचें"
            ),
            body = BiText(
                when (spec?.simType) {
                    MicroSimType.PISTON_RING_FIT -> "Set the correct ring end-gap stagger (0°, 120°, or 240° apart) before pressing Install."
                    MicroSimType.VALVE_SPRING_COMPRESS -> "Compress the valve spring to ≥75% using the compressor slider before fitting collets."
                    else -> "Confirm the required tool is active and dial the exact Nm + angle from the Digital Service Manual, then tap Install / Torque."
                },
                when (spec?.simType) {
                    MicroSimType.PISTON_RING_FIT -> "इंस्टॉल करने से पहले रिंग गैप कोण (0°, 120°, या 240°) सही सेट करें।"
                    MicroSimType.VALVE_SPRING_COMPRESS -> "कॉलेट्स फिट करने से पहले वाल्व स्प्रिंग स्लाइडर को ≥75% दबाएं।"
                    else -> "सुनिश्चित करें कि सही औज़ार चुना है और डिजिटल मैनुअल के अनुसार टॉर्क (Nm) सेट है, फिर इंस्टॉल दबाएं।"
                }
            ),
            actionHint = BiText(
                "Tool: ${spec?.requiredTool?.title?.en ?: "Torque Wrench"}",
                "औज़ार: ${spec?.requiredTool?.title?.hi ?: "टॉर्क रिंच"}"
            )
        )
    }

    private fun workshopTourStep(index: Int, state: GameUiState): ContextualTooltip {
        return when (index) {
            0 -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_INTRO_INVENTORY,
                targetId = TooltipTargetId.PARTS_INVENTORY,
                stepIndex = 1,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 1/$WORKSHOP_TOUR_COUNT • INVENTORY", "गाइड 1/$WORKSHOP_TOUR_COUNT • सूची"),
                title = BiText("Pick Parts from the Horizontal Tray", "हॉरिजॉन्टल सूची से पुर्जा चुनें"),
                body = BiText(
                    "Swipe left/right through the bottom tray to inspect each level's components. Yellow wrench items are ready; padlocked parts depend on an earlier mount.",
                    "नीचे की सूची को बाएं/दाएं स्क्रॉल करके प्रत्येक लेवल के पुर्जे देखें। पीले रिंच वाले पुर्जे तैयार हैं; लॉक वाले पुर्जे पहले वाले भागों पर निर्भर हैं।"
                ),
                actionHint = BiText("Tap L1–L10 chips to jump between unlocked levels", "अनलॉक लेवल बदलने के लिए L1–L10 चिप्स दबाएं")
            )
            1 -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_ORBIT_AND_ALIGN,
                targetId = TooltipTargetId.VIEWPORT_3D,
                stepIndex = 2,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 2/$WORKSHOP_TOUR_COUNT • 3D PREVIEW", "गाइड 2/$WORKSHOP_TOUR_COUNT • 3D प्रीव्यू"),
                title = BiText("Orbit 360° & Zoom in the 3D Window", "3D विंडो में 360° घुमाएं और ज़ूम करें"),
                body = BiText(
                    "Touch and drag anywhere in the 3D workshop viewport to inspect angles, pinch or tap +/- to zoom close, and watch the glowing cyan ghost guide.",
                    "कोण देखने के लिए 3D वर्कशॉप में उंगली से ड्रैग करें, ज़ूम के लिए पिंच या +/- दबाएं और चमकते सियान गाइड पर नज़र रखें।"
                ),
                actionHint = BiText("Camera icon in header toggles 3rd-person & 1st-person cab view", "हेडर का कैमरा आइकन केबिन और बाहरी व्यू बदलता है")
            )
            2 -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_ORBIT_AND_ALIGN,
                targetId = if (state.activeManip != null) TooltipTargetId.INSPECTOR_ALIGN else TooltipTargetId.VIEWPORT_3D,
                stepIndex = 3,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 3/$WORKSHOP_TOUR_COUNT • PHYSICS & ALIGN", "गाइड 3/$WORKSHOP_TOUR_COUNT • फिजिक्स व संरेखण"),
                title = BiText("Align with Gravity & Magnetic Snap", "गुरुत्वाकर्षण और मैग्नेटिक स्नैप से संरेखित करें"),
                body = BiText(
                    "Once a part is picked up, use X/Y/Z & RX/RY/RZ steppers on the right. Collision physics stops clipping into installed steel, and 'Snap' pulls parts cleanly into their seat.",
                    "पुर्जा चुनने के बाद दाएं ओर X/Y/Z व RX/RY/RZ से संरेखित करें। कोलिजन फिजिक्स टकराव रोकता है और 'स्नैप' पुर्जे को सही सीट में खींच लेता है।"
                ),
                actionHint = BiText("Tap 'Drop' to test real gravity onto the workshop floor", "वर्कशॉप फर्श पर गुरुत्वाकर्षण देखने के लिए 'गिराएं' दबाएं")
            )
            3 -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_LOCK_POSITION,
                targetId = if (state.activeManip != null && !state.activeManip.placedInSocket) TooltipTargetId.LOCK_BTN else TooltipTargetId.VIEWPORT_3D,
                stepIndex = 4,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 4/$WORKSHOP_TOUR_COUNT • SEAT PART", "गाइड 4/$WORKSHOP_TOUR_COUNT • स्थान लॉक"),
                title = BiText("Lock Position When Guide Turns Green", "गाइड हरा होने पर स्थान लॉक करें"),
                body = BiText(
                    "When position offset Δ ≤ 0.28m and angle error ∠ ≤ 25°, the readout turns green. Press 'Lock Position' to seat the component on its mount.",
                    "जब दूरी Δ ≤ 0.28m और कोण ≤ 25° हो, तो इंडिकेटर हरा हो जाता है। माउंट पर बैठाने के लिए 'स्थान लॉक करें' दबाएं।"
                ),
                actionHint = BiText("Seating unlocks the final bolt-tightening phase", "लॉक करने के बाद बोल्ट कसने का चरण शुरू होता है")
            )
            4 -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_SELECT_TOOL_AND_FASTEN,
                targetId = TooltipTargetId.TOOL_BELT,
                stepIndex = 5,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 5/$WORKSHOP_TOUR_COUNT • 10 TOOLS", "गाइड 5/$WORKSHOP_TOUR_COUNT • 10 औज़ार"),
                title = BiText("Use the Required Workshop Tool", "सही वर्कशॉप औज़ार का उपयोग करें"),
                body = BiText(
                    "Heavy frames need the Engine Crane or Jack; bolts require Spanner, Socket, or Torque Wrench; hydraulics need the Hose Wrench.",
                    "भारी फ्रेम के लिए इंजन क्रेन/जैक, बोल्ट के लिए स्पैनर/सॉकेट/टॉर्क रिंच और पाइपों के लिए हाइड्रोलिक होज़ रिंच चुनें।"
                ),
                actionHint = BiText("Selecting a part auto-highlights its recommended tool", "पुर्जा चुनते ही सही औज़ार अपने आप हाइलाइट हो जाता है")
            )
            else -> ContextualTooltip(
                stage = TooltipStage.WORKSHOP_LANGUAGE_SWITCH,
                targetId = TooltipTargetId.LANGUAGE_TOGGLE,
                stepIndex = 6,
                totalSteps = WORKSHOP_TOUR_COUNT,
                badgeText = BiText("TOUR 6/$WORKSHOP_TOUR_COUNT • EN / हिन्दी", "गाइड 6/$WORKSHOP_TOUR_COUNT • भाषा"),
                title = BiText("Instant English / Hindi Toggle", "तुरंत अंग्रेजी / हिन्दी भाषा बदलें"),
                body = BiText(
                    "Tap the 'EN | हिन्दी' switch at the top bar or inside any tooltip at any time to switch part names, tools, diagnostics, and instructions.",
                    "शीर्ष बार या किसी भी टूलटिप में 'EN | हिन्दी' बटन दबाकर कभी भी पुर्जों के नाम, औज़ार और निर्देश अपनी भाषा में पढ़ें।"
                ),
                actionHint = BiText("Tap 'Got It' to start building your JCB 4CX!", "अपनी JCB 4CX बनाना शुरू करने के लिए 'समझ गया' दबाएं!")
            )
        }
    }

    private fun microTourStep(index: Int): ContextualTooltip {
        return when (index) {
            0 -> ContextualTooltip(
                stage = TooltipStage.MICRO_EXPLORE_MODES,
                targetId = TooltipTargetId.MICRO_TOOLBAR,
                stepIndex = 1,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO 1/$MICRO_TOUR_COUNT • X-RAY & EXPLODE", "माइक्रो 1/$MICRO_TOUR_COUNT • दृश्य मोड"),
                title = BiText("Explode, X-Ray & Inspect Hidden Parts", "एक्सप्लोड, पारदर्शी और निरीक्षण मोड"),
                body = BiText(
                    "Toggle Explode to spread internal engine layers, X-Ray for a translucent block, Inspect to highlight hidden seals/bearings, or +/- for 0.22m macro zoom.",
                    "अंदरूनी इंजन परतें अलग देखने के लिए एक्सप्लोड, ब्लॉक आर-पार देखने के लिए पारदर्शी (X-Ray) और छिपे सील/बेयरिंग के लिए निरीक्षण मोड उपयोग करें।"
                ),
                actionHint = BiText("Open 'Manual' to verify JCB 444 vs Perkins 1004-4T specs", "JCB 444 व Perkins 1004-4T टॉर्क मान देखने के लिए 'मैनुअल' खोलें")
            )
            1 -> ContextualTooltip(
                stage = TooltipStage.MICRO_PICK_PART,
                targetId = TooltipTargetId.MICRO_PARTS,
                stepIndex = 2,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO 2/$MICRO_TOUR_COUNT • 46 PARTS", "माइक्रो 2/$MICRO_TOUR_COUNT • 46 पुर्जे"),
                title = BiText("Follow Micro-Assembly Order & Checks", "सही क्रम व क्लियरेंस जांच अपनाएं"),
                body = BiText(
                    "Every bearing shell, piston ring, valve collet, seal, and bolt is separate. Run Plastigage clearance and 720° Crankshaft rotation checks from below the list.",
                    "हर बेयरिंग शेल, पिस्टन रिंग, वाल्व कॉलेट, सील और बोल्ट अलग है। सूची के नीचे से प्लास्टिगेज क्लियरेंस और 720° क्रैंक घुमाव जांच चलाएं।"
                ),
                actionHint = BiText("Each part displays its target seating depth in mm", "हर पुर्जे के साथ उसकी गहराई (mm) दिखाई जाती है")
            )
            2 -> ContextualTooltip(
                stage = TooltipStage.MICRO_ALIGN_AND_SIM,
                targetId = TooltipTargetId.MICRO_INSPECTOR,
                stepIndex = 3,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO 3/$MICRO_TOUR_COUNT • SIMULATIONS", "माइक्रो 3/$MICRO_TOUR_COUNT • सिमुलेशन"),
                title = BiText("Ring Stagger, Valve Compression & Snap", "रिंग गैप, वाल्व कम्प्रेशन और स्नैप"),
                body = BiText(
                    "Align within 0.14m & 25°, stagger piston rings 120° apart (0° / 120° / 240°), and compress valve springs ≥75% before fitting collets.",
                    "0.14m और 25° के भीतर संरेखित करें, पिस्टन रिंग गैप 120° अंतर (0° / 120° / 240°) पर रखें और कॉलेट से पहले वाल्व स्प्रिंग ≥75% दबाएं।"
                ),
                actionHint = BiText("Use 'Animate Assembly Path' to watch the insertion vector", "सही फिटिंग मार्ग देखने के लिए 'असेंबली एनीमेशन' दबाएं")
            )
            else -> ContextualTooltip(
                stage = TooltipStage.MICRO_TORQUE_AND_CHECK,
                targetId = TooltipTargetId.MICRO_INSPECTOR,
                stepIndex = 4,
                totalSteps = MICRO_TOUR_COUNT,
                badgeText = BiText("MICRO 4/$MICRO_TOUR_COUNT • TORQUE", "माइक्रो 4/$MICRO_TOUR_COUNT • टॉर्क"),
                title = BiText("Match Factory Torque & Angle Specs", "फैक्ट्री टॉर्क व कोण मान मिलाएं"),
                body = BiText(
                    "Dial the specified Nm + angle with Torque Wrench selected. Serial-dependent entries ask you to confirm specs in the Digital Service Manual.",
                    "टॉर्क रिंच के साथ सही Nm और डिग्री सेट करें। सीरियल-निर्भर मान के लिए डिजिटल सर्विस मैनुअल में डेटा सत्यापित करें।"
                ),
                actionHint = BiText("No invented specs — unverified values stay flagged safely", "बिना सत्यापित डेटा सुरक्षित रूप से फ्लैग किया जाता है")
            )
        }
    }

    private fun commissioningTooltip(state: GameUiState): ContextualTooltip {
        val c = state.commissioning
        val nextStepHint = when {
            !state.allPartsBuilt -> BiText(
                "Finish Levels 1–9 parts before starting fluids & ignition tests.",
                "फ्लूइड व इंजन टेस्ट से पहले लेवल 1–9 के सभी पुर्जे पूरे करें।"
            )
            !c.engineOilFilled || !c.hydraulicOilFilled || !c.coolantAdded || !c.dieselAdded -> BiText(
                "Step 1–4: Fill Engine Oil, Hydraulic Oil, Coolant, and Diesel first.",
                "चरण 1–4: पहले इंजन ऑयल, हाइड्रोलिक ऑयल, कूलेंट और डीजल भरें।"
            )
            !c.batteryChecked -> BiText(
                "Step 5: Pick Diagnostic Scanner in Tool Belt, then tap '5. Scan Battery'.",
                "चरण 5: टूल बेल्ट में डायग्नोस्टिक स्कैनर चुनें और '5. बैटरी जांचें' दबाएं।"
            )
            !c.engineStarted -> BiText(
                "Step 6: Fluids & battery OK — tap '6. Start Engine'!",
                "चरण 6: सभी जांच पूर्ण — अब '6. इंजन चालू करें' दबाएं!"
            )
            !c.finalInspectionDone -> BiText(
                "Steps 7–13: Check warning lights, steering, gears, loader, backhoe & stabilizers.",
                "चरण 7–13: वॉर्निंग लाइट्स, स्टीयरिंग, गियर, लोडर, बैकहो और स्टेबलाइज़र टेस्ट करें।"
            )
            else -> BiText(
                "All tests complete! Tap 'DRIVE' in the top bar to operate the 4CX!",
                "सभी टेस्ट पूर्ण! JCB 4CX चलाने के लिए ऊपर 'चलाएं' (DRIVE) बटन दबाएं!"
            )
        }
        return ContextualTooltip(
            stage = TooltipStage.WORKSHOP_LEVEL10_COMMISSIONING,
            targetId = TooltipTargetId.LEVEL_10_LIST,
            stepIndex = 6,
            totalSteps = WORKSHOP_TOUR_COUNT,
            badgeText = BiText("LEVEL 10 • COMMISSIONING", "लेवल 10 • अंतिम जांच"),
            title = BiText("Run Pre-Drive Fluids & Functional Tests", "ड्राइविंग से पहले सभी जांच पूरी करें"),
            body = BiText(
                "Scroll the horizontal Level 10 checklist left-to-right and complete each step in sequence to unlock Driving & Excavation Simulator.",
                "हॉरिजॉन्टल लेवल 10 चेकलिस्ट को क्रम से पूरा करें ताकि ड्राइविंग और खुदाई सिम्युलेटर अनलॉक हो सके।"
            ),
            actionHint = nextStepHint
        )
    }

    private fun nextAvailablePart(state: GameUiState): JcbPartSpec? {
        val lvlParts = JcbCatalog.partsForLevel(state.currentLevel)
        return lvlParts.firstOrNull { part ->
            part.id !in state.installedParts && part.prerequisites.all { it in state.installedParts }
        }
    }
}

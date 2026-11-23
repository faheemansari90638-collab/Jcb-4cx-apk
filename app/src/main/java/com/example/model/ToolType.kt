package com.example.model

enum class ToolType(
    val id: String,
    val title: BiText,
    val spec: BiText,
    val actionVerb: BiText
) {
    SPANNER(
        id = "spanner",
        title = BiText("Spanner", "पाना (स्पैनर)"),
        spec = BiText("24mm Forged Chrome-Vanadium", "24mm क्रोम-वैनेडियम पाना"),
        actionVerb = BiText("Tightening nuts", "नट कस रहा है")
    ),
    SOCKET_WRENCH(
        id = "socket_wrench",
        title = BiText("Socket Wrench", "सॉकेट रिंच"),
        spec = BiText("1/2\" Drive Ratchet Set", "1/2\" रैचेट सॉकेट सेट"),
        actionVerb = BiText("Ratcheting bolts", "बोल्ट कस रहा है")
    ),
    SCREWDRIVER(
        id = "screwdriver",
        title = BiText("Screwdriver", "पेचकस"),
        spec = BiText("Heavy-Duty Torx & Phillips", "हेवी-ड्यूटी पेचकस"),
        actionVerb = BiText("Fastening screws", "स्क्रू कस रहा है")
    ),
    HAMMER(
        id = "hammer",
        title = BiText("Hammer", "हथौड़ा"),
        spec = BiText("2kg Sledge & Brass Drift", "2kg पीतल ड्रिफ्ट हथौड़ा"),
        actionVerb = BiText("Driving pivot pins", "पिवट पिन ठोक रहा है")
    ),
    PLIERS(
        id = "pliers",
        title = BiText("Pliers", "प्लास"),
        spec = BiText("Circlip & Heavy Combination", "सर्कलिप और कॉम्बिनेशन प्लास"),
        actionVerb = BiText("Securing clips & linkages", "क्लिप लॉक कर रहा है")
    ),
    HYDRAULIC_WRENCH(
        id = "hydraulic_wrench",
        title = BiText("Hydraulic Hose Wrench", "हाइड्रोलिक होज़ रिंच"),
        spec = BiText("BSP Flare Nut Wrench", "BSP फ्लेयर नट रिंच"),
        actionVerb = BiText("Sealing JIC/BSP fittings", "हाइड्रोलिक फिटिंग सील कर रहा है")
    ),
    TORQUE_WRENCH(
        id = "torque_wrench",
        title = BiText("Torque Wrench", "टॉर्क रिंच"),
        spec = BiText("Calibrated 40–320 Nm", "कैलिब्रेटेड 40–320 Nm"),
        actionVerb = BiText("Torquing to spec", "निर्दिष्ट टॉर्क पर कस रहा है")
    ),
    ENGINE_CRANE(
        id = "engine_crane",
        title = BiText("Engine Lifting Crane", "इंजन लिफ्टिंग क्रेन"),
        spec = BiText("2-Ton Workshop Gantry Hoist", "2-टन गैन्ट्री होइस्ट क्रेन"),
        actionVerb = BiText("Hoisting heavy assembly", "भारी पुर्जा उठा रहा है")
    ),
    JACK(
        id = "jack",
        title = BiText("Hydraulic Jack", "हाइड्रोलिक जैक"),
        spec = BiText("5-Ton Trolley Axle Jack", "5-टन ट्रॉली एक्सल जैक"),
        actionVerb = BiText("Supporting axle & frame", "एक्सल को जैक से सहारा दे रहा है")
    ),
    DIAGNOSTIC_SCANNER(
        id = "diagnostic_scanner",
        title = BiText("Diagnostic Scanner", "डायग्नोस्टिक स्कैनर"),
        spec = BiText("JCB ServiceMaster CAN-Bus", "JCB सर्विसमास्टर CAN-बस"),
        actionVerb = BiText("Scanning ECU & circuits", "ECU और सर्किट स्कैन कर रहा है")
    )
}

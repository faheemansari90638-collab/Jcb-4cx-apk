package com.example.model

enum class MissionId {
    BUILD_JCB,
    START_ENGINE,
    MOVE_SOIL,
    DIG_TRENCH,
    LOAD_TRUCK,
    PARK_MACHINE,
    FINAL_INSPECTION
}

data class MissionSpec(
    val id: MissionId,
    val number: Int,
    val title: BiText,
    val description: BiText,
    val objectiveHint: BiText,
    val xpReward: Int,
    val requiresDrivingMode: Boolean
)

object MissionCatalog {
    val all: List<MissionSpec> = listOf(
        MissionSpec(
            id = MissionId.BUILD_JCB,
            number = 1,
            title = BiText("Build the JCB from Zero", "शून्य से JCB का निर्माण करें"),
            description = BiText(
                "Assemble chassis, engine, transmission, hydraulics, wheels, cabin, loader, backhoe, and body panels across Levels 1–9.",
                "लेवल 1–9 में चेसिस, इंजन, ट्रांसमिशन, हाइड्रोलिक्स, पहिये, केबिन, लोडर, बैकहो और बॉडी को जोड़ें।"
            ),
            objectiveHint = BiText("Install all 72 mechanical parts in Workshop", "वर्कशॉप में सभी 72 पुर्जे इंस्टॉल करें"),
            xpReward = 1000,
            requiresDrivingMode = false
        ),
        MissionSpec(
            id = MissionId.START_ENGINE,
            number = 2,
            title = BiText("Start the Engine Successfully", "इंजन को सफलतापूर्वक चालू करें"),
            description = BiText(
                "Fill engine oil, hydraulic fluid, coolant, and diesel, run the diagnostic scanner, and crank the JCB 444/EcoMAX diesel engine.",
                "इंजन ऑयल, हाइड्रोलिक ऑयल, कूलेंट और डीजल भरें, स्कैनर जांचें और इंजन चालू करें।"
            ),
            objectiveHint = BiText("Complete Level 10 checks & start ignition", "लेवल 10 की जांच पूरी करें और इंजन चालू करें"),
            xpReward = 400,
            requiresDrivingMode = false
        ),
        MissionSpec(
            id = MissionId.MOVE_SOIL,
            number = 3,
            title = BiText("Move Soil with Front Bucket", "फ्रंट बकेट से मिट्टी हटाएं"),
            description = BiText(
                "Drive into the earth pile, lower and curl the front loader bucket to scoop soil, and dump it at the grading pad.",
                "मिट्टी के ढेर पर जाएं, फ्रंट लोडर बकेट से मिट्टी भरें और समतल क्षेत्र में डालें।"
            ),
            objectiveHint = BiText("Scoop & deliver 2 front bucket loads", "फ्रंट बकेट से 2 बार मिट्टी पहुंचाएं"),
            xpReward = 500,
            requiresDrivingMode = true
        ),
        MissionSpec(
            id = MissionId.DIG_TRENCH,
            number = 4,
            title = BiText("Dig a Trench with Rear Bucket", "रियर बकेट से खाई खोदें"),
            description = BiText(
                "Position the JCB 4CX over the marked trench line, lower rear stabilizer legs, and excavate using boom, dipper, and bucket.",
                "निशान वाली खाई लाइन पर मशीन लगाएं, स्टेबलाइज़र नीचे करें और बैकहो से खाई खोदें।"
            ),
            objectiveHint = BiText("Deploy stabilizers & dig trench to 100%", "स्टेबलाइज़र लगाकर 100% खाई खोदें"),
            xpReward = 650,
            requiresDrivingMode = true
        ),
        MissionSpec(
            id = MissionId.LOAD_TRUCK,
            number = 5,
            title = BiText("Load the Dump Truck", "डंप ट्रक लोड करें"),
            description = BiText(
                "Scoop excavated earth and lift the front loader arms high to empty material cleanly into the waiting tipper truck bed.",
                "मिट्टी उठाकर फ्रंट लोडर आर्म्स ऊपर उठाएं और डंप ट्रक के बेड में भरें।"
            ),
            objectiveHint = BiText("Fill dump truck bed to 100%", "डंप ट्रक को 100% भरें"),
            xpReward = 700,
            requiresDrivingMode = true
        ),
        MissionSpec(
            id = MissionId.PARK_MACHINE,
            number = 6,
            title = BiText("Park the Machine", "मशीन को सही स्थान पर पार्क करें"),
            description = BiText(
                "Drive the JCB 4CX into the yellow safety parking bay, lower attachments safely to ground level, raise stabilizers, and shut down.",
                "JCB 4CX को पीले पार्किंग बे में ले जाएं, लोडर और बकेट को जमीन पर टिकाएं और इंजन बंद करें।"
            ),
            objectiveHint = BiText("Center in parking bay, rest buckets & stop engine", "पार्किंग बे में पार्क कर बकेट नीचे रखें"),
            xpReward = 450,
            requiresDrivingMode = true
        ),
        MissionSpec(
            id = MissionId.FINAL_INSPECTION,
            number = 7,
            title = BiText("Final Maintenance Inspection", "अंतिम रखरखाव निरीक्षण पूरा करें"),
            description = BiText(
                "Perform post-operation diagnostic scan, verify fluid pressure, bolt torque integrity, and certify the JCB 4CX fleet-ready.",
                "काम के बाद डायग्नोस्टिक स्कैन करें, ऑयल प्रेशर और बोल्ट की मजबूती जांचकर निरीक्षण पूरा करें।"
            ),
            objectiveHint = BiText("Run post-work Diagnostic Scan in Parking Bay", "पार्किंग बे में अंतिम स्कैन पूरा करें"),
            xpReward = 800,
            requiresDrivingMode = true
        )
    )
}

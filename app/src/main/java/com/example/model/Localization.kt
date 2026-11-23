package com.example.model

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "English"),
    HI("hi", "हिन्दी")
}

data class BiText(val en: String, val hi: String) {
    fun get(lang: AppLanguage): String = if (lang == AppLanguage.HI) hi else en
    fun dual(): String = "$en / $hi"
}

object AppStrings {
    val appTitle = BiText("JCB 4CX BUILD MASTER", "जेसीबी 4CX बिल्ड मास्टर")
    val appSubtitle = BiText("Realistic 3D Mechanical Assembly & Excavator Sim", "वास्तविक 3D मैकेनिकल असेंबली और उत्खनन सिम्युलेटर")
    val resumeWorkshop = BiText("WORKSHOP ASSEMBLY", "वर्कशॉप असेंबली")
    val drivingSimulator = BiText("DRIVING SIMULATOR", "ड्राइविंग सिम्युलेटर")
    val missionsTitle = BiText("MISSIONS", "मिशन")
    val specGuideTitle = BiText("4CX REFERENCE MANUAL", "4CX संदर्भ गाइड")
    val settingsTitle = BiText("SETTINGS", "सेटिंग्स")
    val assemblyProgress = BiText("Assembly Progress", "असेंबली प्रगति")
    val partsInventory = BiText("Parts Inventory", "पुर्जों की सूची")
    val checklist = BiText("Checklist", "चेकलिस्ट")
    val toolsTitle = BiText("Tools", "औज़ार")
    val levelLabel = BiText("Level", "लेवल")
    val xpLabel = BiText("XP", "अनुभव अंक")
    val locked = BiText("Locked", "लॉक")
    val installed = BiText("Installed", "इंस्टॉल हो गया")
    val readyToBolt = BiText("Aligned! Secure with Tool", "सही स्थिति! औज़ार से कसें")
    val alignPart = BiText("Move & Rotate into Guide", "गाइड में घुमाएं और रखें")
    val lockPlacement = BiText("Lock Position", "स्थान लॉक करें")
    val fastenBolt = BiText("Secure / Tighten", "कसें / फिट करें")
    val camera3rd = BiText("3rd Person", "तीसरा व्यक्ति")
    val camera1st = BiText("1st Person Cab", "केबिन व्यू")
    val resetCam = BiText("Reset Cam", "कैमरा रीसेट")
    val pause = BiText("Pause", "रोकें")
    val resume = BiText("Resume", "जारी रखें")
    val saveProgress = BiText("Save Game", "प्रगति सहेजें")
    val savedToast = BiText("Progress Saved Locally!", "प्रगति सहेजी गई!")
    val restartLevel = BiText("Restart Level", "लेवल पुनः शुरू करें")
    val language = BiText("Language / भाषा", "भाषा / Language")
    val soundFx = BiText("Engine & Tool Sounds", "इंजन और औज़ार ध्वनि")
    val graphicsQuality = BiText("3D Graphics LOD", "3D ग्राफिक्स गुणवत्ता")
    val controlLayout = BiText("Cab Control Layout", "केबिन कंट्रोल लेआउट")
    val unlockHint = BiText("Complete Levels 1–10 to unlock full Driving Mode", "ड्राइविंग मोड अनलॉक करने के लिए लेवल 1-10 पूरा करें")
    val engineStart = BiText("START ENGINE", "इंजन चालू करें")
    val engineStop = BiText("STOP ENGINE", "इंजन बंद करें")
    val driveMode2Wd = BiText("2WD", "2WD")
    val driveMode4Wd = BiText("4WD", "4WD")
    val steerMode2Ws = BiText("2WS", "2WS")
    val steerMode4Ws = BiText("4WS", "4WS")
    val stabilizers = BiText("Stabilizers", "स्टेबलाइज़र")
    val frontLoader = BiText("Front Loader", "फ्रंट लोडर")
    val rearBackhoe = BiText("Rear Backhoe", "रियर बैकहो")
    val diagnosticScan = BiText("Run Scan", "स्कैन करें")
    val inspectMachine = BiText("Final Inspection", "अंतिम निरीक्षण")
    val backToMenu = BiText("Main Menu", "मुख्य मेनू")
}

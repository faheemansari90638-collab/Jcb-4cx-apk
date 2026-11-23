package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppStrings
import com.example.model.BiText
import com.example.model.GameUiState
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.TextSecondary

@Composable
fun SpecGuideScreen(
    state: GameUiState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = state.language

    val specs = listOf(
        Triple(
            BiText("1. Equal-Wheel 4WS Chassis (4CX Hallmark)", "1. बराबर पहिये और 4WS चेसिस (4CX पहचान)"),
            BiText(
                "Unlike the 3CX (which has smaller front steer wheels), the JCB 4CX SiteMaster uses four equal-sized 28-inch heavy tyres with 4WD and 3-mode All-Wheel Steer (2WS, 4WS tight turn, and Crab Steer).",
                "3CX के विपरीत, JCB 4CX में चारों पहिये बराबर 28-इंच के होते हैं, साथ में 4WD और 3-मोड ऑल-व्हील स्टीयरिंग (2WS, 4WS और क्रैब स्टीयर) मिलता है।"
            ),
            BiText("Standard across all 4CX generations", "सभी 4CX मॉडलों में मानक")
        ),
        Triple(
            BiText("2. Engine Variants by Model Year", "2. मॉडल वर्ष के अनुसार इंजन प्रकार"),
            BiText(
                "2005–2014 JCB 4CX units use the JCB 444 Dieselmax 4.4L turbocharged engine (Tier 3, mechanical or electronic pump). 2015–2025 Stage IV / Stage V models use the JCB EcoMAX 4.4L/4.8L with Variable Geometry Turbo, DPF, and AdBlue SCR.",
                "2005–2014 मॉडल में JCB 444 डीजलमैक्स 4.4L इंजन होता है। 2015+ नए मॉडल में EcoMAX स्टेज-V इंजन (DPF और AdBlue के साथ) आता है।"
            ),
            BiText("Varies by Model Year & Emissions Region", "मॉडल वर्ष और देश के उत्सर्जन मानकों पर निर्भर")
        ),
        Triple(
            BiText("3. Transmission: Synchro vs Autoshift", "3. ट्रांसमिशन: सिंक्रो बनाम ऑटोशिफ्ट"),
            BiText(
                "Base JCB 4CX specs use a 4-speed Synchro Shuttle gearbox with torque converter. High-spec Eco/PRO models upgrade to 6-speed Autoshift with TorqueLock clutch for roading efficiency.",
                "बेस मॉडल में 4-स्पीड सिंक्रो शटल गियरबॉक्स होता है, जबकि प्रो मॉडल में 6-स्पीड ऑटोशिफ्ट और टॉर्कलॉक मिलता है।"
            ),
            BiText("Varies by Trim (Sitemaster / Eco / Pro)", "ट्रिम के अनुसार भिन्न")
        ),
        Triple(
            BiText("4. Control Layout: 6-Lever vs Joysticks", "4. कंट्रोल लेआउट: 6-लीवर बनाम जॉयस्टिक"),
            BiText(
                "Traditional mechanical 4CX models feature a classic 6-lever rear control bank (4 excavation levers + 2 stabilizer levers). Modern EasyControl / Servo models place twin ISO/SAE joysticks on the suspension seat armrests. You can switch between both in Settings!",
                "पारंपरिक 4CX में क्लासिक 6-लीवर बैंक होता है, जबकि आधुनिक मॉडल में सीट पर लगे दो सर्वो जॉयस्टिक होते हैं। आप सेटिंग्स में दोनों बदल सकते हैं!"
            ),
            BiText("Configurable in Settings / Level 6", "सेटिंग्स और लेवल 6 में बदलने योग्य")
        ),
        Triple(
            BiText("5. Rear Kingpost & Front Shovel Options", "5. रियर किंगपोस्ट और फ्रंट बकेट विकल्प"),
            BiText(
                "European/Asian 4CX models use side-shift PowerSlide hydraclamp rails and JCB Extradig telescopic dipper. Front shovel options include a 1.1–1.3 m³ GP bucket or a 6-in-1 multi-purpose clamshell.",
                "यूरोपीय/एशियाई 4CX में साइड-शिफ्ट पावरस्लाइड रेल और एक्स्ट्राडिग टेलीस्कोपिक डिपर होता है। आगे GP बकेट या 6-इन-1 क्लैमशेल लगता है।"
            ),
            BiText("Varies by Regional Spec", "क्षेत्रीय कॉन्फ़िगरेशन के अनुसार")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("spec_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JcbYellow)
            }
            Spacer(Modifier.width(8.dp))
            Text(AppStrings.specGuideTitle.get(lang), style = MaterialTheme.typography.headlineMedium, color = JcbYellow)
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(specs.size) { idx ->
                val (title, body, tag) = specs[idx]
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, SteelBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(title.get(lang), style = MaterialTheme.typography.titleMedium, color = JcbYellow)
                        Text(body.get(lang), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(tag.get(lang), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
                    }
                }
            }
        }
    }
}

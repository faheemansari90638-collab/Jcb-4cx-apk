package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BiText
import com.example.model.EngineVariant
import com.example.model.GameUiState
import com.example.model.MicroPartCatalog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelDarkBg
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextSecondary

@Composable
fun ServiceManualScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onSelectVariant: (EngineVariant) -> Unit,
    onSaveCustomSpec: (key: String, value: String) -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = state.language
    val variant = state.micro.selectedVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SteelDarkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("manual_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = JcbYellow)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                BiText("DIGITAL SERVICE MANUAL", "डिजिटल सर्विस मैनुअल").get(lang),
                style = MaterialTheme.typography.headlineMedium,
                color = JcbYellow
            )
        }

        Spacer(Modifier.height(8.dp))

        // Engine Variant Switcher
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            EngineVariant.entries.forEach { v ->
                val sel = v == variant
                Button(
                    onClick = { onSelectVariant(v) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sel) JcbYellow else SteelSurfaceElevated,
                        contentColor = if (sel) TextOnYellow else Color.White
                    ),
                    modifier = Modifier.weight(1f).height(52.dp).testTag("variant_btn_${v.name}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(v.title.get(lang), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        Text(v.years, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Mechanical Blueprint Diagrams Card
            item {
                MechanicalDiagramsCard(lang = lang)
            }

            // Specifications Table & Unverified Manual-Data Entry
            items(MicroPartCatalog.serviceSpecs, key = { it.key }) { spec ->
                val verified = spec.verifiedValueFor(variant)
                val customVal = state.micro.customManualSpecs[spec.key]

                Card(
                    colors = CardDefaults.cardColors(containerColor = SteelSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (verified != null || !customVal.isNullOrBlank()) SteelBorder else AmberWarning, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (verified != null || !customVal.isNullOrBlank()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (verified != null || !customVal.isNullOrBlank()) HydraulicGreen else AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(spec.label.get(lang), style = MaterialTheme.typography.titleMedium)
                        }

                        if (verified != null) {
                            Text(verified, style = MaterialTheme.typography.titleMedium, color = JcbYellow)
                            Text(spec.sourceNote.get(lang), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
                        } else {
                            Text(
                                BiText(
                                    "Unverified for ${variant.code} — Please enter verified service-manual data (${spec.unit}):",
                                    "${variant.code} हेतु असत्यापित — कृपया सर्विस मैनुअल से सही मान दर्ज करें (${spec.unit}):"
                                ).get(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmberWarning
                            )
                            var draft by remember(spec.key, customVal) { mutableStateOf(customVal ?: "") }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = draft,
                                    onValueChange = { draft = it },
                                    singleLine = true,
                                    placeholder = { Text("e.g. Verified ${spec.unit}") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onSaveCustomSpec(spec.key, draft) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HydraulicGreen, contentColor = TextOnYellow),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text(BiText("Verify", "सहेजें").get(lang), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Text(spec.sourceNote.get(lang), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MechanicalDiagramsCard(lang: com.example.model.AppLanguage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BlueprintCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                BiText("TECHNICAL MECHANICAL DIAGRAMS", "यांत्रिक आरेख (ब्लूप्रिंट)").get(lang),
                style = MaterialTheme.typography.titleMedium,
                color = BlueprintCyan
            )

            // Diagram 1: Cylinder Head Torque Sequence + Piston Ring 120° Stagger
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF0A1624), RoundedCornerShape(8.dp))
                    ) {
                        // Cylinder Head Bolt Center-Out Spiral Grid
                        drawRect(color = Color(0xFF38BDF8), topLeft = Offset(14f, 14f), size = Size(size.width - 28f, size.height - 28f), style = Stroke(2f))
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        val dx = size.width * 0.16f
                        val dy = size.height * 0.22f
                        val pts = listOf(
                            Offset(cx, cy - dy), Offset(cx, cy + dy),
                            Offset(cx - dx, cy + dy), Offset(cx + dx, cy - dy),
                            Offset(cx + dx, cy + dy), Offset(cx - dx, cy - dy),
                            Offset(cx - dx * 2, cy - dy), Offset(cx + dx * 2, cy + dy)
                        )
                        pts.forEachIndexed { idx, p ->
                            drawCircle(color = if (idx < 2) Color(0xFFF5B800) else Color(0xFF38BDF8), radius = 10f, center = p, style = Stroke(2.5f))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(BiText("Head Bolt Spiral (Center-Out)", "हेड बोल्ट क्रम (केंद्र से बाहर)").get(lang), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF0A1624), RoundedCornerShape(8.dp))
                    ) {
                        val c = Offset(size.width * 0.5f, size.height * 0.5f)
                        val r = size.minDimension * 0.34f
                        drawCircle(color = Color(0xFFF5B800), radius = r, center = c, style = Stroke(3f))
                        drawCircle(color = Color(0xFF38BDF8), radius = r * 0.78f, center = c, style = Stroke(2.5f))
                        drawCircle(color = Color(0xFF10B981), radius = r * 0.56f, center = c, style = Stroke(2.5f))
                        // 120-degree gap indicators
                        for (deg in listOf(0f, 120f, 240f)) {
                            val rad = Math.toRadians(deg.toDouble()).toFloat()
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = c,
                                end = Offset(c.x + kotlin.math.cos(rad) * r, c.y + kotlin.math.sin(rad) * r),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(BiText("Piston Ring 120° Gap Stagger", "पिस्टन रिंग 120° गैप कोण").get(lang), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Diagram 2: Valve Train Stack + Hydraulic Spool O-Ring / Backup Ring
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0A1624), RoundedCornerShape(8.dp))
                    ) {
                        val cx = size.width * 0.5f
                        // Valve stem + head
                        drawLine(Color(0xFFD8E0E8), Offset(cx, 14f), Offset(cx, size.height - 16f), strokeWidth = 5f)
                        drawLine(Color(0xFFD8E0E8), Offset(cx - 24f, size.height - 16f), Offset(cx + 24f, size.height - 16f), strokeWidth = 6f)
                        // Valve stem seal & spring coils
                        drawRect(Color(0xFF10B981), Offset(cx - 10f, size.height * 0.55f), Size(20f, 12f))
                        for (yOff in 28..52 step 8) {
                            drawLine(Color(0xFFF5B800), Offset(cx - 16f, yOff.toFloat()), Offset(cx + 16f, yOff + 4f), strokeWidth = 2.5f)
                        }
                        // Split collets at top
                        drawRect(Color(0xFF38BDF8), Offset(cx - 14f, 18f), Size(28f, 8f))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(BiText("Valve, Seal, Spring & Collets", "वाल्व, सील, स्प्रिंग व कॉलेट्स").get(lang), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0A1624), RoundedCornerShape(8.dp))
                    ) {
                        val cy = size.height * 0.5f
                        // Spool bore walls
                        drawLine(Color(0xFF94A3B8), Offset(18f, cy - 22f), Offset(size.width - 18f, cy - 22f), strokeWidth = 3f)
                        drawLine(Color(0xFF94A3B8), Offset(18f, cy + 22f), Offset(size.width - 18f, cy + 22f), strokeWidth = 3f)
                        // Spool plunger
                        drawRect(Color(0xFFD8E0E8), Offset(28f, cy - 12f), Size(size.width - 56f, 24f))
                        // O-Rings + PTFE backup ring
                        drawCircle(Color(0xFFEF4444), 6f, Offset(size.width * 0.34f, cy - 17f))
                        drawCircle(Color(0xFFEF4444), 6f, Offset(size.width * 0.34f, cy + 17f))
                        drawRect(Color(0xFF38BDF8), Offset(size.width * 0.42f, cy - 20f), Size(6f, 40f))
                        drawCircle(Color(0xFFEF4444), 6f, Offset(size.width * 0.68f, cy - 17f))
                        drawCircle(Color(0xFFEF4444), 6f, Offset(size.width * 0.68f, cy + 17f))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(BiText("Hydraulic Spool O-Ring + PTFE", "हाइड्रोलिक स्पूल O-रिंग व PTFE").get(lang), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

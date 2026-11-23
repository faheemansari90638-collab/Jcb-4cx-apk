package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.BiText
import com.example.model.ContextualTooltip
import com.example.model.TooltipTargetId
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.HydraulicGreen
import com.example.ui.theme.JcbYellow
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

class TooltipAnchorRegistry {
    val anchors = mutableStateMapOf<TooltipTargetId, Rect>()
}

@Composable
fun rememberTooltipAnchorRegistry(): TooltipAnchorRegistry = remember { TooltipAnchorRegistry() }

fun Modifier.tooltipAnchor(
    targetId: TooltipTargetId,
    registry: TooltipAnchorRegistry
): Modifier = composed {
    this.onGloballyPositioned { coords ->
        if (coords.isAttached) {
            registry.anchors[targetId] = coords.boundsInRoot()
        }
    }
}

@Composable
fun BoxScope.AssemblyTooltipOverlay(
    tooltip: ContextualTooltip,
    language: AppLanguage,
    spotlightOpen: Boolean,
    autoContextualEnabled: Boolean,
    isPaused: Boolean,
    hideLivePill: Boolean,
    registry: TooltipAnchorRegistry,
    onToggleLanguage: () -> Unit,
    onOpenSpotlight: () -> Unit,
    onDismissSpotlight: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onMuteAutoTips: () -> Unit
) {
    if (isPaused) return

    // 1. Live non-blocking contextual ribbon while assembling
    AnimatedVisibility(
        visible = !spotlightOpen && autoContextualEnabled && !hideLivePill,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(150)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 60.dp, start = 80.dp, end = 80.dp)
    ) {
        LiveContextualHintRibbon(
            tooltip = tooltip,
            language = language,
            onOpenSpotlight = onOpenSpotlight,
            onToggleLanguage = onToggleLanguage,
            onMuteAutoTips = onMuteAutoTips
        )
    }

    // 2. Full Spotlight Walkthrough / Coach Card Overlay
    AnimatedVisibility(
        visible = spotlightOpen,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(180)),
        modifier = Modifier.fillMaxSize()
    ) {
        SpotlightTooltipLayer(
            tooltip = tooltip,
            language = language,
            registry = registry,
            onToggleLanguage = onToggleLanguage,
            onDismiss = onDismissSpotlight,
            onNext = onNextStep,
            onPrev = onPrevStep,
            onMute = onMuteAutoTips
        )
    }
}

@Composable
private fun LiveContextualHintRibbon(
    tooltip: ContextualTooltip,
    language: AppLanguage,
    onOpenSpotlight: () -> Unit,
    onToggleLanguage: () -> Unit,
    onMuteAutoTips: () -> Unit
) {
    val accent = if (tooltip.isWarning) AmberWarning else BlueprintCyan
    Surface(
        color = SteelSurface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .widthIn(max = 460.dp)
            .border(1.2.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenSpotlight)
            .testTag("live_tooltip_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
        ) {
            Icon(
                imageVector = if (tooltip.isWarning) Icons.Default.WarningAmber else Icons.Default.Lightbulb,
                contentDescription = null,
                tint = if (tooltip.isWarning) AmberWarning else JcbYellow,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tooltip.title.get(language),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (tooltip.isWarning) AmberWarning else JcbYellow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tooltip.actionHint.get(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = SteelSurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .clickable(onClick = onToggleLanguage)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("live_tooltip_lang_btn")
            ) {
                Text(
                    text = if (language == AppLanguage.EN) "हिन्दी" else "EN",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onMuteAutoTips,
                modifier = Modifier.size(30.dp).testTag("live_tooltip_close_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = BiText("Hide tips", "सुझाव छुपाएं").get(language),
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun SpotlightTooltipLayer(
    tooltip: ContextualTooltip,
    language: AppLanguage,
    registry: TooltipAnchorRegistry,
    onToggleLanguage: () -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onMute: () -> Unit
) {
    val density = LocalDensity.current
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    val pulseTransition = rememberInfiniteTransition(label = "tooltipPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(780),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val rawRect = registry.anchors[tooltip.targetId]
    val padPx = with(density) { 6.dp.toPx() }
    val spotlightRect = rawRect?.let { r ->
        Rect(
            left = r.left - overlayOrigin.x - padPx,
            top = r.top - overlayOrigin.y - padPx,
            right = r.right - overlayOrigin.x + padPx,
            bottom = r.bottom - overlayOrigin.y + padPx
        )
    }

    val highlightColor = when {
        tooltip.isWarning -> AmberWarning
        tooltip.targetId == TooltipTargetId.LOCK_BTN -> HydraulicGreen
        else -> JcbYellow
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                if (coords.isAttached) overlayOrigin = coords.positionInRoot()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("tooltip_spotlight_overlay")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val cardMaxWidthPx = with(density) { 370.dp.toPx() }.coerceAtMost(widthPx - with(density) { 24.dp.toPx() })
        val estCardHeightPx = with(density) { 248.dp.toPx() }
        val marginPx = with(density) { 14.dp.toPx() }

        // Compute smart non-overlapping Coach Card offset
        val cardOffset = remember(spotlightRect, widthPx, heightPx) {
            computeSmartCardOffset(
                spotlight = spotlightRect,
                containerW = widthPx,
                containerH = heightPx,
                cardW = cardMaxWidthPx,
                cardH = estCardHeightPx,
                margin = marginPx
            )
        }

        // Canvas scrim with transparent spotlight cutout & pulsing highlight ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.58f))
            if (spotlightRect != null && spotlightRect.width > 4f && spotlightRect.height > 4f) {
                val corner = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = spotlightRect.topLeft,
                    size = Size(spotlightRect.width, spotlightRect.height),
                    cornerRadius = corner,
                    blendMode = BlendMode.Clear
                )
                drawRoundRect(
                    color = highlightColor.copy(alpha = pulseAlpha),
                    topLeft = spotlightRect.topLeft,
                    size = Size(spotlightRect.width, spotlightRect.height),
                    cornerRadius = corner,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Draw crisp guide connector from spotlight edge to Coach Card center
                val cardCenter = Offset(cardOffset.x + cardMaxWidthPx * 0.5f, cardOffset.y + estCardHeightPx * 0.5f)
                val targetAnchorPt = Offset(
                    x = cardCenter.x.coerceIn(spotlightRect.left, spotlightRect.right),
                    y = cardCenter.y.coerceIn(spotlightRect.top, spotlightRect.bottom)
                )
                drawLine(
                    color = BlueprintCyan.copy(alpha = 0.85f),
                    start = targetAnchorPt,
                    end = cardCenter,
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(
                    color = highlightColor,
                    radius = 5.dp.toPx(),
                    center = targetAnchorPt
                )
            }
        }

        // Coach Card (consumes clicks inside the card so taps don't bubble to scrim)
        val otherLang = if (language == AppLanguage.EN) AppLanguage.HI else AppLanguage.EN
        Surface(
            color = SteelSurface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 10.dp,
            modifier = Modifier
                .offset { IntOffset(cardOffset.x.roundToInt(), cardOffset.y.roundToInt()) }
                .widthIn(max = 370.dp)
                .border(1.5.dp, highlightColor, RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(12.dp)
                .testTag("tooltip_coach_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                // Header: Step Badge + Inline Language Switcher + Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = (if (tooltip.isWarning) AmberWarning else BlueprintCyan).copy(alpha = 0.20f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (tooltip.isWarning) Icons.Default.WarningAmber else Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = highlightColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = tooltip.badgeText.get(language),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tooltip.isWarning) AmberWarning else BlueprintCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Inline EN / हिन्दी Switcher directly on the Tooltip Card
                    Surface(
                        color = SteelSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, BlueprintCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleLanguage)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("tooltip_lang_toggle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = if (language == AppLanguage.EN) "EN → हिन्दी" else "हिन्दी → EN",
                                style = MaterialTheme.typography.labelSmall,
                                color = JcbYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .testTag("tooltip_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = BiText("Close guide", "गाइड बंद करें").get(language),
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Primary Title + Secondary Language Paired Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tooltip.title.get(language),
                        style = MaterialTheme.typography.titleMedium,
                        color = highlightColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tooltip.title.get(otherLang),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Detailed Contextual Instruction
                Text(
                    text = tooltip.body.get(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                // Action / Tool Hint Chip
                Surface(
                    color = SteelSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SteelBorder, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = tooltip.actionHint.get(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = HydraulicGreen,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }

                // Navigation Controls: Prev, Dots, Next / Got It, Mute Auto-Tips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (tooltip.stepIndex > 1) {
                        OutlinedButton(
                            onClick = onPrev,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(38.dp).testTag("tooltip_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                            Spacer(Modifier.width(3.dp))
                            Text(BiText("Prev", "पिछला").get(language), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    } else {
                        TextButton(
                            onClick = onMute,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.height(38.dp).testTag("tooltip_mute_btn")
                        ) {
                            Text(
                                text = BiText("Mute Auto-Tips", "टिप्स बंद करें").get(language),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        for (i in 1..tooltip.totalSteps) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == tooltip.stepIndex) 8.dp else 6.dp)
                                    .background(
                                        color = if (i == tooltip.stepIndex) highlightColor else SteelBorder,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    val isLast = tooltip.stepIndex >= tooltip.totalSteps
                    OutlinedButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp).testTag("tooltip_got_it_btn")
                    ) {
                        Text(BiText("Got It", "समझ गया").get(language), style = MaterialTheme.typography.labelSmall, color = BlueprintCyan)
                    }

                    Button(
                        onClick = if (isLast) onDismiss else onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = highlightColor, contentColor = TextOnYellow),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp).testTag("tooltip_next_btn")
                    ) {
                        Text(
                            text = if (isLast) BiText("Start", "शुरू करें").get(language) else BiText("Next", "अगला").get(language),
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (!isLast) {
                            Spacer(Modifier.width(3.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun computeSmartCardOffset(
    spotlight: Rect?,
    containerW: Float,
    containerH: Float,
    cardW: Float,
    cardH: Float,
    margin: Float
): Offset {
    val maxX = (containerW - cardW - margin).coerceAtLeast(margin)
    val maxY = (containerH - cardH - margin).coerceAtLeast(margin)
    if (spotlight == null) {
        return Offset(
            x = ((containerW - cardW) * 0.5f).coerceIn(margin, maxX),
            y = ((containerH - cardH) * 0.45f).coerceIn(margin, maxY)
        )
    }

    // Prefer placing the Coach Card opposite the spotlighted side so the target remains uncovered
    val spaceAbove = spotlight.top - margin
    val spaceBelow = containerH - spotlight.bottom - margin
    val spaceLeft = spotlight.left - margin
    val spaceRight = containerW - spotlight.right - margin

    val pos = when {
        // Right-panel targets (Inspector / Lock / Fasten) -> place cleanly to the left
        spotlight.left > containerW * 0.45f && spaceLeft >= cardW * 0.85f -> {
            Offset(
                x = (spotlight.left - cardW - margin).coerceIn(margin, maxX),
                y = (spotlight.center.y - cardH * 0.5f).coerceIn(margin, maxY)
            )
        }
        // Left-panel targets (Micro Parts List) -> place cleanly to the right
        spotlight.right < containerW * 0.52f && spaceRight >= cardW * 0.85f -> {
            Offset(
                x = (spotlight.right + margin).coerceIn(margin, maxX),
                y = (spotlight.center.y - cardH * 0.5f).coerceIn(margin, maxY)
            )
        }
        // Bottom targets (Inventory Tray / Tool Belt / L10 Checklist) -> place above
        spaceAbove >= cardH -> {
            Offset(
                x = (spotlight.center.x - cardW * 0.5f).coerceIn(margin, maxX),
                y = (spotlight.top - cardH - margin).coerceIn(margin, maxY)
            )
        }
        // Top targets (Header language toggle / micro toolbar) -> place below
        spaceBelow >= cardH -> {
            Offset(
                x = (spotlight.center.x - cardW * 0.5f).coerceIn(margin, maxX),
                y = (spotlight.bottom + margin).coerceIn(margin, maxY)
            )
        }
        // Full-viewport fallback -> anchor near top-left clear of right inspector
        else -> Offset(margin, (containerH * 0.16f).coerceIn(margin, maxY))
    }
    return pos
}

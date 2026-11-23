package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JcbDarkColorScheme = darkColorScheme(
    primary = JcbYellow,
    onPrimary = TextOnYellow,
    primaryContainer = JcbYellowDark,
    onPrimaryContainer = TextPrimary,
    secondary = BlueprintCyan,
    onSecondary = TextOnYellow,
    secondaryContainer = SteelSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = HydraulicGreen,
    onTertiary = TextOnYellow,
    background = SteelDarkBg,
    onBackground = TextPrimary,
    surface = SteelSurface,
    onSurface = TextPrimary,
    surfaceVariant = SteelSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SteelBorder,
    error = HazardRed,
    onError = TextPrimary
)

@Composable
fun JcbBuildMasterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JcbDarkColorScheme,
        typography = Typography,
        content = content
    )
}

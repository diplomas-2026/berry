package com.company.product.mobile.presentation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF99F6E4),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = Color(0xFF1D4ED8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6EDF5),
    onSurfaceVariant = Color(0xFF334155)
)

private val AppTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(),
    titleLarge = Typography().titleLarge.copy(),
    titleMedium = Typography().titleMedium.copy(),
    bodyMedium = Typography().bodyMedium.copy(),
    labelLarge = Typography().labelLarge.copy()
)

@Composable
fun BerryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}

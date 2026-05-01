package com.company.product.mobile.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF44C2B1),
    onPrimary = Color(0xFF041B19),
    primaryContainer = Color(0xFF0E3B35),
    onPrimaryContainer = Color(0xFFBFF7EE),
    secondary = Color(0xFF7DD3FC),
    onSecondary = Color(0xFF04111A),
    secondaryContainer = Color(0xFF18324A),
    onSecondaryContainer = Color(0xFFCDEBFF),
    tertiary = Color(0xFFF5B84E),
    onTertiary = Color(0xFF231300),
    background = Color(0xFF071018),
    onBackground = Color(0xFFE5EEF6),
    surface = Color(0xFF0B1720),
    onSurface = Color(0xFFE5EEF6),
    surfaceVariant = Color(0xFF132230),
    onSurfaceVariant = Color(0xFF9EB0BE),
    outline = Color(0xFF294154),
    outlineVariant = Color(0xFF1E3443)
)

private val AppTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(),
    headlineMedium = Typography().headlineMedium.copy(),
    titleLarge = Typography().titleLarge.copy(),
    titleMedium = Typography().titleMedium.copy(),
    bodyLarge = Typography().bodyLarge.copy(),
    bodyMedium = Typography().bodyMedium.copy(),
    labelLarge = Typography().labelLarge.copy()
)

@Composable
fun BerryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkColors.background,
            contentColor = DarkColors.onBackground
        ) {
            content()
        }
    )
}

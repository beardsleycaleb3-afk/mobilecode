package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NaturalDarkColorScheme = darkColorScheme(
    primary = NaturalPrimaryContainer,
    onPrimary = NaturalOnPrimaryContainer,
    primaryContainer = NaturalPrimary,
    onPrimaryContainer = NaturalPrimaryContainer,
    secondary = NaturalSecondaryContainer,
    onSecondary = NaturalOnSecondaryContainer,
    background = Color(0xFF141612),
    onBackground = Color(0xFFE2E4DC),
    surface = Color(0xFF1B1E19),
    onSurface = Color(0xFFE2E4DC),
    surfaceVariant = Color(0xFF232720),
    onSurfaceVariant = Color(0xFFC4C8BA),
    outline = Color(0xFF43483E),
    outlineVariant = Color(0xFF2E332A),
    error = NaturalError,
    errorContainer = NaturalErrorContainer,
    onError = NaturalOnError
)

private val NaturalLightColorScheme = lightColorScheme(
    primary = NaturalPrimary,
    onPrimary = NaturalOnPrimary,
    primaryContainer = NaturalPrimaryContainer,
    onPrimaryContainer = NaturalOnPrimaryContainer,
    secondary = NaturalSecondary,
    onSecondary = NaturalOnSecondary,
    secondaryContainer = NaturalSecondaryContainer,
    onSecondaryContainer = NaturalOnSecondaryContainer,
    tertiary = NaturalTertiary,
    onTertiary = NaturalOnTertiary,
    tertiaryContainer = NaturalTertiaryContainer,
    onTertiaryContainer = NaturalOnTertiaryContainer,
    background = NaturalBackground,
    onBackground = NaturalOnBackground,
    surface = NaturalSurface,
    onSurface = NaturalOnSurface,
    surfaceVariant = NaturalSurfaceVariant,
    onSurfaceVariant = NaturalOnSurfaceVariant,
    outline = NaturalOutline,
    outlineVariant = NaturalOutlineVariant,
    error = NaturalError,
    errorContainer = NaturalErrorContainer,
    onError = NaturalOnError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to clean Natural Tones light theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NaturalDarkColorScheme else NaturalLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

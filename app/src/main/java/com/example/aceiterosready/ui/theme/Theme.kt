package com.example.aceiterosready.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MochaColorScheme = darkColorScheme(
    primary = mauve,
    onPrimary = crust,
    primaryContainer = surface1,
    onPrimaryContainer = text,
    inversePrimary = blue,
    secondary = lavender,
    onSecondary = crust,
    secondaryContainer = surface1,
    onSecondaryContainer = text,
    tertiary = pink,
    onTertiary = crust,
    tertiaryContainer = surface1,
    onTertiaryContainer = text,
    background = base,
    onBackground = text,
    surface = base,
    onSurface = text,
    surfaceVariant = surface1,
    onSurfaceVariant = subtext0,
    surfaceTint = mauve,
    inverseSurface = text,
    inverseOnSurface = base,
    error = red,
    onError = crust,
    errorContainer = maroon,
    onErrorContainer = crust,
    outline = overlay1,
    outlineVariant = surface2,
    scrim = crust,
    surfaceBright = surface1,
    surfaceDim = mantle,
    surfaceContainer = base,
    surfaceContainerHigh = surface0,
    surfaceContainerHighest = surface1,
    surfaceContainerLow = mantle,
    surfaceContainerLowest = crust,
)

/**
 * Variante clara derivada de la paleta Mocha: los acentos se mantienen y los
 * neutros se generan mezclando Base con Rosewater.
 */
private val MochaLightColorScheme = lightColorScheme(
    primary = mauve,
    onPrimary = crust,
    primaryContainer = lavender,
    onPrimaryContainer = crust,
    inversePrimary = blue,
    secondary = blue,
    onSecondary = crust,
    secondaryContainer = sky,
    onSecondaryContainer = crust,
    tertiary = teal,
    onTertiary = crust,
    tertiaryContainer = green,
    onTertiaryContainer = crust,
    background = mochaTint(0.97f),
    onBackground = base,
    surface = mochaTint(0.99f),
    onSurface = base,
    surfaceVariant = mochaTint(0.88f),
    onSurfaceVariant = surface2,
    surfaceTint = mauve,
    inverseSurface = base,
    inverseOnSurface = rosewater,
    error = red,
    onError = crust,
    errorContainer = maroon,
    onErrorContainer = crust,
    outline = surface2,
    outlineVariant = overlay0,
    scrim = crust,
    surfaceBright = mochaTint(1f),
    surfaceDim = mochaTint(0.84f),
    surfaceContainer = mochaTint(0.94f),
    surfaceContainerHigh = mochaTint(0.90f),
    surfaceContainerHighest = mochaTint(0.86f),
    surfaceContainerLow = mochaTint(0.97f),
    surfaceContainerLowest = mochaTint(1f),
)

@Composable
fun AceiterosReadyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MochaColorScheme else MochaLightColorScheme,
        typography = Typography,
        content = content,
    )
}

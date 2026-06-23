package com.fidit.memberlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkContainer,
    onPrimaryContainer = DarkOnContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    error = StatusUnpaidDark,
    onError = DarkOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IrisPrimary,
    onPrimary = IrisOnPrimary,
    primaryContainer = IrisContainer,
    onPrimaryContainer = IrisOnContainer,
    secondary = LightSecondary,
    onSecondary = IrisOnPrimary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightSurfaceVariant,
    error = StatusUnpaid,
    onError = IrisOnPrimary
)

@Composable
fun MemberLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.fidit.memberlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalDarkTheme = staticCompositionLocalOf { false }

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
    surfaceContainerLowest = DarkContainerLowest,
    surfaceContainerLow = DarkContainerLow,
    surfaceContainer = DarkContainer2,
    surfaceContainerHigh = DarkContainerHigh,
    surfaceContainerHighest = DarkContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
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
    surfaceContainerLowest = LightContainerLowest,
    surfaceContainerLow = LightContainerLow,
    surfaceContainer = LightContainer,
    surfaceContainerHigh = LightContainerHigh,
    surfaceContainerHighest = LightContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = StatusUnpaid,
    onError = IrisOnPrimary
)

@Composable
fun MemberLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

@Composable
fun paidColor(): Color = if (LocalDarkTheme.current) StatusPaidDark else StatusPaid

@Composable
fun partialColor(): Color = if (LocalDarkTheme.current) StatusPartialDark else StatusPartial

@Composable
fun unpaidColor(): Color = if (LocalDarkTheme.current) StatusUnpaidDark else StatusUnpaid

package com.fidit.memberlog.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

val RolePalette = listOf(
    "#6750A4", "#1E88E5", "#2E9E6B", "#F59E0B", "#E5484D",
    "#00897B", "#8E24AA", "#3949AB", "#EF6C00", "#5E35B1"
)

fun roleColor(hex: String): Color = try {
    Color(hex.toColorInt())
} catch (e: Exception) {
    Color(0xFF6750A4)
}

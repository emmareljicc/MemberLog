@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.fidit.memberlog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.R

val DisplayFont = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.space_grotesk, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.space_grotesk, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

val Typography = Typography(
    displayMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    )
)

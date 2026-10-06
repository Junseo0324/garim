package com.devhjs.garim.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun type(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.Default, fontSize = size.sp, lineHeight = line.sp,
    fontWeight = weight, letterSpacing = 0.sp,
)

val Typography = Typography(
    headlineLarge = type(32, 42, FontWeight.ExtraBold),
    headlineMedium = type(26, 34, FontWeight.Bold),
    titleLarge = type(24, 32, FontWeight.Bold),
    titleMedium = type(18, 26, FontWeight.SemiBold),
    titleSmall = type(14, 20, FontWeight.SemiBold),
    bodyLarge = type(16, 24),
    bodyMedium = type(14, 20),
    bodySmall = type(12, 16),
    labelLarge = type(14, 20, FontWeight.Medium),
    labelMedium = type(12, 16, FontWeight.Medium),
    labelSmall = type(11, 16, FontWeight.Medium),
)

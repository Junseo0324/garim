package com.devhjs.garim.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/** A redaction bar on a sheet, shared by the wordmark and launcher. */
@Composable
fun GarimMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / 40f
        drawRoundRect(Color(0xFFD6ED88), cornerRadius = CornerRadius(8f * unit))
        drawRoundRect(Color(0xFF242B23), Offset(10f * unit, 7f * unit), Size(21f * unit, 27f * unit), CornerRadius(2f * unit))
        drawRect(Color(0xFFD6ED88), Offset(14f * unit, 13f * unit), Size(13f * unit, 5f * unit))
        drawRect(Color(0xFF87927B), Offset(14f * unit, 23f * unit), Size(11f * unit, 1.5f * unit))
        drawRect(Color(0xFF87927B), Offset(14f * unit, 27f * unit), Size(7f * unit, 1.5f * unit))
    }
}

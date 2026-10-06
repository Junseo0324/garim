package com.devhjs.garim.presentation.result
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.devhjs.garim.core.ui.LoadingContent
import com.devhjs.garim.core.ui.rememberPhotoBitmap
import com.devhjs.garim.domain.model.ExportedPhoto
import com.devhjs.garim.domain.model.PhotoViewport
import kotlin.math.roundToInt

@Composable
fun ResultPhoto(photo: ExportedPhoto, modifier: Modifier = Modifier) {
    val bitmap by rememberPhotoBitmap(photo.previewPath)
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    var zoom by rememberSaveable(photo.id) { mutableFloatStateOf(1f) }
    var panXFraction by rememberSaveable(photo.id) { mutableFloatStateOf(0f) }
    var panYFraction by rememberSaveable(photo.id) { mutableFloatStateOf(0f) }
    val background = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier.background(background)) {
        if (bitmap == null) LoadingContent("결과를 불러오고 있어요.")
        else Canvas(Modifier.fillMaxSize().onSizeChanged { bounds = it }
            .semantics { contentDescription = "가린 결과 사진. 두 손가락으로 확대해 확인할 수 있어요." }
            .pointerInput(photo.id, bounds) {
                if (bounds.width == 0 || bounds.height == 0) return@pointerInput
                detectTransformGestures { center, pan, factor, _ ->
                    val view = PhotoViewport(bounds.width.toFloat(), bounds.height.toFloat(),
                        photo.width.toFloat(), photo.height.toFloat(), zoom)
                        .withPanFractions(panXFraction, panYFraction)
                        .transformed(center.x, center.y, pan.x, pan.y, factor)
                    zoom = view.zoom; panXFraction = view.panX / view.width; panYFraction = view.panY / view.height
                }
            }) {
            val view = PhotoViewport(size.width, size.height, photo.width.toFloat(), photo.height.toFloat(), zoom)
                .withPanFractions(panXFraction, panYFraction)
            clipRect {
                drawImage(bitmap!!, dstOffset = IntOffset(view.left.roundToInt(), view.top.roundToInt()),
                    dstSize = IntSize(view.width.roundToInt().coerceAtLeast(1), view.height.roundToInt().coerceAtLeast(1)))
            }
        }
    }
}

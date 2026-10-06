package com.devhjs.garim.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.devhjs.garim.core.ui.LoadingContent
import com.devhjs.garim.core.ui.rememberPhotoBitmap
import com.devhjs.garim.domain.model.MaskRect
import com.devhjs.garim.domain.model.PhotoViewport
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun PhotoEditorCanvas(state: EditorState, onAction: (EditorAction) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    val photo = state.photo ?: return
    val bitmap by rememberPhotoBitmap(photo.previewPath)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    // A draft belongs only to the current pointer gesture; committed edits live in the ViewModel.
    var draft by remember(photo.id) { mutableStateOf<MaskRect?>(null) }
    var precisionFocus by remember(photo.id) { mutableStateOf<Offset?>(null) }
    val current by rememberUpdatedState(state)
    val dispatch by rememberUpdatedState(onAction)
    val enabledNow by rememberUpdatedState(enabled)
    val handleHitRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val outsideHitRadius = with(LocalDensity.current) { 12.dp.toPx() }
    val accent = MaterialTheme.colorScheme.secondary
    val background = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(modifier.background(background)) {
        if (bitmap == null) {
            LoadingContent("사진을 불러오고 있어요.")
        } else Canvas(Modifier.fillMaxSize().onSizeChanged { canvasSize = it }
            .semantics { contentDescription = "사진 편집 영역. 한 손가락으로 가리고 두 손가락으로 확대하세요." }
            .pointerInput(photo.id, canvasSize) {
                if (canvasSize.width == 0 || canvasSize.height == 0) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!enabledNow) return@awaitEachGesture
                    var view = PhotoViewport(canvasSize.width.toFloat(), canvasSize.height.toFloat(),
                        photo.width.toFloat(), photo.height.toFloat(), current.zoom)
                        .withPanFractions(current.panXFraction, current.panYFraction)
                    val initialMasks = current.history.masks
                    val target = maskGestureTarget(initialMasks, current.selectedId, view,
                        down.position, handleHitRadius, outsideHitRadius)
                    val hit = target?.mask
                    val corner = target?.corner
                    val canDraw = hit == null && view.contains(down.position.x, down.position.y)
                    val startX = view.x(down.position.x)
                    val startY = view.y(down.position.y)
                    val newId = UUID.randomUUID().toString()
                    var moved = false
                    var multiTouch = false
                    try {
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.count { it.pressed }
                            if (pressed >= 2) {
                                multiTouch = true
                                draft = null
                                precisionFocus = null
                                val center = event.calculateCentroid(useCurrent = false)
                                val pan = event.calculatePan()
                                view = view.transformed(center.x, center.y, pan.x, pan.y, event.calculateZoom())
                                dispatch(EditorAction.Transform(view.zoom, view.panX / view.width, view.panY / view.height))
                            } else if (!multiTouch) {
                                val pointer = event.changes.firstOrNull { it.id == down.id }
                                if (pointer != null) {
                                    if ((pointer.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                                    if (moved) {
                                        val x = view.x(pointer.position.x)
                                        val y = view.y(pointer.position.y)
                                        val dx = (pointer.position.x - down.position.x) / view.width
                                        val dy = (pointer.position.y - down.position.y) / view.height
                                        draft = when {
                                            corner != null && hit != null -> hit.resized(corner, dx, dy)
                                            hit != null -> hit.moved(dx, dy)
                                            canDraw -> MaskRect.between(newId, startX, startY, x, y)
                                            else -> null
                                        }
                                        precisionFocus = when {
                                            corner != null -> draft?.cornerPosition(corner, view)
                                            canDraw && draft != null -> Offset(view.left + x * view.width, view.top + y * view.height)
                                            else -> null
                                        }
                                    }
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                        if (!multiTouch) {
                            val completed = draft
                            if (completed != null) {
                                val next = if (hit == null) initialMasks + completed
                                    else initialMasks.map { if (it.id == completed.id) completed else it }
                                dispatch(EditorAction.CommitMasks(next, completed.id))
                            } else if (!moved) dispatch(EditorAction.SelectMask(hit?.id))
                        }
                    } finally {
                        draft = null
                        precisionFocus = null
                    }
                }
            }) {
            val view = PhotoViewport(size.width, size.height, photo.width.toFloat(), photo.height.toFloat(),
                state.zoom).withPanFractions(state.panXFraction, state.panYFraction)
            val visible = state.history.masks.map { mask -> if (draft?.id == mask.id) draft!! else mask } +
                listOfNotNull(draft?.takeIf { d -> state.history.masks.none { it.id == d.id } })
            clipRect {
                drawImage(bitmap!!, dstOffset = IntOffset(view.left.roundToInt(), view.top.roundToInt()),
                    dstSize = IntSize(view.width.roundToInt().coerceAtLeast(1), view.height.roundToInt().coerceAtLeast(1)))
                visible.forEach { mask ->
                    val x = view.left + mask.left * view.width
                    val y = view.top + mask.top * view.height
                    val w = (mask.right - mask.left) * view.width
                    val h = (mask.bottom - mask.top) * view.height
                    drawRect(Color.Black, Offset(x, y), Size(w, h))
                    if (mask.id == (draft?.id ?: state.selectedId)) {
                        drawRect(accent, Offset(x, y), Size(w, h), style = Stroke(2.dp.toPx()))
                        listOf(Offset(x, y), Offset(x + w, y), Offset(x + w, y + h), Offset(x, y + h)).forEach {
                            drawCircle(Color.White, 5.dp.toPx(), it)
                            drawCircle(accent, 5.dp.toPx(), it, style = Stroke(2.dp.toPx()))
                        }
                    }
                }
            }
            precisionFocus?.let { focus ->
                // The opposite corner keeps the loupe away from the active finger.
                // This overlay is drawn only in the editor, never in the output image.
                val margin = 12.dp.toPx()
                val width = minOf(120.dp.toPx(), size.width * .45f)
                val height = minOf(92.dp.toPx(), size.height * .35f)
                val left = if (focus.x < size.width / 2f) size.width - margin - width else margin
                val top = if (focus.y < size.height / 2f) size.height - margin - height else margin
                val center = Offset(left + width / 2f, top + height / 2f)
                val magnification = 2.5f
                val zoomView = view.copy(
                    canvasWidth = size.width, canvasHeight = size.height,
                    zoom = view.zoom * magnification,
                    panX = center.x - size.width / 2f + (view.left - focus.x + view.width / 2f) * magnification,
                    panY = center.y - size.height / 2f + (view.top - focus.y + view.height / 2f) * magnification,
                )
                drawRect(Color.White, Offset(left, top), Size(width, height))
                clipRect(left, top, left + width, top + height) {
                    drawImage(bitmap!!, dstOffset = IntOffset(zoomView.left.roundToInt(), zoomView.top.roundToInt()),
                        dstSize = IntSize(zoomView.width.roundToInt().coerceAtLeast(1), zoomView.height.roundToInt().coerceAtLeast(1)))
                    visible.forEach { mask ->
                        drawRect(Color.Black, Offset(zoomView.left + mask.left * zoomView.width, zoomView.top + mask.top * zoomView.height),
                            Size((mask.right - mask.left) * zoomView.width, (mask.bottom - mask.top) * zoomView.height))
                    }
                    // White underlay remains visible against both the mask and the source.
                    drawFocusCrosshair(center, Color.White, 3.dp.toPx())
                    drawFocusCrosshair(center, accent, 1.dp.toPx())
                }
                drawRect(accent, Offset(left, top), Size(width, height), style = Stroke(2.dp.toPx()))
            }
        }
    }
}

private fun DrawScope.drawFocusCrosshair(center: Offset, color: Color, stroke: Float) {
    val length = 10.dp.toPx()
    drawLine(color, center - Offset(length, 0f), center + Offset(length, 0f), stroke)
    drawLine(color, center - Offset(0f, length), center + Offset(0f, length), stroke)
}

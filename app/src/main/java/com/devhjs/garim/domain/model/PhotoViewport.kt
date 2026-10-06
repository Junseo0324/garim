package com.devhjs.garim.domain.model
import kotlin.math.min

/** Maps the oriented photograph to the visible canvas. Masks always stay in photo coordinates. */
data class PhotoViewport(
    val canvasWidth: Float, val canvasHeight: Float,
    val photoWidth: Float, val photoHeight: Float,
    val zoom: Float = 1f, val panX: Float = 0f, val panY: Float = 0f,
) {
    val scale: Float get() = min(canvasWidth / photoWidth, canvasHeight / photoHeight) * zoom
    val width: Float get() = photoWidth * scale
    val height: Float get() = photoHeight * scale
    val left: Float get() = (canvasWidth - width) / 2f + panX
    val top: Float get() = (canvasHeight - height) / 2f + panY
    fun x(pixel: Float) = ((pixel - left) / width).coerceIn(0f, 1f)
    fun y(pixel: Float) = ((pixel - top) / height).coerceIn(0f, 1f)
    fun contains(x: Float, y: Float) = x in left..(left + width) && y in top..(top + height)
    fun withPanFractions(x: Float, y: Float): PhotoViewport = copy(panX = x * width, panY = y * height).clamped()
    fun transformed(cx: Float, cy: Float, dx: Float, dy: Float, factor: Float): PhotoViewport {
        val nextZoom = (zoom * factor).coerceIn(1f, 6f)
        val ratio = nextZoom / zoom
        val next = copy(
            zoom = nextZoom,
            panX = (panX + canvasWidth / 2f - cx) * ratio + cx - canvasWidth / 2f + dx,
            panY = (panY + canvasHeight / 2f - cy) * ratio + cy - canvasHeight / 2f + dy,
        )
        return next.clamped()
    }
    private fun clamped(): PhotoViewport {
        val maxX = ((width - canvasWidth) / 2f).coerceAtLeast(0f)
        val maxY = ((height - canvasHeight) / 2f).coerceAtLeast(0f)
        return copy(panX = panX.coerceIn(-maxX, maxX), panY = panY.coerceIn(-maxY, maxY))
    }
}

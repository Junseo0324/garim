package com.devhjs.garim.domain.model
import kotlin.math.max
import kotlin.math.min

enum class MaskCorner(val isLeft: Boolean, val isTop: Boolean) {
    TOP_LEFT(true, true), TOP_RIGHT(false, true), BOTTOM_RIGHT(false, false), BOTTOM_LEFT(true, false)
}

data class MaskRect(val id: String, val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init {
        require(listOf(left, top, right, bottom).all { it.isFinite() && it in 0f..1f })
        require(left < right && top < bottom)
    }
    fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
    fun moved(dx: Float, dy: Float): MaskRect {
        val x = dx.coerceIn(-left, 1f - right)
        val y = dy.coerceIn(-top, 1f - bottom)
        return copy(left = left + x, right = right + x, top = top + y, bottom = bottom + y)
    }
    // Deltas preserve the distance between the finger and the corner at pickup.
    // Keep the opposite corner fixed, even when the drag crosses it.
    fun resized(corner: MaskCorner, dx: Float, dy: Float): MaskRect {
        val minWidth = min(right - left, 0.0002f)
        val minHeight = min(bottom - top, 0.0002f)
        return copy(
            left = if (corner.isLeft) (left + dx).coerceIn(0f, right - minWidth) else left,
            right = if (!corner.isLeft) (right + dx).coerceIn(left + minWidth, 1f) else right,
            top = if (corner.isTop) (top + dy).coerceIn(0f, bottom - minHeight) else top,
            bottom = if (!corner.isTop) (bottom + dy).coerceIn(top + minHeight, 1f) else bottom,
        )
    }
    companion object {
        fun between(id: String, x1: Float, y1: Float, x2: Float, y2: Float): MaskRect? {
            val l = min(x1, x2).coerceIn(0f, 1f)
            val r = max(x1, x2).coerceIn(0f, 1f)
            val t = min(y1, y2).coerceIn(0f, 1f)
            val b = max(y1, y2).coerceIn(0f, 1f)
            return if (r - l > 0.0001f && b - t > 0.0001f) MaskRect(id, l, t, r, b) else null
        }
    }
}

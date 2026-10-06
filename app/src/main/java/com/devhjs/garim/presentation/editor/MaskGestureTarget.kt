package com.devhjs.garim.presentation.editor

import androidx.compose.ui.geometry.Offset
import com.devhjs.garim.domain.model.MaskCorner
import com.devhjs.garim.domain.model.MaskRect
import com.devhjs.garim.domain.model.PhotoViewport

internal data class MaskGestureTarget(val mask: MaskRect, val corner: MaskCorner? = null)

internal fun maskGestureTarget(
    masks: List<MaskRect>, selectedId: String?, view: PhotoViewport,
    position: Offset, handleRadius: Float, outsideRadius: Float,
): MaskGestureTarget? {
    val body = if (view.contains(position.x, position.y))
        masks.lastOrNull { it.contains(view.x(position.x), view.y(position.y)) } else null
    val selected = masks.firstOrNull { it.id == selectedId }
    // A selected handle must not steal a touch from a different box above it.
    if (selected != null && (body == null || body.id == selected.id)) {
        val nearest = MaskCorner.entries.minBy { corner ->
            (selected.cornerPosition(corner, view) - position).getDistance()
        }
        // Inside thin boxes, leave the middle available for moving. Outside,
        // keep the hit area compact so nearby lines can receive a new box.
        val radius = if (body != null) minOf(handleRadius,
            (selected.right - selected.left) * view.width / 4f,
            (selected.bottom - selected.top) * view.height / 4f) else outsideRadius
        if ((selected.cornerPosition(nearest, view) - position).getDistance() <= radius) {
            return MaskGestureTarget(selected, nearest)
        }
    }
    return body?.let { MaskGestureTarget(it) }
}

internal fun MaskRect.cornerPosition(corner: MaskCorner, view: PhotoViewport) = Offset(
    view.left + (if (corner.isLeft) left else right) * view.width,
    view.top + (if (corner.isTop) top else bottom) * view.height,
)

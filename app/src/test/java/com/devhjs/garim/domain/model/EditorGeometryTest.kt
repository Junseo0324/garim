package com.devhjs.garim.domain.model
import org.junit.Assert.*
import org.junit.Test

class EditorGeometryTest {
    private val mask = MaskRect("one", .2f, .3f, .5f, .6f)
    @Test fun reversedDragProducesNormalizedMask() {
        assertEquals(mask, MaskRect.between("one", .5f, .6f, .2f, .3f))
    }
    @Test fun emptyDragIsIgnored() { assertNull(MaskRect.between("one", .2f, .3f, .2f, .3f)) }
    @Test fun moveStopsAtEdgesAndPreservesSize() {
        val moved = mask.moved(1f, -1f)
        assertEquals(1f, moved.right, .0001f)
        assertEquals(0f, moved.top, .0001f)
        assertEquals(mask.right - mask.left, moved.right - moved.left, .0001f)
    }
    @Test fun letterboxedCanvasMapsBackToPhoto() {
        val view = PhotoViewport(1000f, 1000f, 500f, 1000f)
        assertEquals(.5f, view.x(500f), .0001f)
        assertEquals(.25f, view.y(250f), .0001f)
        assertFalse(view.contains(100f, 250f))
    }
    @Test fun resizeKeepsOppositeCornerAndUsesDragDelta() {
        val resized = mask.resized(MaskCorner.BOTTOM_RIGHT, .1f, -.1f)
        assertEquals(mask.left, resized.left, .0001f)
        assertEquals(mask.top, resized.top, .0001f)
        assertEquals(.6f, resized.right, .0001f)
        assertEquals(.5f, resized.bottom, .0001f)
    }
    @Test fun resizeCannotFlipAcrossOppositeCornerOrLeavePhoto() {
        MaskCorner.entries.forEach { corner ->
            val shrinking = mask.resized(corner,
                if (corner.isLeft) 1f else -1f, if (corner.isTop) 1f else -1f)
            assertTrue(shrinking.left < shrinking.right)
            assertTrue(shrinking.top < shrinking.bottom)
            val expanding = mask.resized(corner,
                if (corner.isLeft) -1f else 1f, if (corner.isTop) -1f else 1f)
            assertEquals(if (corner.isLeft) 0f else mask.left, expanding.left, .0001f)
            assertEquals(if (corner.isLeft) mask.right else 1f, expanding.right, .0001f)
            assertEquals(if (corner.isTop) 0f else mask.top, expanding.top, .0001f)
            assertEquals(if (corner.isTop) mask.bottom else 1f, expanding.bottom, .0001f)
        }
    }
    @Test fun zoomAroundPointKeepsPhotoCoordinate() {
        val view = PhotoViewport(1000f, 1000f, 1000f, 1000f)
        val transformed = view.transformed(400f, 400f, 0f, 0f, 2f)
        assertEquals(view.x(400f), transformed.x(400f), .0001f)
        assertEquals(view.y(400f), transformed.y(400f), .0001f)
    }
    @Test fun historyBranchesAfterUndo() {
        val first = EditorHistory().commit(listOf(mask))
        val second = first.commit(listOf(mask, mask.copy(id = "two")))
        assertEquals(first.masks, second.undo().masks)
        assertEquals(second.masks, second.undo().redo().masks)
        assertTrue(second.undo().commit(listOf(mask.copy(id = "three"))).redo.isEmpty())
    }
    @Test fun normalizedPanKeepsPhotoCenterWhenCanvasSizeChanges() {
        val original = PhotoViewport(400f, 600f, 800f, 1000f, 3f).withPanFractions(.1f, -.1f)
        val resized = PhotoViewport(600f, 400f, 800f, 1000f, 3f).withPanFractions(.1f, -.1f)
        assertEquals(original.x(200f), resized.x(300f), .0001f)
        assertEquals(original.y(300f), resized.y(200f), .0001f)
        val fitted = PhotoViewport(400f, 600f, 800f, 1000f).withPanFractions(.4f, -.4f)
        assertEquals(0f, fitted.panX, .0001f)
        assertEquals(0f, fitted.panY, .0001f)
    }
}

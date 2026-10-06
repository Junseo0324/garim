package com.devhjs.garim.presentation.editor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.ui.theme.GarimTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class EditorGestureTest {
    @get:Rule val compose = createComposeRule()
    private val state = mutableStateOf(EditorState())
    private val canvasDescription = "사진 편집 영역. 한 손가락으로 가리고 두 손가락으로 확대하세요."
    private fun prepare(masks: List<MaskRect> = emptyList(), selectedId: String? = null): EditorRobot {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "gesture-test.png")
        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
            recycle()
        }
        state.value = EditorState(photo = PhotoDocument("gesture", file.absolutePath, 100, 100),
            history = EditorHistory(masks = masks), selectedId = selectedId)
        compose.setContent {
            GarimTheme {
                PhotoEditorCanvas(state.value, { action ->
                    when (action) {
                        is EditorAction.CommitMasks -> state.value = state.value.copy(
                            history = state.value.history.commit(action.masks), selectedId = action.selectedId)
                        is EditorAction.SelectMask -> state.value = state.value.copy(selectedId = action.id)
                        is EditorAction.Transform -> state.value = state.value.copy(zoom = action.zoom, panXFraction = action.panXFraction, panYFraction = action.panYFraction)
                        else -> Unit
                    }
                }, Modifier.size(300.dp))
            }
        }
        compose.waitUntil { compose.onAllNodesWithContentDescription(canvasDescription).fetchSemanticsNodes().isNotEmpty() }
        compose.waitForIdle()
        return EditorRobot()
    }

    private inner class EditorRobot {
        val canvas get() = compose.onNodeWithContentDescription(canvasDescription)
        fun drag(x1: Float, y1: Float, x2: Float, y2: Float): EditorRobot {
            canvas.performTouchInput {
                swipe(Offset(width * x1, height * y1), Offset(width * x2, height * y2), 400)
            }
            return this
        }
        fun assertSingleMask(left: Float, top: Float, right: Float, bottom: Float): EditorRobot {
            compose.runOnIdle {
                val mask = state.value.history.masks.single()
                assertEquals(left, mask.left, .005f)
                assertEquals(top, mask.top, .005f)
                assertEquals(right, mask.right, .005f)
                assertEquals(bottom, mask.bottom, .005f)
            }
            return this
        }
    }

    @Test fun thinSelectedBoxMovesWithoutResizingOrAddingBox() {
        val mask = MaskRect("thin", .2f, .3f, .6f, .32f)
        prepare(listOf(mask), mask.id).drag(.4f, .31f, .5f, .41f)
            .assertSingleMask(.3f, .4f, .7f, .42f)
    }

    @Test fun unselectedBoxMovesWithFirstDrag() {
        val mask = MaskRect("one", .2f, .2f, .5f, .5f)
        prepare(listOf(mask)).drag(.35f, .35f, .55f, .55f)
            .assertSingleMask(.4f, .4f, .7f, .7f)
        compose.runOnIdle { assertEquals(mask.id, state.value.selectedId) }
    }

    @Test fun smallBoxResizesNearestCornerAndPreservesGrabOffset() {
        val mask = MaskRect("small", .4f, .4f, .46f, .46f)
        prepare(listOf(mask), mask.id).drag(.455f, .455f, .555f, .555f)
            .assertSingleMask(.4f, .4f, .56f, .56f)
    }

    @Test fun newBoxCanBeDrawnOnNearbyLine() {
        val mask = MaskRect("line", .2f, .3f, .6f, .32f)
        prepare(listOf(mask), mask.id).drag(.2f, .38f, .6f, .42f)
        compose.runOnIdle {
            assertEquals(2, state.value.history.masks.size)
            assertEquals(mask, state.value.history.masks.first())
            assertEquals(.38f, state.value.history.masks.last().top, .005f)
        }
    }

    @Test fun loupeShowsDuringDrawingAndDisappearsAfterRelease() {
        val robot = prepare()
        val before = robot.canvas.captureToImage().toPixelMap()
        val x = (before.width * 12f / 300f).toInt()
        val y = (before.height * 40f / 300f).toInt()
        val originalPixel = before[x, y]
        robot.canvas.performTouchInput {
            down(Offset(width * .2f, height * .2f))
            moveTo(Offset(width * .5f, height * .5f))
        }
        // The loupe border must appear and disappear, independent of the brand palette.
        fun hasLoupeBorder(): Boolean {
            val pixels = robot.canvas.captureToImage().toPixelMap()
            return pixels[x, y] != originalPixel
        }
        assertTrue(hasLoupeBorder())
        robot.canvas.performTouchInput { up() }
        assertFalse(hasLoupeBorder())
        robot.assertSingleMask(.2f, .2f, .5f, .5f)
    }

    @Test fun pinchCancelsInProgressBoxMove() {
        val mask = MaskRect("one", .2f, .2f, .5f, .5f)
        val robot = prepare(listOf(mask), mask.id)
        robot.canvas.performTouchInput {
            down(0, Offset(width * .35f, height * .35f))
            updatePointerTo(0, Offset(width * .45f, height * .45f)); move()
            down(1, Offset(width * .7f, height * .7f))
            updatePointerTo(0, Offset(width * .25f, height * .25f))
            updatePointerTo(1, Offset(width * .85f, height * .85f)); move()
            up(0); up(1)
        }
        compose.runOnIdle {
            assertEquals(listOf(mask), state.value.history.masks)
            assertTrue(state.value.history.undo.isEmpty())
            assertTrue(state.value.zoom > 1f)
        }
    }

    @Test fun dragCreatesNormalizedMaskAndMovesSelectedBox() {
        prepare()
        val node = compose.onNodeWithContentDescription(canvasDescription)
        node.performTouchInput { swipe(Offset(width * .2f, height * .2f), Offset(width * .5f, height * .5f), 400) }
        compose.runOnIdle {
            val mask = state.value.history.masks.single()
            assertEquals(.2f, mask.left, .005f)
            assertEquals(.5f, mask.right, .005f)
        }
        node.performTouchInput { swipe(Offset(width * .35f, height * .35f), Offset(width * .55f, height * .55f), 400) }
        compose.runOnIdle {
            val mask = state.value.history.masks.single()
            assertEquals(.4f, mask.left, .005f)
            assertEquals(.7f, mask.right, .005f)
        }
    }

    @Test fun twoFingerZoomDoesNotCommitAccidentalMask() {
        prepare()
        compose.onNodeWithContentDescription(canvasDescription).performTouchInput {
            down(0, Offset(width * .3f, height * .3f))
            updatePointerTo(0, Offset(width * .28f, height * .28f)); move()
            down(1, Offset(width * .7f, height * .7f))
            updatePointerTo(0, Offset(width * .15f, height * .15f))
            updatePointerTo(1, Offset(width * .85f, height * .85f)); move()
            up(0); up(1)
        }
        compose.runOnIdle {
            assertTrue(state.value.zoom > 1f)
            assertTrue(state.value.history.masks.isEmpty())
            assertTrue(state.value.history.undo.isEmpty())
        }
    }
}

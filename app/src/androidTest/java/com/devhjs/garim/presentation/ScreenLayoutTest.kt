package com.devhjs.garim.presentation

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.presentation.editor.*
import com.devhjs.garim.presentation.home.*
import com.devhjs.garim.presentation.result.*
import com.devhjs.garim.ui.theme.GarimTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ScreenLayoutTest {
    @get:Rule val compose = createComposeRule()
    private val editorDescription = "사진 편집 영역. 한 손가락으로 가리고 두 손가락으로 확대하세요."
    private val resultDescription = "가린 결과 사진. 두 손가락으로 확대해 확인할 수 있어요."
    private val mask = MaskRect("one", .1f, .1f, .5f, .3f)

    private inner class LayoutRobot {
        fun show(width: Int, height: Int, fontScale: Float, dark: Boolean = false, content: @Composable () -> Unit): LayoutRobot {
            compose.setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale)) {
                    GarimTheme(dark) {
                        Surface(Modifier.requiredSize(width.dp, height.dp).testTag("screen")) { content() }
                    }
                }
            }
            compose.waitForIdle()
            return this
        }
        fun assertVisibleInside(node: SemanticsNodeInteraction): LayoutRobot {
            node.assertIsDisplayed()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            val screen = compose.onNodeWithTag("screen").fetchSemanticsNode().boundsInRoot
            assertTrue("Control outside screen: $bounds / $screen", bounds.left >= screen.left && bounds.right <= screen.right + 1f &&
                bounds.top >= screen.top && bounds.bottom <= screen.bottom + 1f)
            return this
        }
        fun assertPhotoHeight(description: String, minimum: Float): LayoutRobot {
            val node = compose.onNodeWithContentDescription(description)
            compose.waitUntil { compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
            assertVisibleInside(node)
            assertTrue(node.fetchSemanticsNode().boundsInRoot.height >= minimum)
            return this
        }
        fun assertSingleLine(label: String): LayoutRobot {
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(label).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals("Unexpected wrapping: $label", 1, layouts.single().lineCount)
            return this
        }
    }

    private fun photo(width: Int = 800, height: Int = 400): PhotoDocument {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "layout-$width-$height.png")
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            file.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
            recycle()
        }
        return PhotoDocument("layout", file.absolutePath, width, height)
    }
    private fun editorState(photo: PhotoDocument) = EditorState(photo = photo,
        history = EditorHistory().commit(listOf(mask)), selectedId = mask.id)
    private fun resultState(photo: PhotoDocument) = ResultState(
        photo = ExportedPhoto("result", photo.previewPath, "content://test", photo.width, photo.height), hasSaved = true)

    @Test fun homeEntryStaysVisibleOnSmallScreenWithLargeText() {
        var selected = false
        val robot = LayoutRobot().show(320, 440, 2f) { HomeScreen(HomeState()) { selected = it == HomeAction.SelectPhoto } }
        robot.assertVisibleInside(compose.onNodeWithText("사진 선택")).assertSingleLine("사진 선택")
        compose.onNodeWithText("사진 선택").performClick()
        assertTrue(selected)
    }
    @Test fun smallEditorWithLargeTextKeepsPhotoAndNextActionVisible() {
        val state = editorState(photo(400, 800))
        var action: EditorAction? = null
        val robot = LayoutRobot().show(320, 440, 2f) { EditorScreen(state) { action = it } }
        robot.assertPhotoHeight(editorDescription, 120f)
            .assertVisibleInside(compose.onNodeWithText("결과 확인"))
            .assertVisibleInside(compose.onNodeWithContentDescription("실행 취소"))
            .assertVisibleInside(compose.onNodeWithContentDescription("삭제"))
            .assertSingleLine("결과 확인")
        compose.onNodeWithText("결과 확인").performClick()
        assertEquals(EditorAction.CheckResult, action)
    }
    @Test fun landscapeEditorPlacesActionsBesideWidePhoto() {
        val state = editorState(photo())
        val robot = LayoutRobot().show(640, 320, 2f, dark = true) { EditorScreen(state, {}) }
        robot.assertPhotoHeight(editorDescription, 180f).assertVisibleInside(compose.onNodeWithText("결과 확인"))
        val picture = compose.onNodeWithContentDescription(editorDescription).fetchSemanticsNode().boundsInRoot
        val button = compose.onNodeWithText("결과 확인").fetchSemanticsNode().boundsInRoot
        assertTrue(picture.right < button.left)
    }
    @Test fun smallResultStacksButtonsWithoutBreakingTheirLabels() {
        val state = resultState(photo(400, 800))
        var action: ResultAction? = null
        val robot = LayoutRobot().show(320, 440, 2f) { ResultScreen(state) { action = it } }
        robot.assertPhotoHeight(resultDescription, 100f)
            .assertVisibleInside(compose.onNodeWithText("저장하기"))
            .assertVisibleInside(compose.onNodeWithText("공유하기"))
            .assertVisibleInside(compose.onNodeWithText("새 사진 선택"))
            .assertSingleLine("저장하기").assertSingleLine("공유하기")
        compose.onNodeWithText("공유하기").performClick()
        assertEquals(ResultAction.Share, action)
    }
    @Test fun landscapeResultKeepsPhotoVisibleWhileControlsScroll() {
        val state = resultState(photo())
        val robot = LayoutRobot().show(640, 320, 2f, dark = true) { ResultScreen(state, {}) }
        robot.assertPhotoHeight(resultDescription, 180f)
        compose.onNodeWithText("공유하기").performScrollTo()
        robot.assertVisibleInside(compose.onNodeWithText("공유하기"))
        compose.onNodeWithText("새 사진 선택").performScrollTo()
        robot.assertVisibleInside(compose.onNodeWithText("새 사진 선택"))
        robot.assertPhotoHeight(resultDescription, 180f)
    }
}

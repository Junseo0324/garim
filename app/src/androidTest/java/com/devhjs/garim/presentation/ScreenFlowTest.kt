package com.devhjs.garim.presentation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.presentation.home.*
import com.devhjs.garim.presentation.editor.*
import com.devhjs.garim.presentation.result.*
import com.devhjs.garim.ui.theme.GarimTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ScreenFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeHasOneObviousEntryAndLoadingFeedback() {
        val state = mutableStateOf(HomeState())
        var selected = false
        compose.setContent { GarimTheme { HomeScreen(state.value) { selected = it == HomeAction.SelectPhoto } } }
        compose.onNodeWithText("사진 선택").performClick()
        assertTrue(selected)
        compose.runOnIdle { state.value = HomeState(isLoading = true) }
        compose.onNodeWithText("사진을 준비하고 있어요.").assertIsDisplayed()
        compose.onNodeWithText("사진 선택").assertDoesNotExist()
    }

    @Test fun editorDisablesResultUntilThereIsAMaskAndShowsErrors() {
        val photo = PhotoDocument("test", "/nonexistent.png", 100, 100)
        val state = mutableStateOf(EditorState(photo = photo))
        compose.setContent { GarimTheme { EditorScreen(state.value, {}) } }
        compose.onNodeWithText("결과 확인").assertIsNotEnabled()
        compose.runOnIdle { state.value = state.value.copy(history = EditorHistory(listOf(MaskRect("one", .1f, .1f, .5f, .5f)))) }
        compose.onNodeWithText("결과 확인").assertIsEnabled()
        compose.runOnIdle { state.value = EditorState(errorMessage = "사진 오류") }
        compose.onNodeWithText("사진 오류").assertIsDisplayed()
        compose.onNodeWithText("사진 다시 선택").assertIsDisplayed()
        compose.runOnIdle { state.value = EditorState(isLoading = true) }
        compose.onNodeWithText("사진을 준비하고 있어요.").assertIsDisplayed()
    }

    @Test fun resultShowsSavedActionsAndDoesNotOfferCompletionScreen() {
        val photo = ExportedPhoto("test", "/nonexistent.png", "content://test", 100, 100)
        val state = mutableStateOf(ResultState(photo = photo))
        var action: ResultAction? = null
        compose.setContent { GarimTheme { ResultScreen(state.value) { action = it } } }
        compose.onNodeWithText("공유하기").performClick()
        assertEquals(ResultAction.Share, action)
        compose.onNodeWithText("새 사진 선택").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(hasSaved = true) }
        compose.onNodeWithText("새 사진 선택").assertIsDisplayed()
        compose.runOnIdle { state.value = ResultState(errorMessage = "결과 오류") }
        compose.onNodeWithText("결과 오류").assertIsDisplayed()
        compose.runOnIdle { state.value = ResultState(isLoading = true) }
        compose.onNodeWithText("결과를 준비하고 있어요.").assertIsDisplayed()
    }
}

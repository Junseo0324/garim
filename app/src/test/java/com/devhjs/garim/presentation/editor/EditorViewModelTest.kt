package com.devhjs.garim.presentation.editor
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.devhjs.garim.domain.model.MaskRect
import com.devhjs.garim.domain.usecase.*
import com.devhjs.garim.fake.FakePhotoRepository
import com.devhjs.garim.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val mask = MaskRect("one", .1f, .1f, .4f, .4f)
    private fun vm(repo: FakePhotoRepository = FakePhotoRepository(), handle: SavedStateHandle = SavedStateHandle()) =
        EditorViewModel(LoadPhotoUseCase(repo), RenderPhotoUseCase(repo), handle)

    @Test fun loadExposesLoadingAndPhoto() = runTest {
        val vm = vm()
        vm.state.test {
            assertNull(awaitItem().photo)
            vm.onAction(EditorAction.LoadPhoto("photo"))
            assertTrue(awaitItem().isLoading)
            assertEquals("photo", awaitItem().photo?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedLoadKeepsErrorState() = runTest {
        val vm = vm(FakePhotoRepository().apply { fail = true })
        vm.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        assertNotNull(vm.state.value.errorMessage)
    }
    @Test fun commitSelectionUndoRedoAndDelete() = runTest {
        val vm = vm()
        vm.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        assertEquals(listOf(mask), vm.state.value.history.masks)
        vm.onAction(EditorAction.SelectMask(null)); assertNull(vm.state.value.selectedId)
        vm.onAction(EditorAction.Undo); assertTrue(vm.state.value.history.masks.isEmpty())
        vm.onAction(EditorAction.Redo); assertEquals(listOf(mask), vm.state.value.history.masks)
        vm.onAction(EditorAction.SelectMask(mask.id))
        vm.onAction(EditorAction.DeleteMask); assertTrue(vm.state.value.history.masks.isEmpty())
    }
    @Test fun transformOnlyChangesViewport() = runTest {
        val vm = vm()
        vm.onAction(EditorAction.Transform(2f, .05f, .1f))
        assertEquals(2f, vm.state.value.zoom)
        assertEquals(.05f, vm.state.value.panXFraction)
        assertEquals(.1f, vm.state.value.panYFraction)
        assertTrue(vm.state.value.history.masks.isEmpty())
    }
    @Test fun backWithEditsRequiresDiscard() = runTest {
        val vm = vm()
        vm.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        vm.onAction(EditorAction.RequestBack); assertTrue(vm.state.value.showDiscardDialog)
        vm.onAction(EditorAction.KeepEditing); assertFalse(vm.state.value.showDiscardDialog)
        vm.event.test {
            vm.onAction(EditorAction.Discard)
            assertEquals(EditorEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun backWithoutEditsNavigates() = runTest {
        val vm = vm()
        vm.event.test {
            vm.onAction(EditorAction.RequestBack)
            assertEquals(EditorEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun renderNavigatesToGeneratedResult() = runTest {
        val repo = FakePhotoRepository()
        val vm = vm(repo)
        vm.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        vm.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        vm.event.test {
            vm.onAction(EditorAction.CheckResult)
            assertEquals(EditorEvent.NavigateToResult(repo.output.id), awaitItem())
            assertEquals(listOf(mask), repo.renderedMasks)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedRenderPreservesEdits() = runTest {
        val repo = FakePhotoRepository()
        val vm = vm(repo)
        vm.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        vm.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        repo.fail = true
        vm.event.test {
            vm.onAction(EditorAction.CheckResult)
            assertTrue(awaitItem() is EditorEvent.ShowSnackBar)
            assertEquals(listOf(mask), vm.state.value.history.masks)
            assertFalse(vm.state.value.isRendering)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun restoredMasksSurviveNewViewModel() = runTest {
        val handle = SavedStateHandle()
        val original = vm(handle = handle)
        original.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        original.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        val restored = vm(handle = handle)
        restored.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        assertEquals(listOf(mask), restored.state.value.history.masks)
    }
    @Test fun newHandleRestoresUndoRedoSelectionAndViewport() = runTest {
        val handle = SavedStateHandle()
        val original = vm(handle = handle)
        original.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        original.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        val second = mask.copy(id = "two", left = .5f, right = .8f)
        original.onAction(EditorAction.CommitMasks(listOf(mask, second), second.id))
        original.onAction(EditorAction.Undo)
        original.onAction(EditorAction.SelectMask(mask.id))
        original.onAction(EditorAction.Transform(3f, .12f, -.08f))
        val copied = SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })
        val restored = vm(handle = copied)
        restored.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        assertEquals(original.state.value.history, restored.state.value.history)
        assertEquals(mask.id, restored.state.value.selectedId)
        assertEquals(3f, restored.state.value.zoom)
        assertEquals(.12f, restored.state.value.panXFraction)
        assertEquals(-.08f, restored.state.value.panYFraction)
        restored.onAction(EditorAction.Redo)
        assertEquals(listOf(mask, second), restored.state.value.history.masks)
        restored.onAction(EditorAction.Undo)
        restored.onAction(EditorAction.Undo)
        assertTrue(restored.state.value.history.masks.isEmpty())
    }
    @Test fun savedEditsAreNotAppliedToAnotherPhoto() = runTest {
        val handle = SavedStateHandle()
        val original = vm(handle = handle)
        original.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        original.onAction(EditorAction.CommitMasks(listOf(mask), mask.id))
        original.onAction(EditorAction.Transform(3f, .1f, .1f))
        val restored = vm(handle = handle)
        restored.onAction(EditorAction.LoadPhoto("another-photo")); advanceUntilIdle()
        assertTrue(restored.state.value.history.masks.isEmpty())
        assertNull(restored.state.value.selectedId)
        assertEquals(1f, restored.state.value.zoom)
    }
    @Test fun malformedSavedHistoryDoesNotCrashOrEnableExport() = runTest {
        val handle = SavedStateHandle(mapOf("photoId" to "photo", "editorHistory" to "broken",
            "selectedId" to mask.id, "viewport" to floatArrayOf(Float.NaN, 0f, 0f)))
        val restored = vm(handle = handle)
        restored.onAction(EditorAction.LoadPhoto("photo")); advanceUntilIdle()
        assertTrue(restored.state.value.history.masks.isEmpty())
        assertNull(restored.state.value.selectedId)
        assertEquals(1f, restored.state.value.zoom)
        assertFalse(restored.state.value.isLoading)
    }
}

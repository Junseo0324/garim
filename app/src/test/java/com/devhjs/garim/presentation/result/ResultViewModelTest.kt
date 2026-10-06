package com.devhjs.garim.presentation.result
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.devhjs.garim.domain.usecase.*
import com.devhjs.garim.fake.FakePhotoRepository
import com.devhjs.garim.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private fun vm(repo: FakePhotoRepository, handle: SavedStateHandle = SavedStateHandle()) =
        ResultViewModel(LoadResultUseCase(repo), SaveResultUseCase(repo), ImportPhotoUseCase(repo), handle)
    @Test fun loadExposesLoadingAndResult() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.state.test {
            assertNull(awaitItem().photo)
            vm.onAction(ResultAction.LoadResult("output"))
            assertTrue(awaitItem().isLoading)
            assertEquals(repo.output, awaitItem().photo)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedLoadShowsError() = runTest {
        val vm = vm(FakePhotoRepository().apply { fail = true })
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        assertNotNull(vm.state.value.errorMessage)
    }
    @Test fun gallerySaveShowsSuccessOnlyAfterSaving() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        vm.event.test {
            vm.onAction(ResultAction.Save(false))
            assertEquals(ResultEvent.ShowSnackBar("사진첩에 저장했어요."), awaitItem())
            assertTrue(vm.state.value.hasSaved)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedSaveDoesNotReportSuccess() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle(); repo.fail = true
        vm.event.test {
            vm.onAction(ResultAction.Save(false))
            assertTrue(awaitItem() is ResultEvent.ShowSnackBar)
            assertFalse(vm.state.value.hasSaved)
            assertNotNull(vm.state.value.photo)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun legacySaveLaunchesPickerAndWritesChosenDestination() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        vm.event.test {
            vm.onAction(ResultAction.Save(true)); assertEquals(ResultEvent.OpenSavePicker, awaitItem())
            vm.onAction(ResultAction.SaveToDocument("content://destination"))
            assertEquals(ResultEvent.ShowSnackBar("파일을 저장했어요."), awaitItem())
            assertEquals("content://destination", repo.savedDestination)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun shareUsesOnlyExportedUri() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        vm.event.test {
            vm.onAction(ResultAction.Share)
            assertEquals(ResultEvent.OpenShareMenu(repo.output.shareUri), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun backAndNewSelectionEmitActions() = runTest {
        val vm = vm(FakePhotoRepository())
        vm.event.test {
            vm.onAction(ResultAction.RequestBack); assertEquals(ResultEvent.NavigateBack, awaitItem())
            vm.onAction(ResultAction.SelectNewPhoto); assertEquals(ResultEvent.OpenPhotoPicker, awaitItem())
            vm.onAction(ResultAction.ImportNewPhoto("content://new"))
            assertEquals(ResultEvent.NavigateToNewEditor("photo"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedNewImportKeepsCurrentResult() = runTest {
        val repo = FakePhotoRepository(); val vm = vm(repo)
        vm.onAction(ResultAction.LoadResult("output")); advanceUntilIdle(); repo.fail = true
        vm.event.test {
            vm.onAction(ResultAction.ImportNewPhoto("content://new"))
            assertTrue(awaitItem() is ResultEvent.ShowSnackBar)
            assertEquals(repo.output, vm.state.value.photo)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun successfulSaveStateIsRestoredWithResult() = runTest {
        val repo = FakePhotoRepository()
        val handle = SavedStateHandle()
        val original = vm(repo, handle)
        original.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        original.onAction(ResultAction.Save(false)); advanceUntilIdle()
        val restored = vm(repo, SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))
        restored.onAction(ResultAction.LoadResult("output")); advanceUntilIdle()
        assertTrue(restored.state.value.hasSaved)
        assertFalse(restored.state.value.isSaving)
    }
}

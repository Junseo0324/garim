package com.devhjs.garim.presentation.home
import app.cash.turbine.test
import com.devhjs.garim.domain.usecase.ImportPhotoUseCase
import com.devhjs.garim.fake.FakePhotoRepository
import com.devhjs.garim.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @Test fun selectionOpensPicker() = runTest {
        val vm = HomeViewModel(ImportPhotoUseCase(FakePhotoRepository()))
        vm.event.test {
            vm.onAction(HomeAction.SelectPhoto)
            assertEquals(HomeEvent.OpenPhotoPicker, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun importExposesLoadingThenNavigates() = runTest {
        val repo = FakePhotoRepository()
        val vm = HomeViewModel(ImportPhotoUseCase(repo))
        vm.event.test {
            vm.state.test {
                assertFalse(awaitItem().isLoading)
                vm.onAction(HomeAction.ImportPhoto("content://test"))
                assertTrue(awaitItem().isLoading)
                assertFalse(awaitItem().isLoading)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(HomeEvent.NavigateToEditor(repo.photo.id), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test fun failedImportShowsMessageAndAllowsRetry() = runTest {
        val repo = FakePhotoRepository().apply { fail = true }
        val vm = HomeViewModel(ImportPhotoUseCase(repo))
        vm.event.test {
            vm.onAction(HomeAction.ImportPhoto("content://test"))
            assertTrue(awaitItem() is HomeEvent.ShowSnackBar)
            assertFalse(vm.state.value.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

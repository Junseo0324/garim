package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.fake.FakePhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RenderPhotoUseCaseTest {
    private val repository = FakePhotoRepository()
    private val useCase = RenderPhotoUseCase(repository)
    private val masks = listOf(MaskRect("one", .1f, .1f, .4f, .4f))
    @Test fun successUsesProvidedMasks() = runTest {
        assertEquals(Result.Success(repository.output), useCase("photo", masks))
        assertEquals(masks, repository.renderedMasks)
    }
    @Test fun failureIsTyped() = runTest {
        repository.fail = true
        assertEquals(Result.Error(ImageError.CANNOT_RENDER), useCase("photo", masks))
    }
    @Test fun unredactedPhotoCannotBeExported() = runTest {
        assertEquals(Result.Error(ImageError.INVALID_MASK), useCase("photo", emptyList()))
        assertEquals(0, repository.calls)
    }
}

package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.Result
import com.devhjs.garim.fake.FakePhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoadResultUseCaseTest {
    private val repository = FakePhotoRepository()
    private val useCase = LoadResultUseCase(repository)
    @Test fun returnsSuccess() = runTest {
        assertEquals(Result.Success(repository.output), useCase("id"))
    }
    @Test fun convertsFailureToTypedError() = runTest {
        repository.fail = true
        assertEquals(Result.Error(ImageError.CANNOT_RENDER), useCase("id"))
    }
}

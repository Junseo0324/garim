package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.Result
import com.devhjs.garim.fake.FakePhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SaveResultUseCaseTest {
    private val repository = FakePhotoRepository()
    private val useCase = SaveResultUseCase(repository)
    @Test fun returnsSuccess() = runTest {
        assertEquals(Result.Success(Unit), useCase("output", "content://destination"))
    }
    @Test fun convertsFailureToTypedError() = runTest {
        repository.fail = true
        assertEquals(Result.Error(ImageError.CANNOT_SAVE), useCase("output", "content://destination"))
    }
}

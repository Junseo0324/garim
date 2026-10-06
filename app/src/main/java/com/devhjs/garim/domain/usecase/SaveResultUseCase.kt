package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.repository.PhotoRepository
import javax.inject.Inject

class SaveResultUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(id: String, destination: String? = null): Result<Unit, ImageError> =
        imageOperation(ImageError.CANNOT_SAVE) { repository.saveResult(id, destination) }
}

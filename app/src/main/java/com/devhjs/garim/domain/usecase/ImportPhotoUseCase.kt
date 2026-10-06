package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.repository.PhotoRepository
import javax.inject.Inject

class ImportPhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(uri: String): Result<PhotoDocument, ImageError> =
        imageOperation(ImageError.CANNOT_OPEN) { repository.importPhoto(uri) }
}

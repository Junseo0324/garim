package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.repository.PhotoRepository
import javax.inject.Inject

class LoadResultUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(id: String): Result<ExportedPhoto, ImageError> =
        imageOperation(ImageError.CANNOT_RENDER) { repository.loadResult(id) }
}

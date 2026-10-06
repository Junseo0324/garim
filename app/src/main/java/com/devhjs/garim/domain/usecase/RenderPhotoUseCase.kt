package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.repository.PhotoRepository
import javax.inject.Inject

class RenderPhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(id: String, masks: List<MaskRect>): Result<ExportedPhoto, ImageError> {
        if (masks.isEmpty()) return Result.Error(ImageError.INVALID_MASK)
        return imageOperation(ImageError.CANNOT_RENDER) { repository.renderPhoto(id, masks) }
    }
}

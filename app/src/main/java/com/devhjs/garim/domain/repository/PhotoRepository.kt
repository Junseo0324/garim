package com.devhjs.garim.domain.repository
import com.devhjs.garim.domain.model.*

interface PhotoRepository {
    suspend fun importPhoto(uri: String): PhotoDocument
    suspend fun loadPhoto(id: String): PhotoDocument
    suspend fun renderPhoto(id: String, masks: List<MaskRect>): ExportedPhoto
    suspend fun loadResult(id: String): ExportedPhoto
    suspend fun saveResult(id: String, destination: String? = null)
}

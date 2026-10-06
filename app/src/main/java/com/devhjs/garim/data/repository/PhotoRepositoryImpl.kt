package com.devhjs.garim.data.repository
import com.devhjs.garim.data.datasource.local.PhotoLocalDataSource
import com.devhjs.garim.data.mapper.toDomain
import com.devhjs.garim.domain.model.MaskRect
import com.devhjs.garim.domain.repository.PhotoRepository
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(private val local: PhotoLocalDataSource) : PhotoRepository {
    override suspend fun importPhoto(uri: String) = local.import(uri).toDomain()
    override suspend fun loadPhoto(id: String) = local.readPhoto(id).toDomain()
    override suspend fun renderPhoto(id: String, masks: List<MaskRect>) = local.render(id, masks).toDomain()
    override suspend fun loadResult(id: String) = local.readOutput(id).toDomain()
    override suspend fun saveResult(id: String, destination: String?) = local.save(id, destination)
}

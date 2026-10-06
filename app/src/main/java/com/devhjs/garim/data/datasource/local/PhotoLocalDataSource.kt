package com.devhjs.garim.data.datasource.local
import com.devhjs.garim.domain.model.MaskRect

data class PhotoRecord(val id: String, val path: String, val width: Int, val height: Int, val downsampled: Boolean)
data class OutputRecord(val id: String, val path: String, val uri: String, val width: Int, val height: Int)

interface PhotoLocalDataSource {
    suspend fun import(uri: String): PhotoRecord
    suspend fun readPhoto(id: String): PhotoRecord
    suspend fun render(id: String, masks: List<MaskRect>): OutputRecord
    suspend fun readOutput(id: String): OutputRecord
    suspend fun save(id: String, destination: String?)
}

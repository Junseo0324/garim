package com.devhjs.garim.fake
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.repository.PhotoRepository
import java.io.IOException

class FakePhotoRepository : PhotoRepository {
    val photo = PhotoDocument("photo", "/test/photo.png", 600, 800)
    val output = ExportedPhoto("output", "/test/output.png", "content://garim/output", 600, 800)
    var fail = false
    var calls = 0
    var savedDestination: String? = null
    var renderedMasks: List<MaskRect> = emptyList()
    private fun checkAccess() { calls++; if (fail) throw IOException("Simulated failure") }
    override suspend fun importPhoto(uri: String): PhotoDocument { checkAccess(); return photo }
    override suspend fun loadPhoto(id: String): PhotoDocument { checkAccess(); return photo }
    override suspend fun renderPhoto(id: String, masks: List<MaskRect>): ExportedPhoto {
        checkAccess(); renderedMasks = masks; return output
    }
    override suspend fun loadResult(id: String): ExportedPhoto { checkAccess(); return output }
    override suspend fun saveResult(id: String, destination: String?) { checkAccess(); savedDestination = destination }
}

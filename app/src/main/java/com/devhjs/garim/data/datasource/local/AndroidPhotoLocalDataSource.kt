package com.devhjs.garim.data.datasource.local

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.devhjs.garim.domain.model.MaskRect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

class AndroidPhotoLocalDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PhotoLocalDataSource {
    private val resolver get() = context.contentResolver
    private val sessions get() = File(context.noBackupFilesDir, "sessions").apply { mkdirs() }
    private val outputs get() = File(context.cacheDir, "exports").apply { mkdirs() }

    override suspend fun import(uri: String): PhotoRecord = withContext(Dispatchers.IO) {
        removeExpiredFiles()
        val selected = Uri.parse(uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(selected)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        val divisor = max(
            max(bounds.outWidth, bounds.outHeight) / 4096f,
            sqrt(bounds.outWidth.toDouble() * bounds.outHeight / 12_000_000.0).toFloat()
        ).coerceAtLeast(1f)
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, selected)) { decoder, info, _ ->
                require(!info.isAnimated) { "Animated photos are not supported" }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val d = max(max(info.size.width, info.size.height) / 4096f,
                    sqrt(info.size.width.toDouble() * info.size.height / 12_000_000.0).toFloat()).coerceAtLeast(1f)
                decoder.setTargetSize((info.size.width / d).toInt().coerceAtLeast(1),
                    (info.size.height / d).toInt().coerceAtLeast(1))
            }
        } else {
            require(bounds.outMimeType != "image/gif")
            val options = BitmapFactory.Options().apply {
                var sample = 1
                while (sample < divisor) sample *= 2
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(selected)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw IOException("Cannot decode photo")
            val exif = resolver.openInputStream(selected)?.use { ExifInterface(it) }
                ?: throw IOException("Cannot read photo orientation")
            val matrix = Matrix().apply {
                if (exif.isFlipped) postScale(-1f, 1f)
                postRotate(exif.rotationDegrees.toFloat())
            }
            val oriented = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            if (oriented !== decoded) decoded.recycle()
            oriented
        }
        val id = UUID.randomUUID().toString()
        val directory = File(sessions, id).apply { mkdirs() }
        var success = false
        try {
            ensureActive()
            val photo = File(directory, "photo.png")
            photo.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val record = PhotoRecord(id, photo.absolutePath, bitmap.width, bitmap.height, divisor > 1f)
            File(directory, "info.json").writeText(JSONObject()
                .put("width", record.width).put("height", record.height)
                .put("downsampled", record.downsampled).toString())
            success = true
            record
        } finally {
            bitmap.recycle()
            if (!success) directory.deleteRecursively()
        }
    }

    override suspend fun readPhoto(id: String): PhotoRecord = withContext(Dispatchers.IO) {
        val directory = File(sessions, validId(id))
        val photo = File(directory, "photo.png")
        if (!photo.isFile) throw FileNotFoundException()
        val info = JSONObject(File(directory, "info.json").readText())
        PhotoRecord(id, photo.absolutePath, info.getInt("width"), info.getInt("height"),
            info.getBoolean("downsampled"))
    }

    override suspend fun render(id: String, masks: List<MaskRect>): OutputRecord = withContext(Dispatchers.IO) {
        require(masks.isNotEmpty())
        val photo = readPhoto(id)
        val bitmap = BitmapFactory.decodeFile(photo.path, BitmapFactory.Options().apply {
            inMutable = true
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }) ?: throw IOException("Cannot read working photo")
        val outputId = UUID.randomUUID().toString()
        val output = File(outputs, "$outputId.png")
        var success = false
        try {
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { color = Color.BLACK; isAntiAlias = false }
            masks.forEach { mask ->
                ensureActive()
                // Round outwards, so every pixel intersecting a selected area is covered.
                canvas.drawRect(
                    floor(mask.left * bitmap.width), floor(mask.top * bitmap.height),
                    ceil(mask.right * bitmap.width), ceil(mask.bottom * bitmap.height), paint)
            }
            output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            success = true
            outputRecord(outputId, output, bitmap.width, bitmap.height)
        } finally {
            bitmap.recycle()
            if (!success) output.delete()
        }
    }

    override suspend fun readOutput(id: String): OutputRecord = withContext(Dispatchers.IO) {
        val file = File(outputs, "${validId(id)}.png")
        if (!file.isFile) throw FileNotFoundException()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        outputRecord(id, file, bounds.outWidth, bounds.outHeight)
    }

    override suspend fun save(id: String, destination: String?): Unit = withContext(Dispatchers.IO) {
        val record = readOutput(id)
        if (destination != null) {
            resolver.openOutputStream(Uri.parse(destination), "wt")?.use { target ->
                File(record.path).inputStream().use { it.copyTo(target) }
            } ?: throw IOException("Cannot open destination")
        } else {
            check(Build.VERSION.SDK_INT >= 29)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Garim_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Garim")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Cannot create gallery image")
            var success = false
            try {
                resolver.openOutputStream(uri)?.use { target ->
                    File(record.path).inputStream().use { it.copyTo(target) }
                } ?: throw IOException("Cannot write gallery image")
                ensureActive()
                check(resolver.update(uri, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null) > 0)
                success = true
            } finally {
                if (!success) resolver.delete(uri, null, null)
            }
        }
    }

    private fun outputRecord(id: String, file: File, width: Int, height: Int) =
        OutputRecord(id, file.absolutePath,
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString(),
            width, height)

    private fun validId(id: String): String {
        require(id.matches(Regex("[0-9a-fA-F-]{36}")))
        return id
    }

    private fun removeExpiredFiles() {
        val now = System.currentTimeMillis()
        sessions.listFiles()?.filter { now - it.lastModified() > 7 * 86_400_000L }
            ?.forEach { it.deleteRecursively() }
        outputs.listFiles()?.filter { now - it.lastModified() > 86_400_000L }?.forEach { it.delete() }
    }
}

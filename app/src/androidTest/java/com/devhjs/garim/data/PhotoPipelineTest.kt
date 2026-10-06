package com.devhjs.garim.data
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devhjs.garim.data.datasource.local.AndroidPhotoLocalDataSource
import com.devhjs.garim.domain.model.MaskRect
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PhotoPipelineTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun orientedInputIsFlattenedAndMetadataIsRemoved() = runTest {
        val input = File(context.cacheDir, "private-test-input.jpg")
        Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            input.outputStream().use { compress(Bitmap.CompressFormat.JPEG, 100, it) }
            recycle()
        }
        ExifInterface(input).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            setLatLong(37.5, 127.0)
            saveAttributes()
        }
        val store = AndroidPhotoLocalDataSource(context)
        val photo = store.import(Uri.fromFile(input).toString())
        assertEquals(80, photo.width)
        assertEquals(120, photo.height)
        val output = store.render(photo.id, listOf(MaskRect("mask", .1f, .2f, .6f, .7f)))
        val pixels = BitmapFactory.decodeFile(output.path)
        assertEquals(Color.BLACK, pixels.getPixel(16, 48))
        assertEquals(Color.WHITE, pixels.getPixel(0, 0))
        pixels.recycle()
        val metadata = ExifInterface(output.path)
        assertNull(metadata.getAttribute(ExifInterface.TAG_GPS_LATITUDE))
        assertNull(metadata.getAttribute(ExifInterface.TAG_GPS_LONGITUDE))
        assertFalse(metadata.hasThumbnail())
        context.contentResolver.openInputStream(Uri.parse(output.uri))!!.use {
            assertArrayEquals(File(output.path).readBytes(), it.readBytes())
        }
        assertThrows(IllegalArgumentException::class.java) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(photo.path))
        }
        input.delete()
        File(photo.path).parentFile!!.deleteRecursively()
        File(output.path).delete()
    }

    @Test fun savedGalleryFileEqualsPreviewFile() = runTest {
        val input = File(context.cacheDir, "save-test.png")
        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            input.outputStream().use { compress(Bitmap.CompressFormat.PNG, 100, it) }
            recycle()
        }
        val store = AndroidPhotoLocalDataSource(context)
        val photo = store.import(Uri.fromFile(input).toString())
        val output = store.render(photo.id, listOf(MaskRect("mask", .2f, .2f, .8f, .8f)))
        store.save(output.id, null)
        val resolver = context.contentResolver
        val cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID), "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?",
            arrayOf("Garim_%"), "${MediaStore.Images.Media._ID} DESC")!!
        val saved = cursor.use {
            assertTrue(it.moveToFirst())
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.getLong(0))
        }
        resolver.openInputStream(saved)!!.use {
            assertArrayEquals(File(output.path).readBytes(), it.readBytes())
        }
        resolver.delete(saved, null, null)
        input.delete()
        File(photo.path).parentFile!!.deleteRecursively()
        File(output.path).delete()
    }
}

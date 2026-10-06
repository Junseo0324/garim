package com.devhjs.garim.core.ui
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun rememberPhotoBitmap(path: String): State<ImageBitmap?> = produceState<ImageBitmap?>(null, path) {
    value = withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
    }
}

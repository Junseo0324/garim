package com.devhjs.garim.domain.usecase
import com.devhjs.garim.domain.error.ImageError
import com.devhjs.garim.domain.model.Result
import kotlinx.coroutines.CancellationException

internal suspend fun <T> imageOperation(error: ImageError, block: suspend () -> T): Result<T, ImageError> =
    try {
        Result.Success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: SecurityException) {
        Result.Error(ImageError.ACCESS_LOST)
    } catch (_: java.io.FileNotFoundException) {
        Result.Error(ImageError.NOT_FOUND)
    } catch (_: Exception) {
        Result.Error(error)
    }

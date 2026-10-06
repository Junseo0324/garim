package com.devhjs.garim.domain.model
data class PhotoDocument(
    val id: String,
    val previewPath: String,
    val width: Int,
    val height: Int,
    val wasDownsampled: Boolean = false,
)
data class ExportedPhoto(
    val id: String,
    val previewPath: String,
    val shareUri: String,
    val width: Int,
    val height: Int,
)

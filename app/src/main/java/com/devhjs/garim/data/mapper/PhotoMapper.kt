package com.devhjs.garim.data.mapper
import com.devhjs.garim.data.datasource.local.*
import com.devhjs.garim.domain.model.*

fun PhotoRecord.toDomain() = PhotoDocument(id, path, width, height, downsampled)
fun OutputRecord.toDomain() = ExportedPhoto(id, path, uri, width, height)

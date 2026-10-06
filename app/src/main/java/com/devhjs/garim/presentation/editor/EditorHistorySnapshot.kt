package com.devhjs.garim.presentation.editor

import com.devhjs.garim.domain.model.EditorHistory
import com.devhjs.garim.domain.model.MaskRect
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Deduplicates rectangles shared by history entries to keep saved instance state small. */
internal object EditorHistorySnapshot {
    @Serializable private data class Rect(val id: String, val left: Float, val top: Float, val right: Float, val bottom: Float)
    @Serializable private data class Snapshot(
        val rectangles: List<Rect>, val current: List<Int>,
        val undo: List<List<Int>>, val redo: List<List<Int>>,
    )
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(history: EditorHistory): String {
        val rectangles = (listOf(history.masks) + history.undo + history.redo).flatten().distinct()
        val indices = rectangles.withIndex().associate { it.value to it.index }
        fun index(masks: List<MaskRect>) = masks.map { indices.getValue(it) }
        return json.encodeToString(Snapshot(
            rectangles.map { Rect(it.id, it.left, it.top, it.right, it.bottom) },
            index(history.masks), history.undo.map(::index), history.redo.map(::index),
        ))
    }

    fun decode(value: String?): EditorHistory? = value?.let {
        runCatching {
            val snapshot = json.decodeFromString<Snapshot>(it)
            val rectangles = snapshot.rectangles.map { r -> MaskRect(r.id, r.left, r.top, r.right, r.bottom) }
            fun masks(indices: List<Int>): List<MaskRect> = indices.map(rectangles::get).also { list ->
                require(list.map { r -> r.id }.distinct().size == list.size)
            }
            EditorHistory(masks(snapshot.current), snapshot.undo.takeLast(50).map(::masks), snapshot.redo.takeLast(50).map(::masks))
        }.getOrNull()
    }
}

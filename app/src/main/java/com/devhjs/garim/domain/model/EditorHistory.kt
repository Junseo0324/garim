package com.devhjs.garim.domain.model
data class EditorHistory(
    val masks: List<MaskRect> = emptyList(),
    val undo: List<List<MaskRect>> = emptyList(),
    val redo: List<List<MaskRect>> = emptyList(),
) {
    fun commit(next: List<MaskRect>): EditorHistory =
        if (next == masks) this else EditorHistory(next.toList(), (undo + listOf(masks)).takeLast(50), emptyList())
    fun undo(): EditorHistory = if (undo.isEmpty()) this
        else EditorHistory(undo.last(), undo.dropLast(1), redo + listOf(masks))
    fun redo(): EditorHistory = if (redo.isEmpty()) this
        else EditorHistory(redo.last(), undo + listOf(masks), redo.dropLast(1))
}

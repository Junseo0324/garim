package com.devhjs.garim.presentation.editor
import com.devhjs.garim.domain.model.MaskRect
sealed interface EditorAction {
    data class LoadPhoto(val id: String) : EditorAction
    data class CommitMasks(val masks: List<MaskRect>, val selectedId: String?) : EditorAction
    data class SelectMask(val id: String?) : EditorAction
    data class Transform(val zoom: Float, val panXFraction: Float, val panYFraction: Float) : EditorAction
    data object Undo : EditorAction
    data object Redo : EditorAction
    data object DeleteMask : EditorAction
    data object CheckResult : EditorAction
    data object RequestBack : EditorAction
    data object KeepEditing : EditorAction
    data object Discard : EditorAction
}

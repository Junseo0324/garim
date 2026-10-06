package com.devhjs.garim.presentation.editor
sealed interface EditorEvent {
    data class NavigateToResult(val outputId: String) : EditorEvent
    data class ShowSnackBar(val message: String) : EditorEvent
    data object NavigateBack : EditorEvent
}

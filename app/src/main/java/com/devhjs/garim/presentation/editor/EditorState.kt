package com.devhjs.garim.presentation.editor
import com.devhjs.garim.domain.model.*

data class EditorState(
    val photo: PhotoDocument? = null,
    val history: EditorHistory = EditorHistory(),
    val selectedId: String? = null,
    val isLoading: Boolean = false,
    val isRendering: Boolean = false,
    val errorMessage: String? = null,
    val showDiscardDialog: Boolean = false,
    val zoom: Float = 1f,
    val panXFraction: Float = 0f,
    val panYFraction: Float = 0f,
)

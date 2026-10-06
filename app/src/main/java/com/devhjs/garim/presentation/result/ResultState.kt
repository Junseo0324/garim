package com.devhjs.garim.presentation.result
import com.devhjs.garim.domain.model.ExportedPhoto
data class ResultState(
    val photo: ExportedPhoto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaved: Boolean = false,
    val errorMessage: String? = null,
)

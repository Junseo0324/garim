package com.devhjs.garim.presentation.result
sealed interface ResultEvent {
    data object NavigateBack : ResultEvent
    data object OpenSavePicker : ResultEvent
    data object OpenPhotoPicker : ResultEvent
    data class NavigateToNewEditor(val id: String) : ResultEvent
    data class OpenShareMenu(val uri: String) : ResultEvent
    data class ShowSnackBar(val message: String) : ResultEvent
}

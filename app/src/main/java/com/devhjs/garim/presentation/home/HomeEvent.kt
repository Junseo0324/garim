package com.devhjs.garim.presentation.home
sealed interface HomeEvent {
    data object OpenPhotoPicker : HomeEvent
    data class NavigateToEditor(val photoId: String) : HomeEvent
    data class ShowSnackBar(val message: String) : HomeEvent
}

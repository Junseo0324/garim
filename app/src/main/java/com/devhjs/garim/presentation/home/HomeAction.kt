package com.devhjs.garim.presentation.home
sealed interface HomeAction {
    data object SelectPhoto : HomeAction
    data class ImportPhoto(val uri: String) : HomeAction
}

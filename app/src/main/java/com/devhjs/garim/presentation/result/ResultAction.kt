package com.devhjs.garim.presentation.result
sealed interface ResultAction {
    data class LoadResult(val id: String) : ResultAction
    data class Save(val useFilePicker: Boolean) : ResultAction
    data class SaveToDocument(val uri: String) : ResultAction
    data object Share : ResultAction
    data object RequestBack : ResultAction
    data object SelectNewPhoto : ResultAction
    data class ImportNewPhoto(val uri: String) : ResultAction
}

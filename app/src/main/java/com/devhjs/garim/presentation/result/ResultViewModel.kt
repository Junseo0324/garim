package com.devhjs.garim.presentation.result
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.devhjs.garim.presentation.component.toMessage
import com.devhjs.garim.domain.model.Result
import com.devhjs.garim.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val loadResult: LoadResultUseCase,
    private val saveResult: SaveResultUseCase,
    private val importPhoto: ImportPhotoUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(ResultState())
    val state = _state.asStateFlow()
    private val _event = MutableSharedFlow<ResultEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: ResultAction) {
        if (_state.value.isLoading || _state.value.isSaving) return
        when (action) {
            is ResultAction.LoadResult -> load(action.id)
            is ResultAction.Save -> if (_state.value.photo != null) {
                if (action.useFilePicker) emit(ResultEvent.OpenSavePicker) else save(null)
            }
            is ResultAction.SaveToDocument -> save(action.uri)
            ResultAction.Share -> _state.value.photo?.let { emit(ResultEvent.OpenShareMenu(it.shareUri)) }
            ResultAction.RequestBack -> emit(ResultEvent.NavigateBack)
            ResultAction.SelectNewPhoto -> emit(ResultEvent.OpenPhotoPicker)
            is ResultAction.ImportNewPhoto -> importNew(action.uri)
        }
    }

    private fun load(id: String) {
        if (_state.value.photo?.id == id) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = loadResult(id)) {
                is Result.Success -> _state.update { it.copy(photo = result.data, isLoading = false,
                    hasSaved = savedStateHandle.get<String>("savedOutputId") == result.data.id) }
                is Result.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.error.toMessage()) }
            }
        }
    }

    private fun save(destination: String?) {
        val id = _state.value.photo?.id ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = saveResult(id, destination)) {
                is Result.Success -> {
                    savedStateHandle["savedOutputId"] = id
                    _state.update { it.copy(isSaving = false, hasSaved = true) }
                    _event.emit(ResultEvent.ShowSnackBar(if (destination == null) "사진첩에 저장했어요." else "파일을 저장했어요."))
                }
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _event.emit(ResultEvent.ShowSnackBar(result.error.toMessage()))
                }
            }
        }
    }

    private fun importNew(uri: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = importPhoto(uri)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(ResultEvent.NavigateToNewEditor(result.data.id))
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(ResultEvent.ShowSnackBar(result.error.toMessage()))
                }
            }
        }
    }
    private fun emit(event: ResultEvent) { viewModelScope.launch { _event.emit(event) } }
}

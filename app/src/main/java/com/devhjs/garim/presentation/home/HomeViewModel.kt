package com.devhjs.garim.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.garim.presentation.component.toMessage
import com.devhjs.garim.domain.model.Result
import com.devhjs.garim.domain.usecase.ImportPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val importPhoto: ImportPhotoUseCase) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: HomeAction) {
        if (_state.value.isLoading) return
        when (action) {
            HomeAction.SelectPhoto -> viewModelScope.launch { _event.emit(HomeEvent.OpenPhotoPicker) }
            is HomeAction.ImportPhoto -> {
                _state.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    when (val result = importPhoto(action.uri)) {
                        is Result.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            _event.emit(HomeEvent.NavigateToEditor(result.data.id))
                        }
                        is Result.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _event.emit(HomeEvent.ShowSnackBar(result.error.toMessage()))
                        }
                    }
                }
            }
        }
    }
}

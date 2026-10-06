package com.devhjs.garim.presentation.editor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devhjs.garim.presentation.component.toMessage
import com.devhjs.garim.domain.model.*
import com.devhjs.garim.domain.usecase.LoadPhotoUseCase
import com.devhjs.garim.domain.usecase.RenderPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val loadPhoto: LoadPhotoUseCase,
    private val renderPhoto: RenderPhotoUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state = _state.asStateFlow()
    private val _event = MutableSharedFlow<EditorEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: EditorAction) {
        if (_state.value.isRendering) return
        when (action) {
            is EditorAction.LoadPhoto -> load(action.id)
            is EditorAction.CommitMasks -> {
                _state.update { it.copy(history = it.history.commit(action.masks), selectedId = action.selectedId) }
                persistMasks()
            }
            is EditorAction.SelectMask -> {
                _state.update { it.copy(selectedId = action.id) }
                savedStateHandle["selectedId"] = action.id
            }
            is EditorAction.Transform -> {
                if (listOf(action.zoom, action.panXFraction, action.panYFraction).all { it.isFinite() }) {
                    _state.update { it.copy(zoom = action.zoom.coerceIn(1f, 6f),
                        panXFraction = action.panXFraction.coerceIn(-.5f, .5f), panYFraction = action.panYFraction.coerceIn(-.5f, .5f)) }
                    savedStateHandle["photoId"] = _state.value.photo?.id
                    savedStateHandle["viewport"] = floatArrayOf(_state.value.zoom, _state.value.panXFraction, _state.value.panYFraction)
                }
            }
            EditorAction.Undo -> { _state.update { it.copy(history = it.history.undo(), selectedId = null) }; persistMasks() }
            EditorAction.Redo -> { _state.update { it.copy(history = it.history.redo(), selectedId = null) }; persistMasks() }
            EditorAction.DeleteMask -> {
                _state.update { it.copy(history = it.history.commit(it.history.masks.filter { mask -> mask.id != it.selectedId }), selectedId = null) }
                persistMasks()
            }
            EditorAction.CheckResult -> render()
            EditorAction.RequestBack -> {
                if (_state.value.history.masks.isNotEmpty()) _state.update { it.copy(showDiscardDialog = true) }
                else emitBack()
            }
            EditorAction.KeepEditing -> _state.update { it.copy(showDiscardDialog = false) }
            EditorAction.Discard -> {
                listOf("photoId", "editorHistory", "selectedId", "viewport", "masks", "maskIds").forEach { savedStateHandle.remove<Any>(it) }
                _state.update { it.copy(showDiscardDialog = false) }; emitBack()
            }
        }
    }

    private fun load(id: String) {
        if (_state.value.photo?.id == id || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = loadPhoto(id)) {
                is Result.Success -> {
                    val coordinates = savedStateHandle.get<FloatArray>("masks") ?: floatArrayOf()
                    val ids = savedStateHandle.get<ArrayList<String>>("maskIds") ?: arrayListOf()
                    val samePhoto = savedStateHandle.get<String>("photoId") == id
                    val legacyMasks = if (samePhoto && coordinates.size == ids.size * 4) runCatching {
                        ids.mapIndexed { i, key -> MaskRect(key, coordinates[i * 4], coordinates[i * 4 + 1], coordinates[i * 4 + 2], coordinates[i * 4 + 3]) }
                    }.getOrDefault(emptyList()) else emptyList()
                    val history = if (samePhoto) EditorHistorySnapshot.decode(savedStateHandle["editorHistory"])
                        ?: EditorHistory(legacyMasks) else EditorHistory()
                    val selected = savedStateHandle.get<String>("selectedId")?.takeIf { key -> history.masks.any { it.id == key } }
                    val viewport = savedStateHandle.get<FloatArray>("viewport")?.takeIf {
                        samePhoto && it.size == 3 && it.all(Float::isFinite)
                    }
                    _state.update { it.copy(photo = result.data, history = history, selectedId = selected,
                        zoom = viewport?.get(0)?.coerceIn(1f, 6f) ?: 1f,
                        panXFraction = viewport?.get(1)?.coerceIn(-.5f, .5f) ?: 0f,
                        panYFraction = viewport?.get(2)?.coerceIn(-.5f, .5f) ?: 0f, isLoading = false) }
                }
                is Result.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.error.toMessage()) }
            }
        }
    }

    private fun render() {
        val current = _state.value
        val photo = current.photo ?: return
        if (current.history.masks.isEmpty() || current.isLoading) return
        _state.update { it.copy(isRendering = true) }
        viewModelScope.launch {
            when (val result = renderPhoto(photo.id, current.history.masks)) {
                is Result.Success -> {
                    _state.update { it.copy(isRendering = false) }
                    _event.emit(EditorEvent.NavigateToResult(result.data.id))
                }
                is Result.Error -> {
                    _state.update { it.copy(isRendering = false) }
                    _event.emit(EditorEvent.ShowSnackBar(result.error.toMessage()))
                }
            }
        }
    }

    private fun persistMasks() {
        savedStateHandle["photoId"] = _state.value.photo?.id
        savedStateHandle["editorHistory"] = EditorHistorySnapshot.encode(_state.value.history)
        savedStateHandle["selectedId"] = _state.value.selectedId
        savedStateHandle.remove<FloatArray>("masks")
        savedStateHandle.remove<ArrayList<String>>("maskIds")
    }
    private fun emitBack() { viewModelScope.launch { _event.emit(EditorEvent.NavigateBack) } }
}

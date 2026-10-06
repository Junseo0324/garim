package com.devhjs.garim.presentation.editor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.garim.core.ui.DiscardDialog
import com.devhjs.garim.core.ui.GarimSnackbar

@Composable
fun EditorScreenRoot(photoId: String, onNavigateToResult: (String) -> Unit,
    onNavigateBack: () -> Unit, viewModel: EditorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is EditorEvent.NavigateToResult -> onNavigateToResult(event.outputId)
                is EditorEvent.ShowSnackBar -> snack.showSnackbar(event.message)
                EditorEvent.NavigateBack -> onNavigateBack()
            }
        }
    }
    LaunchedEffect(photoId) { viewModel.onAction(EditorAction.LoadPhoto(photoId)) }
    BackHandler { viewModel.onAction(EditorAction.RequestBack) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) { GarimSnackbar(it) } }) { padding ->
        Box(Modifier.padding(padding)) { EditorScreen(state, viewModel::onAction) }
    }
    if (state.showDiscardDialog) DiscardDialog(
        onKeepEditing = { viewModel.onAction(EditorAction.KeepEditing) },
        onDiscard = { viewModel.onAction(EditorAction.Discard) },
    )
}

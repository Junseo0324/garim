package com.devhjs.garim.presentation.home
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.garim.core.ads.HomeBanner
import com.devhjs.garim.core.ui.GarimSnackbar

@Composable
fun HomeScreenRoot(onNavigateToEditor: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onAction(HomeAction.ImportPhoto(it.toString())) }
    }
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                HomeEvent.OpenPhotoPicker -> picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                is HomeEvent.NavigateToEditor -> onNavigateToEditor(event.photoId)
                is HomeEvent.ShowSnackBar -> snack.showSnackbar(event.message)
            }
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) { GarimSnackbar(it) } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f)) { HomeScreen(state, viewModel::onAction) }
            HomeBanner()
        }
    }
}

package com.devhjs.garim.presentation.result
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.garim.core.ui.GarimSnackbar

@Composable
fun ResultScreenRoot(outputId: String, onNavigateBack: () -> Unit,
    onNavigateToNewEditor: (String) -> Unit, viewModel: ResultViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val context = LocalContext.current
    val savePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        uri?.let { viewModel.onAction(ResultAction.SaveToDocument(it.toString())) }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onAction(ResultAction.ImportNewPhoto(it.toString())) }
    }
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                ResultEvent.NavigateBack -> onNavigateBack()
                ResultEvent.OpenSavePicker -> savePicker.launch("Garim_${System.currentTimeMillis()}.png")
                ResultEvent.OpenPhotoPicker -> photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                is ResultEvent.NavigateToNewEditor -> onNavigateToNewEditor(event.id)
                is ResultEvent.ShowSnackBar -> snack.showSnackbar(event.message)
                is ResultEvent.OpenShareMenu -> {
                    val uri = Uri.parse(event.uri)
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        clipData = ClipData.newRawUri("가린 사진", uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(share, "가린 사진 공유"))
                }
            }
        }
    }
    LaunchedEffect(outputId) { viewModel.onAction(ResultAction.LoadResult(outputId)) }
    BackHandler { viewModel.onAction(ResultAction.RequestBack) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) { GarimSnackbar(it) } }) { padding ->
        Box(Modifier.padding(padding)) { ResultScreen(state, viewModel::onAction) }
    }
}

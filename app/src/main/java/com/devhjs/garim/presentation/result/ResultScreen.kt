package com.devhjs.garim.presentation.result

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devhjs.garim.core.ui.*
import com.devhjs.garim.ui.theme.GarimTheme

@Composable
fun ResultScreen(state: ResultState, onAction: (ResultAction) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("결과 확인", "3 / 3", { onAction(ResultAction.RequestBack) }, !state.isSaving)
        when {
            state.isLoading -> LoadingContent("결과를 준비하고 있어요.", Modifier.weight(1f))
            state.errorMessage != null -> ErrorContent("결과를 불러올 수 없어요.", state.errorMessage,
                "편집으로 돌아가기", { onAction(ResultAction.RequestBack) }, Modifier.weight(1f))
            state.photo != null -> BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val fontScale = LocalDensity.current.fontScale
                val availableWidth = maxWidth
                val compact = maxHeight < 600.dp || fontScale > 1.3f
                if (maxWidth >= 480.dp && maxWidth > maxHeight) {
                    Row(Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultPhoto(state.photo, Modifier.weight(1f).fillMaxHeight().border(1.dp, MaterialTheme.colorScheme.outlineVariant))
                        Column(Modifier.width((availableWidth * .42f).coerceIn(184.dp, 280.dp))
                            .fillMaxHeight().verticalScroll(rememberScrollState())) {
                            ResultInstruction(compact = true)
                            ResultActions(state, onAction, stacked = true)
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        ResultPhoto(state.photo, Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant))
                        ResultInstruction(compact)
                        Box(Modifier.padding(horizontal = 20.dp)) {
                            ResultActions(state, onAction, stacked = availableWidth < 360.dp || fontScale > 1.3f)
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            else -> Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultInstruction(compact: Boolean) {
    Text(if (compact) "가림을 확인하세요." else "가릴 부분이 모두 가려졌는지 확인해 주세요.",
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 18.dp),
        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ResultActions(state: ResultState, onAction: (ResultAction) -> Unit, stacked: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        if (stacked) {
            SaveButton(state, onAction, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            ShareButton(state, onAction, Modifier.fillMaxWidth())
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SaveButton(state, onAction, Modifier.weight(1f))
            ShareButton(state, onAction, Modifier.weight(1f))
        }
        if (state.hasSaved) TextAction("새 사진 선택", { onAction(ResultAction.SelectNewPhoto) },
            modifier = Modifier.align(Alignment.CenterHorizontally), enabled = !state.isSaving)
    }
}

@Composable
private fun SaveButton(state: ResultState, onAction: (ResultAction) -> Unit, modifier: Modifier) {
    SecondaryButton(if (state.isSaving) "저장 중…" else "저장하기", Icons.Rounded.SaveAlt,
        { onAction(ResultAction.Save(Build.VERSION.SDK_INT < 29)) }, modifier,
        enabled = !state.isSaving, isLoading = state.isSaving)
}

@Composable
private fun ShareButton(state: ResultState, onAction: (ResultAction) -> Unit, modifier: Modifier) {
    PrimaryButton("공유하기", Icons.Rounded.Share, { onAction(ResultAction.Share) }, modifier, !state.isSaving)
}

@Preview(showBackground = true)
@Composable private fun ResultPreview() { GarimTheme { ResultScreen(ResultState(isLoading = true), {}) } }

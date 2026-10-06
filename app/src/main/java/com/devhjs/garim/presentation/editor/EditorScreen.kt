package com.devhjs.garim.presentation.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devhjs.garim.core.ui.*
import com.devhjs.garim.ui.theme.GarimTheme

@Composable
fun EditorScreen(state: EditorState, onAction: (EditorAction) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("가리기", "2 / 3", { onAction(EditorAction.RequestBack) }, !state.isRendering)
        when {
            state.isLoading -> LoadingContent("사진을 준비하고 있어요.", Modifier.weight(1f))
            state.errorMessage != null -> ErrorContent("사진을 열 수 없어요.", state.errorMessage,
                "사진 다시 선택", { onAction(EditorAction.Discard) }, Modifier.weight(1f))
            state.photo != null -> BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val availableWidth = maxWidth
                val compact = maxHeight < 600.dp || LocalDensity.current.fontScale > 1.3f
                val landscape = maxWidth >= 480.dp && maxWidth > maxHeight
                if (landscape) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PhotoEditorCanvas(state, onAction, Modifier.weight(1f).fillMaxHeight().border(1.dp, MaterialTheme.colorScheme.outlineVariant), !state.isRendering)
                        Column(Modifier.width((availableWidth * .4f).coerceIn(184.dp, 260.dp)).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            EditorInstruction(compact = true)
                            EditorTools(state, onAction, compact = true, besidePhoto = true)
                            EditorResultButton(state, onAction)
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        EditorInstruction(compact)
                        PhotoEditorCanvas(state, onAction,
                            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant), !state.isRendering)
                        Text(if (compact) "두 손가락으로 확대" else "두 손가락으로 확대 · 박스를 끌어 이동",
                            Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.photo.wasDownsampled && !compact) Text("큰 사진은 공유하기 좋은 크기로 조정했어요.",
                            Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        EditorTools(state, onAction, compact)
                        Box(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                            EditorResultButton(state, onAction)
                        }
                    }
                }
            }
            else -> Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun EditorInstruction(compact: Boolean) {
    Text(if (compact) "드래그해 가리세요." else "가릴 부분을 손가락으로 드래그하세요.",
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (compact) 4.dp else 8.dp),
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun EditorTools(state: EditorState, onAction: (EditorAction) -> Unit, compact: Boolean, besidePhoto: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = if (besidePhoto) 4.dp else 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EditorTool("실행 취소", Icons.AutoMirrored.Rounded.Undo,
            state.history.undo.isNotEmpty() && !state.isRendering, compact, Modifier.weight(1f)) { onAction(EditorAction.Undo) }
        EditorTool("다시 실행", Icons.AutoMirrored.Rounded.Redo,
            state.history.redo.isNotEmpty() && !state.isRendering, compact, Modifier.weight(1f)) { onAction(EditorAction.Redo) }
        EditorTool("삭제", Icons.Rounded.DeleteOutline,
            state.selectedId != null && !state.isRendering, compact, Modifier.weight(1f)) { onAction(EditorAction.DeleteMask) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTool(label: String, icon: ImageVector, enabled: Boolean, compact: Boolean,
    modifier: Modifier, onClick: () -> Unit) {
    // TooltipBox applies its modifier to an inner anchor, so the Row weight belongs on this Box.
    Box(modifier) {
      TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } }, state = rememberTooltipState()) {
        val colors = MaterialTheme.colorScheme
        val ink = colors.onSurface.copy(alpha = if (enabled) 1f else .3f)
        Column(Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small).background(colors.surface)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.small)
            .semantics { contentDescription = label }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = if (compact) 12.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = ink)
            if (!compact) Text(label, style = MaterialTheme.typography.labelSmall, color = ink)
        }
      }
    }
}

@Composable
private fun EditorResultButton(state: EditorState, onAction: (EditorAction) -> Unit) {
    PrimaryButton(if (state.isRendering) "결과 준비 중…" else "결과 확인", Icons.Rounded.Check,
        { onAction(EditorAction.CheckResult) }, enabled = state.history.masks.isNotEmpty() && !state.isRendering, isLoading = state.isRendering)
}

@Preview(showBackground = true)
@Composable private fun EditorPreview() { GarimTheme { EditorScreen(EditorState(isLoading = true), {}) } }

package com.devhjs.garim.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DiscardDialog(onKeepEditing: () -> Unit, onDiscard: () -> Unit) {
    Dialog(onDismissRequest = onKeepEditing) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
                GarimMark(Modifier.size(36.dp))
                Spacer(Modifier.height(20.dp))
                Text("편집한 내용을 버릴까요?", style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp, lineHeight = 28.sp)
                Spacer(Modifier.height(12.dp))
                Text("가리기 작업을 끝내고 사진 선택 화면으로 돌아갑니다.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                PrimaryButton("계속 편집", Icons.AutoMirrored.Rounded.ArrowForward, onKeepEditing)
                Spacer(Modifier.height(8.dp))
                SecondaryButton("버리기", Icons.Rounded.DeleteOutline, onDiscard)
            }
        }
    }
}

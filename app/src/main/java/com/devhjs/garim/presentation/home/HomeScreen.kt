package com.devhjs.garim.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devhjs.garim.core.ui.*
import com.devhjs.garim.ui.theme.GarimTheme

@Composable
fun HomeScreen(state: HomeState, onAction: (HomeAction) -> Unit) {
    if (state.isLoading) {
        LoadingContent("사진을 준비하고 있어요.")
        return
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val largeText = LocalDensity.current.fontScale > 1.3f
        val compact = maxHeight < 580.dp || largeText
        val colors = MaterialTheme.colorScheme
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(if (compact) 14.dp else 24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GarimMark(Modifier.size(32.dp))
                    Text("가림", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (!largeText) Text("사진 개인정보 가리기", style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(if (compact) 24.dp else 32.dp))
                val heading = buildAnnotatedString {
                    append("가릴 건 ")
                    withStyle(SpanStyle(background = colors.tertiary, color = colors.onTertiary)) { append("가리고,") }
                    append("\n사진은 그대로.")
                }
                Text(heading, style = if (largeText) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text("이름, 주소, 전화번호.\n공개하고 싶지 않은 부분만 덮으세요.", style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant)
                if (!largeText) {
                    Spacer(Modifier.height(if (compact) 20.dp else 28.dp))
                    HomeExample(compact)
                    Spacer(Modifier.height(22.dp))
                    HomeSteps()
                }
                Spacer(Modifier.height(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton("사진 선택", Icons.AutoMirrored.Rounded.ArrowForward, { onAction(HomeAction.SelectPhoto) })
            Text("회원가입 없이 · 사진은 기기에서 처리", style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp))
        }
    }
}

@Composable
private fun HomeExample(compact: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column {
        Box(Modifier.fillMaxWidth().height(if (compact) 216.dp else 246.dp)
            .clip(MaterialTheme.shapes.medium).background(colors.surfaceContainerHighest)) {
            val gridColor = colors.outlineVariant
            Canvas(Modifier.fillMaxSize()) {
                val gap = 18.dp.toPx()
                for (x in 0..(size.width / gap).toInt()) for (y in 0..(size.height / gap).toInt()) {
                    drawCircle(gridColor, .8.dp.toPx(), Offset(x * gap, y * gap))
                }
            }
            Column(Modifier.align(Alignment.Center).padding(28.dp).widthIn(max = 240.dp).fillMaxWidth()
                .graphicsLayer { rotationZ = -5f }
                .border(1.dp, Color(0xFFD9DDCE), RoundedCornerShape(3.dp))
                .background(Color(0xFFFEFDF8), RoundedCornerShape(3.dp)).padding(20.dp)) {
                Text("배송 정보", style = MaterialTheme.typography.titleSmall, color = Color(0xFF242B23))
                Spacer(Modifier.height(4.dp))
                Text("송장 사진 예시", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A8370))
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("이름", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A8370))
                    Box(Modifier.width(82.dp).height(15.dp).background(Color.Black))
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("주소", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7A8370))
                    Box(Modifier.weight(1f).height(15.dp).background(Color.Black))
                }
                Spacer(Modifier.height(18.dp))
                Canvas(Modifier.fillMaxWidth().height(24.dp)) {
                    for (i in 0..52) {
                        drawRect(Color(0xFFB7BEAB), Offset(i * size.width / 56f, 0f),
                            Size(if (i % 3 == 0) 3f else 1f, size.height))
                    }
                }
            }
            Text("가림 적용", style = MaterialTheme.typography.labelSmall, color = colors.onTertiary,
                modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)
                    .background(colors.tertiary, RoundedCornerShape(3.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
            Text("원본은 그대로", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp))
        }
    }
}

@Composable
private fun HomeSteps() {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        listOf("선택", "가리기", "확인").forEachIndexed { index, label ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("0${index + 1}", fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable private fun HomePreview() { GarimTheme { HomeScreen(HomeState(), {}) } }

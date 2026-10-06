package com.devhjs.garim.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BrokenImage
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppHeader(title: String, step: String, onBack: (() -> Unit)? = null, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    val largeText = LocalDensity.current.fontScale > 1.3f
    val current = step.substringBefore(" / ").toIntOrNull() ?: 1
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onBack != null) Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.small)
            .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, role = Role.Button, onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "뒤로 가기", Modifier.size(22.dp),
                tint = colors.onSurface.copy(alpha = if (enabled) 1f else .4f))
        }
        Text(title, style = MaterialTheme.typography.titleLarge,
            fontSize = if (largeText) 20.sp else 24.sp,
            lineHeight = if (largeText) 24.sp else 32.sp, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!largeText) Text("0$current / 03", fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Row(Modifier.semantics { contentDescription = step }, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) { index ->
                    Box(Modifier.width(12.dp).height(4.dp).background(
                        if (index < current) colors.secondary else colors.outlineVariant))
                }
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, isLoading: Boolean = false) {
    GarimActionButton(text, icon, onClick, modifier, enabled, isLoading, primary = true)
}

@Composable
fun SecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, isLoading: Boolean = false) {
    GarimActionButton(text, icon, onClick, modifier, enabled, isLoading, primary = false)
}

@Composable
private fun GarimActionButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier,
    enabled: Boolean, isLoading: Boolean, primary: Boolean) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val background = when {
        !enabled -> colors.surfaceContainerHighest
        primary -> colors.primary
        else -> colors.surface
    }
    val foreground = when {
        !enabled -> colors.onSurface.copy(alpha = .4f)
        primary -> colors.onPrimary
        else -> colors.onSurface
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val narrow = maxWidth < 220.dp
        val largeText = LocalDensity.current.fontScale > 1.3f
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(background)
            .then(if (!primary) Modifier.border(1.dp, colors.outlineVariant, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (narrow) 12.dp else 20.dp, vertical = if (largeText) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, style = if (narrow) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                color = foreground, modifier = Modifier.weight(1f), maxLines = 1)
            val badgeColor = if (primary && enabled) {
                if (colors.primary.red < .5f) colors.tertiary else colors.onPrimary
            } else colors.surfaceContainerHighest
            Box(Modifier.size(if (narrow) 26.dp else 30.dp).clip(RoundedCornerShape(4.dp)).background(badgeColor),
                contentAlignment = Alignment.Center) {
                val badgeInk = if (primary && enabled) {
                    if (colors.primary.red < .5f) colors.onTertiary else colors.primary
                } else foreground
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), color = badgeInk, strokeWidth = 2.dp)
                else Icon(icon, null, Modifier.size(18.dp), tint = badgeInk)
            }
        }
    }
}

@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(modifier.heightIn(min = 48.dp).clip(MaterialTheme.shapes.extraSmall)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else .4f))
    }
}

@Composable
fun LoadingContent(message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        GarimMark(Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text(message, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(Modifier.width(112.dp).height(3.dp), color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun ErrorContent(title: String, message: String, actionLabel: String, onAction: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(64.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.BrokenImage, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        SecondaryButton(actionLabel, Icons.AutoMirrored.Rounded.ArrowForward, onAction,
            Modifier.widthIn(max = 320.dp))
    }
}

@Composable
fun GarimSnackbar(data: SnackbarData) {
    Snackbar(data, shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionColor = MaterialTheme.colorScheme.tertiary)
}

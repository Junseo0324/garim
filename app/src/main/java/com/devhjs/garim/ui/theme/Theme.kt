package com.devhjs.garim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF242B23), onPrimary = Color(0xFFFAFBF5),
    primaryContainer = Color(0xFFE5EAD9), onPrimaryContainer = Color(0xFF242B23),
    secondary = Color(0xFF4A6332), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5EAD9), onSecondaryContainer = Color(0xFF34462A),
    tertiary = Color(0xFFD6ED88), onTertiary = Color(0xFF26331B),
    tertiaryContainer = Color(0xFFEDF4D7), onTertiaryContainer = Color(0xFF34462A),
    background = Color(0xFFF5F4EE), surface = Color(0xFFFEFDF8),
    surfaceVariant = Color(0xFFE8EAE1), surfaceContainer = Color(0xFFEEEEE5),
    surfaceContainerLow = Color(0xFFF5F4EE), surfaceContainerHigh = Color(0xFFE8EAE1),
    surfaceContainerHighest = Color(0xFFE1E4D9), surfaceContainerLowest = Color.White,
    onBackground = Color(0xFF242B23), onSurface = Color(0xFF242B23),
    onSurfaceVariant = Color(0xFF656D60), outline = Color(0xFF8C9584), outlineVariant = Color(0xFFD8DDCF),
    error = Color(0xFFB34632), onError = Color.White,
    errorContainer = Color(0xFFFBE8DF), onErrorContainer = Color(0xFF723326),
    inverseSurface = Color(0xFF242B23), inverseOnSurface = Color(0xFFFAFBF5), inversePrimary = Color(0xFFD6ED88),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFD6ED88), onPrimary = Color(0xFF1C2716),
    primaryContainer = Color(0xFF34432B), onPrimaryContainer = Color(0xFFDDEABF),
    secondary = Color(0xFFBDD58F), onSecondary = Color(0xFF1C2716),
    secondaryContainer = Color(0xFF34432B), onSecondaryContainer = Color(0xFFDDEABF),
    tertiary = Color(0xFFD6ED88), onTertiary = Color(0xFF26331B),
    tertiaryContainer = Color(0xFF34432B), onTertiaryContainer = Color(0xFFDDEABF),
    background = Color(0xFF171D18), surface = Color(0xFF242D25),
    surfaceVariant = Color(0xFF303A30), surfaceContainer = Color(0xFF202820),
    surfaceContainerLow = Color(0xFF1D241D), surfaceContainerHigh = Color(0xFF303A30),
    surfaceContainerHighest = Color(0xFF394536), surfaceContainerLowest = Color(0xFF0E140F),
    onBackground = Color(0xFFF0F2E8), onSurface = Color(0xFFF0F2E8),
    onSurfaceVariant = Color(0xFFB7C0AE), outline = Color(0xFF7E8C73), outlineVariant = Color(0xFF3F4B3B),
    error = Color(0xFFFFB29C), onError = Color(0xFF442119),
    errorContainer = Color(0xFF4B2B24), onErrorContainer = Color(0xFFFBD9C9),
    inverseSurface = Color(0xFFE9EEDC), inverseOnSurface = Color(0xFF242B23), inversePrimary = Color(0xFF4A6332),
)
private val GarimShapes = Shapes(
    extraSmall = RoundedCornerShape(3.dp), small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp), large = RoundedCornerShape(14.dp), extraLarge = RoundedCornerShape(18.dp),
)

@Composable
fun GarimTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography, shapes = GarimShapes, content = content)
}

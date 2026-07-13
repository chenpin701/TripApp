package com.tripapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TripBlue = Color(0xFF1A6B8A)
val TripBlueDark = Color(0xFF0B3F55)
val TripBg = Color(0xFFF0F5F8)

private val LightColors = lightColorScheme(
    primary = TripBlue,
    secondary = TripBlueDark,
    background = TripBg,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = TripBlue,
    secondary = TripBlueDark
)

@Composable
fun TripAppTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}

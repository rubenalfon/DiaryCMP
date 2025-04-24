package es.diaryCMP.utilsModule.utils.statistics

import androidx.compose.ui.graphics.Color

fun interpolateColor(
    color1: Color,
    color2: Color,
    fraction: Float
): Color {
    val red = (color1.red * (1 - fraction) + color2.red * fraction).coerceIn(0f, 1f)
    val green = (color1.green * (1 - fraction) + color2.green * fraction).coerceIn(0f, 1f)
    val blue = (color1.blue * (1 - fraction) + color2.blue * fraction).coerceIn(0f, 1f)
    val alpha = (color1.alpha * (1 - fraction) + color2.alpha * fraction).coerceIn(0f, 1f)
    return Color(red, green, blue, alpha)
}
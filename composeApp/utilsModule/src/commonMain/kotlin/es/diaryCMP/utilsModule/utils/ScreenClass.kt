package es.diaryCMP.utilsModule.utils

import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable

enum class ScreenClass {
    Compact, Medium, Large
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun getScreenClass(): ScreenClass {
    val windowClass = calculateWindowSizeClass()
    return if (windowClass.widthSizeClass == WindowWidthSizeClass.Compact || windowClass.heightSizeClass == WindowHeightSizeClass.Compact) {
        ScreenClass.Compact
    } else if (windowClass.widthSizeClass == WindowWidthSizeClass.Medium || windowClass.heightSizeClass == WindowHeightSizeClass.Medium) {
        ScreenClass.Medium
    } else {
        ScreenClass.Large
    }
}
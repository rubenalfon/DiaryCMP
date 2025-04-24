package com.diaryCMP.uimodule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.Roboto_Bold
import diary.composeapp.utilsmodule.generated.resources.Roboto_Light
import diary.composeapp.utilsmodule.generated.resources.Roboto_Medium
import diary.composeapp.utilsmodule.generated.resources.Roboto_Regular
import org.jetbrains.compose.resources.Font

@Composable
fun RobotoFontFamily() = FontFamily(
    Font(Res.font.Roboto_Light, weight = FontWeight.Light),
    Font(Res.font.Roboto_Regular, weight = FontWeight.Normal),
    Font(Res.font.Roboto_Medium, weight = FontWeight.Medium),
    Font(Res.font.Roboto_Bold, weight = FontWeight.Bold)
)

@Composable
fun RobotoTypography() = Typography().run {
    val fontFamily = RobotoFontFamily()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily =  fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}
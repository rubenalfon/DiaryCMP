package com.diaryCMP.uimodule.ui.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import moe.tlaster.precompose.navigation.transition.NavTransition

private val di = slideInHorizontally(initialOffsetX = { it })
private val ai =
    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(easing = LinearEasing))
private val dp = slideOutHorizontally(targetOffsetX = { -it / 3 })
private val ap = slideInHorizontally(initialOffsetX = { -it / 3 })

actual fun getNavTransition(): NavTransition =
    NavTransition(
        // Delante y Atras, Padre e hIjo
        createTransition = di, // D I
        destroyTransition = ai, // A I
        pauseTransition = dp, // D P
        resumeTransition = ap, // A P
        enterTargetContentZIndex = 0f, // Zindex P
        exitTargetContentZIndex = 1f, // Zindex I
    )

actual fun getInvertNavTransition(): NavTransition =
    NavTransition(
        // Delante y Atras, Padre e hIjo
        createTransition = ap, // D I
        destroyTransition = dp, // A I
        pauseTransition = ai, // D P
        resumeTransition = di, // A P
        enterTargetContentZIndex = 1f, // Zindex P
        exitTargetContentZIndex = 0f, // Zindex I
    )
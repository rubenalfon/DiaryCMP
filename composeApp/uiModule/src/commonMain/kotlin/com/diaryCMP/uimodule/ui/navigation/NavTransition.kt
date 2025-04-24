package com.diaryCMP.uimodule.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import moe.tlaster.precompose.navigation.transition.NavTransition

/**
 * Gets the best ux based animation for the current os.
 */
expect fun getNavTransition(): NavTransition

expect fun getInvertNavTransition(): NavTransition

fun getNoNavTransition() = NavTransition(
    // Delante y Atras, Padre e hIjo
    createTransition = fadeIn(animationSpec = tween(durationMillis = 0)), // D I
    destroyTransition = fadeOut(animationSpec = tween(durationMillis = 0)), // A I
    pauseTransition = fadeOut(animationSpec = tween(durationMillis = 0)), // D P
    resumeTransition = fadeIn(animationSpec = tween(durationMillis = 0)), // A P
    enterTargetContentZIndex = 0f, // Zindex P
    exitTargetContentZIndex = 1f, // Zindex I
)
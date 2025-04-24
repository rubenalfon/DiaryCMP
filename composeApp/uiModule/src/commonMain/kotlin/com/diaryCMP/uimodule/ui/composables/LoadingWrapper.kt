package com.diaryCMP.uimodule.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingWrapper(
    isLoading: Boolean,
    content: @Composable (() -> Unit),
    modifier: Modifier = Modifier
) {
    Crossfade(targetState = isLoading, modifier = modifier) { targetState ->
        when (targetState) {
            true -> LoadingComposable()
            false -> content()
        }
    }
}
package com.diaryCMP.uimodule.ui.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import com.diaryCMP.uimodule.ui.theme.buttonTooltipPadding
import com.diaryCMP.uimodule.ui.theme.noButtonTooltipPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericPlainTooltip(
    text: String,
    content: @Composable () -> Unit,
    isOnButton: Boolean = true
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(if (isOnButton) buttonTooltipPadding else noButtonTooltipPadding),
        tooltip = {
            PlainTooltip { Text(text) }
        },
        state = rememberTooltipState()
    ) {
        content.invoke()
    }
}
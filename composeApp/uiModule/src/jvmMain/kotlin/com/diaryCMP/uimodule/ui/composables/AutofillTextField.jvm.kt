package com.diaryCMP.uimodule.ui.composables

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun AutofillTextField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onAutofill: (String) -> Unit,
    visualTransformation: VisualTransformation,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    supportingText: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    autofillTypes: List<AutofillType>,
    modifier: Modifier
) {
    NormalOutlinedTextField(
        value = value,
        isError = isError,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        label = label,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}
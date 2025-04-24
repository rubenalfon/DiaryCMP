package com.diaryCMP.uimodule.ui.composables

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
expect fun AutofillTextField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onAutofill: (String) -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    supportingText: @Composable (() -> Unit),
    trailingIcon: @Composable (() -> Unit),
    autofillTypes: List<AutofillType> = emptyList(),
    modifier: Modifier = Modifier
)

@Composable
internal fun NormalOutlinedTextField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    supportingText: @Composable (() -> Unit),
    trailingIcon: @Composable (() -> Unit),
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        isError = isError,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        label = label,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        supportingText = supportingText,
        modifier = modifier,
        trailingIcon = trailingIcon
    )
}
package com.diaryCMP.uimodule.ui.composables

import android.os.Build
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.text.input.VisualTransformation
import es.diaryCMP.utilsModule.utils.AutoFillHandler
import es.diaryCMP.utilsModule.utils.autoFillRequestHandler
import es.diaryCMP.utilsModule.utils.connectNode
import es.diaryCMP.utilsModule.utils.defaultFocusChangeAutoFill

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
    val autoFillHandler: AutoFillHandler? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        autoFillRequestHandler(
            autofillTypes = autofillTypes,
            onFill = onAutofill
        )
    } else null

    NormalOutlinedTextField(
        value = value,
        isError = isError,
        onValueChange = {
            onValueChange(it)
            if (it.isEmpty()) autoFillHandler?.requestVerifyManual()
        },
        visualTransformation = visualTransformation,
        label = label,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        modifier = if (autoFillHandler == null) modifier
        else modifier
            .connectNode(handler = autoFillHandler)
            .defaultFocusChangeAutoFill(handler = autoFillHandler)
    )
}
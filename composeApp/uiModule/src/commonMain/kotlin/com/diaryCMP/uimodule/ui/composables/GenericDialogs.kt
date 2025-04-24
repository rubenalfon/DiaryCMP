package com.diaryCMP.uimodule.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.accept
import diary.composeapp.utilsmodule.generated.resources.cancel
import diary.composeapp.utilsmodule.generated.resources.error_loading_generic_body
import diary.composeapp.utilsmodule.generated.resources.error_loading_generic_title
import diary.composeapp.utilsmodule.generated.resources.error_save_generic_body
import diary.composeapp.utilsmodule.generated.resources.error_save_generic_title
import diary.composeapp.utilsmodule.generated.resources.login_title_error
import diary.composeapp.utilsmodule.generated.resources.register_title_error
import diary.composeapp.utilsmodule.generated.resources.return_
import org.jetbrains.compose.resources.stringResource


@Composable
fun GenericDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm.invoke()
            }) {
                Text(stringResource(Res.string.accept), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
fun GenericDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    onDismiss: () -> Unit,
    confirmButton: (@Composable () -> Unit),
    dismissButton: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(body)
            }
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun GenericAlertErrorSaving(
    modifier: Modifier = Modifier,
    text: String = stringResource(Res.string.error_save_generic_body),
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.error_save_generic_title)) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(Res.string.return_))
            }
        }
    )
}

@Composable
fun GenericAlertErrorLoading(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AlertDialog(modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.error_loading_generic_title)) },
        text = { Text(stringResource(Res.string.error_loading_generic_body)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(Res.string.return_))
            }
        }
    )
}


@Composable
fun LoginErrorDialog(
    isLogin: Boolean, body: String, onDismiss: () -> Unit
) {
    val title = if (isLogin) stringResource(Res.string.login_title_error)
    else stringResource(Res.string.register_title_error)

    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning, contentDescription = null
            )
        },
        title = { Text(text = title) },
        text = { Text(text = body) },
        onDismissRequest = { onDismiss.invoke() },
        confirmButton = {
            TextButton(onClick = { onDismiss.invoke() }) {
                Text(text = stringResource(Res.string.return_))
            }
        },
    )
}
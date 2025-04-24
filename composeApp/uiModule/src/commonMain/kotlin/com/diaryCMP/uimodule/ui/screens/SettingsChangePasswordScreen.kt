package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.authenticationScreenWidth
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.change_password
import diary.composeapp.utilsmodule.generated.resources.confirm_new_password
import diary.composeapp.utilsmodule.generated.resources.confirm_password_error
import diary.composeapp.utilsmodule.generated.resources.error_bad_password
import diary.composeapp.utilsmodule.generated.resources.error_old_password
import diary.composeapp.utilsmodule.generated.resources.error_save_generic_title
import diary.composeapp.utilsmodule.generated.resources.generic_error_change_password
import diary.composeapp.utilsmodule.generated.resources.new_password
import diary.composeapp.utilsmodule.generated.resources.old_password
import diary.composeapp.utilsmodule.generated.resources.return_
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsChangePasswordScreenViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsChangePasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsChangePasswordScreenViewModel,
    popBackStack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.empty()
    }

    val isLoading by viewModel.isLoading.collectAsState()

    val oldPassword by viewModel.oldPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val oldPasswordError by viewModel.oldPasswordError.collectAsState()
    val newPasswordError by viewModel.newPasswordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val errorUpdatingPassword by viewModel.errorUpdatingPassword.collectAsState()
    val errorMessage = viewModel.errorMessage

    val scrollState = rememberScrollState()

    LoadingWrapper(isLoading = isLoading, content = {
        val animatedColor by animateColorAsState(if (scrollState.value > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        Column(modifier = modifier) {
            SettingsChangePasswordScreenHeader(
                containerColor = if (getScreenClass() == ScreenClass.Compact) {
                    animatedColor
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                popBackStack = popBackStack
            )
            KeyboardAware(content = {
                SettingsChangePasswordScreenContent(
                    modifier = if (getScreenClass() != ScreenClass.Compact) {
                        Modifier
                            .clip(RoundedCornerShape(surfaceCornerRadius))
                    } else {
                        Modifier
                    }
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    scrollState = scrollState,

                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,

                    onOldPasswordChange = { viewModel.updateOldPassword(it) },
                    onNewPasswordChange = { viewModel.updateNewPassword(it) },
                    onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },

                    oldPasswordError = oldPasswordError,
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError,

                    onChangePasswordButtonTapped = {
                        viewModel.changePassword()
                    }
                )
            })
        }
    })

    if (errorUpdatingPassword) {
        ErrorUpdatingDialog(viewModel, errorMessage)
    }
}

@Composable
private fun SettingsChangePasswordScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color,
    popBackStack: () -> Unit
) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.change_password),
        modifier = modifier,
        containerColor = containerColor,
        popBackStack = popBackStack
    )
}

@Composable
private fun SettingsChangePasswordScreenContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    oldPassword: String,
    newPassword: String,
    confirmPassword: String,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    oldPasswordError: Boolean,
    newPasswordError: Boolean,
    confirmPasswordError: Boolean,
    onChangePasswordButtonTapped: () -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(scrollState)
            .padding(horizontal = surfaceToWindowPadding).padding(smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (getScreenClass() != ScreenClass.Compact)
            Spacer(modifier = Modifier.weight(1f))

        var showOldPassword by rememberSaveable { mutableStateOf(false) }
        OutlinedTextField(
            value = oldPassword,
            isError = oldPasswordError,
            onValueChange = onOldPasswordChange,
            visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
            label = { Text(stringResource(Res.string.old_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
            ),
            singleLine = true,
            supportingText = {
                AnimatedVisibility(visible = oldPasswordError) {
                    Text(
                        text = stringResource(Res.string.error_old_password)
                    )
                }
            },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { showOldPassword = showOldPassword.not() },
                    ) {
                        Icon(
                            imageVector = if (showOldPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                }
            },
            modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            }
        )

        Spacer(modifier = Modifier.height(smallPadding))

        var showNewPassword by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = newPassword,
            isError = newPasswordError,
            onValueChange = { onNewPasswordChange(it) },
            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            label = { Text(stringResource(Res.string.new_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
            ),
            singleLine = true,
            supportingText = {
                AnimatedVisibility(visible = newPasswordError) {
                    Text(
                        text = stringResource(Res.string.error_bad_password)
                    )
                }
            },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { showNewPassword = showNewPassword.not() },
                    ) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                }
            },
            modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            }
        )


        Spacer(modifier = Modifier.height(smallPadding))

        var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = confirmPassword,
            isError = confirmPasswordError,
            onValueChange = { onConfirmPasswordChange(it) },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            label = { Text(stringResource(Res.string.confirm_new_password)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
            singleLine = true,
            supportingText = {
                AnimatedVisibility(visible = confirmPasswordError) {
                    Text(
                        text = stringResource(Res.string.confirm_password_error)
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showConfirmPassword = showConfirmPassword.not()
                    },
                ) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            },
            onClick = { onChangePasswordButtonTapped.invoke() }
        ) {
            Text(text = stringResource(Res.string.change_password))
        }

        Spacer(modifier = Modifier.height(smallPadding))
    }
}

@Composable
private fun ErrorUpdatingDialog(
    viewModel: SettingsChangePasswordScreenViewModel,
    errorMessage: String
) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissErrors() },
        title = { Text(stringResource(Res.string.error_save_generic_title)) },
        text = { Text(stringResource(Res.string.generic_error_change_password, errorMessage)) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.dismissErrors()
            }) {
                Text(stringResource(Res.string.return_))
            }
        }
    )
}
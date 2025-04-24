package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import com.diaryCMP.uimodule.ui.composables.AutofillTextField
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.LoginErrorDialog
import com.diaryCMP.uimodule.ui.theme.authenticationScreenWidth
import com.diaryCMP.uimodule.ui.theme.extraExtraSmallPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.spacingTopLoginScreens
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.accept
import diary.composeapp.utilsmodule.generated.resources.confirm_password
import diary.composeapp.utilsmodule.generated.resources.confirm_password_error
import diary.composeapp.utilsmodule.generated.resources.email
import diary.composeapp.utilsmodule.generated.resources.error_bad_email
import diary.composeapp.utilsmodule.generated.resources.error_bad_password
import diary.composeapp.utilsmodule.generated.resources.log_in_to_existing_account
import diary.composeapp.utilsmodule.generated.resources.login
import diary.composeapp.utilsmodule.generated.resources.password
import diary.composeapp.utilsmodule.generated.resources.register
import diary.composeapp.utilsmodule.generated.resources.register_new_account
import diary.composeapp.utilsmodule.generated.resources.register_password_alert_body
import diary.composeapp.utilsmodule.generated.resources.register_password_alert_title
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardDismissOnSwipeIOS
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationScreenViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreenLoginRegisterEmail(
    viewModel: AuthenticationScreenViewModel,
    navigateToRegisterNameScreen: () -> Unit,
    restartNavigation: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.setNavigation(navigateToRegisterNameScreen)
        viewModel.setRestartNavigation(restartNavigation)
    }

    val isLoading by viewModel.isLoading.collectAsState()

    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val isLogin by viewModel.isLogin.collectAsState()
    val passwordAlertHasAppeared by viewModel.passwordAlertHasAppeared.collectAsState()

    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val showError by viewModel.showError.collectAsState()

    val errorMessage = viewModel.errorMessage

    if (showError) LoginErrorDialog(isLogin = isLogin,
        body = errorMessage,
        onDismiss = { viewModel.dismissAlert() })

    LoadingWrapper(isLoading, content = {
        KeyboardAware(content = {
            val keyboardController = LocalSoftwareKeyboardController.current

            AuthenticationScreenContentPane(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                isLogin = isLogin,
                passwordAlertHasAppeared = passwordAlertHasAppeared,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                updateEmail = { viewModel.updateEmail(it) },
                updatePassword = { viewModel.updatePassword(it) },
                updateConfirmPassword = { viewModel.updateConfirmPassword(it) },
                updatePasswordAlertHasAppeared = { viewModel.updatePasswordAlertHasAppeared(true) },
                nextButtonTapped = {
                    viewModel.next()
                },
                toggleSignIn = { viewModel.toggleLogin() },
                modifier = Modifier
                    .keyboardDismissOnSwipeIOS { keyboardController?.hide() }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        )
                    )
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            )
        })
    })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticationScreenContentPane(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    confirmPassword: String,
    isLogin: Boolean,
    passwordAlertHasAppeared: Boolean,
    emailError: Boolean,
    passwordError: Boolean,
    confirmPasswordError: Boolean,
    updateEmail: (String) -> Unit,
    updatePassword: (String) -> Unit,
    updateConfirmPassword: (String) -> Unit,
    updatePasswordAlertHasAppeared: () -> Unit,
    nextButtonTapped: () -> Unit,
    toggleSignIn: () -> Unit
) {
    var showPasswordAlert by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLogin) {
        if (!isLogin && !passwordAlertHasAppeared) {
            showPasswordAlert = true
        }
    }

    if (showPasswordAlert) {
        PasswordAlert(
            onDismiss = {
                showPasswordAlert = false
                updatePasswordAlertHasAppeared()
            }
        )
    }
    var buttonColumnHeight by remember { mutableStateOf(noPadding) }

    Box(modifier) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.verticalScroll(scrollState).fillMaxSize()
                .padding(horizontal = surfaceToWindowPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(spacingTopLoginScreens))
            Text(text = if (isLogin) stringResource(Res.string.login) else stringResource(Res.string.register),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(smallPadding * 2))



            AutofillTextField(
                value = email,
                isError = emailError,
                onValueChange = updateEmail,
                onAutofill = {
                    updateEmail(it)
                },
                label = { Text(stringResource(Res.string.email)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
                singleLine = true,
                supportingText = {
                    AnimatedVisibility(visible = emailError) {
                        Text(
                            text = stringResource(Res.string.error_bad_email)
                        )
                    }
                },
                trailingIcon = {
                    if (emailError) Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                autofillTypes = listOf(AutofillType.Username),
                modifier = if (getScreenClass() == ScreenClass.Compact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.width(authenticationScreenWidth)
                }
                    .onPreviewKeyEvent {
                        it.key == Key.Enter || it.key == Key.NumPadEnter && it.type == KeyEventType.KeyDown // Can't tab with enter
                    }
            )

            Spacer(modifier = Modifier.height(smallPadding))

            var showPassword by rememberSaveable { mutableStateOf(false) }

            AutofillTextField(
                value = password,
                isError = passwordError,
                onValueChange = { updatePassword(it) },
                onAutofill = {
                    updatePassword(it)
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                label = { Text(stringResource(Res.string.password)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isLogin) ImeAction.Done else ImeAction.Next
                ),
                singleLine = true,
                supportingText = {
                    AnimatedVisibility(visible = passwordError) {
                        Text(
                            text = stringResource(Res.string.error_bad_password)
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = { showPassword = showPassword.not() },
                        ) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                },
                autofillTypes = listOf(AutofillType.Password),
                modifier = if (getScreenClass() == ScreenClass.Compact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.width(authenticationScreenWidth)
                }
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter && it.type == KeyEventType.KeyUp) {
                            if (isLogin) {
                                nextButtonTapped.invoke()
                            }
                            true
                        } else false
                    }
            )

            AnimatedVisibility(
                visible = !isLogin,
                modifier = Modifier.animateContentSize()
            ) {
                Spacer(modifier = Modifier.height(smallPadding))

                var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

                AutofillTextField(
                    value = confirmPassword,
                    isError = confirmPasswordError,
                    onValueChange = { updateConfirmPassword(it) },
                    onAutofill = {
                        updateConfirmPassword(it)
                        updatePassword(it) // Won't do automatically
                    },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    label = { Text(stringResource(Res.string.confirm_password)) },
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
                    autofillTypes = listOf(AutofillType.NewPassword),
                    modifier = if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(authenticationScreenWidth)
                    }
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter || it.key == Key.NumPadEnter && it.type == KeyEventType.KeyUp) {
                                nextButtonTapped.invoke()
                                true
                            } else false
                        }
                )
            }

            Spacer(modifier = Modifier.height(buttonColumnHeight + smallPadding))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            val density = LocalDensity.current.density

            Column(modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            }
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHighest
                        ), end = Offset(0f, 70f)
                    )
                )
                .onGloballyPositioned { coordinates ->
                    buttonColumnHeight = Dp(coordinates.size.height / density)
                }
                .padding(largePadding)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { nextButtonTapped.invoke() }) {
                    Text(text = stringResource(if (isLogin) Res.string.login else Res.string.register))
                }

                Spacer(modifier = Modifier.height(smallPadding))

                ClickableText(
                    text = if (isLogin) AnnotatedString(stringResource(Res.string.register_new_account))
                    else AnnotatedString(stringResource(Res.string.log_in_to_existing_account)),
                    onClick = {
                        toggleSignIn.invoke()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = extraExtraSmallPadding),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.secondary,
                    )
                )
            }
        }
    }
}

@Composable
private fun PasswordAlert(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.register_password_alert_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(Res.string.register_password_alert_body))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(Res.string.accept))
            }
        }
    )
}
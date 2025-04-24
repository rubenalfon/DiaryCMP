package com.diaryCMP.uimodule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.LoginErrorDialog
import com.diaryCMP.uimodule.ui.theme.authenticationScreenWidth
import com.diaryCMP.uimodule.ui.theme.extraLargePadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.spacingTopLoginScreens
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.error_bad_username
import diary.composeapp.utilsmodule.generated.resources.name
import diary.composeapp.utilsmodule.generated.resources.register
import diary.composeapp.utilsmodule.generated.resources.write_name
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardDismissOnSwipeIOS
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationScreenViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreenRegisterName(
    viewModel: AuthenticationScreenViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val isErrorNameTextField by viewModel.isErrorNameTextField.collectAsState()
    val showError by viewModel.showError.collectAsState()

    val errorMessage = viewModel.errorMessage


    if (showError) LoginErrorDialog(isLogin = false,
        body = errorMessage,
        onDismiss = { viewModel.dismissAlert() })

    LoadingWrapper(isLoading = isLoading, content = {
        KeyboardAware(content =  {
            val keyboardController = LocalSoftwareKeyboardController.current

            AuthenticationScreenContentPane(
                userName = userName,
                isErrorNameTextField = isErrorNameTextField,
                updateUserName = { viewModel.updateUserName(it) },
                nextButtonTapped = { viewModel.registerUserName() },
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

@Composable
private fun AuthenticationScreenContentPane(
    modifier: Modifier = Modifier,
    userName: String,
    isErrorNameTextField: Boolean,
    updateUserName: (String) -> Unit,
    nextButtonTapped: () -> Unit
) {
    var buttonColumnHeight by remember { mutableStateOf(noPadding) }

    Box(modifier) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                .padding(horizontal = surfaceToWindowPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacingTopLoginScreens))
            Text(
                text = stringResource(Res.string.write_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(extraLargePadding * 2))

            OutlinedTextField(
                value = userName,
                isError = isErrorNameTextField,
                onValueChange = updateUserName,
                label = { Text(stringResource(Res.string.name)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                singleLine = true,
                supportingText = {
                    if (isErrorNameTextField) Text(
                        text = stringResource(Res.string.error_bad_username)
                    )
                },
                trailingIcon = {
                    if (isErrorNameTextField) Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
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

            Spacer(modifier = Modifier.height(buttonColumnHeight))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            val density = LocalDensity.current.density

            Column(
                modifier = if (getScreenClass() == ScreenClass.Compact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.width(authenticationScreenWidth)
                }
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            ), end = Offset(0f, 70f)
                        )
                    )
                    .padding(largePadding)
                    .onGloballyPositioned { coordinates ->
                        buttonColumnHeight = Dp(coordinates.size.height / density)
                    }
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { nextButtonTapped.invoke() }) {
                    Text(text = stringResource(Res.string.register))
                }
            }
        }
    }
}
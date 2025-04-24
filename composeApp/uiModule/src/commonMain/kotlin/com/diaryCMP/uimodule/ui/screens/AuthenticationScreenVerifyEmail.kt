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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import diary.composeapp.utilsmodule.generated.resources.next
import diary.composeapp.utilsmodule.generated.resources.resend_verification_email
import diary.composeapp.utilsmodule.generated.resources.verify_your_email
import diary.composeapp.utilsmodule.generated.resources.verify_your_email_body
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationScreenViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreenVerifyEmail(
    navigateToRegisterName: () -> Unit,
    viewModel: AuthenticationScreenViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()

    val isEmailVerified by viewModel.isEmailVerified.collectAsState()

    val showError by viewModel.showError.collectAsState()
    val errorMessage = viewModel.errorMessage


    LaunchedEffect(Unit) {
        viewModel.resendVerificationEmail()
        viewModel.startServiceCheckEmailVerification()
    }

    if (showError) LoginErrorDialog(isLogin = false,
        body = errorMessage,
        onDismiss = { viewModel.dismissAlert() })

    LoadingWrapper(isLoading = isLoading, content = {
        AuthenticationScreenContentPane(
            isEmailVerified = isEmailVerified,
            resendVerificationEmail = {
                viewModel.resendVerificationEmail()
            },
            nextButtonTapped = {
                viewModel.stopServiceCheckEmailVerification()
                navigateToRegisterName()
            },
            modifier = Modifier
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
}

@Composable
private fun AuthenticationScreenContentPane(
    modifier: Modifier = Modifier,
    isEmailVerified: Boolean,
    resendVerificationEmail: () -> Unit,
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

            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(extraLargePadding * 2))

            Text(
                text = stringResource(Res.string.verify_your_email),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(extraLargePadding * 2))

            Text(
                text = stringResource(Res.string.verify_your_email_body),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(extraLargePadding * 2))

            var isJustClicked by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(isJustClicked) {
                if (isJustClicked) {
                    delay(15_000)
                    isJustClicked = false
                }
            }
            OutlinedButton(
                onClick = {
                    resendVerificationEmail()

                    isJustClicked = true
                },
                enabled = !isJustClicked
            ) {
                Text(stringResource(Res.string.resend_verification_email))
            }

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
                    onClick = { nextButtonTapped.invoke() },
                    enabled = isEmailVerified
                ) {
                    Text(text = stringResource(Res.string.next))
                }
            }
        }
    }
}
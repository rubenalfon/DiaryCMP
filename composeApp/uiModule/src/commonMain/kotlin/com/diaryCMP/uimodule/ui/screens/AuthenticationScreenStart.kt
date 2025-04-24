package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.diaryCMP.uimodule.ui.theme.authenticationScreenImageSize
import com.diaryCMP.uimodule.ui.theme.authenticationScreenWidth
import com.diaryCMP.uimodule.ui.theme.extraLargePadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.diary_cmp
import diary.composeapp.utilsmodule.generated.resources.ic_notification
import diary.composeapp.utilsmodule.generated.resources.next
import diary.composeapp.utilsmodule.generated.resources.skip
import diary.composeapp.utilsmodule.generated.resources.welcome_description
import diary.composeapp.utilsmodule.generated.resources.welcome_title
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreenStart(
    modifier: Modifier = Modifier,
    navigateToLoginRegisterEmail: () -> Unit,
    navigateToLoginStrongPoints: () -> Unit
) {
    AuthenticationScreenStartContentPane(
        onSkipClicked = navigateToLoginRegisterEmail,
        onNextClicked = navigateToLoginStrongPoints,
        modifier = modifier
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
}

@Composable
private fun AuthenticationScreenStartContentPane(
    modifier: Modifier = Modifier,
    onSkipClicked: () -> Unit = {},
    onNextClicked: () -> Unit
) {
    var buttonColumnHeight by remember { mutableStateOf(noPadding) }

    Box(modifier) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                .padding(surfaceToWindowPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(extraLargePadding)
        ) {
            val animation = remember { Animatable(0f) }
            val animationSpeed = 350
            LaunchedEffect(Unit) {
                animation.animateTo(
                    1f,
                    initialVelocity = 0.01f,
                    animationSpec = tween(easing = LinearEasing)
                )
            }


            AnimatedVisibility(
                visible = animation.value >= 0.2f,
                enter = fadeIn(animationSpec = tween(animationSpeed))
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_notification),
                    contentDescription = stringResource(Res.string.diary_cmp),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = largePadding)
                        .size(authenticationScreenImageSize)
                )
            }

            AnimatedVisibility(
                visible = animation.value >= 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(animationSpeed)
                ) + fadeIn(animationSpec = tween(animationSpeed)) + expandVertically(
                    initialHeight = { it },
                    animationSpec = tween(animationSpeed)
                )
            ) {
                Text(
                    stringResource(Res.string.welcome_title),
                    style = MaterialTheme.typography.displayMedium
                )
            }
            AnimatedVisibility(
                visible = animation.value == 1f,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(animationSpeed)
                ) + fadeIn(animationSpec = tween(animationSpeed)) + expandVertically(
                    initialHeight = { it },
                    animationSpec = tween(animationSpeed)
                )
            ) {
                Text(
                    stringResource(Res.string.welcome_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(authenticationScreenWidth)
                    },
                    textAlign = TextAlign.Center
                )
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
                                Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHighest
                            ), end = Offset(0f, 70f)
                        )
                    )
                    .padding(largePadding)
                    .onGloballyPositioned { coordinates ->
                        buttonColumnHeight = Dp(coordinates.size.height / density)
                    }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(surfaceToWindowPadding)) {
                    OutlinedButton(onClick = onSkipClicked, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.skip))
                    }

                    Button(onClick = onNextClicked, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.next))
                    }
                }
            }
        }
    }
}
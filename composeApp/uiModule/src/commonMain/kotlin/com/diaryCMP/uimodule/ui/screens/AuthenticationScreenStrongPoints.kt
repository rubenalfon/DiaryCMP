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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import com.diaryCMP.uimodule.ui.theme.authenticationScreenImageSize
import com.diaryCMP.uimodule.ui.theme.authenticationScreenWidth
import com.diaryCMP.uimodule.ui.theme.extraLargePadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.mediumPadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.app_strengths_autosave
import diary.composeapp.utilsmodule.generated.resources.app_strengths_multiplatform
import diary.composeapp.utilsmodule.generated.resources.app_strengths_secure
import diary.composeapp.utilsmodule.generated.resources.diary_cmp
import diary.composeapp.utilsmodule.generated.resources.ic_notification
import diary.composeapp.utilsmodule.generated.resources.next
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreenStrongPoints(
    modifier: Modifier = Modifier,
    navigateToLoginRegisterEmail: () -> Unit
) {
    AuthenticationScreenStrongPointsContentPane(
        onNextClicked = navigateToLoginRegisterEmail,
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
private fun AuthenticationScreenStrongPointsContentPane(
    modifier: Modifier = Modifier,
    onNextClicked: () -> Unit
) {
    var buttonColumnHeight by remember { mutableStateOf(noPadding) }

    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = if (getScreenClass() == ScreenClass.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(authenticationScreenWidth)
            }
                .verticalScroll(rememberScrollState())
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

            Spacer(Modifier.height(mediumPadding))

            AnimatedVisibility(
                visible = animation.value != 0f,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(animationSpeed)
                ) + fadeIn(animationSpec = tween(animationSpeed)) + expandVertically(
                    initialHeight = { it },
                    animationSpec = tween(animationSpeed)
                )
            ) {
                Row {
                    Icon(
                        painter = painterResource(Res.drawable.ic_notification),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = largePadding)
                            .size(authenticationScreenImageSize / 4)
                    )

                    Text(
                        stringResource(Res.string.diary_cmp),
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }

            Spacer(Modifier.height(extraLargePadding))

            AppStrengthsColumn(
                animation = animation,
                animationSpeed = animationSpeed
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
                                Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHighest
                            ), end = Offset(0f, 70f)
                        )
                    )
                    .padding(largePadding)
                    .onGloballyPositioned { coordinates ->
                        buttonColumnHeight = Dp(coordinates.size.height / density)
                    }
            ) {
                Button(onClick = onNextClicked, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.next))
                }
            }
        }
    }
}

@Composable
private fun AppStrengthsColumn(
    modifier: Modifier = Modifier,
    animationSpeed: Int = 350,
    animation: Animatable<Float, *>
) {
    Column(
        modifier = modifier.padding(horizontal = smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(extraLargePadding * 2)
    ) {
        AnimatedVisibility(
            visible = animation.value != 0f,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(animationSpeed)
            ) + fadeIn(animationSpec = tween(animationSpeed)) + expandVertically(
                initialHeight = { it },
                animationSpec = tween(animationSpeed)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(largePadding))
                Text(
                    stringResource(Res.string.app_strengths_secure),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(largePadding))
                Text(
                    stringResource(Res.string.app_strengths_multiplatform),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(largePadding))
                Text(
                    stringResource(Res.string.app_strengths_autosave),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
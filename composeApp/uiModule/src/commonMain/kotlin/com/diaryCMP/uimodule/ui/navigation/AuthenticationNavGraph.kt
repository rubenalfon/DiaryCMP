@file:OptIn(KoinExperimentalAPI::class)

package com.diaryCMP.uimodule.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.diaryCMP.uimodule.ui.screens.AuthenticationScreenLoginRegisterEmail
import com.diaryCMP.uimodule.ui.screens.AuthenticationScreenRegisterName
import com.diaryCMP.uimodule.ui.screens.AuthenticationScreenStart
import com.diaryCMP.uimodule.ui.screens.AuthenticationScreenStrongPoints
import com.diaryCMP.uimodule.ui.screens.AuthenticationScreenVerifyEmail
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationScreenViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import moe.tlaster.precompose.navigation.RouteBuilder
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun AuthenticationNavGraph(navigator: Navigator, modifier: Modifier) {
    NavHost(
        navigator = navigator,
        initialRoute = NavGroup.Authentication.path,
        navTransition = getInvertNavTransition(),
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            )
    ) {
        addAuthenticationGroup(navigator = navigator, routeBuilder = this)
    }
}

private fun addAuthenticationGroup(navigator: Navigator, routeBuilder: RouteBuilder) {
    routeBuilder.group(
        route = NavGroup.Authentication.path,
        initialRoute = NavRoute.GStart.path
    ) {
        addAuthenticationScreenStart(navigator = navigator, routeBuilder = this)
        addAuthenticationLoginRegisterStrongPointsScreenComposable(
            navigator = navigator,
            routeBuilder = this
        )
        addAuthenticationLoginRegisterEmailScreenComposable(
            navigator = navigator,
            routeBuilder = this
        )
        addAuthenticationRegisterVerifyEmailScreenComposable(
            navigator = navigator,
            routeBuilder = this
        )
        addAuthenticationRegisterNameScreenComposable(routeBuilder = this)
    }
}

private fun addAuthenticationScreenStart(navigator: Navigator, routeBuilder: RouteBuilder) {
    routeBuilder.scene(
        route = NavRoute.GStart.path
    ) {
        AuthenticationScreenStart(
            navigateToLoginStrongPoints = {
                navigator.navigate(
                    NavRoute.GStrongPoints.path,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
            },
            navigateToLoginRegisterEmail = {
                navigator.navigate(
                    NavRoute.GLoginRegister.path,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
            }
        )
    }
}

private fun addAuthenticationLoginRegisterStrongPointsScreenComposable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GStrongPoints.path
    ) {
        AuthenticationScreenStrongPoints(navigateToLoginRegisterEmail = {
            navigator.navigate(
                NavRoute.GLoginRegister.path,
                options = NavOptions(popUpTo = PopUpTo.First())
            )
        })
    }
}

@OptIn(KoinExperimentalAPI::class)
private fun addAuthenticationLoginRegisterEmailScreenComposable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GLoginRegister.path
    ) {
        AuthenticationScreenLoginRegisterEmail(
            viewModel = koinViewModel<AuthenticationScreenViewModel>(),
            navigateToRegisterNameScreen = {
                navigator.navigate(
                    NavRoute.GRegisterVerifyEmail.path,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
            },
            restartNavigation = {
                navigator.navigate(
                    NavRoute.GStart.path,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
            }
        )
    }
}

private fun addAuthenticationRegisterVerifyEmailScreenComposable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GRegisterVerifyEmail.path
    ) {
        AuthenticationScreenVerifyEmail(
            viewModel = koinViewModel<AuthenticationScreenViewModel>(),
            navigateToRegisterName = {
                navigator.navigate(
                    NavRoute.GRegisterName.path,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
            }
        )
    }
}

private fun addAuthenticationRegisterNameScreenComposable(
    routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GRegisterName.path
    ) {
        AuthenticationScreenRegisterName(
            viewModel = koinViewModel<AuthenticationScreenViewModel>()
        )
    }
}
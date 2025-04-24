package com.diaryCMP.uimodule

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.navigation.AuthenticationNavGraph
import com.diaryCMP.uimodule.ui.navigation.NavGraph
import com.diaryCMP.uimodule.ui.navigation.NavGroup
import com.diaryCMP.uimodule.ui.navigation.NavRoute
import com.diaryCMP.uimodule.ui.navigation.Navigation
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.accept
import diary.composeapp.utilsmodule.generated.resources.calendar
import diary.composeapp.utilsmodule.generated.resources.notification_alert_body
import diary.composeapp.utilsmodule.generated.resources.notification_alert_button
import diary.composeapp.utilsmodule.generated.resources.notification_alert_title
import diary.composeapp.utilsmodule.generated.resources.password_changed_body
import diary.composeapp.utilsmodule.generated.resources.password_changed_title
import diary.composeapp.utilsmodule.generated.resources.settings
import diary.composeapp.utilsmodule.generated.resources.statistics
import diary.composeapp.utilsmodule.generated.resources.today
import es.diaryCMP.modelsModule.models.NavigationItem
import es.diaryCMP.utilsModule.utils.ScreenClass.Compact
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.GlobalViewModel
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun AppContent() {
    AppContentInjected()
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun AppContentInjected(
    viewModel: AuthenticationViewModel = koinViewModel<AuthenticationViewModel>(),
    globalViewModel: GlobalViewModel = koinViewModel<GlobalViewModel>()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isLogged by viewModel.isLogged.collectAsState()
    val hasChangedHisPassword by viewModel.hasChangedHisPassword.collectAsState()

    val loggedNavigation by globalViewModel.loggedNavigation.collectAsState()
    val authenticationNavigation by globalViewModel.authenticationNavigation.collectAsState()

    val showNotificationPermissionDialog by globalViewModel.showNotificationPermissionDialog.collectAsState()


    val backgroundColor =
        if (getScreenClass() == Compact) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHighest

    val density = LocalDensity.current.density
    Surface(
        modifier = Modifier.fillMaxWidth()
            .onGloballyPositioned {
                globalViewModel.updateScreenSize(
                    DpSize(
                        Dp(it.size.width / density),
                        Dp(it.size.height / density)
                    )
                )
            },
        color = backgroundColor
    ) {
        LoadingWrapper(isLoading = isLoading, content = {
            Crossfade(targetState = isLogged) { targetState ->
                when (targetState) {
                    true -> AppNavigation(loggedNavigation)
                    false -> AuthenticationNavigation(authenticationNavigation)
                }
            }
        })
    }

    if (hasChangedHisPassword) {
        AlertDialog(
            title = { Text(stringResource(Res.string.password_changed_title)) },
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.PublishedWithChanges,
                    contentDescription = null
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(Res.string.password_changed_body))
                }
            },
            onDismissRequest = { viewModel.dismissPasswordChanged() },
            confirmButton = {
                Button(onClick = { viewModel.dismissPasswordChanged() }) {
                    Text(stringResource(Res.string.accept))
                }
            }
        )
    }

    if (showNotificationPermissionDialog) {
        AlertDialog(
            title = { Text(stringResource(Res.string.notification_alert_title)) },
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null
                )
            },
            iconContentColor = MaterialTheme.colorScheme.onSurface,
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(Res.string.notification_alert_body))
                }
            },
            onDismissRequest = { globalViewModel.hideNotificationPermissionDialog() },
            confirmButton = {
                TextButton(onClick = { globalViewModel.openNotificationSettings() }) {
                    Text(stringResource(Res.string.notification_alert_button))
                }
            }

        )
    }
}

@Composable
private fun AuthenticationNavigation(navigator: Navigator) {
    AuthenticationNavGraph(navigator, Modifier)
}

@Composable
private fun AppNavigation(navigator: Navigator) {

    LaunchedEffect(Unit) {
        navigator.navigate(NavGroup.Today.path)
    }

    val items = listOf(
        NavigationItem(
            title = stringResource(Res.string.today),
            itemRoute = NavGroup.Today.path,
            selectedIcon = Icons.Filled.Edit,
            unselectedIcon = Icons.Outlined.Edit
        ),
        NavigationItem(
            title = stringResource(Res.string.calendar),
            itemRoute = NavGroup.Calendar.path,
            selectedIcon = Icons.Filled.CalendarMonth,
            unselectedIcon = Icons.Outlined.CalendarMonth
        ),
        NavigationItem(
            title = stringResource(Res.string.statistics),
            itemRoute = NavGroup.Statistics.path,
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart
        ),
        NavigationItem(
            title = stringResource(Res.string.settings),
            itemRoute = NavRoute.GSettings.path,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        ),
    )

    val backgroundColor =
        if (getScreenClass() == Compact) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHighest

    Navigation(items = items,
        navigator = navigator,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = { paddingValues ->
            val modifier = if (getScreenClass() == Compact) {
                Modifier.background(backgroundColor)
            } else {
                Modifier.padding(end = surfaceToWindowPadding, bottom = surfaceToWindowPadding)

            }.padding(paddingValues).fillMaxSize()

            NavGraph(navigator, modifier)
        })
}
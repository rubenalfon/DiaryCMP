@file:OptIn(KoinExperimentalAPI::class)

package com.diaryCMP.uimodule.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.diaryCMP.uimodule.ui.screens.CalendarDetailScreen
import com.diaryCMP.uimodule.ui.screens.CalendarScreen
import com.diaryCMP.uimodule.ui.screens.SettingsChangePasswordScreen
import com.diaryCMP.uimodule.ui.screens.SettingsScreen
import com.diaryCMP.uimodule.ui.screens.StatisticsOrderScreen
import com.diaryCMP.uimodule.ui.screens.StatisticsScreen
import com.diaryCMP.uimodule.ui.screens.TodayOrderScreen
import com.diaryCMP.uimodule.ui.screens.TodayScreen
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.CalendarScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsChangePasswordScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsOrderScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.TodayOrderScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.TodayScreenViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.SwipeProperties
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun NavGraph(navigator: Navigator, modifier: Modifier) {
    NavHost(
        navigator = navigator,
        initialRoute = NavGroup.Today.path,
        navTransition = getNavTransition(),
        modifier = modifier,
        swipeProperties = if (getScreenClass() != ScreenClass.Large) SwipeProperties() else null
    ) {
        addTodayScreensGroup(navigator = navigator, routeBuilder = this)

        addCalendarScreensGroup(navigator = navigator, routeBuilder = this)

        addStatisticsScreensGroup(navigator = navigator, routeBuilder = this)

        addSettingsScreensGroup(navigator = navigator, routeBuilder = this)
    }
}

private fun addTodayScreensGroup(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.group(route = NavGroup.Today.path, initialRoute = NavGroup.Today.routes[0].path) {
        addTodayScreenComposable(navigator = navigator, routeBuilder = routeBuilder)
        addTodayOrderScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
    }
}

private fun addTodayScreenComposable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Today.routes[0].path,
        navTransition = getNoNavTransition()
    ) {
        TodayScreen(
            viewModel = koinViewModel<TodayScreenViewModel>(),
            navigateToTodayOrderScreen = {
                navigator.navigate(NavGroup.Today.routes[1].path)
            }
        )
    }
}

private fun addTodayOrderScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Today.routes[1].path
    ) {
        TodayOrderScreen(
            viewModel = koinViewModel<TodayOrderScreenViewModel>(),
            popBackStack = { navigator.popBackStack() }
        )
    }
}


private fun addCalendarScreensGroup(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.group(
        route = NavGroup.Calendar.path,
        initialRoute = NavGroup.Calendar.routes[0].path
    ) {
        addCalendarScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
        addCalendarDetailScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
    }
}

private fun addCalendarScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Calendar.routes[0].path,
        navTransition = getNoNavTransition()
    ) {
        CalendarScreen(
            viewModel = koinViewModel<CalendarScreenViewModel>(),
            navigateToCalendarEntry = {
                navigator.navigate(NavGroup.Calendar.routes[1].path)
            }
        )
    }
}

private fun addCalendarDetailScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Calendar.routes[1].path
    ) {
        CalendarDetailScreen(
            viewModel = koinViewModel<CalendarScreenViewModel>(),
            popBackStack = { navigator.popBackStack() }
        )
    }
}


private fun addStatisticsScreensGroup(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.group(
        route = NavGroup.Statistics.path,
        initialRoute = NavGroup.Statistics.routes[0].path
    ) {
        addStatisticsScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
        addStatisticsOrderScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
    }
}

private fun addStatisticsScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Statistics.routes[0].path,
        navTransition = getNoNavTransition()
    ) {
        StatisticsScreen(
            viewModel = koinViewModel<StatisticsScreenViewModel>(),
            navigateToStatisticsOrderScreen = {
                navigator.navigate(NavGroup.Statistics.routes[1].path)
            }
        )
    }
}

private fun addStatisticsOrderScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavGroup.Statistics.routes[1].path
    ) {
        StatisticsOrderScreen(
            viewModel = koinViewModel<StatisticsOrderScreenViewModel>(),
            popBackStack = { navigator.popBackStack() }
        )
    }
}

private fun addSettingsScreensGroup(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.group(
        route = NavGroup.Settings.path,
        initialRoute = NavRoute.GSettings.path
    ) {
        addSettingsScreenCompostable(navigator = navigator, routeBuilder = routeBuilder)
        addSettingsChangePasswordScreenCompostable(
            navigator = navigator,
            routeBuilder = routeBuilder
        )
    }
}

private fun addSettingsScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GSettings.path,
        navTransition = getNoNavTransition()
    ) {
        SettingsScreen(
            viewModel = koinViewModel<SettingsScreenViewModel>(),
            navigateToChangePassword = {
                navigator.navigate(NavRoute.GChangePassword.path)
            }
        )
    }
}

private fun addSettingsChangePasswordScreenCompostable(
    navigator: Navigator, routeBuilder: RouteBuilder
) {
    routeBuilder.scene(
        route = NavRoute.GChangePassword.path
    ) {
        SettingsChangePasswordScreen(
            viewModel = koinViewModel<SettingsChangePasswordScreenViewModel>(),
            popBackStack = { navigator.popBackStack() }
        )
    }
}
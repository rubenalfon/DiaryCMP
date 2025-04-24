package com.diaryCMP.uimodule.ui.navigation

sealed class NavGroup(
    val path: String,
    val routes: List<NavRoute>
) {
    data object Authentication : NavGroup(
        "authenticationGroup",
        listOf(
            NavRoute.GStart,
            NavRoute.GStrongPoints,
            NavRoute.GLoginRegister,
            NavRoute.GRegisterName
        )
    )

    data object Today : NavGroup(
        "todayGroup",
        listOf(
            NavRoute.GToday,
            NavRoute.GTodayOrder
        )
    )

    data object Calendar : NavGroup(
        "calendarGroup",
        listOf(
            NavRoute.GCalendar,
            NavRoute.GCalendarEntry
        )
    )

    data object Statistics : NavGroup(
        "statisticsGroup",
        listOf(
            NavRoute.GStatistics,
            NavRoute.GStatisticsOrder
        )
    )

    data object Settings : NavGroup(
        "settingsGroup",
        emptyList()
    )
}

sealed class NavRoute(val path: String) {

    data object GStart : NavRoute("start")
    data object GStrongPoints : NavRoute("strongPoints")
    data object GLoginRegister : NavRoute("loginRegister")
    data object GRegisterVerifyEmail : NavRoute("registerVerifyEmail")
    data object GRegisterName : NavRoute("registerName")


    data object GToday: NavRoute("today")
    data object GTodayOrder: NavRoute("todayOrder")

    data object GCalendar : NavRoute("calendar")
    data object GCalendarEntry : NavRoute("calendarEntry")

    data object GStatistics : NavRoute("statistics")
    data object GStatisticsOrder : NavRoute("statisticsOrder")

    data object GSettings : NavRoute("settings")
    data object GChangePassword : NavRoute("settingsChangePassword")


    // build navigation path (for screen navigation)
    fun withArgs(vararg args: String): String {
        return buildString {
            append(path)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    // build and setup route format (in navigation graph)
    fun withArgsFormat(vararg args: String): String {
        return buildString {
            append(path)
            args.forEach { arg ->
                append("/{$arg}")
            }
        }
    }
}



package es.diaryCMP.diModule.di

import es.calendarMultiplatform.ui.CalendarViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.AuthenticationViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.CalendarScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.GlobalViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsChangePasswordScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsOrderScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.TodayOrderScreenViewModel
import es.diaryCMP.viewmodelsModule.viewmodels.TodayScreenViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelsModule = module {
    singleOf(::GlobalViewModel)
    viewModelOf(::AuthenticationViewModel)


    viewModelOf(::AuthenticationScreenViewModel)

    viewModelOf(::TodayScreenViewModel)
    viewModelOf(::TodayOrderScreenViewModel)

    viewModelOf(::CalendarScreenViewModel)

    viewModelOf(::StatisticsScreenViewModel)
    viewModelOf(::StatisticsOrderScreenViewModel)

    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::SettingsChangePasswordScreenViewModel)

    viewModelOf(::CalendarViewModel)
}
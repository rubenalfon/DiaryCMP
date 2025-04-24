package es.diaryCMP.diModule.di

import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepositoryImpl
import es.diaryCMP.repositoriesModule.repositories.DiaryOrderRepository
import es.diaryCMP.repositoriesModule.repositories.DiaryOrderRepositoryImpl
import es.diaryCMP.repositoriesModule.repositories.StatisticsOrderRepository
import es.diaryCMP.repositoriesModule.repositories.StatisticsOrderRepositoryImpl
import es.diaryCMP.repositoriesModule.repositories.UserRepository
import es.diaryCMP.repositoriesModule.repositories.UserRepositoryImpl
import es.diaryCMP.repositoriesModule.repositories.UserSettingsRepository
import es.diaryCMP.repositoriesModule.repositories.UserSettingsRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<UserRepository> {
        UserRepositoryImpl(
            firebase = get(),
            dataSource = get(),
            diaryDatabaseQueries = get()
        )
    }

    single<DiaryOrderRepository> {
        DiaryOrderRepositoryImpl(
            firebase = get(),
            dataSource = get(),
            diaryDatabaseQueries = get(),
            userRepository = get()
        )
    }

    single<DiaryEntryRepository> {
        DiaryEntryRepositoryImpl(
            firebase = get(),
            dataSource = get(),
            diaryDatabaseQueries = get(),
            userRepository = get()
        )
    }

    single<StatisticsOrderRepository> {
        StatisticsOrderRepositoryImpl(
            firebase = get(),
            dataSource = get(),
            diaryDatabaseQueries = get(),
            userRepository = get()
        )
    }

    single<UserSettingsRepository> {
        UserSettingsRepositoryImpl(
            firebase = get(),
            dataSource = get(),
            diaryDatabaseQueries = get(),
            userRepository = get()
        )
    }
}
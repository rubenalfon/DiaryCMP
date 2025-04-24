package es.diaryCMP.diModule.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.kotlinmultiplatformmobilelocalnotifier.Notifier
import com.example.kotlinmultiplatformmobilelocalnotifier.NotifierJVM
import es.diaryCMP.sqlDelightModule.db.MyDatabase
import es.diaryCMP.utilsModule.utils.SettingsHelper
import es.diaryCMP.utilsModule.utils.SettingsHelperJVM
import org.koin.dsl.module

actual val platformModule = module {
    single<JdbcSqliteDriver> {
        val driver = JdbcSqliteDriver("jdbc:sqlite:diaryDatabase.db")

        MyDatabase.Schema.create(driver)
        return@single driver
    }

    single<MyDatabase> {
        MyDatabase(
            driver = get<JdbcSqliteDriver>(),
            UserSettingsAdapter = get(),
            UserAdapter = get(),
            DiaryEntryAdapter = get(),
            DiaryEntryOrderAdapter = get(),
            StatisticsOrderAdapter = get()
        )
    }

    single<Notifier> {
        NotifierJVM()
    }

    single<SettingsHelper> {
        SettingsHelperJVM()
    }
}
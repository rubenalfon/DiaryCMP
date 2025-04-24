package es.diaryCMP.diModule.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.kotlinmultiplatformmobilelocalnotifier.Notifier
import com.example.kotlinmultiplatformmobilelocalnotifier.NotifierIOS
import es.diaryCMP.sqlDelightModule.db.MyDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<NativeSqliteDriver> {
        NativeSqliteDriver(MyDatabase.Schema, "diaryDatabase.sq")
    }

    single<MyDatabase> {
        MyDatabase(
            get<NativeSqliteDriver>(),
            UserSettingsAdapter = get(),
            UserAdapter = get(),
            DiaryEntryAdapter = get(),
            DiaryEntryOrderAdapter = get(),
            StatisticsOrderAdapter = get()
        )
    }

    single<Notifier> {
        NotifierIOS()
    }
}
package es.diaryCMP.diModule.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.kotlinmultiplatformmobilelocalnotifier.Notifier
import com.example.kotlinmultiplatformmobilelocalnotifier.NotifierAndroid
import es.diaryCMP.sqlDelightModule.db.MyDatabase
import es.diaryCMP.utilsModule.utils.PermissionHandler
import es.diaryCMP.utilsModule.utils.SettingsHelper
import es.diaryCMP.utilsModule.utils.SettingsHelperAndroid
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single<AndroidSqliteDriver> {
        AndroidSqliteDriver(MyDatabase.Schema, get<Context>(), "diaryDatabase.sq")
    }

    single<MyDatabase> {
        MyDatabase(
            get<AndroidSqliteDriver>(),
            UserSettingsAdapter = get(),
            UserAdapter = get(),
            DiaryEntryAdapter = get(),
            DiaryEntryOrderAdapter = get(),
            StatisticsOrderAdapter = get()
        )
    }

    single<Notifier> {
        NotifierAndroid(
            context = get(),
            appActivityClass = get(named("appActivityClass")),
            notificationChannelID = get(named("notificationChannelID")),
            permissionHandler = get()
        )
    }

    single<SettingsHelper> {
        SettingsHelperAndroid(
            context = get()
        )
    }

    single<PermissionHandler> {
        PermissionHandler(
            activityProvider = get(named("activityProvider"))
        )
    }
}
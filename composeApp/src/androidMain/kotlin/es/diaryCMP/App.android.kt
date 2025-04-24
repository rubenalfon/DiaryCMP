package es.diaryCMP

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import es.diaryCMP.diModule.di.initKoin
import es.diaryCMP.utilsModule.utils.ActivityProvider
import es.diaryCMP.utilsModule.utils.PermissionHandler
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationChannelID = "notification_channel_id"

        initKoin(
            config = { androidContext(this@AndroidApp) },
            platformSpecificModule = module {
                single(named("appActivityClass")) {
                    AppActivity::class.java
                }
                single(named("notificationChannelID")) {
                    notificationChannelID
                }
                single(named("activityProvider")) {
                    ActivityProvider()
                }
            }
        )

        createNotificationChannel(this, notificationChannelID)
    }

    private fun createNotificationChannel(
        context: Context,
        @Suppress("SameParameterValue") notificationChannelID: String
    ) {
        val name = "Recordatorios diarios"
        val descriptionText = "Canal para recordar a los usuarios que escriban en su diario"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(notificationChannelID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

class AppActivity : ComponentActivity() {
    private val activityProvider: ActivityProvider by inject(named("activityProvider"))
    private val permissionHandler: PermissionHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            installSplashScreen()
        } else {
            setTheme(R.style.NormalTheme)
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        activityProvider.setActivity(this)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                permissionHandler.handlePermissionResult(isGranted)
            }

        permissionHandler.registerLauncher(requestPermissionLauncher)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityProvider.setActivity(null)
    }
}
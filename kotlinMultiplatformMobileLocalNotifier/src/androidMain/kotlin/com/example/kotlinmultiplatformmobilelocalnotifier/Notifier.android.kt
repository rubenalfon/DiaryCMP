package com.example.kotlinmultiplatformmobilelocalnotifier

import android.app.Activity
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import es.diaryCMP.utilsModule.utils.PermissionHandler
import es.diaryCMP.utilsModule.utils.calendar.localDateTimeNow
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import es.diaryCMP.utilsModule.utils.calendar.toMillis
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import java.util.concurrent.TimeUnit

class NotifierAndroid(
    private val context: Context,
    private val appActivityClass: Class<Activity>,
    private val notificationChannelID: String,
    private val permissionHandler: PermissionHandler
) :
    Notifier {
    private val notificationWorkerTag = "notificationWorkerTag"

    override fun requestPermission(callback: ((Boolean) -> Unit)?) {
        if (callback != null) {
            permissionHandler.requestNotificationPermission(callback)
        }
    }

    override suspend fun scheduleNotification(title: String, body: String, time: LocalTime) {
        requestPermission { isGranted ->
            if (!isGranted) {
                throw Exception("Notification permission not granted")
            }
        }

        val workManager = WorkManager.getInstance(context)

        val data = Data.Builder()
            .putString("title", title)
            .putString("message", body)
            .putString("channelId", notificationChannelID)
            .putString("className", appActivityClass.name)
            .build()

        val currentDateTime = localDateTimeNow()
        var dueDate = LocalDateTime(localDateToday(), LocalTime(time.hour, time.minute))

        if (dueDate < currentDateTime) {
            dueDate = LocalDateTime(localDateToday().plus(1, DateTimeUnit.DAY), dueDate.time)
        }

        val initialDelay = dueDate.toMillis() - currentDateTime.toMillis()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(notificationWorkerTag)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailyNotificationWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    override suspend fun removeAllPendingNotificationRequests() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(notificationWorkerTag)
    }
}
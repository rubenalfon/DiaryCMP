package com.example.kotlinmultiplatformmobilelocalnotifier

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import es.kotlinMultiplatformMobileLocalNotifier.R
import io.github.aakira.napier.Napier
import kotlin.random.Random

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val className = inputData.getString("className") ?: return Result.failure()

        val channelId = inputData.getString("channelId") ?: return Result.failure()

        val title = inputData.getString("title") ?: return Result.failure()
        val message = inputData.getString("message") ?: return Result.failure()
        val imageResource = inputData.getInt("imageResource", R.drawable.ic_notification)


        val targetClass = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            Napier.e { e.printStackTrace().toString() }
            return Result.failure()
        }

        val intent = Intent(applicationContext, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(imageResource)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(Random.nextInt(), notification)

        return Result.success()
    }
}

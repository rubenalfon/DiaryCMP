package com.example.kotlinmultiplatformmobilelocalnotifier

import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalTime
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.random.Random

class NotifierIOS : Notifier {
    override fun requestPermission(callback: ((Boolean) -> Unit)?) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = (UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge),
            completionHandler = { granted, error ->
                if (!granted)
                    Napier.e("Permiso denegado para notificaciones. Error: $error")
            }
        )
    }

    override suspend fun scheduleNotification(title: String, body: String, time: LocalTime) {
        val content = UNMutableNotificationContent()

        content.setTitle(title)
        content.setBody(body)
        content.setSound(UNNotificationSound.defaultSound)

        val dateComponents = NSDateComponents()
        dateComponents.hour = localTime.hour.toLong()
        dateComponents.minute = localTime.minute.toLong()

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = Random.nextInt().toString(),
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error ->
                if (error != null) {
                    Napier.e("Error al agregar la notificación: $error")
                }
            }
    }

    override suspend fun removeAllPendingNotificationRequests() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllPendingNotificationRequests()
    }
}
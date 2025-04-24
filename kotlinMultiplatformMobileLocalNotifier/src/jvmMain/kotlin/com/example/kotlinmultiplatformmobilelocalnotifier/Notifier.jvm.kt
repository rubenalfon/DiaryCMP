package com.example.kotlinmultiplatformmobilelocalnotifier

import kotlinx.datetime.LocalTime

class NotifierJVM : Notifier {
    override fun requestPermission(callback: ((Boolean) -> Unit)?) {
        throw Exception("Notifications in JVM are not supported")
    }

    override suspend fun scheduleNotification(title: String, body: String, time: LocalTime) {
        throw Exception("Notifications in JVM are not supported")
    }

    override suspend fun removeAllPendingNotificationRequests() {
        throw Exception("Notifications in JVM are not supported")
    }
}
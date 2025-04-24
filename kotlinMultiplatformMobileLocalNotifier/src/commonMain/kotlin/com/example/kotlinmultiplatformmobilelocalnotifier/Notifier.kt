package com.example.kotlinmultiplatformmobilelocalnotifier

import kotlinx.datetime.LocalTime

interface Notifier {
    fun requestPermission(callback: ((Boolean) -> Unit)? = null)

    suspend fun scheduleNotification(
        title: String,
        body: String,
        time: LocalTime
    )

    suspend fun removeAllPendingNotificationRequests()
}
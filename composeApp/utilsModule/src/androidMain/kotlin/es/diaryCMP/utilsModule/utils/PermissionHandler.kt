package es.diaryCMP.utilsModule.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionHandler(
    private val activityProvider: ActivityProvider,
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null,
    private var permissionCallback: ((Boolean) -> Unit)? = null
) {
    fun registerLauncher(launcher: ActivityResultLauncher<String>) {
        requestPermissionLauncher = launcher
    }

    fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        val activity = activityProvider.getActivity() ?: return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            callback(true)
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            callback(true)
        } else {
            permissionCallback = callback
            requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun handlePermissionResult(isGranted: Boolean) {
        permissionCallback?.invoke(isGranted)
        permissionCallback = null
    }
}
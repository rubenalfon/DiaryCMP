package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.kotlinmultiplatformmobilelocalnotifier.Notifier
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.notification_body
import diary.composeapp.utilsmodule.generated.resources.notification_title
import es.diaryCMP.utilsModule.utils.SettingsHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalTime
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.getString

class GlobalViewModel(
    private val notifier: Notifier,
    private val settingsHelper: SettingsHelper
) : ViewModel() {

    // PART: Navigation
    private val _authenticationNavigation = MutableStateFlow(Navigator())
    val authenticationNavigation: StateFlow<Navigator> = _authenticationNavigation.asStateFlow()

    private val _loggedNavigation = MutableStateFlow(Navigator())
    val loggedNavigation: StateFlow<Navigator> = _loggedNavigation.asStateFlow()

    // PART: ScreenSize
    private val _screenSize = MutableStateFlow(DpSize(0.dp, 0.dp))
    val screenSize: StateFlow<DpSize> = _screenSize.asStateFlow()

    fun updateScreenSize(size: DpSize) {
        _screenSize.value = size
    }

    private val _navigationComposableSize = MutableStateFlow(DpSize(0.dp, 0.dp))
    val navigationComposableSize: StateFlow<DpSize> = _navigationComposableSize.asStateFlow()

    fun updateNavigationComposableSize(size: DpSize) {
        _navigationComposableSize.value = size
    }

    // PART: Authentication
    private val _eventLogout = MutableSharedFlow<Unit>()
    val eventLogout = _eventLogout.asSharedFlow()

    suspend fun notifyLogout() {
        _eventLogout.emit(Unit)
        notifier.removeAllPendingNotificationRequests()
    }

    private val _eventLogin = MutableSharedFlow<Unit>()
    val eventLogin = _eventLogin.asSharedFlow()

    suspend fun notifyLogin() {
        _eventLogin.emit(Unit)
    }

    private val _eventChangedPassword = MutableSharedFlow<Unit>()
    val eventChangedPassword = _eventChangedPassword.asSharedFlow()

    suspend fun notifyChangedPassword() {
        _eventChangedPassword.emit(Unit)
    }

    // PART TodayScreens
    private val _eventDiaryOrderChanged = MutableSharedFlow<Unit>()
    val eventDiaryOrderChanged = _eventDiaryOrderChanged.asSharedFlow()

    suspend fun notifyDiaryOrderChanged() {
        _eventDiaryOrderChanged.emit(Unit)
    }

    private val _eventDiaryChanged = MutableSharedFlow<Unit>()
    val eventDiaryChanged = _eventDiaryChanged.asSharedFlow()

    suspend fun notifyDiaryChanged() {
        _eventDiaryChanged.emit(Unit)
    }

    // PART StatisticsScreen
    private val _eventStatisticsOrderChanged = MutableSharedFlow<Unit>()
    val eventStatisticsOrderChanged = _eventStatisticsOrderChanged.asSharedFlow()

    suspend fun notifyStatisticsOrderChanged() {
        _eventStatisticsOrderChanged.emit(Unit)
    }

    // PART SettingsScreen
    private val _eventEndDayHourChanged = MutableSharedFlow<Unit>()
    val eventEndDayHourChanged = _eventEndDayHourChanged.asSharedFlow()

    suspend fun notifyEndDayHourChanged() {
        _eventEndDayHourChanged.emit(Unit)
    }

    // PART Notifications
    private val _showNotificationPermissionDialog: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val showNotificationPermissionDialog: StateFlow<Boolean> =
        _showNotificationPermissionDialog.asStateFlow()

    fun hideNotificationPermissionDialog() {
        _showNotificationPermissionDialog.value = false
    }

    fun openNotificationSettings() {
        settingsHelper.openSettings()
        hideNotificationPermissionDialog()
    }

    fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        notifier.requestPermission { isGranted ->
            if (!isGranted) {
                _showNotificationPermissionDialog.value = true
            }
            callback(isGranted)
        }
    }

    suspend fun scheduleNotification(
        time: LocalTime
    ) {
        try {
            notifier.scheduleNotification(
                title = getString(Res.string.notification_title),
                body = getString(Res.string.notification_body),
                time = time
            )
        } catch (ex: Exception) {
            _showNotificationPermissionDialog.value = true
            throw Exception("Error scheduling notification")
        }
    }

    suspend fun cancelNotifications() {
        notifier.removeAllPendingNotificationRequests()
    }
}


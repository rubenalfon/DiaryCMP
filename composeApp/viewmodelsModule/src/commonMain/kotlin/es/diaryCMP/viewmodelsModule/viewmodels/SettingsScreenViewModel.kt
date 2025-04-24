package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.error_no_connection
import diary.composeapp.utilsmodule.generated.resources.sync_body_error_generic
import es.diaryCMP.ktorModule.datasource.NoConnectionException
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.repositoriesModule.repositories.DiaryOrderRepository
import es.diaryCMP.repositoriesModule.repositories.StatisticsOrderRepository
import es.diaryCMP.repositoriesModule.repositories.UserRepository
import es.diaryCMP.repositoriesModule.repositories.UserSettingsRepository
import es.diaryCMP.sqlDelight.db.UserSettings
import es.diaryCMP.utilsModule.utils.Platform
import es.diaryCMP.utilsModule.utils.getPlatform
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString

class SettingsScreenViewModel(
    private val globalViewModel: GlobalViewModel,
    private val userRepository: UserRepository,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val diaryOrderRepository: DiaryOrderRepository,
    private val statisticsOrderRepository: StatisticsOrderRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private var isInitialized = false

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _canReceiveNotifications = MutableStateFlow(false)
    val canReceiveNotifications: StateFlow<Boolean> get() = _canReceiveNotifications.asStateFlow()

    private val _receiveNotifications = MutableStateFlow(false)
    val receiveNotifications: StateFlow<Boolean> get() = _receiveNotifications.asStateFlow()

    private val _notificationsTime = MutableStateFlow(LocalTime(0, 0))
    val notificationsTime: StateFlow<LocalTime> get() = _notificationsTime.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> get() = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> get() = _currentUserEmail.asStateFlow()

    private val _endDayHour = MutableStateFlow(LocalTime(0, 0))
    val endDayHour: StateFlow<LocalTime> get() = _endDayHour.asStateFlow()

    private val _errorSaving = MutableStateFlow(false)
    val errorSaving: StateFlow<Boolean> get() = _errorSaving.asStateFlow()

    var errorMessage: String = ""
        private set

    fun dismissErrorSaving() {
        _errorSaving.value = false
    }

    private val _isSyncInProgress = MutableStateFlow(false)
    val isSyncInProgress: StateFlow<Boolean> get() = _isSyncInProgress.asStateFlow()

    private val _errorSync = MutableStateFlow(false)
    val errorSync: StateFlow<Boolean> get() = _errorSync.asStateFlow()

    fun dismissErrorSync() {
        _errorSync.value = false
    }

    init {
        viewModelScope.launch {
            launch {
                globalViewModel.eventLogout.collect {
                    empty()
                }
            }
            launch {
                _canReceiveNotifications.value =
                    getPlatform() in listOf(Platform.ANDROID, Platform.IOS)
            }
        }
    }

    fun fakeInit() {
        if (isInitialized) return
        isInitialized = true

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try { // If there is no settings, it will be created
                userSettingsRepository.getLatest()
            } catch (ex: NoConnectionException) {
                errorMessage = getString(Res.string.error_no_connection)
                _errorSync.value = true
            } catch (ex: Exception) {

                Napier.e { "No settings found ${ex.message}" }
                userSettingsRepository.saveNew(null) // Set default values
            }
            val user = userRepository.getCurrentUser()

            _currentUserName.value = user.name
            _currentUserEmail.value = user.email

            var userSettings: UserSettings? = null

            try {
                userSettings = userSettingsRepository.getLatest()
            } catch (_: Exception) {
            }

            _receiveNotifications.value = userSettings?.doSendNotifications ?: false
            _notificationsTime.value = userSettings?.notificationTime ?: LocalTime(0, 0)
            _endDayHour.value = userSettings?.endDayHour ?: LocalTime(0, 0)

            _isLoading.value = false
        }
    }

    fun toggleReceiveNotifications() {
        _receiveNotifications.value = !receiveNotifications.value

        viewModelScope.launch {
            if (!receiveNotifications.value) {
                globalViewModel.cancelNotifications()
                return@launch
            }

            var isPermissionGranted = false

            globalViewModel.requestNotificationPermission { isGranted ->
                isPermissionGranted = isGranted

                _receiveNotifications.value = isGranted
            }

            if (!isPermissionGranted) {
                return@launch
            }

            userSettingsRepository.updateDoSentNotifications(receiveNotifications.value)
        }
    }

    fun updateNotificationsTime(time: LocalTime) {
        _notificationsTime.value = time

        viewModelScope.launch {
            launch {
                globalViewModel.cancelNotifications()

                try {
                    globalViewModel.scheduleNotification(notificationsTime.value)
                } catch (ex: Exception) {
                    _receiveNotifications.value = false
                    globalViewModel.cancelNotifications()
                }
            }

            launch {
                userSettingsRepository.updateNotificationTime(time)
            }
        }
    }

    fun forceSync() {
        _isSyncInProgress.value = true

        viewModelScope.launch {
            try {
                diaryEntryRepository.forceSyncDiaryEntries()
                diaryOrderRepository.forceSyncDiaryEntryOrder()
                statisticsOrderRepository.forceSyncOrderRepository()
            } catch (ex: Exception) {
                _errorSync.value = true
                errorMessage =
                    if (ex is SocketTimeoutException || ex.message?.contains("Failed to connect") == true) {
                        getString(Res.string.error_no_connection)
                    } else {
                        getString(Res.string.sync_body_error_generic, ex.message ?: "UNKNOWN ERROR")
                    }
            } finally {
                _isSyncInProgress.value = false
            }
        }
    }

    fun onChangeEndDayHour(endDayHour: LocalTime) {
        _endDayHour.value = endDayHour
        viewModelScope.launch {
            userSettingsRepository.updateEndDayHour(endDayHour)
            globalViewModel.notifyEndDayHourChanged()
        }
    }

    fun changeUserName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                _currentUserName.value = name.trim()
                userRepository.updateUserName(currentUserName.value!!)
            } catch (ex: Exception) {
                if (ex is SocketTimeoutException) {
                    errorMessage = getString(Res.string.error_no_connection)
                    _errorSaving.value = true
                    return@launch
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            globalViewModel.cancelNotifications()

            userRepository.emptyDatabase()
            globalViewModel.notifyLogout()
        }
    }

    private fun empty() {
        isInitialized = false
        _isLoading.value = true
        _receiveNotifications.value = false
        _currentUserName.value = ""
        _currentUserEmail.value = ""
        _endDayHour.value = LocalTime(0, 0)
        _notificationsTime.value = LocalTime(0, 0)
        _errorSaving.value = false
        errorMessage = ""
    }
}
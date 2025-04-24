package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.error_no_connection
import diary.composeapp.utilsmodule.generated.resources.sync_body_error_generic
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class SettingsChangePasswordScreenViewModel(
    private val globalViewModel: GlobalViewModel,
    private val authenticationViewModel: AuthenticationViewModel
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _oldPassword = MutableStateFlow("")
    val oldPassword = _oldPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    fun updateOldPassword(value: String) {
        _oldPassword.value = value
    }

    fun updateNewPassword(value: String) {
        _newPassword.value = value
    }

    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
    }

    private val _oldPasswordError = MutableStateFlow(false)
    val oldPasswordError = _oldPasswordError.asStateFlow()

    private val _newPasswordError = MutableStateFlow(false)
    val newPasswordError = _newPasswordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow(false)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    var errorMessage: String = ""
        private set

    private val _errorUpdatingPassword = MutableStateFlow(false)
    val errorUpdatingPassword: StateFlow<Boolean> get() = _errorUpdatingPassword.asStateFlow()

    fun dismissErrors() {
        _oldPasswordError.value = false
        _newPasswordError.value = false
        _confirmPasswordError.value = false
        _errorUpdatingPassword.value = false
    }

    init {
        viewModelScope.launch {
            launch {
                globalViewModel.eventLogout.collect {
                    empty()
                }
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_oldPassword.value.isEmpty()) {
                _oldPasswordError.value = true
                return@launch
            }
            if (_newPassword.value.isEmpty()) {
                _newPasswordError.value = true
                return@launch
            }
            dismissErrors()
            _isLoading.value = true
            if (!authenticationViewModel.isPasswordValid(_newPassword.value)) {
                _newPasswordError.value = true
                _isLoading.value = false
                return@launch
            }

            if (_newPassword.value != _confirmPassword.value) {
                _confirmPasswordError.value = true
                _isLoading.value = false
                return@launch
            }

            try {
                authenticationViewModel.changePassword(_oldPassword.value, _newPassword.value)

                globalViewModel.notifyLogout()

            } catch (ex: Exception) {
                if (ex.message == "WRONG_PASSWORD") {
                    _oldPasswordError.value = true
                    return@launch
                } else if (ex.message == "NO_CONNECTION") {
                    errorMessage = getString(Res.string.error_no_connection)
                    _errorUpdatingPassword.value = true
                    return@launch
                } else if (ex is SocketTimeoutException) {
                    errorMessage = getString(Res.string.error_no_connection)
                } else {
                    errorMessage =
                        getString(Res.string.sync_body_error_generic, ex.message.toString())
                }
                _errorUpdatingPassword.value = true
                return@launch
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun empty() {
        _isLoading.value = false

        _oldPassword.value = ""
        _newPassword.value = ""
        _confirmPassword.value = ""

        _oldPasswordError.value = false
        _newPasswordError.value = false
        _confirmPasswordError.value = false

        _errorUpdatingPassword.value = false
        errorMessage = ""
    }
}
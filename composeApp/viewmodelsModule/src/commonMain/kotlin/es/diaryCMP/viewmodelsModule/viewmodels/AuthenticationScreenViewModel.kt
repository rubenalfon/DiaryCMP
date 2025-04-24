package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.error_bad_email
import diary.composeapp.utilsmodule.generated.resources.error_no_connection
import diary.composeapp.utilsmodule.generated.resources.register_body_error_generic
import diary.composeapp.utilsmodule.generated.resources.register_body_error_same_email
import diary.composeapp.utilsmodule.generated.resources.register_body_invalid_login_credentials
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class AuthenticationScreenViewModel(
    private val authenticationViewModel: AuthenticationViewModel
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _isLogin = MutableStateFlow(true)
    val isLogin: StateFlow<Boolean> get() = _isLogin.asStateFlow()

    fun toggleLogin() {
        _isLogin.value = !_isLogin.value
        turnOffErrors()
    }

    private val _passwordAlertHasAppeared = MutableStateFlow(false)
    val passwordAlertHasAppeared: StateFlow<Boolean> get() = _passwordAlertHasAppeared.asStateFlow()
    fun updatePasswordAlertHasAppeared(value: Boolean) {
        _passwordAlertHasAppeared.value = value
    }

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> get() = _email.asStateFlow()
    fun updateEmail(text: String) {
        if (isLoading.value) return
        _email.value = text
    }

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password.asStateFlow()
    fun updatePassword(text: String) {
        if (isLoading.value) return
        _password.value = text
    }

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> get() = _confirmPassword.asStateFlow()
    fun updateConfirmPassword(text: String) {
        if (isLoading.value) return
        _confirmPassword.value = text
    }

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> get() = _userName.asStateFlow()
    fun updateUserName(text: String) {
        if (isLoading.value) return
        _userName.value = text
    }

    private fun isEmailValid(email: String): Boolean {
        if (email == "") return false
        val emailRegex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    private val _emailError = MutableStateFlow(false)
    val emailError: StateFlow<Boolean> get() = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow(false)
    val passwordError: StateFlow<Boolean> get() = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow(false)
    val confirmPasswordError: StateFlow<Boolean> get() = _confirmPasswordError.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> get() = _showError.asStateFlow()

    var errorMessage: String = ""
        private set

    private fun turnOffErrors() {
        _emailError.value = false
        _passwordError.value = false
        _confirmPasswordError.value = false
        _showError.value = false
    }

    fun dismissAlert() {
        _showError.value = false
    }

    private val _isErrorNameTextField = MutableStateFlow(false)
    val isErrorNameTextField: StateFlow<Boolean> get() = _isErrorNameTextField.asStateFlow()

    private lateinit var navigateToRegisterNameScreen: () -> Unit

    fun setNavigation(navigationOrder: () -> Unit) {
        navigateToRegisterNameScreen = navigationOrder
    }

    private lateinit var restartNavigation: () -> Unit

    fun setRestartNavigation(navigationOrder: () -> Unit) {
        restartNavigation = navigationOrder
    }

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> get() = _isEmailVerified.asStateFlow()

    private var checkEmailVerificationJob: Job? = null

    fun startServiceCheckEmailVerification() {
        if (checkEmailVerificationJob != null) {
            return
        }
        checkEmailVerificationJob = viewModelScope.launch {
            while (true) {
                try {
                    _isEmailVerified.value = authenticationViewModel.checkEmailVerification()

                    if (!_isEmailVerified.value) {
                        delay(3_000)
                        continue
                    }

                    break
                } catch (ex: Exception) {
                    Napier.e { "AuthenticationViewModel.startServiceCheckEmailVerification() ${ex.message.toString()}" }
                }
            }
        }
    }

    fun stopServiceCheckEmailVerification() {
        checkEmailVerificationJob?.cancel()
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            authenticationViewModel.sendVerificationEmail()
        }
    }

    fun next() {
        turnOffErrors()
        if (email.value.isEmpty()) _emailError.value = true
        else if (password.value.isEmpty()) _passwordError.value = true

        if (emailError.value || passwordError.value) return

        when (isLogin.value) {
            true -> {
                login()
            }

            false -> {
                registerNoUserName()
            }
        }
    }

    private fun login() {
        _email.value = email.value.trim()

        _emailError.value = !isEmailValid(email.value)
        _passwordError.value = !authenticationViewModel.isPasswordValid(password.value)

        if (emailError.value || passwordError.value) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                authenticationViewModel.login(email.value, password.value)
                postLoginTasks()
            } catch (ex: Exception) {
                showAlertByError(errorText = ex.message ?: ex.toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun registerNoUserName() {
        _email.value = email.value.trim()

        _emailError.value = !isEmailValid(email.value)
        _passwordError.value = !authenticationViewModel.isPasswordValid(password.value)
        _confirmPasswordError.value =
            password.value != confirmPassword.value /*|| passwordError.value*/

        if (emailError.value || passwordError.value || confirmPasswordError.value) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                authenticationViewModel.register(email.value, password.value)
                navigateToRegisterNameScreen.invoke()
            } catch (ex: Exception) {
                showAlertByError(errorText = ex.message ?: ex.toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerUserName() {
        if (userName.value.isBlank()) {
            _isErrorNameTextField.value = true
            return
        }
        _isErrorNameTextField.value = false

        _userName.value = userName.value.trim()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                authenticationViewModel.updateUserName(
                    userName = userName.value, onlyLocal = false
                )
                postLoginTasks()
            } catch (ex: Exception) {
                showAlertByError(errorText = ex.message ?: ex.toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun showAlertByError(errorText: String) {
        when (errorText) {
            "EMAIL_EXISTS" -> {
                errorMessage = getString(Res.string.register_body_error_same_email)
                _showError.value = true
            }

            "INVALID_LOGIN_CREDENTIALS" -> {
                errorMessage = getString(Res.string.register_body_invalid_login_credentials)
                _showError.value = true
            }

            "INVALID_EMAIL" -> {
                errorMessage = getString(Res.string.error_bad_email)
                _emailError.value = true
            }

            "NO_CONNECTION" -> {
                errorMessage = getString(Res.string.error_no_connection)
                _showError.value = true
            }

            else -> {
                errorMessage = getString(Res.string.register_body_error_generic, errorText)
                _showError.value = true
            }
        }
    }

    private suspend fun postLoginTasks() {
        delay(1_000) // For navigation animation
        restartNavigation.invoke()
        clean()
    }

    private fun clean() {
        _isLoading.value = false
        _isLogin.value = true
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _userName.value = ""
        turnOffErrors()
    }
}


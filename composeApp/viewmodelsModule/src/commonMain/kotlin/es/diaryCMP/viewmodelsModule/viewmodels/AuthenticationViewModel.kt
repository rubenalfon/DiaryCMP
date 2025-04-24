package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.modelsModule.models.ErrorResponse
import es.diaryCMP.modelsModule.models.FirebaseUser
import es.diaryCMP.repositoriesModule.repositories.UserRepository
import es.diaryCMP.sqlDelight.db.User
import es.diaryCMP.utilsModule.utils.encription.UserEncryption
import es.diaryCMP.utilsModule.utils.encription.decrypt
import es.diaryCMP.utilsModule.utils.encription.encrypt
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthenticationViewModel(
    private var firebase: Firebase,
    private val userRepository: UserRepository,
    private val globalViewModel: GlobalViewModel
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _isLogged = MutableStateFlow(false)
    val isLogged: StateFlow<Boolean> get() = _isLogged.asStateFlow()

    private val _hasChangedHisPassword = MutableStateFlow(false)
    val hasChangedHisPassword: StateFlow<Boolean> get() = _hasChangedHisPassword.asStateFlow()

    fun dismissPasswordChanged() {
        _hasChangedHisPassword.value = false
    }

    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false

        val hasLowerCase = Regex("[a-z]").containsMatchIn(password)
        val hasUpperCase = Regex("[A-Z]").containsMatchIn(password)
        val hasNumber = Regex("[0-9]").containsMatchIn(password)
        val hasEspecialCharacter = Regex("[^a-zA-Z0-9]").containsMatchIn(password)

        return hasLowerCase && hasUpperCase && hasNumber && hasEspecialCharacter
    }

    suspend fun register(
        email: String,
        password: String
    ) {
        try {
            firebase.signup(email, password)

            val userKey = UserEncryption.generateUserKey()

            userRepository.updateUserKey(userKey)
            uploadEncryptedKey(password, userKey)

        } catch (ex: Exception) {
            logout()
            manageLoginRegisterException(ex)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ) {
        try {
            firebase.login(email, password)

            downloadCheckAndSaveKey(password)

            updateUserName(userRepository.getUserNameFromServer() ?: "-", onlyLocal = true)
            globalViewModel.notifyLogin()
        } catch (ex: Exception) {
            logout()
            manageLoginRegisterException(ex)
        }
    }

    private suspend fun uploadEncryptedKey(password: String, userKey: ByteArray) {
        val salt = UserEncryption.generateRandom16ByteArray()
        val temporalPasswordKey = UserEncryption.deriveKey(password, salt)
        val encryptedUserKey =
            encrypt(plainText = Json.encodeToString(userKey), key = temporalPasswordKey)

        userRepository.uploadSaltAndEncryptedKey(salt = salt, encryptedKey = encryptedUserKey)
    }

    private suspend fun downloadCheckAndSaveKey(password: String) {
        val saltEncryptedKey = userRepository.downloadSaltAndEncryptedKey()
        val salt = saltEncryptedKey.first
        val encryptedUserKey = saltEncryptedKey.second

        val temporalPasswordKey = UserEncryption.deriveKey(password, salt)

        try {
            val userKey = decrypt(cipherText = encryptedUserKey, key = temporalPasswordKey)

            userRepository.updateUserKey(Json.decodeFromString(userKey))
        } catch (ex: Exception) {
            Napier.e { "AuthenticationViewModel.checkAndSaveKey() ${ex.message.toString()}" }
            throw ex
        }
    }

    suspend fun updateUserName(userName: String, onlyLocal: Boolean) {
        try {
            userRepository.safeCreateUserIfNotExists()
            userRepository.updateUserName(userName, onlyLocal)

            globalViewModel.notifyLogin()
        } catch (ex: Exception) {
            Napier.e { "AuthenticationViewModel.updateUserName() ${ex.message.toString()}" }
            globalViewModel.notifyLogin()
        }
    }

    private fun manageLoginRegisterException(ex: Exception) {
        if (ex.message == "Failed to connect to identitytoolkit.googleapis.com/142.250.184.10:443") {
            throw Exception("NO_CONNECTION")
        } else if (ex.message == "WRONG_KeyHash") {
            throw Exception("WRONG_KeyHash")
        }

        val json = Json { ignoreUnknownKeys = true }
        val error = json.decodeFromString<ErrorResponse>(ex.message!!)
        throw Exception(error.error.message)
    }

    private fun checkSession() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val user: User? = firebase.checkSession()

            if (user == null) {
                _isLogged.value = false
                _isLoading.value = false
                return@launch
            }
            _isLogged.value = true
            _isLoading.value = false

            firebase.setCurrentUser(
                FirebaseUser(
                    emailID = user.email, idToken = user.idToken, uid = user.localId!!
                )
            )
        }
    }

    init {
        checkSession()
        viewModelScope.launch {
            launch {
                globalViewModel.eventLogout.collect {
                    logout()
                }
            }
            launch {
                globalViewModel.eventLogin.collect {
                    expressLogin()
                }
            }
            launch {
                globalViewModel.eventChangedPassword.collect {
                    _hasChangedHisPassword.value = true
                }
            }
        }
    }

    private fun logout() {
        firebase.logout()
        _isLogged.value = false
    }

    private fun expressLogin() {
        _isLogged.value = true
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!verifyOldPassword(oldPassword)) {
            throw Exception("WRONG_PASSWORD")
        }

        try {
            firebase.changeUserPassword(newPassword = newPassword)

            uploadEncryptedKey(newPassword, userKey = userRepository.localGetUserKey()!!)

            globalViewModel.notifyChangedPassword()
            logout()

            return true
        } catch (ex: Exception) {
            logout()
            Napier.e { "AuthenticationViewModel.changePassword() ${ex.message.toString()}" }
            if (ex.message?.contains("Failed to connect") == true) {
                throw Exception("NO_CONNECTION")
            }
        }
        return false
    }

    private suspend fun verifyOldPassword(oldPassword: String): Boolean {
        var isPasswordCorrect = false

        try {
            val saltEncryptedKey = userRepository.downloadSaltAndEncryptedKey()
            val salt = saltEncryptedKey.first
            val encryptedUserKey = saltEncryptedKey.second

            val temporalPasswordKey = UserEncryption.deriveKey(oldPassword, salt)

            decrypt(cipherText = encryptedUserKey, key = temporalPasswordKey)
            isPasswordCorrect = true
        } catch (ex: Exception) {
            Napier.e { "AuthenticationViewModel.verifyOldPassword() ${ex.message.toString()}" }
        } finally {
            return isPasswordCorrect
        }
    }

    suspend fun sendVerificationEmail() {
        try {
            firebase.sendVerificationEmail()
        } catch (ex: Exception) {
            logout()
            if (ex.message?.contains("Failed to connect") == true) {
                throw Exception("NO_CONNECTION")
            }
        }
    }

    suspend fun checkEmailVerification(): Boolean {
        try {
            return firebase.isEmailVerified()

        } catch (ex: Exception) {
            logout()
            if (ex.message?.contains("Failed to connect") == true) {
                throw Exception("NO_CONNECTION")
            }
        }
        return false
    }
}


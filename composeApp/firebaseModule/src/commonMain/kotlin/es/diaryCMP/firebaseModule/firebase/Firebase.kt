package es.diaryCMP.firebaseModule.firebase

import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.modelsModule.models.AuthResponse
import es.diaryCMP.modelsModule.models.ChangePasswordResponse
import es.diaryCMP.modelsModule.models.FirebaseUser
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.User
import io.github.aakira.napier.Napier

class Firebase(
    private val databaseQueries: DiaryDatabaseQueries,
    private val ktorDataSource: KtorDataSource
) {
    private var _currentUser: FirebaseUser? = null

    fun getCurrentUser() = _currentUser

    fun setCurrentUser(currentUser: FirebaseUser?) {
        _currentUser = currentUser
    }

    suspend fun checkSession(): User? {
        try {
            if (databaseQueries.selectAllUsers().executeAsList().size != 1) { // Forbidden
                databaseQueries.removeAllUsers()
                return null
            }
        } catch (_: Exception) {
            return null
        }

        val user = databaseQueries.selectAllUsers().executeAsOne()

        var userToReturn: User? = null
        try {
            val response = ktorDataSource.getRefreshToken(user.refreshToken)
            userToReturn = User(
                idToken = response.idToken,
                email = user.email,
                localId = response.userId,
                refreshToken = response.refreshToken,
                name = user.email,
                encryptionKey = user.encryptionKey
            )
        } catch (ex: Exception) {
            userToReturn =
                if (ex.message!!.contains("Failed to connect to identitytoolkit.googleapis.com/")) {
                    user
                } else {
                    null
                }
        } finally {
            return userToReturn
        }
    }

    suspend fun login(email: String, password: String) {
        val response = ktorDataSource.login(email = email, password = password)
        storeUserDetails(response = response)
    }

    suspend fun signup(email: String, password: String) {
        val response = ktorDataSource.signup(email = email, password = password)
        storeUserDetails(response = response)
    }

    suspend fun sendVerificationEmail() {
        ktorDataSource.sendVerificationEmail(idToken = _currentUser!!.idToken)
    }

    suspend fun isEmailVerified(): Boolean {
        return ktorDataSource.isEmailVerified(idToken = _currentUser!!.idToken)
    }

    fun logout() {
        databaseQueries.removeAllUsers()
    }

    private fun storeUserDetails(response: AuthResponse) {
        databaseQueries.removeAllUsers()
        try {
            databaseQueries.insertUser(
                idToken = response.idToken,
                email = response.email,
                refreshToken = response.refreshToken,
                name = "-",
                localId = response.localId,
                encryptionKey = "null".encodeToByteArray()
            )
        } catch (ex: Exception) {
            Napier.e { "Firebase.storeUserDetails() ${ex.message.toString()}" }
        }
        setCurrentUser(
            currentUser = FirebaseUser(
                uid = response.localId,
                emailID = response.email,
                idToken = response.idToken
            )
        )
    }

    suspend fun changeUserPassword(newPassword: String): ChangePasswordResponse {
        return ktorDataSource.changeUserPassword(
            idToken = getCurrentUser()!!.idToken,
            password = newPassword
        )
    }
}
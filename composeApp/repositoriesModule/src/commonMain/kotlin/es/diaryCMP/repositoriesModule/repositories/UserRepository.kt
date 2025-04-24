package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.User
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface UserRepository {
    suspend fun safeCreateUserIfNotExists(): Boolean
    suspend fun getUserNameFromServer(): String?
    suspend fun getCurrentUser(): User
    suspend fun updateUserName(userName: String, onlyLocal: Boolean = false)
    suspend fun localGetUserKey(): ByteArray?
    suspend fun updateUserKey(key: ByteArray)
    suspend fun downloadSaltAndEncryptedKey(): Pair<ByteArray, ByteArray>
    suspend fun uploadSaltAndEncryptedKey(salt: ByteArray, encryptedKey: ByteArray)
    suspend fun emptyDatabase()
}

class UserRepositoryImpl(
    private val firebase: Firebase,
    private val dataSource: KtorDataSource,
    private val diaryDatabaseQueries: DiaryDatabaseQueries
) : GenericRepository, UserRepository {
    override val userRepository: UserRepository = this

    override fun getChild() = listOf(
        "users"
    )

    override fun getEntryName(data: Any?) = firebase.getCurrentUser()!!.uid


    override suspend fun safeCreateUserIfNotExists(): Boolean {
        val user = diaryDatabaseQueries.selectAllUsers().executeAsOneOrNull() ?: return false

        var userExists = false
        try {
            if (getUserNameFromServer() == null) throw Exception("Default value name")
        } catch (ex: Exception) {
            try {
                dataSource.firebaseFirestorePatch(
                    child = getChild() + getEntryName(),
                    parameter = hashMapOf(
                        "fields" to hashMapOf(
                            "name" to hashMapOf(
                                "stringValue" to user.name
                            ),
                            "email" to hashMapOf(
                                "stringValue" to user.email
                            )
                        )
                    ), currentUser = firebase.getCurrentUser()!!
                )
                userExists = true

            } catch (ex: Exception) {
                Napier.e { "UserRepositoryImpl.safeCreateUserIfNotExists() ${ex.message.toString()}" }
            }
        }
        return userExists
    }

    override suspend fun getUserNameFromServer(): String? {
        var name: String? = null
        try {
            val response = dataSource.firebaseFirestoreGet(
                child = getChild(),
                query = getEntryName(),
                currentUser = firebase.getCurrentUser()!!
            )

            val jsonElement = Json.parseToJsonElement(response)
            val fields =
                requireNotNull(jsonElement.jsonObject["fields"]) { "Missing 'fields' in response" }

            name =
                requireNotNull(fields.jsonObject["name"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'name' in response" }
        } catch (ex: Exception) {
            Napier.e { "UserRepositoryImpl.getDetailsFromServer() ${ex.message.toString()}" }
        }
        return name
    }

    override suspend fun getCurrentUser(): User {
        return diaryDatabaseQueries.selectAllUsers().executeAsOne()
    }

    override suspend fun updateUserName(userName: String, onlyLocal: Boolean) {
        diaryDatabaseQueries.selectAllUsers().executeAsOneOrNull() ?: return

        storeUserName(userName)

        if (onlyLocal) return

        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "name" to hashMapOf(
                        "stringValue" to userName
                    )
                )
            ), currentUser = firebase.getCurrentUser()!!
        )
    }

    private fun storeUserName(name: String) {
        val savedUser = diaryDatabaseQueries.selectAllUsers().executeAsOne()

        diaryDatabaseQueries.updateUserName(name = name, idToken = savedUser.idToken)
    }

    override suspend fun localGetUserKey(): ByteArray? {
        val savedUser = diaryDatabaseQueries.selectAllUsers().executeAsOne()
        return savedUser.encryptionKey
    }

    override suspend fun updateUserKey(key: ByteArray) {
        val savedUser = diaryDatabaseQueries.selectAllUsers().executeAsOne()

        diaryDatabaseQueries.updateEncryptionKey(encryptionKey = key, idToken = savedUser.idToken)
    }

    override suspend fun downloadSaltAndEncryptedKey(): Pair<ByteArray, ByteArray> {
        try {
            val response = dataSource.firebaseFirestoreGet(
                child = getChild(),
                query = getEntryName(),
                currentUser = firebase.getCurrentUser()!!
            )

            val jsonElement = Json.parseToJsonElement(response)
            val fields = jsonElement.jsonObject["fields"] ?: error("Missing 'fields' in response")

            val saltEncryptedKeyJson =
                fields.jsonObject["saltEncryptedKeyJson"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
                    ?: error("Missing 'saltEncryptedKeyJson' in response")

            val saltEncryptedKey = Json.decodeFromString<ByteArray>(saltEncryptedKeyJson)

            val salt = saltEncryptedKey.copyOfRange(0, 16)
            val encryptedKey = saltEncryptedKey.copyOfRange(16, saltEncryptedKey.size)

            return salt to encryptedKey

        } catch (ex: Exception) {
            Napier.e { "UserRepositoryImpl.downloadSaltAndKeyHash() ${ex.message.toString()}" }
            throw ex
        }
    }

    override suspend fun uploadSaltAndEncryptedKey(salt: ByteArray, encryptedKey: ByteArray) {
        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "saltEncryptedKeyJson" to hashMapOf(
                        "stringValue" to Json.encodeToString(salt + encryptedKey)
                    )
                )
            ), currentUser = firebase.getCurrentUser()!!
        )
    }

    override suspend fun emptyDatabase() {

        diaryDatabaseQueries.removeAllUserSettings()

        diaryDatabaseQueries.removeAllDiaryEntries()

        diaryDatabaseQueries.removeAllDiaryEntryOrders()

        diaryDatabaseQueries.removeAllStatisticsOrders()
    }

}
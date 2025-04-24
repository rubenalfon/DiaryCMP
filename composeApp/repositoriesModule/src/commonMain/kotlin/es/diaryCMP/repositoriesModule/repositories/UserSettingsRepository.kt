package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface UserSettingsRepository {
    suspend fun getLatest(): UserSettings?
    suspend fun getLatestFromServer(forceSync: Boolean = false): UserSettings
    suspend fun saveNew(userSettings: UserSettings?)
    suspend fun deleteLocal()
    suspend fun updateDoSentNotifications(doSendNotifications: Boolean)
    suspend fun updateNotificationTime(notificationTime: LocalTime)
    suspend fun updateEndDayHour(endDayHour: LocalTime)
}

class UserSettingsRepositoryImpl(
    private val firebase: Firebase,
    private val dataSource: KtorDataSource,
    private val diaryDatabaseQueries: DiaryDatabaseQueries,
    override val userRepository: UserRepository
) : GenericRepository, UserSettingsRepository {

    override fun getChild() = listOf(
        "users"
    )

    override fun getEntryName(data: Any?): String =
        firebase.getCurrentUser()!!.uid

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var lastServiceResponseCache: String? = null

    override suspend fun getLatest(): UserSettings? {
        val serverUserSettings = getLatestFromServer()
        val localUserSettings = diaryDatabaseQueries.getAllUserSettings().executeAsOneOrNull()

        if (localUserSettings == null) {
            diaryDatabaseQueries.insertUserSettings(
                localId = firebase.getCurrentUser()!!.uid,
                doSendNotifications = false,                      // Default value
                notificationTime = LocalTime(0, 0),  // Default value
                endDayHour = serverUserSettings.endDayHour
            )
        } else {
            if (localUserSettings.endDayHour != serverUserSettings.endDayHour) {
                diaryDatabaseQueries.updateEndDayHour(
                    endDayHour = serverUserSettings.endDayHour
                )
            }
        }

        return localUserSettings
    }

    override suspend fun getLatestFromServer(forceSync: Boolean): UserSettings {
        var response: String? = lastServiceResponseCache

        if (response == null || forceSync) {
            lastServiceResponseCache = dataSource.firebaseFirestoreGet(
                child = getChild(),
                query = getEntryName(),
                currentUser = firebase.getCurrentUser()!!
            )
            response = lastServiceResponseCache

            coroutineScope.launch {
                delay(timeMillis = 5_000)
                lastServiceResponseCache = null
            }
        }

        val jsonElement = Json.parseToJsonElement(response!!)
        val fields =
            requireNotNull(jsonElement.jsonObject["fields"]) { "Missing 'fields' in response" }

        val endDayHour =
            requireNotNull(fields.jsonObject["endDayHour"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'endDayHour' in response" }

        return UserSettings(
            localId = firebase.getCurrentUser()!!.uid,
            doSendNotifications = null,
            notificationTime = null,

            endDayHour = LocalTime.parse(
                getDecryptedFromJson(
                    json = endDayHour
                )
            )
        )
    }

    override suspend fun saveNew(userSettings: UserSettings?) {
        @Suppress("NAME_SHADOWING") val userSettings = userSettings ?: UserSettings(
            localId = firebase.getCurrentUser()!!.uid,
            doSendNotifications = false,
            notificationTime = LocalTime(0, 0),
            endDayHour = LocalTime(0, 0)
        )

        if (diaryDatabaseQueries.getAllUserSettings().executeAsOneOrNull() != null) {
            diaryDatabaseQueries.updateDoSentNotifications(
                doSendNotifications = userSettings.doSendNotifications
            )
            diaryDatabaseQueries.updateNotificationTime(
                notificationTime = userSettings.notificationTime
            )
            diaryDatabaseQueries.updateEndDayHour(
                endDayHour = userSettings.endDayHour
            )
        } else {
            diaryDatabaseQueries.insertUserSettings(
                localId = firebase.getCurrentUser()!!.uid,
                doSendNotifications = userSettings.doSendNotifications,
                notificationTime = userSettings.notificationTime,
                endDayHour = userSettings.endDayHour
            )
        }

        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "endDayHour" to hashMapOf(
                        "stringValue" to getEncryptedJson(
                            json = Json.encodeToString(userSettings.endDayHour)
                        )
                    )
                )
            ),
            currentUser = firebase.getCurrentUser()!!
        )
    }

    override suspend fun deleteLocal() {
        diaryDatabaseQueries.removeAllUserSettings()
    }

    override suspend fun updateDoSentNotifications(doSendNotifications: Boolean) {
        diaryDatabaseQueries.updateDoSentNotifications(doSendNotifications)
    }

    override suspend fun updateNotificationTime(notificationTime: LocalTime) {
        diaryDatabaseQueries.updateNotificationTime(notificationTime)
    }

    override suspend fun updateEndDayHour(endDayHour: LocalTime) {
        diaryDatabaseQueries.updateEndDayHour(endDayHour)

        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "endDayHour" to hashMapOf(
                        "stringValue" to getEncryptedJson(
                            json = endDayHour.toString()
                        )
                    )
                )
            ),
            currentUser = firebase.getCurrentUser()!!
        )
    }
}
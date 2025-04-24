package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface DiaryEntryRepository {
    suspend fun getAllDiaryEntries(): List<DiaryEntry>
    suspend fun getEntriesLastSevenDays(): List<DiaryEntry>
    suspend fun getDiaryEntryByDate(date: LocalDate): DiaryEntry
    suspend fun saveTodayEntry(
        id: String,
        components: List<DiaryEntryComponent>,
        date: LocalDate,
        updatedDateTime: LocalDateTime,
        createdDateTime: LocalDateTime
    )

    suspend fun forceSyncDiaryEntries()
    suspend fun deleteLocal()
}

class DiaryEntryRepositoryImpl(
    private val firebase: Firebase,
    private val dataSource: KtorDataSource,
    private val diaryDatabaseQueries: DiaryDatabaseQueries,
    override val userRepository: UserRepository
) : GenericRepository, DiaryEntryRepository {

    override fun getChild() = listOf(
        "users",
        firebase.getCurrentUser()!!.uid,
        "diaryEntries"
    )

    override fun getEntryName(data: Any?): String =
        "diaryEntry-${data as LocalDate? ?: localDateToday()}"

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val jobList: MutableMap<String, Job> = mutableMapOf()
    private var lastServiceResponseCache: String? = null

    override suspend fun getAllDiaryEntries(): List<DiaryEntry> {
        val localDiaryEntries = diaryDatabaseQueries.getAllDiaryEntries().executeAsList()


        val response = downloadDiaryEntriesResponse()

        val jsonElement = Json.parseToJsonElement(response)
        val documents = jsonElement.jsonObject["documents"]?.jsonArray ?: emptyList()

        if (localDiaryEntries.size >= documents.size) {
            return localDiaryEntries
        }

        val serverDiaryEntries = documents.map { doc ->
            diaryEntryFromResponse(doc)
        }

        serverDiaryEntries.forEach {
            localSaveDiaryEntry(it)
        }

        return serverDiaryEntries
    }

    override suspend fun getEntriesLastSevenDays(): List<DiaryEntry> {
        val allEntries = getAllDiaryEntries()
        val sevenDaysAgo = localDateToday().minus(7, DateTimeUnit.DAY)
        return allEntries.filter { it.date >= sevenDaysAgo }
    }

    override suspend fun getDiaryEntryByDate(date: LocalDate): DiaryEntry {
        val savedDiaryEntry = diaryDatabaseQueries.getDiaryEntryByDate(date).executeAsOneOrNull()

        val isToday = localDateToday().minus(date).days < 2

        if (savedDiaryEntry != null && !isToday) {
            return savedDiaryEntry
        }

        val response = dataSource.firebaseFirestoreGet(
            child = getChild(),
            query = getEntryName(date),
            currentUser = firebase.getCurrentUser()!!
        )
        val jsonElement = Json.parseToJsonElement(response)

        if (savedDiaryEntry != null && fastGetUpdatedTime(jsonElement) < savedDiaryEntry.updatedDateTime) {
            return savedDiaryEntry
        }

        val serverDiaryEntry = diaryEntryFromResponse(jsonElement)

        localSaveDiaryEntry(serverDiaryEntry)

        return serverDiaryEntry
    }

    override suspend fun saveTodayEntry(
        id: String,
        components: List<DiaryEntryComponent>,
        date: LocalDate,
        updatedDateTime: LocalDateTime,
        createdDateTime: LocalDateTime
    ) {
        val diaryEntry = DiaryEntry(
            id = id,
            localId = firebase.getCurrentUser()!!.uid,
            date = date,
            components = components,
            createdDateTime = createdDateTime,
            updatedDateTime = updatedDateTime
        )

        localSaveDiaryEntry(diaryEntry)

        uploadDiaryEntry(diaryEntry)
    }

    override suspend fun forceSyncDiaryEntries() {
        val response = downloadDiaryEntriesResponse(forceSync = true)

        val jsonElement = Json.parseToJsonElement(response)
        val documents = jsonElement.jsonObject["documents"]?.jsonArray ?: emptyList()
        val serverDiaryEntries = documents.map { doc ->
            diaryEntryFromResponse(doc)
        }

        val localDiaryEntries = diaryDatabaseQueries.getAllDiaryEntries().executeAsList()

        if (serverDiaryEntries.isEmpty() && localDiaryEntries.isNotEmpty()) {
            for (localDiaryEntry in localDiaryEntries) {
                directUploadDiaryEntry(localDiaryEntry)
            }
            return
        }
        if (serverDiaryEntries.isNotEmpty() && localDiaryEntries.isEmpty()) {
            for (serverDiaryEntry in serverDiaryEntries) {
                localSaveDiaryEntry(serverDiaryEntry)
            }
            return
        }

        for (serverDiaryEntry in serverDiaryEntries) {
            localDiaryEntries.find { it.id == serverDiaryEntry.id }?.let { localDiaryEntry ->
                if (localDiaryEntry.updatedDateTime > serverDiaryEntry.updatedDateTime) {
                    directUploadDiaryEntry(localDiaryEntry)
                } else {
                    localSaveDiaryEntry(serverDiaryEntry)
                }
            }
        }
    }

    override suspend fun deleteLocal() {
        diaryDatabaseQueries.removeAllDiaryEntries()
    }

    private fun fastGetUpdatedTime(doc: JsonElement): LocalDateTime {
        val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }
        val updatedDateTime =
            requireNotNull(fields.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in document" }
        return LocalDateTime.parse(updatedDateTime)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun diaryEntryFromResponse(doc: JsonElement): DiaryEntry {
        val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }

        val id =
            requireNotNull(fields.jsonObject["id"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'id' in document" }
        val date =
            requireNotNull(fields.jsonObject["date"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'date' in document" }
        val componentsJson =
            requireNotNull(fields.jsonObject["components"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'components' in document" }

        val components = Json.decodeFromString(
            ArraySerializer(DiaryEntryComponent.serializer()),
            getDecryptedFromJson(
                componentsJson
            )
        ).toList()


        val createdDateTime =
            requireNotNull(fields.jsonObject["createdDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'createdDateTime' in document" }
        val updatedDateTime =
            requireNotNull(fields.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in document" }

        return DiaryEntry(
            id = id,
            localId = firebase.getCurrentUser()!!.uid,
            date = LocalDate.parse(date),
            components = components,
            createdDateTime = LocalDateTime.parse(createdDateTime),
            updatedDateTime = LocalDateTime.parse(updatedDateTime)
        )
    }

    private suspend fun downloadDiaryEntriesResponse(forceSync: Boolean = false): String {
        var response: String? = lastServiceResponseCache

        if (response == null || forceSync) {
            response = dataSource.firebaseFirestoreGet(
                child = getChild(),
                query = "",
                currentUser = firebase.getCurrentUser()!!
            )
            lastServiceResponseCache = response

            coroutineScope.launch {
                delay(timeMillis = 5_000)
                lastServiceResponseCache = null
            }
        }
        return response
    }

    private fun localSaveDiaryEntry(serverDiaryEntry: DiaryEntry) {
        diaryDatabaseQueries.insertDiaryEntry(
            id = serverDiaryEntry.id,
            localId = firebase.getCurrentUser()!!.uid,
            date = serverDiaryEntry.date,
            components = serverDiaryEntry.components,
            createdDateTime = serverDiaryEntry.createdDateTime,
            updatedDateTime = serverDiaryEntry.updatedDateTime
        )
    }

    private suspend fun uploadDiaryEntry(localDiaryEntry: DiaryEntry) {
        jobList[localDiaryEntry.id]?.cancelAndJoin()

        jobList[localDiaryEntry.id] = coroutineScope.launch {
            delay(timeMillis = 3_000)

            if (jobList[localDiaryEntry.id]?.isCancelled == true)
                return@launch

            directUploadDiaryEntry(localDiaryEntry)

            jobList.remove(localDiaryEntry.id)
        }
    }

    private suspend fun directUploadDiaryEntry(localDiaryEntry: DiaryEntry) {
        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(localDiaryEntry.date),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "id" to hashMapOf(
                        "stringValue" to getEntryName(localDiaryEntry.date)
                    ),
                    "date" to hashMapOf(
                        "stringValue" to localDiaryEntry.date.toString()
                    ),
                    "components" to hashMapOf(
                        "stringValue" to getEncryptedJson(
                            json = Json.encodeToString(localDiaryEntry.components)
                        )
                    ),
                    "createdDateTime" to hashMapOf(
                        "stringValue" to localDiaryEntry.createdDateTime.toString()
                    ),
                    "updatedDateTime" to hashMapOf(
                        "stringValue" to localDiaryEntry.updatedDateTime.toString()
                    )
                )
            ),
            currentUser = firebase.getCurrentUser()!!
        )
    }
}
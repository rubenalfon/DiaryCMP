package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.StatisticsOrder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface StatisticsOrderRepository {
    suspend fun getLatest(): StatisticsOrder
    suspend fun saveNew(statisticsOrder: List<String>, updatedDateTime: LocalDateTime)
    suspend fun forceSyncOrderRepository()
    suspend fun deleteLocal()
}

class StatisticsOrderRepositoryImpl(
    private val firebase: Firebase,
    private val dataSource: KtorDataSource,
    private val diaryDatabaseQueries: DiaryDatabaseQueries,
    override val userRepository: UserRepository
) : GenericRepository, StatisticsOrderRepository {

    override fun getChild() = listOf(
        "users"
    )

    override fun getEntryName(data: Any?) = firebase.getCurrentUser()!!.uid

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val jobList: MutableMap<String, Job> = mutableMapOf()
    private var lastServiceResponseCache: String? = null


    override suspend fun getLatest(): StatisticsOrder {
        val localStatisticsOrder =
            diaryDatabaseQueries.getStatisticsOrder().executeAsOneOrNull()

        val response = downloadStatisticsOrderResponse()
        val jsonElement = Json.parseToJsonElement(response)

        if (localStatisticsOrder != null && fastGetUpdatedTime(jsonElement) < localStatisticsOrder.updatedDateTime) {
            return localStatisticsOrder
        } else {
            localSaveStatisticsOrder(statisticsOrderFromResponse(jsonElement))
            return diaryDatabaseQueries.getStatisticsOrder().executeAsOne()
        }
    }

    override suspend fun saveNew(statisticsOrder: List<String>, updatedDateTime: LocalDateTime) {
        @Suppress("NAME_SHADOWING") val statisticsOrder = StatisticsOrder(
            localId = firebase.getCurrentUser()!!.uid,
            statisticsOrder = statisticsOrder,
            updatedDateTime = updatedDateTime
        )

        localSaveStatisticsOrder(statisticsOrder)

        uploadStatisticsOrder(statisticsOrder)
    }

    override suspend fun forceSyncOrderRepository() {
        val response = downloadStatisticsOrderResponse()

        val jsonElement = Json.parseToJsonElement(response)
        val serverStatisticsOrder: StatisticsOrder? = null

        try {
            statisticsOrderFromResponse(jsonElement)
        } catch (ex: Exception) {
            Napier.e { "DiaryOrderRepositoryImpl.forceSyncDiaryEntries: $ex" }
        }

        val localStatisticsOrder = diaryDatabaseQueries.getStatisticsOrder().executeAsOneOrNull()

        if (serverStatisticsOrder == null && localStatisticsOrder != null) {
            directUploadStatisticsOrder(localStatisticsOrder)
            return
        }
        if (serverStatisticsOrder != null && localStatisticsOrder == null) {
            localSaveStatisticsOrder(serverStatisticsOrder)
            return
        }
    }

    override suspend fun deleteLocal() {
        diaryDatabaseQueries.removeAllStatisticsOrders()
    }

    private fun fastGetUpdatedTime(doc: JsonElement): LocalDateTime {
        val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }

        val diaryEntryOrderObject =
            requireNotNull(fields.jsonObject["statisticsOrder"]?.jsonObject?.get("mapValue")) { "Missing 'statisticsOrder' in response" }

        val mapValue =
            requireNotNull(diaryEntryOrderObject.jsonObject["fields"]) { "Missing 'fields' in statisticsOrder" }

        val updatedDateTime =
            requireNotNull(mapValue.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in response" }

        return Json.decodeFromString(updatedDateTime)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun statisticsOrderFromResponse(doc: JsonElement): StatisticsOrder {
        val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }

        val statisticsOrderObject =
            requireNotNull(fields.jsonObject["statisticsOrder"]?.jsonObject?.get("mapValue")) { "Missing 'statisticsOrder' in response" }

        val mapValue =
            requireNotNull(statisticsOrderObject.jsonObject["fields"]) { "Missing 'fields' in statisticsOrder" }

        val encryptedComponents =
            requireNotNull(mapValue.jsonObject["encryptedComponents"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'encryptedComponents' in fields" }

        val updatedDateTime =
            requireNotNull(mapValue.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in fields" }

        val components = Json.decodeFromString(
            string = getDecryptedFromJson(encryptedComponents),
            deserializer = ArraySerializer(String.serializer())
        ).toList()

        return StatisticsOrder(
            localId = firebase.getCurrentUser()!!.uid,
            statisticsOrder = components,
            updatedDateTime = Json.decodeFromString(updatedDateTime)
        )
    }

    private suspend fun downloadStatisticsOrderResponse(forceSync: Boolean = false): String {
        var response = lastServiceResponseCache

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

        return response!!
    }

    private fun localSaveStatisticsOrder(serverStatisticsOrder: StatisticsOrder) {
        diaryDatabaseQueries.removeAllStatisticsOrders()
        diaryDatabaseQueries.insertStatisticsOrder(
            localId = firebase.getCurrentUser()!!.uid,
            statisticsOrder = serverStatisticsOrder.statisticsOrder,
            updatedDateTime = serverStatisticsOrder.updatedDateTime
        )
    }

    private suspend fun uploadStatisticsOrder(statisticsOrder: StatisticsOrder) {
        jobList["statisticsOrder"]?.cancelAndJoin()

        jobList["statisticsOrder"] = coroutineScope.launch {
            delay(timeMillis = 3_000)

            if (jobList["statisticsOrder"]?.isCancelled == true)
                return@launch

            directUploadStatisticsOrder(statisticsOrder)

            jobList.remove("statisticsOrder")
        }
    }

    private suspend fun directUploadStatisticsOrder(statisticsOrder: StatisticsOrder) {
        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "statisticsOrder" to hashMapOf(
                        "mapValue" to hashMapOf(
                            "fields" to hashMapOf(
                                "encryptedComponents" to hashMapOf(
                                    "stringValue" to getEncryptedJson(
                                        json = Json.encodeToString(statisticsOrder.statisticsOrder)
                                    )
                                ),
                                "updatedDateTime" to hashMapOf(
                                    "stringValue" to Json.encodeToString(statisticsOrder.updatedDateTime)
                                )
                            )
                        )
                    )
                )
            ),
            currentUser = firebase.getCurrentUser()!!
        )
    }
}

package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.ktorModule.datasource.DocumentNotFoundException
import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelight.db.DiaryEntryOrder
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface DiaryOrderRepository {
    suspend fun getLatest(onlyLocal: Boolean = false): DiaryEntryOrder?
    suspend fun saveNew(diaryEntryOrder:  List<DiaryEntryComponent>, updatedDateTime: LocalDateTime)
    suspend fun forceSyncDiaryEntryOrder()
    suspend fun deleteLocal()
}

class DiaryOrderRepositoryImpl(
    private val firebase: Firebase,
    private val dataSource: KtorDataSource,
    private val diaryDatabaseQueries: DiaryDatabaseQueries,
    override val userRepository: UserRepository
) : GenericRepository, DiaryOrderRepository {
    override fun getChild() = listOf(
        "users"
    )

    override fun getEntryName(data: Any?): String = firebase.getCurrentUser()!!.uid

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val jobList: MutableMap<String, Job> = mutableMapOf()
    private var lastServiceResponseCache: String? = null


    override suspend fun getLatest(onlyLocal: Boolean): DiaryEntryOrder? {
        val localDiaryEntryOrder =
            diaryDatabaseQueries.getDiaryEntryOrder().executeAsOneOrNull()

        if (onlyLocal) return localDiaryEntryOrder

        val response = downloadDiaryEntryOrderResponse()
        val jsonElement = Json.parseToJsonElement(response)

        if (localDiaryEntryOrder != null && fastGetUpdatedTime(jsonElement) < localDiaryEntryOrder.updatedDateTime) {
            return localDiaryEntryOrder
        } else {
            try {
                localSaveDiaryEntryOrder(diaryEntryOrderFromResponse(jsonElement))
            } catch (ex: Exception) {
                throw if (ex.message?.contains("Missing 'diaryEntryOrder' in response") == true)
                    DocumentNotFoundException(message = ex.message!!)
                else ex
            }

            return diaryDatabaseQueries.getDiaryEntryOrder().executeAsOne()
        }
    }

    override suspend fun saveNew(
        diaryEntryOrder: List<DiaryEntryComponent>,
        updatedDateTime: LocalDateTime
    ) {
        @Suppress("NAME_SHADOWING") val diaryEntryOrder = DiaryEntryOrder(
            localId = firebase.getCurrentUser()!!.uid,
            diaryEntryOrder = diaryEntryOrder,
            updatedDateTime = updatedDateTime
        )

        localSaveDiaryEntryOrder(diaryEntryOrder)

        uploadDiaryEntryOrder(diaryEntryOrder)
    }

    override suspend fun forceSyncDiaryEntryOrder() {
        val response = downloadDiaryEntryOrderResponse(forceSync = true)

        val jsonElement = Json.parseToJsonElement(response)
        val serverDiaryEntryOrder: DiaryEntryOrder? = null

        try {
            diaryEntryOrderFromResponse(jsonElement)
        } catch (ex: Exception) {
            Napier.e { "DiaryOrderRepositoryImpl.forceSyncDiaryEntries: $ex" }
        }

        val localDiaryOrder = diaryDatabaseQueries.getDiaryEntryOrder().executeAsOneOrNull()

        if (serverDiaryEntryOrder == null && localDiaryOrder != null) {
            directUploadDiaryEntryOrder(localDiaryOrder)
            return
        }
        if (serverDiaryEntryOrder != null && localDiaryOrder == null) {
            localSaveDiaryEntryOrder(serverDiaryEntryOrder)
            return
        }
    }

    override suspend fun deleteLocal() {
        diaryDatabaseQueries.removeAllDiaryEntryOrders()
    }


    private fun fastGetUpdatedTime(doc: JsonElement): LocalDateTime {
        try {
            val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }

            val diaryEntryOrderObject =
                requireNotNull(fields.jsonObject["diaryEntryOrder"]?.jsonObject?.get("mapValue")) { "Missing 'diaryEntryOrder' in response" }

            val mapValue =
                requireNotNull(diaryEntryOrderObject.jsonObject["fields"]) { "Missing 'fields' in diaryEntryOrder" }

            val updatedDateTime =
                requireNotNull(mapValue.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in fields" }

            return Json.decodeFromString(updatedDateTime)
        } catch (ex: Exception) {
            if (ex.message?.contains("Missing 'diaryEntryOrder' in response") == true)
                throw DocumentNotFoundException(message = ex.message!!)
            throw ex
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun diaryEntryOrderFromResponse(doc: JsonElement): DiaryEntryOrder {
        val fields = requireNotNull(doc.jsonObject["fields"]) { "Missing 'fields' in document" }

        val diaryEntryOrderObject =
            requireNotNull(fields.jsonObject["diaryEntryOrder"]?.jsonObject?.get("mapValue")) { "Missing 'diaryEntryOrder' in response" }

        val mapValue =
            requireNotNull(diaryEntryOrderObject.jsonObject["fields"]) { "Missing 'fields' in diaryEntryOrder" }

        val encryptedComponents =
            requireNotNull(mapValue.jsonObject["encryptedComponents"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'encryptedComponents' in fields" }

        val updatedDateTime =
            requireNotNull(mapValue.jsonObject["updatedDateTime"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content) { "Missing 'updatedDateTime' in fields" }

        val components = Json.decodeFromString(
            ArraySerializer(DiaryEntryComponent.serializer()),
            getDecryptedFromJson(encryptedComponents)
        ).toList()

        return DiaryEntryOrder(
            localId = firebase.getCurrentUser()!!.uid,
            diaryEntryOrder = components,
            updatedDateTime = Json.decodeFromString(updatedDateTime)
        )
    }

    private suspend fun downloadDiaryEntryOrderResponse(forceSync: Boolean = false): String {
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

    private fun localSaveDiaryEntryOrder(serverDiaryEntryOrder: DiaryEntryOrder) {
        diaryDatabaseQueries.removeAllDiaryEntryOrders()
        diaryDatabaseQueries.insertDiaryEntryOrder(
            localId = firebase.getCurrentUser()!!.uid,
            diaryEntryOrder = serverDiaryEntryOrder.diaryEntryOrder,
            updatedDateTime = serverDiaryEntryOrder.updatedDateTime
        )
    }

    private suspend fun uploadDiaryEntryOrder(diaryEntryOrder: DiaryEntryOrder) {
        jobList["diaryEntryOrder"]?.cancelAndJoin()

        jobList["diaryEntryOrder"] = coroutineScope.launch {
            delay(timeMillis = 3_000)

            if (jobList["diaryEntryOrder"]?.isCancelled == true)
                return@launch

            directUploadDiaryEntryOrder(diaryEntryOrder)

            jobList.remove("diaryEntryOrder")
        }
    }

    private suspend fun directUploadDiaryEntryOrder(diaryEntryOrder: DiaryEntryOrder) {
        dataSource.firebaseFirestorePatch(
            child = getChild() + getEntryName(),
            parameter = hashMapOf(
                "fields" to hashMapOf(
                    "diaryEntryOrder" to hashMapOf(
                        "mapValue" to hashMapOf(
                            "fields" to hashMapOf(
                                "encryptedComponents" to hashMapOf(
                                    "stringValue" to getEncryptedJson(
                                        json = Json.encodeToString(diaryEntryOrder.diaryEntryOrder)
                                    )
                                ),
                                "updatedDateTime" to hashMapOf(
                                    "stringValue" to Json.encodeToString(diaryEntryOrder.updatedDateTime)
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
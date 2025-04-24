package es.diaryCMP.modelsModule.models

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data class StatisticsHelper(
    val componentId: String, // use as its own id
    val componentTitle: String,
    val componentType: KClass<out DiaryEntryComponent>?,
    val componentValues: List<Int?>?, // Convert everything to int as Any? is not valid
    val canBeArchived: Boolean = true
)
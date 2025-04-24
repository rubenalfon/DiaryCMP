package es.diaryCMP.modelsModule.models


import kotlinx.serialization.Serializable

@Serializable
sealed interface DiaryEntryComponent {
    val id: String
    var title: String
    var canBeDeleted: Boolean
}

@Serializable
data class ShortTextDiaryComponent(
    override val id: String,
    override var title: String,
    override var canBeDeleted: Boolean = true,
    var text: String,
    var textSize: Int = 16
) : DiaryEntryComponent

@Serializable
data class LongTextDiaryComponent(
    override val id: String,
    override var title: String,
    override var canBeDeleted: Boolean = true,
    var text: String,
    var textSize: Int = 16
) : DiaryEntryComponent

@Serializable
data class BooleanDiaryComponent(
    override val id: String,
    override var title: String,
    override var canBeDeleted: Boolean = true,
    var isChecked: Boolean? = null
) : DiaryEntryComponent

@Serializable
data class FiveOptionDiaryComponent(
    override val id: String,
    override var title: String,
    override var canBeDeleted: Boolean = true,
    var index: Int? = null
) : DiaryEntryComponent


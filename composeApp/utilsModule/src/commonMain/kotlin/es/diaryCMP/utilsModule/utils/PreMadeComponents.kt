package es.diaryCMP.utilsModule.utils

import com.benasher44.uuid.uuid4
import diary.composeapp.utilsmodule.generated.resources.*
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import org.jetbrains.compose.resources.*

suspend fun getAllExampleComponents(): List<DiaryEntryComponent> {
    return listOf(
        ShortTextDiaryComponent(
            id = uuid4().toString(),
            title = getString(Res.string.short_component_title),
            text = getString(Res.string.short_component_text_example)
        ), LongTextDiaryComponent(
            id = uuid4().toString(),
            title = getString(Res.string.long_component_title),
            text = getString(Res.string.long_component_text_example)
        ), BooleanDiaryComponent(
            id = uuid4().toString(),
            title = getString(Res.string.yes_no_component_title),
            isChecked = true
        ), FiveOptionDiaryComponent(
            id = uuid4().toString(),
            title = getString(Res.string.five_option_component_title),
            index = 4
        )
    )
}

suspend fun getStartingComponents(): List<DiaryEntryComponent> {
    return listOf(
        ShortTextDiaryComponent(
            id = "01",
            title = getString(Res.string.title_example_component_title),
            text = getString(Res.string.title_example_component_body),
            textSize = 28,
            canBeDeleted = false
        ), LongTextDiaryComponent(
            id = "02",
            title = getString(Res.string.summary_example_component_title),
            text = getString(Res.string.summary_example_component_body),
            canBeDeleted = true
        ), FiveOptionDiaryComponent(
            id = "03",
            title = getString(Res.string.happiness),
            index = 4,
            canBeDeleted = true
        )
    )
}
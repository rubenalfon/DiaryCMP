package es.diaryCMP.utilsModule.utils.statistics

import androidx.compose.runtime.Composable
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.today
import diary.composeapp.utilsmodule.generated.resources.yesterday
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource


@Composable
@OptIn(FormatStringsInDatetimeFormats::class)
fun getDaysOfWeekSevenLatest(): List<String> {
    val sixDaysBeforeToday = localDateToday().minus(6, DAY)

    val latestSevenEntriesIds = (0..4).map { day ->
        sixDaysBeforeToday.plus(day.toLong(), DAY).format(LocalDate.Format {
            byUnicodePattern("dd/MM")
        })
    }

    val daysOfWeek = latestSevenEntriesIds + listOf(
        stringResource(Res.string.yesterday),
        stringResource(Res.string.today)
    )
    return daysOfWeek
}
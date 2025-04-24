package es.diaryCMP.utilsModule.utils.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

object DateUtil {
    val daysOfWeek: Array<String>
        get() {
//            val format = LocalDate.Format {
//                date(LocalDate.Formats.ISO)
//                chars(", ")
//                dayOfWeek(DayOfWeekNames.ENGLISH_FULL) // "Mon", "Tue", ...
//            }

            val daysOfWeek = arrayOf("M", "T", "W", "T", "F", "S", "S")

//            TODO: Change this when kotlinx-datetime is updated
//            for (dayOfWeek in DayOfWeek.entries) {
//                val localizedDayName =
//                    dayOfWeek.name.subSequence(0, 1).toString()
//                daysOfWeek[dayOfWeek.ordinal] = localizedDayName
//            }
            return daysOfWeek
        }
}

fun YearMonth.getDayOfMonthStartingFromMonday(): List<LocalDate> {
    val firstDayOfMonth = LocalDate(year = yearValue, monthNumber = monthValue, dayOfMonth = 1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek

    val daysToAdd = if (dayOfWeek == DayOfWeek.MONDAY) {
        0
    } else {
        (DayOfWeek.MONDAY.ordinal - dayOfWeek.ordinal + 7) % 7 - 7
    }

    val firstMondayOfMonth = firstDayOfMonth.plus(daysToAdd.toLong(), DateTimeUnit.DAY)
    val firstDayOfNextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)

    return generateSequence(firstMondayOfMonth) { it.plus(1, DateTimeUnit.DAY) }
        .takeWhile { it < firstDayOfNextMonth }
        .toList()
}

fun YearMonth.getDisplayName(): String {
    return "${getMonthName()} $yearValue"
}
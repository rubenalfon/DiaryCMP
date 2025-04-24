package es.calendarMultiplatform.data

import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import es.diaryCMP.utilsModule.utils.calendar.getDayOfMonthStartingFromMonday
import es.diaryCMP.utilsModule.utils.calendar.localDateToday

object CalendarDataSource {
    fun getDates(yearMonth: YearMonth): List<CalendarMonthState.Date> {
        return yearMonth.getDayOfMonthStartingFromMonday()
            .map { date ->
                CalendarMonthState.Date(
                    dayOfMonth = if (date.monthNumber == yearMonth.monthValue) {
                        "${date.dayOfMonth}"
                    } else {
                        "" // Fill with empty string for days outside the current month
                    },
                    isToday = date == localDateToday() && date.monthNumber == yearMonth.monthValue
                )
            }
    }
}

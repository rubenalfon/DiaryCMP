package es.calendarMultiplatform.data

import es.diaryCMP.utilsModule.utils.calendar.YearMonth

data class CalendarUiState(
    val calendarMonths: List<CalendarMonthState>, // 3 months
    val pagerOffset: Int,
    val lastSelectedPage: Int? = null
)

data class CalendarMonthState(
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    data class Date(
        val dayOfMonth: String,
        val isToday: Boolean
    ) {
        companion object {
            val Empty = Date("", false)
        }
    }
}
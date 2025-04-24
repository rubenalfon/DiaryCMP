package es.diaryCMP.utilsModule.utils.calendar

import kotlinx.datetime.LocalDate

class YearMonth(
    var monthValue: Int,
    var yearValue: Int
) {
    fun getMonthName(): String {
        return LocalDate(year = yearValue, monthNumber = monthValue, dayOfMonth = 1).month.name.lowercase()
    }

    fun minusOneMonth(): YearMonth {
        return if (monthValue ==  1) {
            YearMonth(monthValue = 12, yearValue = yearValue - 1)
        } else {
            YearMonth(monthValue = monthValue - 1, yearValue = yearValue)
        }
    }

    fun plusOneMonth(): YearMonth {
        return if (monthValue == 12) {
            YearMonth(monthValue = 1, yearValue = yearValue + 1)
        } else {
            YearMonth(monthValue = monthValue + 1, yearValue = yearValue)
        }
    }

    override fun toString(): String {
        return "YearMonth(monthValue=$monthValue, yearValue=$yearValue)"
    }

    companion object {
        fun now(): YearMonth {
            val nowLocalDate = localDateToday()
            return YearMonth(yearValue = nowLocalDate.year, monthValue = nowLocalDate.monthNumber)
        }
    }
}

package es.diaryCMP.utilsModule.utils.calendar

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


fun localDateToday(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
package es.diaryCMP.diModule.di

import app.cash.sqldelight.ColumnAdapter
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.sqlDelight.db.DiaryEntryOrder
import es.diaryCMP.sqlDelight.db.StatisticsOrder
import es.diaryCMP.sqlDelight.db.User
import es.diaryCMP.sqlDelight.db.UserSettings
import es.diaryCMP.utilsModule.utils.calendar.localDateTimeNow
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val databaseAdaptersModule = module {
    single<UserSettings.Adapter> {
        UserSettings.Adapter(
            notificationTimeAdapter = get(named("LocalTimeStringAdapter")),
            endDayHourAdapter = get(named("LocalTimeStringAdapter"))
        )
    }

    single<User.Adapter> {
        User.Adapter(
            encryptionKeyAdapter = get(named("ByteArrayStringAdapter"))
        )
    }

    single<DiaryEntry.Adapter> {
        DiaryEntry.Adapter(
            dateAdapter = get(named("LocalDateStringAdapter")),
            componentsAdapter = get(named("DiaryEntryComponentListAdapter")),
            createdDateTimeAdapter = get(named("LocalDateTimeStringAdapter")),
            updatedDateTimeAdapter = get(named("LocalDateTimeStringAdapter"))
        )
    }

    single<DiaryEntryOrder.Adapter> {
        DiaryEntryOrder.Adapter(
            diaryEntryOrderAdapter = get(named("DiaryEntryComponentListAdapter")),
            updatedDateTimeAdapter = get(named("LocalDateTimeStringAdapter"))
        )
    }

    single<StatisticsOrder.Adapter> {
        StatisticsOrder.Adapter(
            statisticsOrderAdapter = get(named("StringListAdapter")),
            updatedDateTimeAdapter = get(named("LocalDateTimeStringAdapter"))
        )

    }

    single<ColumnAdapter<LocalDateTime, String>>(named("LocalDateTimeStringAdapter")) {
        object : ColumnAdapter<LocalDateTime, String> {
            override fun decode(databaseValue: String): LocalDateTime =
                if (databaseValue.isEmpty()) {
                    localDateTimeNow()
                } else {
                    LocalDateTime.parse(databaseValue)
                }

            override fun encode(value: LocalDateTime): String {
                return value.toString()
            }
        }
    }

    single<ColumnAdapter<LocalDate, String>>(named("LocalDateStringAdapter")) {
        object : ColumnAdapter<LocalDate, String> {
            override fun decode(databaseValue: String): LocalDate =
                if (databaseValue.isEmpty()) {
                    localDateToday()
                } else {
                    LocalDate.parse(databaseValue)
                }

            override fun encode(value: LocalDate): String {
                return value.toString()
            }
        }
    }

    single<ColumnAdapter<LocalTime, String>>(named("LocalTimeStringAdapter")) {
        object : ColumnAdapter<LocalTime, String> {
            override fun decode(databaseValue: String): LocalTime =
                if (databaseValue.isEmpty()) {
                    LocalTime.fromSecondOfDay(0)
                } else {
                    LocalTime.parse(databaseValue)
                }

            override fun encode(value: LocalTime): String {
                return value.toString()
            }
        }
    }

    single<ColumnAdapter<ByteArray, String>>(named("ByteArrayStringAdapter")) {
        object : ColumnAdapter<ByteArray, String> {
            override fun decode(databaseValue: String): ByteArray {
                return Json.decodeFromString(databaseValue)
            }

            override fun encode(value: ByteArray): String {
                return Json.encodeToString(value)
            }
        }
    }

    single<ColumnAdapter<List<DiaryEntryComponent>, String>>(named("DiaryEntryComponentListAdapter")) {
        object : ColumnAdapter<List<DiaryEntryComponent>, String> {
            override fun decode(databaseValue: String): List<DiaryEntryComponent> {
                return Json.decodeFromString(databaseValue)
            }

            override fun encode(value: List<DiaryEntryComponent>): String {
                return Json.encodeToString(value)
            }
        }
    }

    single<ColumnAdapter<List<String>, String>>(named("StringListAdapter")) {
        object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String): List<String> {
                return Json.decodeFromString(databaseValue)
            }
            override fun encode(value: List<String>): String {
                return Json.encodeToString(value)
            }
        }
    }
}
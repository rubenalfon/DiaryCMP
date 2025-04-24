package es.diaryCMP.diModule.di

import es.diaryCMP.ktorModule.datasource.KtorDataSource
import es.diaryCMP.sqlDelight.db.DiaryDatabaseQueries
import es.diaryCMP.sqlDelightModule.db.MyDatabase
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataSourceModule = module {
    single<HttpClient> {
        HttpClient {
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v(tag = "HTTP Client", message = message)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 1)
                constantDelay(millis = 1000)
            }

        }.also { get(named("NapierLogger")) }
    }

    single<KtorDataSource> {
        KtorDataSource(
            httpClient = get(),
            appConstants = get()
        )
    }

    single<DiaryDatabaseQueries> {
        get<MyDatabase>().diaryDatabaseQueries
    }
}


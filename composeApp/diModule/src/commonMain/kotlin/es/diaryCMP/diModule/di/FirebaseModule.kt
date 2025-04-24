package es.diaryCMP.diModule.di

import es.diaryCMP.firebaseModule.firebase.Firebase
import es.diaryCMP.utilsModule.utils.FirebaseConstants
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseConstants }

    single<Firebase> {
        Firebase(databaseQueries = get(), ktorDataSource = get())
    }
}

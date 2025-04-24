package es.diaryCMP.diModule.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.qualifier.named
import org.koin.dsl.module

val NapierModule = module {
    single(named("NapierLogger")) {
        Napier.base(DebugAntilog())
    }
}
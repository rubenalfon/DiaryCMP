package es.diaryCMP.diModule.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null, platformSpecificModule: Module = module {}) {
    startKoin {
        config?.invoke(this)
        modules(
            platformSpecificModule,
            databaseAdaptersModule,
            platformModule,
            firebaseModule,
            NapierModule,
            dataSourceModule,
            repositoryModule,
            viewModelsModule
        )
    }
}
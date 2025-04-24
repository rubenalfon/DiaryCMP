import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                    freeCompilerArgs.add("-Xjdk-release=${JavaVersion.VERSION_11}")
                }
            }
        }
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                debugImplementation(libs.androidx.testManifest)
                implementation(libs.androidx.junit4)
            }
        }
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "diModule"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)

            // Napier
            implementation(libs.napier)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Ktor
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // DateTime
            implementation(libs.kotlinx.datetime)


            implementation(project(":composeApp:repositoriesModule"))
            implementation(project(":composeApp:utilsModule"))
            implementation(project(":composeApp:sqlDelightModule"))
            implementation(project(":composeApp:modelsModule"))
            implementation(project(":composeApp:firebaseModule"))
            implementation(project(":composeApp:ktorModule"))
            implementation(project(":composeApp:viewmodelsModule"))

            implementation(project(":CalendarMultiplatform"))

            implementation(project(":kotlinMultiplatformMobileLocalNotifier"))
        }

        androidMain.dependencies {
            // Koin
            implementation(libs.koin.android)

            // Ktor
            implementation(libs.ktor.client.okhttp)

            // SqlDelight
            implementation(libs.sqlDelight.driver.android)
        }

        jvmMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.okhttp)

            // SqlDelight
            implementation(libs.sqlDelight.driver.sqlite)
        }

        iosMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.ios)
            implementation(libs.ktor.client.darwin)

            // SqlDelight
            implementation(libs.sqlDelight.driver.native)
        }
    }
}

android {
    namespace = "es.diaryCMP.diModule"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

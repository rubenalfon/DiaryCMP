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
            baseName = "viewmodelsModule"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.components.resources)

            // Napier
            implementation(libs.napier)

            // UUID
            implementation(libs.uuid)

            //Serialization
            implementation(libs.ktor.serialization.kotlinx.json)

            // Ktor Exception
            implementation(libs.ktor.core)

            // Kotlinx Datetime
            implementation(libs.kotlinx.datetime)

            // ViewModel
            implementation(libs.koin.compose.viewmodel)

            // PreCompose (navigation)
            api(libs.navigation.precompose)

            implementation(project(":composeApp:ktorModule")) // For exceptions
            implementation(project(":composeApp:modelsModule"))
            implementation(project(":composeApp:sqlDelightModule"))
            implementation(project(":composeApp:repositoriesModule"))
            implementation(project(":composeApp:firebaseModule"))
            implementation(project(":composeApp:utilsModule"))

            implementation(project(":kotlinMultiplatformMobileLocalNotifier"))
        }
    }
}

android {
    namespace = "es.diaryCMP.viewmodelsModule"
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

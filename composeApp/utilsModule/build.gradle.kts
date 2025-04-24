import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.android.library)
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
            baseName = "utilsModule"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)

            // Napier
            implementation(libs.napier)

            // WindowSizeClass
            implementation(libs.chrisbanes.window.size)

            // UUID
            implementation(libs.uuid)

            // Kotlinx Datetime
            implementation(libs.kotlinx.datetime)

            // Navigator
            implementation(libs.navigation.precompose)

            implementation("com.ionspin.kotlin:multiplatform-crypto-libsodium-bindings:0.9.2")

            val core = "0.5.3"
            implementation("org.kotlincrypto.core:digest:$core")
            implementation("org.kotlincrypto.core:mac:$core")
            implementation("org.kotlincrypto.core:xof:$core")


            implementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.5.3"))
            implementation("org.kotlincrypto.hash:sha3")

            implementation(project.dependencies.platform("org.kotlincrypto.macs:bom:0.5.3"))
            implementation("org.kotlincrypto.macs:hmac-sha2")
            implementation("org.kotlincrypto.macs:hmac-sha3")

            implementation("org.kotlincrypto:secure-random:0.3.2")
            implementation("org.kotlincrypto.sponges:keccak:0.3.1")

            implementation(project(":composeApp:modelsModule"))
        }

        androidMain.dependencies { }

        iosMain.dependencies { }

        jvmMain.dependencies { }
    }
}

android {
    namespace = "es.diaryCMP.utilsModule"
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

compose.resources {
    publicResClass = true
}

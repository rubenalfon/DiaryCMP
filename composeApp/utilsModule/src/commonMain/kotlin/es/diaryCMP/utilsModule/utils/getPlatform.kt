package es.diaryCMP.utilsModule.utils

enum class Platform {
    ANDROID, IOS, DESKTOP // add web when supported
}

expect fun getPlatform(): Platform


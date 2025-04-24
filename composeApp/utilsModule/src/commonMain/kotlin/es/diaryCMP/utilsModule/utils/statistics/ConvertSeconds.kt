package es.diaryCMP.utilsModule.utils.statistics

import androidx.compose.runtime.Composable
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.short_hours
import diary.composeapp.utilsmodule.generated.resources.short_minutes
import diary.composeapp.utilsmodule.generated.resources.short_seconds
import org.jetbrains.compose.resources.stringResource


@Composable
fun convertSeconds(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return (
            if (hours != 0)
                "${stringResource(Res.string.short_hours, hours)}, "
            else ""
            ) + (
            if (minutes != 0)
                "${stringResource(Res.string.short_minutes, minutes)}, "
            else ""
            ) + stringResource(Res.string.short_seconds, seconds)
}
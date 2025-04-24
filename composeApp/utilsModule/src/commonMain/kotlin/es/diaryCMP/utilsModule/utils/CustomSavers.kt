package es.diaryCMP.utilsModule.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import moe.tlaster.precompose.navigation.Navigator

val TextFieldValueSaver = Saver<TextFieldValue, String>(
    save = { it.text },
    restore = { TextFieldValue(it, TextRange(it.length)) }
)

val NavigatorSaver = Saver<Navigator, String>(
    save = { it.currentEntry.toString() },
    restore = { Navigator().apply { navigate(it) } }
)

val YearMonthSaver = Saver<YearMonth?, String>(
    save = { if (it == null) "" else "${it.yearValue},${it.monthValue}" },
    restore = { if (it == "") null else YearMonth(it.split(",")[0].toInt(), it.split(",")[1].toInt()) }
)

val DpSaver = Saver<Dp, String>(
    save = { it.value.toString() },
    restore = { it.toFloat().dp }
)

val DpSizeSaver = Saver<DpSize, String>(
    save = { "${it.width.value},${it.height.value}" },
    restore = { DpSize(it.split(",")[0].toFloat().dp, it.split(",")[1].toFloat().dp) }
)
package es.diaryCMP.utilsModule.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Move
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Press
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.keyboardDismissOnSwipeIOS(
    onSwipe: () -> Unit
): Modifier {
    if (getPlatform() != Platform.IOS) {
        return this
    }

    return this.pointerInput(Unit) {
        var initialPosition by mutableStateOf(0f)


        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()

                for (changes in event.changes) {
                    if (event.type == Press) {
                        initialPosition = changes.position.y

                    } else if (event.type == Move) {
                        val positionNow = changes.position.y

                        if (positionNow - initialPosition > 200) {
                            onSwipe()
                        }
                    }
                }
            }
        }
    }
}
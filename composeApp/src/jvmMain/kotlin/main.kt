import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.diary_cmp
import diary.composeapp.utilsmodule.generated.resources.diary_icon
import es.diaryCMP.App
import es.diaryCMP.diModule.di.initKoin
import moe.tlaster.precompose.ProvidePreComposeLocals
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension

fun main() {
    initKoin()
    application {
        Window(
            title = stringResource(Res.string.diary_cmp),
            state = rememberWindowState(width = 900.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
            icon = painterResource(Res.drawable.diary_icon)
        ) {
            window.minimumSize = Dimension(350, 600)
            ProvidePreComposeLocals {
                App()
            }
        }
    }
}
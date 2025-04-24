import androidx.compose.ui.window.ComposeUIViewController
import es.diaryCMP.App
import es.diaryCMP.diModule.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
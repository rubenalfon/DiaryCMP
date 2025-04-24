package es.diaryCMP

import androidx.compose.runtime.Composable
import com.diaryCMP.uimodule.AppContent
import com.diaryCMP.uimodule.ui.theme.AppTheme
import moe.tlaster.precompose.PreComposeApp
import org.koin.compose.KoinContext

@Composable
internal fun App() {
    AppTheme {
        KoinContext {
            PreComposeApp {
                AppContent()
            }
        }
    }
}
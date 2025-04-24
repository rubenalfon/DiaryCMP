package es.diaryCMP.modelsModule.models

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val itemRoute: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)
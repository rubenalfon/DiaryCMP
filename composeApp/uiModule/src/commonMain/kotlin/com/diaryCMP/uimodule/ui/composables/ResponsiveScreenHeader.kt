package com.diaryCMP.uimodule.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.diaryCMP.uimodule.ui.theme.compactScreenClassHeaderPadding
import com.diaryCMP.uimodule.ui.theme.defaultIconButtonContainerPadding
import com.diaryCMP.uimodule.ui.theme.extraSmallPadding
import com.diaryCMP.uimodule.ui.theme.largeScreenClassHeaderPadding
import com.diaryCMP.uimodule.ui.theme.mediumScreenClassHeaderPadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.return_
import es.diaryCMP.utilsModule.utils.Platform
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getPlatform
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.GlobalViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ResponsiveScreenHeader(
    modifier: Modifier = Modifier,
    title: String,
    popBackStack: (() -> Unit)? = null,
    titleOfParent: String? = null,
    trailingButtons: @Composable () -> Unit = {
        IconButton(
            enabled = false,
            onClick = {}
        ) {}
    },
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    globalViewModel: GlobalViewModel = koinViewModel<GlobalViewModel>()
) {
    val padding: Dp =
        when (getScreenClass()) {
            ScreenClass.Compact -> compactScreenClassHeaderPadding
            ScreenClass.Medium -> mediumScreenClassHeaderPadding
            ScreenClass.Large -> largeScreenClassHeaderPadding
        }

    val fullSize by globalViewModel.screenSize.collectAsState()
    val navigationComposableSize by globalViewModel.navigationComposableSize.collectAsState()

    val alignmentOffset =
        fullSize.width / 2 - (padding + if (getScreenClass() != ScreenClass.Compact) navigationComposableSize.width else noPadding)

    Row(
        modifier = modifier
            .background(containerColor)
            .padding(
                start = if (popBackStack == null) padding else padding - defaultIconButtonContainerPadding,
                end = max(noPadding, padding - defaultIconButtonContainerPadding)
            )
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val density = LocalDensity.current.density
        var buttonsByPlatformWidth by remember { mutableStateOf(noPadding) }
        if (popBackStack != null) {
            ButtonsByPlatform(
                titleOfParent = titleOfParent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                popBackStack = popBackStack,
                modifier = Modifier.onGloballyPositioned {
                    buttonsByPlatformWidth = Dp(it.size.width / density)
                })
        }

        val responsiveTitleStyle =
            if (getScreenClass() == ScreenClass.Compact) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleLarge
        val titleWidth = measureTextWidth(title, responsiveTitleStyle)
        val safeAreaPaddingStart =
            WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(LayoutDirection.Ltr)

        Row(Modifier.weight(1f)) {
            if (getPlatform() == Platform.IOS)
                Spacer(Modifier.width(alignmentOffset - safeAreaPaddingStart - titleWidth / 2 - buttonsByPlatformWidth))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = responsiveTitleStyle,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = if (getPlatform() != Platform.IOS) extraSmallPadding else noPadding)
            )
            Spacer(Modifier.weight(1f))
        }

        Box {
            trailingButtons()
        }
    }
}

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
private fun ButtonsByPlatform(
    titleOfParent: String?,
    contentColor: Color,
    popBackStack: () -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        GenericPlainTooltip(
            text = stringResource(Res.string.return_),
            content = {
                if (getPlatform() == Platform.IOS) {
                    TextButton(
                        onClick = { popBackStack.invoke() },
                    ) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = null,
                                tint = contentColor
                            )
                            Text(
                                titleOfParent ?: stringResource(Res.string.return_),
                                modifier = Modifier.offset(x = (-8).dp),
                                color = contentColor
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { popBackStack.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.return_),
                            tint = contentColor
                        )
                    }
                }
            }
        )
    }
}

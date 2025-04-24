package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.diaryCMP.uimodule.ui.composables.ComponentByType
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorSaving
import com.diaryCMP.uimodule.ui.composables.GenericDialog
import com.diaryCMP.uimodule.ui.composables.GenericPlainTooltip
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.extraLargePadding
import com.diaryCMP.uimodule.ui.theme.halfListSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.listSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.sizeArrowTutorial
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.components_order
import diary.composeapp.utilsmodule.generated.resources.curved_arrow
import diary.composeapp.utilsmodule.generated.resources.five_more_minutes
import diary.composeapp.utilsmodule.generated.resources.okey
import diary.composeapp.utilsmodule.generated.resources.squiggly_arrow
import diary.composeapp.utilsmodule.generated.resources.start_diary_select_components
import diary.composeapp.utilsmodule.generated.resources.times_up_alert_body
import diary.composeapp.utilsmodule.generated.resources.times_up_alert_title
import diary.composeapp.utilsmodule.generated.resources.today
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardDismissOnSwipeIOS
import es.diaryCMP.viewmodelsModule.viewmodels.TodayScreenViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayScreenViewModel,
    navigateToTodayOrderScreen: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }
    val components by viewModel.components.collectAsState()
    val alertNextDay by viewModel.alertNextDay.collectAsState()
    val errorSaving by viewModel.errorSaving.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()

    val lazyListState = rememberLazyListState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (lazyListState.firstVisibleItemScrollOffset > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        TodayScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            navigateToTodayOrderScreen = navigateToTodayOrderScreen
        )

        LoadingWrapper(isLoading = isLoading, content = {
            TodayScreenContent(
                lazyListState = lazyListState,
                components = components,
                onComponentValueChange = { id, value ->
                    viewModel.editEntryValue(id, value)
                }
            )
        })
    }

    if (alertNextDay) {
        AlertNextDay(
            onDismiss = {
                viewModel.dismissAlertNextDay()
                viewModel.nextDay()
            },
            onConfirm = {
                viewModel.dismissAlertNextDay()
                viewModel.fiveMoreMinutes()
            }
        )
    }

    if (errorSaving) {
        GenericAlertErrorSaving(onDismiss = { viewModel.dismissErrorSaving() })
    }

    if (errorLoading) {
        GenericAlertErrorLoading(onDismiss = { viewModel.dismissLoadingError() })
    }
}

@Composable
private fun TodayScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color,
    navigateToTodayOrderScreen: () -> Unit
) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.today),
        modifier = modifier,
        containerColor = containerColor,
        trailingButtons = {
            GenericPlainTooltip(
                text = stringResource(Res.string.components_order),
                content = {
                    IconButton(
                        onClick = { navigateToTodayOrderScreen.invoke() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(Res.string.components_order)
                        )
                    }
                }
            )
        }
    )
}

@Composable
private fun TodayScreenContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    components: List<DiaryEntryComponent>,
    onComponentValueChange: (String, Any) -> Unit
) {
    KeyboardAware(content = {
        val keyboardController = LocalSoftwareKeyboardController.current

        TodayScreenPane(
            modifier = if (getScreenClass() != ScreenClass.Compact) {
                modifier.clip(RoundedCornerShape(surfaceCornerRadius))
            } else {
                modifier
                    .fillMaxSize()
            }.background(MaterialTheme.colorScheme.surfaceContainer)
                .keyboardDismissOnSwipeIOS(onSwipe = { keyboardController?.hide() }),
            lazyListState = lazyListState,
            components = components,
            onComponentValueChange = onComponentValueChange
        )
    })
}

@Composable
private fun TodayScreenPane(
    modifier: Modifier = Modifier,
    components: List<DiaryEntryComponent>,
    onComponentValueChange: (String, Any) -> Unit,
    lazyListState: LazyListState
) {
    Crossfade(targetState = components.isNotEmpty()) { targetState ->
        when (targetState) {
            true -> {
                TodayScreenPaneComponentList(
                    modifier = modifier,
                    components = components,
                    onComponentValueChange = onComponentValueChange,
                    lazyListState = lazyListState
                )
            }

            false -> {
                TodayScreenPaneTutorial()
            }
        }
    }
}

@Composable
private fun TodayScreenPaneComponentList(
    modifier: Modifier,
    lazyListState: LazyListState,
    components: List<DiaryEntryComponent>,
    onComponentValueChange: (String, Any) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = if (getScreenClass() == ScreenClass.Compact) {
            modifier.fillMaxSize()
                .focusRequester(focusRequester)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusRequester.requestFocus()
                        }
                    )
                }
        } else {
            modifier
                .padding(pageListHorizontalPadding)
                .padding(horizontal = interiorCardPadding)
        }
            .animateContentSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding)
    ) {
        item {
            if (getScreenClass() != ScreenClass.Compact) {
                Spacer(modifier.height(interiorCardPadding - halfListSpacedByPadding))
            }
        }

        items(components.size) {
            val item = components[it]

            ComponentByType(
                item = item,
                onValueChange = { value ->
                    onComponentValueChange.invoke(item.id, value)
                },
                modifier = if (getScreenClass() == ScreenClass.Compact) {
                    Modifier.padding(horizontal = smallPadding).padding(pageListHorizontalPadding)
                } else {
                    Modifier
                }
            )
        }

        item {
            if (getScreenClass() != ScreenClass.Compact) {
                Spacer(modifier.height(interiorCardPadding - halfListSpacedByPadding))
            }
        }
    }
}

@Composable
private fun TodayScreenPaneTutorial() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = extraLargePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(Res.string.start_diary_select_components),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row {
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(Res.drawable.squiggly_arrow),
                contentDescription = stringResource(Res.string.curved_arrow),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = largePadding).size(sizeArrowTutorial)
            )
        }
    }
}

@Composable
private fun AlertNextDay(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    GenericDialog(
        title = stringResource(Res.string.times_up_alert_title),
        body = stringResource(Res.string.times_up_alert_body),
        onDismiss = { onDismiss.invoke() },
        confirmButton = {
            TextButton(onClick = { onConfirm.invoke() }) {
                Text(stringResource(Res.string.five_more_minutes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss.invoke() }) {
                Text(stringResource(Res.string.okey))
            }
        }
    )
}
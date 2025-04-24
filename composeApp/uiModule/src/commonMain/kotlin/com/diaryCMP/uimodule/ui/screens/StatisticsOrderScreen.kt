package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorSaving
import com.diaryCMP.uimodule.ui.composables.GenericDialog
import com.diaryCMP.uimodule.ui.composables.GenericPlainTooltip
import com.diaryCMP.uimodule.ui.composables.LengthAgainstStatisticsComponentPreview
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.NumberStatisticsComponentPreview
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.composables.TimeAgainstStatisticsComponentPreview
import com.diaryCMP.uimodule.ui.composables.YesNoStatisticsComponentPreview
import com.diaryCMP.uimodule.ui.theme.extraSmallPadding
import com.diaryCMP.uimodule.ui.theme.halfListSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.listSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.mediumPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import com.diaryCMP.uimodule.ui.theme.widthRightSupportPane
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.archive_statistic
import diary.composeapp.utilsmodule.generated.resources.archive_statistic_body
import diary.composeapp.utilsmodule.generated.resources.are_you_sure
import diary.composeapp.utilsmodule.generated.resources.move_down_component
import diary.composeapp.utilsmodule.generated.resources.move_up_component
import diary.composeapp.utilsmodule.generated.resources.no_archived_statistics
import diary.composeapp.utilsmodule.generated.resources.preview
import diary.composeapp.utilsmodule.generated.resources.reorder_component
import diary.composeapp.utilsmodule.generated.resources.statistics
import diary.composeapp.utilsmodule.generated.resources.statistics_order
import diary.composeapp.utilsmodule.generated.resources.unarchive_statistics
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.StatisticsHelper
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsOrderScreenViewModel
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun StatisticsOrderScreen(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit,
    viewModel: StatisticsOrderScreenViewModel
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }
    val statistics by viewModel.statistics.collectAsState()
    val archivedStatistics by viewModel.archivedStatistics.collectAsState()
    val isAddingStatistics by viewModel.isAddingStatistics.collectAsState()
    val errorSaving by viewModel.errorSaving.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (scrollState.value > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        StatisticsOrderScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            popBackStack = popBackStack,
            onAddTapped = { viewModel.setAddingStatistics(true) }
        )

        LoadingWrapper(isLoading = isLoading, content = {
            StatisticsOrderScreenContent(
                modifier = if (getScreenClass() != ScreenClass.Compact) {
                    Modifier.clip(RoundedCornerShape(surfaceCornerRadius))
                } else {
                    Modifier.fillMaxSize()
                }.background(MaterialTheme.colorScheme.surfaceContainer),
                scrollState = scrollState,
                statistics = statistics,
                archivedStatistics = archivedStatistics,
                isAddingItems = isAddingStatistics,
                onDismissRequest = { viewModel.setAddingStatistics(false) },
                onAddItem = { id ->
                    viewModel.unarchiveStatistic(id)
                },
                onReorderItem = { fromIndex, toIndex ->
                    viewModel.reorderStatistic(fromIndex, toIndex)
                },
                onArchiveItem = { id ->
                    viewModel.archiveStatistic(id)

                }
            )
        })
    }

    if (errorSaving) {
        GenericAlertErrorSaving(onDismiss = { viewModel.dismissErrorSaving() })
    }

    if (errorLoading) {
        GenericAlertErrorLoading(onDismiss = {
            viewModel.dismissLoadingError()
            popBackStack.invoke()
        })
    }
}

@Composable
private fun StatisticsOrderScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color,
    popBackStack: () -> Unit,
    onAddTapped: () -> Unit
) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.statistics_order),
        modifier = modifier,
        containerColor = containerColor,
        popBackStack = popBackStack,
        titleOfParent = stringResource(Res.string.statistics),
        trailingButtons = {
            if (getScreenClass() == ScreenClass.Compact || getScreenClass() == ScreenClass.Medium) {
                GenericPlainTooltip(
                    text = stringResource(Res.string.unarchive_statistics),
                    content = {
                        IconButton(onClick = { onAddTapped.invoke() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(Res.string.unarchive_statistics)
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun StatisticsOrderScreenContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    statistics: List<StatisticsHelper>,
    archivedStatistics: List<StatisticsHelper>,
    isAddingItems: Boolean,
    onDismissRequest: () -> Unit,
    onAddItem: (String) -> Unit,
    onReorderItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onArchiveItem: (String) -> Unit
) {
    Row {
        KeyboardAware(modifier = modifier
            .weight(1f),
            content = {
                ReordenableRowPane(
                    scrollState = scrollState,
                    statistics = statistics,
                    onReorderItem = onReorderItem,
                    onArchiveItem = onArchiveItem
                )
            }
        )

        if (getScreenClass() == ScreenClass.Large) {
            UnarchiveStatisticsSupportPane(
                archivedStatistics = archivedStatistics,
                onAddItem = onAddItem,
                modifier = Modifier
                    .padding(start = surfaceToWindowPadding)
                    .width(widthRightSupportPane)
            )
        } else {
            if (isAddingItems) {
                UnarchiveStatisticsBottomSheet(
                    archivedStatistics = archivedStatistics,
                    onAddItem = onAddItem,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun ReordenableRowPane(
    scrollState: ScrollState,
    statistics: List<StatisticsHelper>,
    onReorderItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onArchiveItem: (String) -> Unit
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
    }

    ReorderableColumn(
        modifier = Modifier.verticalScroll(scrollState)
            .animateContentSize(),
        list = statistics,
        onSettle = { fromIndex, toIndex ->
            onReorderItem.invoke(fromIndex, toIndex)
        }) { index, item, _ ->
        key(item.componentId) {
            val interactionSource = remember { MutableInteractionSource() }

            var showArchiveAlert by rememberSaveable { mutableStateOf(false) }
            if (showArchiveAlert) {
                GenericDialog(
                    title = stringResource(Res.string.are_you_sure),
                    body = stringResource(Res.string.archive_statistic_body),
                    onDismiss = { showArchiveAlert = false },
                    onConfirm = { onArchiveItem.invoke(item.componentId) }
                )
            }

            val moveUpLabel = stringResource(Res.string.move_up_component)
            val moveDownLabel = stringResource(Res.string.move_down_component)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), onClick = {},
                modifier = Modifier.fillMaxWidth().semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(label = moveUpLabel, action = {
                            if (index > 0) {
                                onReorderItem.invoke(index - 1, index)
                                true
                            } else {
                                false
                            }
                        }),
                        CustomAccessibilityAction(label = moveDownLabel, action = {
                            if (index < statistics.size - 1) {
                                onReorderItem.invoke(index + 1, index)
                                true
                            } else {
                                false
                            }
                        }),
                    )
                }, interactionSource = interactionSource
            ) {
                Row(
                    modifier = if (getScreenClass() == ScreenClass.Compact) Modifier.padding(
                        pageListHorizontalPadding
                    ).padding(vertical = halfListSpacedByPadding)
                    else Modifier.padding(vertical = halfListSpacedByPadding)
                        .padding(horizontal = interiorCardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatisticDetail(
                        item = item,
                        animatedProgress = animatedProgress,
                        modifier = Modifier.weight(4f).padding(vertical = extraSmallPadding)
                            .padding(start = smallPadding)
                    )

                    ResponsiveButtons(
                        buttonsModifier = Modifier.draggableHandle(
                            interactionSource = interactionSource,
                        ).clearAndSetSemantics { },
                        onArchiveButtonTapped = { showArchiveAlert = true },
                        item = item
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponsiveButtons(
    buttonsModifier: Modifier, onArchiveButtonTapped: () -> Unit, item: StatisticsHelper
) {
    if (getScreenClass() == ScreenClass.Compact) {
        Column {
            Buttons(
                modifier = buttonsModifier,
                onArchiveButtonTapped = onArchiveButtonTapped,
                item = item
            )
        }
    } else {
        Row {
            Buttons(
                modifier = buttonsModifier,
                onArchiveButtonTapped = onArchiveButtonTapped,
                item = item
            )
        }
    }
}

@Composable
private fun Buttons(
    modifier: Modifier, onArchiveButtonTapped: () -> Unit, item: StatisticsHelper
) {
    GenericPlainTooltip(
        text = stringResource(Res.string.reorder_component),
        content = {
            IconButton(
                modifier = modifier,
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(Res.string.reorder_component),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
    if (item.canBeArchived) {
        GenericPlainTooltip(
            text = stringResource(Res.string.archive_statistic),
            content = {
                IconButton(
                    onClick = { onArchiveButtonTapped.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = stringResource(Res.string.archive_statistic),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnarchiveStatisticsBottomSheet(
    archivedStatistics: List<StatisticsHelper>,
    onAddItem: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(containerColor = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = { onDismissRequest.invoke() }) {
        UnarchivedStatisticsSharedList(
            archivedStatistics = archivedStatistics,
            onAddItem = { id ->
                onAddItem.invoke(id)
                onDismissRequest.invoke()
            },
            modifier = Modifier.padding(pageListHorizontalPadding)
        )
    }
}

@Composable
private fun UnarchiveStatisticsSupportPane(
    modifier: Modifier = Modifier,
    archivedStatistics: List<StatisticsHelper>,
    onAddItem: (String) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(Res.string.unarchive_statistics),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.padding(mediumPadding)
            )
        }

        UnarchivedStatisticsSharedList(
            archivedStatistics = archivedStatistics,
            onAddItem = onAddItem
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnarchivedStatisticsSharedList(
    modifier: Modifier = Modifier,
    archivedStatistics: List<StatisticsHelper>,
    onAddItem: (String) -> Unit,
) {
    if (archivedStatistics.isEmpty()) {
        Column(
            modifier = if (getScreenClass() != ScreenClass.Large) modifier.fillMaxSize()
            else modifier.fillMaxWidth().clip(RoundedCornerShape(surfaceCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(Res.string.no_archived_statistics),
                modifier = Modifier.padding(interiorCardPadding)
            )
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding),

        ) {
        items(
            archivedStatistics.size,
            key = { index -> archivedStatistics[index].componentId }) { index ->
            val item = archivedStatistics[index]

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth().animateItemPlacement()
            ) {
                Row(
                    modifier = Modifier.padding(interiorCardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(4f)) {
                        StatisticDetail(
                            item = item,
                            animatedProgress = animatedProgress,
                            modifier = Modifier.padding(bottom = smallPadding)
                        )
                    }
                    IconButton(
                        onClick = { onAddItem.invoke(item.componentId) },
                    ) {
                        Icon(
                            Icons.Default.Unarchive,
                            contentDescription = stringResource(Res.string.unarchive_statistics),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(listSpacedByPadding))
        }
    }
}

@Composable
private fun StatisticDetail(
    item: StatisticsHelper,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(listSpacedByPadding)
        ) {
            Text(item.componentTitle)
            Text(
                stringResource(Res.string.preview),
                modifier = Modifier.clip(RoundedCornerShape(mediumPadding))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(extraSmallPadding)
            )
        }

        Box(modifier = Modifier.width(IntrinsicSize.Min).alpha(0.5f)) {
            if (item.componentId == "TIME_AGAINST") {
                TimeAgainstStatisticsComponentPreview(
                    secondsSevenDays = listOf(1, 2, 2, 5, 2, 6, 2),
                    animatedProgress = animatedProgress,
                    modifier = Modifier.padding(vertical = smallPadding)
                )
                return
            }

            when (item.componentType) {
                LongTextDiaryComponent::class -> {
                    LengthAgainstStatisticsComponentPreview(
                        numberWordsSevenDays = listOf(1, 1, 4, 1, 6, 10),
                        animatedProgress = animatedProgress,
                        modifier = Modifier.padding(vertical = smallPadding)
                    )
                }

                BooleanDiaryComponent::class -> {
                    YesNoStatisticsComponentPreview(
                        yesNoSevenDays = listOf(true, false, true, false, true, false, true),
                        animatedProgress = animatedProgress,
                        modifier = Modifier.padding(vertical = smallPadding)
                    )
                }

                FiveOptionDiaryComponent::class -> {
                    NumberStatisticsComponentPreview(
                        numbersSevenDays = listOf(1, 2, 3, 4, 0, 0, 4),
                        animatedProgress = animatedProgress,
                        modifier = Modifier.padding(vertical = smallPadding)
                    )
                }
            }
        }
    }
}

package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.GenericPlainTooltip
import com.diaryCMP.uimodule.ui.composables.LengthAgainstStatisticsComponent
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.NumberStatisticsComponent
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.composables.TimeAgainstStatisticsComponent
import com.diaryCMP.uimodule.ui.composables.YesNoStatisticsComponent
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import com.diaryCMP.uimodule.ui.theme.widthGridCells
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.statistics
import diary.composeapp.utilsmodule.generated.resources.statistics_order
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.StatisticsHelper
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.StatisticsScreenViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsScreenViewModel,
    navigateToStatisticsOrderScreen: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit(navigateToStatisticsOrderScreen)
    }

    val statisticsHelperList by viewModel.statisticsHelperList.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    val lazyListState = rememberLazyStaggeredGridState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (lazyListState.firstVisibleItemScrollOffset > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        StatisticsScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            navigateToStatisticsOrderScreen = navigateToStatisticsOrderScreen
        )

        LoadingWrapper(isLoading = isLoading, content = {
            StatisticsScreenContent(
                modifier = if (getScreenClass() != ScreenClass.Compact) {
                    Modifier
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                },
                lazyListState = lazyListState,
                statisticsHelperList = statisticsHelperList
            )
        })
    }


    if (errorLoading) {
        GenericAlertErrorLoading(onDismiss = { viewModel.dismissLoadingError() })
    }
}

@Composable
private fun StatisticsScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color,
    navigateToStatisticsOrderScreen: () -> Unit
) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.statistics),
        modifier = modifier,
        containerColor = containerColor,
        trailingButtons = {
            GenericPlainTooltip(
                text = stringResource(Res.string.statistics_order),
                content = {
                    IconButton(
                        onClick = { navigateToStatisticsOrderScreen.invoke() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(Res.string.statistics_order)
                        )
                    }
                }
            )
        }
    )
}

@Composable
fun StatisticsScreenContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyStaggeredGridState,
    statisticsHelperList: List<StatisticsHelper>
) {
    StatisticsScreenPanel(
        modifier = if (getScreenClass() != ScreenClass.Compact) {
            modifier.clip(RoundedCornerShape(surfaceCornerRadius))
        } else {
            modifier.fillMaxSize()
        },
        lazyListState = lazyListState,
        statisticsHelperList = statisticsHelperList
    )
}

@Composable
fun StatisticsScreenPanel(
    modifier: Modifier = Modifier,
    lazyListState: LazyStaggeredGridState,
    statisticsHelperList: List<StatisticsHelper>
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
    }

    val columns =
        if (getScreenClass() != ScreenClass.Compact) StaggeredGridCells.Adaptive(widthGridCells)
        else StaggeredGridCells.Fixed(1)

    LazyVerticalStaggeredGrid(
        columns = columns,
        modifier = modifier,
        state = lazyListState,
        verticalItemSpacing = surfaceToWindowPadding,
        horizontalArrangement = Arrangement.spacedBy(surfaceToWindowPadding)
    ) {
        itemsIndexed(statisticsHelperList) { _, statisticsHelper ->
            Column {
                if (statisticsHelper.componentId == "TIME_AGAINST") {
                    TimeAgainstStatisticsComponent(
                        secondsSevenDays = statisticsHelper.componentValues ?: emptyList(),
                        animatedProgress = animatedProgress,
                        modifier = Modifier
                    )
                    return@itemsIndexed
                }

                when (statisticsHelper.componentType) {
                    LongTextDiaryComponent::class -> {
                        LengthAgainstStatisticsComponent(
                            componentTitle = statisticsHelper.componentTitle,
                            animatedProgress = animatedProgress,
                            numberWordsSevenDays = statisticsHelper.componentValues ?: emptyList()
                        )
                    }

                    BooleanDiaryComponent::class -> {
                        YesNoStatisticsComponent(
                            componentTitle = statisticsHelper.componentTitle,
                            yesNoSevenDays = (statisticsHelper.componentValues?.map { if (it == 1) true else if (it == 0) false else null })
                                ?: emptyList(),
                            animatedProgress = animatedProgress
                        )
                    }

                    FiveOptionDiaryComponent::class -> {
                        NumberStatisticsComponent(
                            componentTitle = statisticsHelper.componentTitle,
                            animatedProgress = animatedProgress,
                            numbersSevenDays = statisticsHelper.componentValues ?: emptyList()
                        )
                    }
                }
            }
        }
    }
}
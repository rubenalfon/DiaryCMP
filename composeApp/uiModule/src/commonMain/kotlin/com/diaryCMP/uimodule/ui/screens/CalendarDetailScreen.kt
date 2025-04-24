package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.diaryCMP.uimodule.ui.composables.ComponentByType
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.halfListSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.listSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.calendar
import diary.composeapp.utilsmodule.generated.resources.ending_hour_number
import diary.composeapp.utilsmodule.generated.resources.no_entry_for_date
import diary.composeapp.utilsmodule.generated.resources.no_entry_for_year_month
import diary.composeapp.utilsmodule.generated.resources.select_date_calendar
import diary.composeapp.utilsmodule.generated.resources.select_date_list
import diary.composeapp.utilsmodule.generated.resources.starting_hour_number
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.CalendarScreenViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.stringResource

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun CalendarDetailScreen(
    viewModel: CalendarScreenViewModel,
    popBackStack: () -> Unit
) {
    val diaryEntry by viewModel.selectedEntry.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()

    if (getScreenClass() != ScreenClass.Compact) {
        popBackStack.invoke()
    }

    val lazyListState = rememberLazyListState()

    Column {

        val animatedColor by animateColorAsState(if (lazyListState.firstVisibleItemScrollOffset > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        CalendarDetailScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            title = diaryEntry?.date?.format(LocalDate.Format { byUnicodePattern("dd/MM/yyyy") })
                ?: "",
            popBackStack = {
                if (viewModel.selectedYearMonth.value != null && viewModel.selectedDate.value != null)
                    viewModel.updateSelectedYearMonth(
                        YearMonth(
                            monthValue = viewModel.selectedDate.value!!.monthNumber,
                            yearValue = viewModel.selectedDate.value!!.year
                        )
                    )
                viewModel.setSelectedEntry(null)
                popBackStack()
            }
        )

        LoadingWrapper(isLoading = isLoading, content = {
            if (!errorLoading) {
                CalendarDetailScreenContent(
                    modifier = if (getScreenClass() != ScreenClass.Compact) {
                        Modifier
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            .fillMaxSize()
                    },
                    lazyListState = lazyListState,
                    diaryEntry = diaryEntry
                )
            }
        })
    }

    if (errorLoading) {
        GenericAlertErrorLoading {
            popBackStack.invoke()
        }
    }
}

@Composable
private fun CalendarDetailScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color,
    title: String,
    popBackStack: () -> Unit
) {
    ResponsiveScreenHeader(
        title = title,
        modifier = modifier,
        containerColor = containerColor,
        popBackStack = popBackStack,
        titleOfParent = stringResource(Res.string.calendar)
    )
}

@Composable
fun CalendarDetailScreenContent(
    diaryEntry: DiaryEntry?,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    CalendarScreenDetailPane(
        modifier = modifier,
        lazyListState = lazyListState,
        diaryEntry = diaryEntry
    )
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun CalendarScreenDetailPane(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    aDateIsSelected: Boolean = true,
    aMonthIsSelected: Boolean = true,
    isEntryListEmpty: Boolean = true,
    diaryEntry: DiaryEntry?
) {
    val animatedColor =
        animateColorAsState(if (diaryEntry == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    LazyColumn(
        modifier = if (getScreenClass() == ScreenClass.Compact) {
            modifier
        } else {
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(surfaceCornerRadius))
                .drawBehind {
                    drawRect(color = animatedColor.value)
                }
                .padding(horizontal = interiorCardPadding)
                .animateContentSize()
        }
            .padding(pageListHorizontalPadding),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding)
    ) {
        if (diaryEntry == null) {
            item {
                NoEntryMessageError(
                    aDateIsSelected,
                    aMonthIsSelected,
                    isEntryListEmpty,
                    Modifier.padding(vertical = interiorCardPadding)
                )
            }
        } else {
            item {
                if (getScreenClass() != ScreenClass.Compact) {
                    Spacer(Modifier.height(interiorCardPadding - halfListSpacedByPadding))
                }
            }
            items(diaryEntry.components.size) {
                val component = diaryEntry.components[it]
                ComponentByType(
                    item = component, enabled = false, isExhibit = true,
                    modifier = if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.padding(horizontal = smallPadding)
                    } else {
                        Modifier
                    }
                )
            }
            item {
                Text(
                    stringResource(
                        Res.string.starting_hour_number,
                        diaryEntry.createdDateTime.time.format(LocalTime.Format {
                            byUnicodePattern("HH:mm")
                        })
                    ), color = MaterialTheme.colorScheme.secondary,
                    modifier = if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.padding(horizontal = smallPadding)
                    } else {
                        Modifier
                    }
                )
                Text(
                    stringResource(
                        Res.string.ending_hour_number,
                        diaryEntry.updatedDateTime.time.format(LocalTime.Format {
                            byUnicodePattern("HH:mm")
                        })
                    ), color = MaterialTheme.colorScheme.secondary,
                    modifier = if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.padding(horizontal = smallPadding)
                    } else {
                        Modifier
                    }
                )
            }
            item {
                if (getScreenClass() != ScreenClass.Compact) {
                    Spacer(Modifier.height(interiorCardPadding - halfListSpacedByPadding))
                }
            }
        }
    }
}

@Composable
private fun NoEntryMessageError(
    aDateIsSelected: Boolean,
    aMonthIsSelected: Boolean,
    isEntryListEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    if (aDateIsSelected) {
        Text(
            text = stringResource(Res.string.no_entry_for_date), modifier = modifier
        )
    } else if (aMonthIsSelected) {
        if (!isEntryListEmpty) {
            Text(
                text = stringResource(Res.string.select_date_list), modifier = modifier
            )
        } else {
            Text(
                text = stringResource(Res.string.no_entry_for_year_month), modifier = modifier
            )
        }
    } else {
        Text(
            text = stringResource(Res.string.select_date_calendar), modifier = modifier
        )
    }
}
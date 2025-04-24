package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.halfListSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import com.diaryCMP.uimodule.ui.theme.widthLeftSupportPane
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.calendar
import es.calendarMultiplatform.ui.CalendarComponent
import es.calendarMultiplatform.ui.CalendarViewModel
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.YearMonthSaver
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.viewmodelsModule.viewmodels.CalendarScreenViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarScreenViewModel,
    navigateToCalendarEntry: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }

    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val allDiaryEntriesDates by viewModel.allDiaryEntriesDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsState()
    val hasNavigatedToDetail by viewModel.hasNavigatedToDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()

    val lazyListState = rememberLazyListState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (lazyListState.firstVisibleItemScrollOffset > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        CalendarScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }
        )

        LoadingWrapper(isLoading = isLoading, content = {
            CalendarScreenContent(
                lazyListState = lazyListState,
                diaryEntries = diaryEntries,
                selectedDate = selectedDate,
                selectedYearMonth = selectedYearMonth,
                allDiaryEntriesDates = allDiaryEntriesDates,
                selectedEntry = selectedEntry,
                hasNavigatedToDetail = hasNavigatedToDetail,
                updateHasNavigatedToDetail = { hasNavigated ->
                    viewModel.updateHasNavigatedToDetail(hasNavigated)
                },
                updateSelectedDate = { date, isCompactScreen ->
                    viewModel.updateSelectedDate(
                        date,
                        isCompactScreen
                    )
                },
                updateSelectedYearMonth = { yearMonth ->
                    viewModel.updateSelectedYearMonth(yearMonth)
                },
                navigateToCalendarEntry = navigateToCalendarEntry,
                setSelectedEntry = { diaryEntry -> viewModel.setSelectedEntry(diaryEntry) }
            )
        })
    }

    if (errorLoading) {
        GenericAlertErrorLoading(onDismiss = { viewModel.dismissLoadingError() })
    }
}

@Composable
private fun CalendarScreenHeader(
    modifier: Modifier = Modifier, containerColor: Color
) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.calendar),
        containerColor = containerColor,
        modifier = modifier
    )
}

@Composable
private fun CalendarScreenContent(
    lazyListState: LazyListState,
    diaryEntries: List<DiaryEntry>,
    selectedDate: LocalDate?,
    selectedYearMonth: YearMonth?,
    allDiaryEntriesDates: List<LocalDate>,
    selectedEntry: DiaryEntry?,
    hasNavigatedToDetail: Boolean,
    updateHasNavigatedToDetail: (Boolean) -> Unit,
    updateSelectedDate: (LocalDate, Boolean) -> Unit,
    updateSelectedYearMonth: (YearMonth) -> Unit,
    navigateToCalendarEntry: () -> Unit,
    setSelectedEntry: (DiaryEntry) -> Unit
) {
    if (getScreenClass() == ScreenClass.Compact) {
        CompactCalendarScreenContent(
            modifier = Modifier,
            lazyListState = lazyListState,
            diaryEntries = diaryEntries,
            selectedYearMonth = selectedYearMonth,
            allDiaryEntriesDates = allDiaryEntriesDates,
            onDateSelected = { date, yearMonth ->
                updateHasNavigatedToDetail(false)
                if (date != null) {
                    updateSelectedDate(date, true)
                } else if (yearMonth != null) {
                    updateSelectedYearMonth(yearMonth)
                }
            },
            onSelectedDiaryEntry = { diaryEntry ->
                setSelectedEntry(diaryEntry)
                updateHasNavigatedToDetail(true)
                navigateToCalendarEntry()
            }
        )
        LaunchedEffect(selectedEntry, hasNavigatedToDetail) {
            if (selectedEntry != null && !hasNavigatedToDetail) {
                updateHasNavigatedToDetail(true)
                navigateToCalendarEntry()
            }
        }
        LaunchedEffect(Unit) {
            if (selectedDate != null && selectedEntry == null) {
                updateSelectedYearMonth(
                    YearMonth(
                        monthValue = selectedDate.monthNumber,
                        yearValue = selectedDate.year
                    )
                )
            }
        }
    } else {
        NotCompactCalendarScreenContent(
            diaryEntries = diaryEntries,
            selectedDate = selectedDate,
            selectedYearMonth = selectedYearMonth,
            allDiaryEntriesDates = allDiaryEntriesDates,
            selectedEntry = selectedEntry,
            onDateSelected = { date, yearMonth ->
                if (date != null) {
                    updateSelectedDate(date, false)
                } else if (yearMonth != null) {
                    updateSelectedYearMonth(yearMonth)
                }
            },
            onSelectedDiaryEntry = { diaryEntry ->
                setSelectedEntry(diaryEntry)
            }
        )
        LaunchedEffect(hasNavigatedToDetail) {
            updateHasNavigatedToDetail(false)
        }
    }
}

@Composable
private fun CompactCalendarScreenContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    diaryEntries: List<DiaryEntry>,
    selectedYearMonth: YearMonth?,
    allDiaryEntriesDates: List<LocalDate>,
    onDateSelected: (LocalDate?, YearMonth?) -> Unit,
    onSelectedDiaryEntry: (DiaryEntry) -> Unit
) {
    var latestMonthSelected by rememberSaveable(stateSaver = YearMonthSaver) {
        mutableStateOf(selectedYearMonth)
    }
    LaunchedEffect(selectedYearMonth) {
        if (selectedYearMonth != null) {
            latestMonthSelected = selectedYearMonth
        }
    }
    CalendarScreenListPane(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        lazyListState = lazyListState,
        onDateSelected = onDateSelected,
        diaryEntries = diaryEntries,
        selectedDate = null,
        selectedYearMonth = latestMonthSelected,
        markedDates = allDiaryEntriesDates,
        onSelectedDiaryEntry = onSelectedDiaryEntry,
        selectedEntry = null
    )
}

@Composable
private fun NotCompactCalendarScreenContent(
    diaryEntries: List<DiaryEntry>,
    selectedDate: LocalDate?,
    selectedYearMonth: YearMonth?,
    allDiaryEntriesDates: List<LocalDate>,
    selectedEntry: DiaryEntry?,
    onDateSelected: (LocalDate?, YearMonth?) -> Unit,
    onSelectedDiaryEntry: (DiaryEntry) -> Unit
) {
    Row {
        CalendarScreenListPane(
            modifier = if (getScreenClass() != ScreenClass.Compact) {
                Modifier.width(widthLeftSupportPane).clip(RoundedCornerShape(surfaceCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            } else {
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)
            },
            onDateSelected = onDateSelected,
            diaryEntries = diaryEntries,
            selectedDate = selectedDate,
            selectedYearMonth = selectedYearMonth,
            markedDates = allDiaryEntriesDates,
            onSelectedDiaryEntry = onSelectedDiaryEntry,
            selectedEntry = selectedEntry
        )

        CalendarScreenDetailPane(
            diaryEntry = selectedEntry,
            aDateIsSelected = selectedDate != null,
            aMonthIsSelected = selectedYearMonth != null,
            isEntryListEmpty = diaryEntries.isEmpty(),
            modifier = Modifier.padding(start = surfaceToWindowPadding)
        )

    }
}

@OptIn(KoinExperimentalAPI::class)
@Suppress("NAME_SHADOWING")
@Composable
private fun CalendarScreenListPane(
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    diaryEntries: List<DiaryEntry>,
    selectedDate: LocalDate?,
    selectedYearMonth: YearMonth?,
    markedDates: List<LocalDate>,
    onDateSelected: (LocalDate?, YearMonth?) -> Unit,
    selectedEntry: DiaryEntry?,
    onSelectedDiaryEntry: (DiaryEntry) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier.animateContentSize()
    ) {
        item {
            val viewModel = koinViewModel<CalendarViewModel>()
            CalendarComponent(
                viewModel = viewModel,
                onSelectedDate = { date, yearMonth -> onDateSelected.invoke(date, yearMonth) },
                markedDates = markedDates,
                selectedDate = selectedDate,
                selectedMonth = selectedYearMonth,
                modifier = Modifier.padding(smallPadding)
            )
        }

        items(diaryEntries.size) {
            val diaryEntry = diaryEntries[it]
            CalendarScreenEntryLine(
                diaryEntry = diaryEntry,
                selectedDiaryEntry = selectedEntry,
                onSelectedDiaryEntry = { diaryEntry ->
                    onSelectedDiaryEntry.invoke(diaryEntry)
                },
                modifier = Modifier
            )
        }
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
private fun CalendarScreenEntryLine(
    diaryEntry: DiaryEntry,
    selectedDiaryEntry: DiaryEntry?,
    onSelectedDiaryEntry: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.clip(
            RoundedCornerShape(size = surfaceCornerRadius)
        ).clickable {
            onSelectedDiaryEntry.invoke(diaryEntry)
        }.background(
            color = if (diaryEntry == selectedDiaryEntry && getScreenClass() != ScreenClass.Compact) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        )
            .fillMaxWidth()
            .padding(smallPadding)
            .padding(vertical = halfListSpacedByPadding)
            .padding(pageListHorizontalPadding)

    ) {
        Text(
            diaryEntry.date.format(LocalDate.Format { byUnicodePattern("dd/MM/yyyy") }),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium
        )

        val titleComponent = diaryEntry.components.first { component ->
            component.id == "01"
        }
        Text(
            text = (titleComponent as ShortTextDiaryComponent).text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
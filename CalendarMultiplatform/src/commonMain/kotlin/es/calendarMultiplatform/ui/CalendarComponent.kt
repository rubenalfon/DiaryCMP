package es.calendarMultiplatform.ui


import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.calendarMultiplatform.data.CalendarMonthState
import es.diaryCMP.utilsModule.utils.calendar.DateUtil
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import es.diaryCMP.utilsModule.utils.calendar.getDisplayName
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    markedDates: List<LocalDate> = emptyList(),
    viewModel: CalendarViewModel,
    selectedDate: LocalDate?,
    selectedMonth: YearMonth?,
    onSelectedDate: (LocalDate?, YearMonth?) -> Unit = { _, _ -> }
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState != null) {
        Content(
            modifier = modifier,
            days = DateUtil.daysOfWeek,
            calendarMonths = uiState!!.calendarMonths,
            selectedDate = selectedDate,
            selectedMonth = selectedMonth,
            lastSelectedPage = uiState!!.lastSelectedPage,
            pagerOffset = uiState!!.pagerOffset,
            onPreviousMonth = { currentPage ->
                viewModel.toPreviousMonth(currentPage)
            },
            onNextMonth = { currentPage ->
                viewModel.toNextMonth(currentPage)
            },
            markedDates = markedDates,
            onDateClickListener = { date, yearMonth ->
                onSelectedDate.invoke(date, yearMonth)
            },
            onUpdateLastSelectedPage = { page ->
                viewModel.updateLastSelectedPage(page)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(
    modifier: Modifier = Modifier,
    days: Array<String>,
    calendarMonths: List<CalendarMonthState>,
    markedDates: List<LocalDate>,
    selectedDate: LocalDate?,
    selectedMonth: YearMonth?,
    lastSelectedPage: Int?,
    pagerOffset: Int,
    onPreviousMonth: (Int) -> Unit,
    onNextMonth: (Int) -> Unit,
    onDateClickListener: (LocalDate?, YearMonth?) -> Unit,
    onUpdateLastSelectedPage: (Int) -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        val coroutineScope = rememberCoroutineScope()
        val pagerState =
            rememberPagerState(initialPage = 1000 + (pagerOffset - 1), pageCount = { 2000 })

        CalendarPager(
            pagerState = pagerState,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        ) { page ->
            Column {
                LaunchedEffect(page) {
                    if (lastSelectedPage == page) return@LaunchedEffect

                    onUpdateLastSelectedPage.invoke(page)

                    if (calendarMonths[page].yearMonth == selectedMonth) return@LaunchedEffect

                    onDateClickListener.invoke(null, calendarMonths[page].yearMonth)
                }

                MonthRowTitle(
                    yearMonth = calendarMonths[page].yearMonth,
                    onClickListener = { yearMonth ->
                        onDateClickListener.invoke(null, yearMonth)
                    },
                    selectedMonth = selectedMonth
                )
                DayNamesRow(days)
                WeekRow(
                    calendarMonthState = calendarMonths[page],
                    selectedDate = selectedDate,
                    markedDates = markedDates,
                    onDateClickListener = { localDate ->
                        onDateClickListener.invoke(localDate, null)
                    }
                )
            }
        }

        MonthRowButtons(onPreviousMonthButtonClicked = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    pagerState.currentPage - 1, animationSpec = tween(durationMillis = 600)
                )
            }
        }, onNextMonthButtonClicked = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1, animationSpec = tween(durationMillis = 600)
                )
            }
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarPager(
    pagerState: PagerState,
    onPreviousMonth: (Int) -> Unit,
    onNextMonth: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    val previousPage = remember { mutableIntStateOf(pagerState.currentPage) }

    HorizontalPager(
        state = pagerState, verticalAlignment = Alignment.Top
    ) { page ->
        content(page % 3)
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentPage = pagerState.currentPage
        if (currentPage > previousPage.intValue) {
            onNextMonth.invoke(currentPage)
        } else if (currentPage < previousPage.intValue) {
            onPreviousMonth.invoke(currentPage)
        }
        previousPage.intValue = currentPage
    }
}


@Composable
private fun DayNamesRow(days: Array<String>) {
    Row {
        repeat(days.size) {
            val item = days[it]
            DayNameItem(item, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DayNameItem(day: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MonthRowButtons(
    onPreviousMonthButtonClicked: () -> Unit,
    onNextMonthButtonClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.surfaceContainer,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.surfaceContainer
                )
            )
        )
    ) {
        IconButton(onClick = {
            onPreviousMonthButtonClicked.invoke()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back"
            )
        }

        Row(modifier = Modifier.weight(1f)) { }

        IconButton(onClick = {
            onNextMonthButtonClicked.invoke()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }
    }
}

@Composable
private fun MonthRowTitle(
    yearMonth: YearMonth,
    modifier: Modifier = Modifier,
    selectedMonth: YearMonth?,
    onClickListener: (YearMonth) -> Unit
) {
    Row(modifier = modifier.padding(horizontal = 12.dp)) {
        IconButton(
            onClick = { }, modifier = Modifier.alpha(0F)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically)

        ) {
            Text(
                text = yearMonth.getDisplayName(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        color = if (selectedMonth != null && selectedMonth == yearMonth)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            Color.Transparent
                    )
                    .clickable {
                        onClickListener(
                            yearMonth
                        )
                    }
                    .padding(8.dp)
            )
        }

        IconButton(
            onClick = { }, modifier = Modifier.alpha(0F)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }
    }
}


@Composable
private fun WeekRow(
    calendarMonthState: CalendarMonthState,
    selectedDate: LocalDate?,
    markedDates: List<LocalDate>,
    onDateClickListener: (LocalDate) -> Unit,
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= calendarMonthState.dates.size) {
                ContentItem(date = CalendarMonthState.Date.Empty,
                    yearMonth = calendarMonthState.yearMonth,
                    isSelected = false,
                    markedDates = markedDates,
                    onClickListener = {})
                return@repeat
            }
            Row {
                repeat(7) {
                    val item =
                        if (index < calendarMonthState.dates.size) calendarMonthState.dates[index] else CalendarMonthState.Date.Empty
                    val isSelected =
                        item.dayOfMonth.isNotEmpty() && selectedDate != null && selectedDate == LocalDate(
                            calendarMonthState.yearMonth.yearValue,
                            calendarMonthState.yearMonth.monthValue,
                            item.dayOfMonth.toInt()
                        )

                    ContentItem(
                        date = item,
                        yearMonth = calendarMonthState.yearMonth,
                        isSelected = isSelected,
                        markedDates = markedDates,
                        onClickListener = { localDate ->
                            onDateClickListener.invoke(localDate)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    index++
                }
            }
        }
    }
}

@Composable
private fun ContentItem(
    date: CalendarMonthState.Date,
    yearMonth: YearMonth,
    isSelected: Boolean,
    markedDates: List<LocalDate>,
    onClickListener: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CircleShape).background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    Color.Transparent
            )
            .border(
                width = 3.dp, color = if (date.isToday) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .clickable(enabled = date.dayOfMonth.isNotEmpty()) {
                onClickListener(
                    LocalDate(
                        yearMonth.yearValue, yearMonth.monthValue, date.dayOfMonth.toInt()
                    )
                )
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Text(
            text = date.dayOfMonth,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (date.dayOfMonth != "")
            Text(
                "",
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        color =
                        if (markedDates.contains(
                                LocalDate(
                                    yearMonth.yearValue,
                                    yearMonth.monthValue,
                                    date.dayOfMonth.toInt()
                                )
                            )
                        )
                            MaterialTheme.colorScheme.tertiary
                        else
                            Color.Transparent
                    )
                    .padding(bottom = 10.dp)
            )
    }
}


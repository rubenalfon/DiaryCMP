package es.calendarMultiplatform.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.calendarMultiplatform.data.CalendarDataSource.getDates
import es.calendarMultiplatform.data.CalendarMonthState
import es.calendarMultiplatform.data.CalendarUiState
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import es.diaryCMP.viewmodelsModule.viewmodels.GlobalViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val sharedViewModel: GlobalViewModel
) : ViewModel() {

    private var isInitialized = false

    private val _uiState = MutableStateFlow<CalendarUiState?>(null)
    val uiState: StateFlow<CalendarUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                sharedViewModel.eventLogout.collect {
                    empty()
                }
            }
        }
    }

    fun fakeInit() {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch {
            _uiState.value = CalendarUiState(
                calendarMonths = listOf(
                    CalendarMonthState(
                        YearMonth.now().minusOneMonth(),
                        getDates(YearMonth.now().minusOneMonth())
                    ),
                    CalendarMonthState(
                        YearMonth.now(),
                        getDates(YearMonth.now())
                    ),
                    CalendarMonthState(
                        YearMonth.now().plusOneMonth(),
                        getDates(YearMonth.now().plusOneMonth())
                    )
                ),
                pagerOffset = 1
            )
        }
    }

    fun toNextMonth(currentPage: Int) {
        if (uiState.value == null) return
        viewModelScope.launch {
            val currentYearMonth = uiState.value!!.calendarMonths[(currentPage) % 3].yearMonth
            val screenToChange = (currentPage + 1) % 3
            val nextYearMonth = currentYearMonth.plusOneMonth()
            _uiState.update { currentState ->
                currentState!!.copy(
                    calendarMonths = when (screenToChange) {
                        0 -> listOf(
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                            currentState.calendarMonths[1],
                            currentState.calendarMonths[2]
                        )

                        1 -> listOf(
                            currentState.calendarMonths[0],
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                            currentState.calendarMonths[2]
                        )

                        2 -> listOf(
                            currentState.calendarMonths[0],
                            currentState.calendarMonths[1],
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                        )

                        else -> currentState.calendarMonths
                    },
                    pagerOffset = currentState.pagerOffset + 1
                )
            }
        }
    }

    fun toPreviousMonth(currentPage: Int) {
        if (uiState.value == null) return
        viewModelScope.launch {
            val currentYearMonth = uiState.value!!.calendarMonths[(currentPage) % 3].yearMonth
            val screenToChange = (currentPage - 1) % 3
            val nextYearMonth = currentYearMonth.minusOneMonth()
            _uiState.update { currentState ->
                currentState!!.copy(
                    calendarMonths = when (screenToChange) {
                        0 -> listOf(
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                            currentState.calendarMonths[1],
                            currentState.calendarMonths[2]
                        )

                        1 -> listOf(
                            currentState.calendarMonths[0],
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                            currentState.calendarMonths[2]
                        )

                        2 -> listOf(
                            currentState.calendarMonths[0],
                            currentState.calendarMonths[1],
                            CalendarMonthState(
                                nextYearMonth,
                                getDates(nextYearMonth)
                            ),
                        )

                        else -> currentState.calendarMonths
                    },
                    pagerOffset = currentState.pagerOffset - 1
                )
            }
        }
    }

    fun updateLastSelectedPage(page: Int) {
        if (uiState.value == null) return
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState!!.copy(
                    lastSelectedPage = page
                )
            }
        }

    }

    private fun empty() {
        isInitialized = false
        _uiState.value = null
    }
}
package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.utilsModule.utils.calendar.YearMonth
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@OptIn(FlowPreview::class)
class CalendarScreenViewModel(
    private val sharedViewModel: GlobalViewModel,
    private val diaryEntryRepository: DiaryEntryRepository
) : ViewModel() {

    private var isInitialized = false

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorLoading = MutableStateFlow(false)
    val errorLoading: StateFlow<Boolean> = _errorLoading.asStateFlow()

    fun dismissLoadingError() {
        _errorLoading.value = false
    }

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow<YearMonth?>(null)
    val selectedYearMonth: StateFlow<YearMonth?> = _selectedYearMonth.asStateFlow()

    fun updateSelectedDate(date: LocalDate, isCompactScreen: Boolean) {
        if (!isCompactScreen) {
            _selectedDate.value = date
            _selectedYearMonth.value = null
        }

        setSelectedEntry(getDiaryEntryByDate(date, isCompactScreen))
    }

    private fun getDiaryEntryByDate(
        date: LocalDate,
        isCompactScreen: Boolean = false
    ): DiaryEntry? {
        if (allDiaryEntries.filter { it.date == date } == emptyList<DiaryEntry>()) { // Ha seleccionado una fecha que no tiene ninguna entrada
            _selectedEntry.value = null

            if (!isCompactScreen)
                _diaryEntries.value = emptyList()

            return null
        }

        val diaryEntry = allDiaryEntries.first {
            it.date == date
        }

        return diaryEntry
    }

    fun updateSelectedYearMonth(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth

        _selectedDate.value = null

        _diaryEntries.value = allDiaryEntries.filter {
            it.date.year == yearMonth.yearValue && it.date.month.ordinal == (yearMonth.monthValue - 1)
        }

        setSelectedEntry(null)
    }

    fun setSelectedEntry(diaryEntry: DiaryEntry?) {
        _selectedEntry.value = diaryEntry
    }

    private var allDiaryEntries: List<DiaryEntry> = emptyList()

    private val _allDiaryEntriesDates = MutableStateFlow<List<LocalDate>>(emptyList())
    val allDiaryEntriesDates: StateFlow<List<LocalDate>> = _allDiaryEntriesDates.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry.asStateFlow()

    private val _hasNavigatedToDetail = MutableStateFlow(false)
    val hasNavigatedToDetail: StateFlow<Boolean> = _hasNavigatedToDetail.asStateFlow()

    fun updateHasNavigatedToDetail(hasNavigated: Boolean) {
        _hasNavigatedToDetail.value = hasNavigated
    }

    init {
        viewModelScope.launch {
            launch {
                sharedViewModel.eventLogout.collect {
                    empty()
                }
            }
            launch {
                sharedViewModel.eventDiaryChanged.debounce { 500 }.collect {
                    viewModelScope.launch {
                        loadAllDiaryEntries()
                        if (selectedDate.value != null) {
                            val selectedDate = selectedDate.value!!
                            updateSelectedYearMonth(
                                YearMonth(
                                    selectedDate.year,
                                    selectedDate.month.ordinal + 1
                                )
                            )

                            updateSelectedDate(selectedDate, false)
                        } else if (selectedYearMonth.value != null) {
                            updateSelectedYearMonth(selectedYearMonth.value!!)
                        }
                    }
                }
            }
        }
    }

    fun fakeInit() {
        if (isInitialized) return
        isInitialized = true

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            loadAllDiaryEntries()
        }
    }

    private suspend fun loadAllDiaryEntries() {
        try {
            allDiaryEntries = diaryEntryRepository.getAllDiaryEntries()
            _allDiaryEntriesDates.value = allDiaryEntries.map { it.date }
        } catch (ex: Exception) {
            Napier.e { "CalendarScreenViewModel.loadAllDiaryEntries: $ex" }
            _errorLoading.value = true
        } finally {
            _isLoading.value = false
        }
    }

    private fun empty() {
        isInitialized = false
        _isLoading.value = true
        _errorLoading.value = false
        _selectedDate.value = null
        _selectedYearMonth.value = null
        allDiaryEntries = emptyList()
        _allDiaryEntriesDates.value = emptyList()
        _diaryEntries.value = emptyList()
        _selectedEntry.value = null
        _hasNavigatedToDetail.value = false
    }
}

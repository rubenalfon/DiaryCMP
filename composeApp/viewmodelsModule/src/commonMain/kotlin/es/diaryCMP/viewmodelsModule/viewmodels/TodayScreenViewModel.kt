package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.diaryCMP.ktorModule.datasource.DocumentNotFoundException
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.repositoriesModule.repositories.DiaryOrderRepository
import es.diaryCMP.repositoriesModule.repositories.UserSettingsRepository
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.sqlDelight.db.DiaryEntryOrder
import es.diaryCMP.utilsModule.utils.calendar.localDateTimeNow
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import es.diaryCMP.utilsModule.utils.calendar.toMillis
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(FlowPreview::class)
class TodayScreenViewModel(
    private val globalViewModel: GlobalViewModel,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val diaryOrderRepository: DiaryOrderRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private var isInitialized = false

    private var checkTomorrowCoroutine: Job? = null

    private var extraTime = 0L

    private val _alertNextDay = MutableStateFlow(false)
    val alertNextDay: StateFlow<Boolean> = _alertNextDay.asStateFlow()

    fun dismissAlertNextDay() {
        _alertNextDay.value = false
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorLoading = MutableStateFlow(false)
    val errorLoading: StateFlow<Boolean> = _errorLoading.asStateFlow()

    fun dismissLoadingError() {
        _errorLoading.value = false
    }

    private val eventDiaryChanged = MutableStateFlow<Unit?>(null)

    private var diaryOrder: DiaryEntryOrder? = null // The entry order downloaded of today
    private var diaryEntry: DiaryEntry? = null // The entry downloaded of today
    private var diaryEntryDate: LocalDateTime? = null // To save the date of the entry downloaded

    private val _components = MutableStateFlow(emptyList<DiaryEntryComponent>())
    val components: StateFlow<List<DiaryEntryComponent>> get() = _components.asStateFlow()

    fun editEntryValue(id: String, value: Any) {
        _components.value = components.value.toMutableList().apply {
            find { it.id == id }?.let {
                when (it) {
                    is LongTextDiaryComponent -> it.text = value as String
                    is ShortTextDiaryComponent -> it.text = value as String
                    is BooleanDiaryComponent -> it.isChecked = value as Boolean
                    is FiveOptionDiaryComponent -> it.index = value as Int
                }
            }
            eventDiaryChanged.value = Unit
        }
    }

    private val _errorSaving = MutableStateFlow(false)
    val errorSaving: StateFlow<Boolean> get() = _errorSaving.asStateFlow()

    fun dismissErrorSaving() {
        _errorSaving.value = false
    }

    init {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                eventDiaryChanged.filterNotNull().debounce { 50 }
                    .collect {
                        eventDiaryChanged.value = null
                        saveDiaryEntry(getEndDayHour())
                    }
            }
            launch(Dispatchers.IO) {
                globalViewModel.eventDiaryOrderChanged.collect {
                    val endDayHour = getEndDayHour()
                    loadDiaryEntry(endDayHour)
                    saveDiaryEntry(endDayHour)
                }
            }
            launch {
                globalViewModel.eventLogout.collect {
                    empty()
                    checkTomorrowCoroutine?.cancel()
                }
            }
            launch {
                globalViewModel.eventEndDayHourChanged.collect {
                    empty()
                    checkTomorrowCoroutine?.cancel()
                }
            }
        }
    }

    private suspend fun getEndDayHour(): LocalTime {
        return try {
            userSettingsRepository.getLatest()!!.endDayHour!!
        } catch (_: Exception) {
            Napier.e { "TodayScreenViewModel.getEndDayHour: No saved end day hour" }
            LocalTime(0, 0)
        }
    }

    fun nextDay() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(250)
            empty()
            fakeInit()
        }
    }

    fun fiveMoreMinutes() {
        extraTime = 5 * 60 * 1000L // 5 minutes
    }

    fun fakeInit() {
        if (isInitialized) return
        isInitialized = true

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val endDayTime = getEndDayHour()

            checkTomorrowCoroutine = launch {
                val currentDateTime = localDateTimeNow()
                var dueDate = LocalDateTime(currentDateTime.date, endDayTime)

                if (dueDate < currentDateTime) {
                    dueDate = LocalDateTime(
                        currentDateTime.date.plus(1, DateTimeUnit.DAY),
                        dueDate.time
                    )
                }

                val delay = dueDate.toMillis() - currentDateTime.toMillis()

                delay(delay)


                while(isInitialized) {
                    _alertNextDay.value = true
                    delay((60 * 5) * 1000L)
                }
            }

            loadDiaryEntry(endDayTime)
        }
    }

    private suspend fun loadDiaryEntry(endDayTime: LocalTime) {
        try {
            diaryOrder = diaryOrderRepository.getLatest()

        } catch (ex: DocumentNotFoundException) {
            diaryOrder = diaryOrderRepository.getLatest(onlyLocal = true)

        } catch (ex: Exception) {
            if (ex is SocketTimeoutException) {
                _errorLoading.value = true
                return
            }
        }

        try {
            Napier.d("diaryEntryDate if  ${localDateTimeNow().time} > $endDayTime {${localDateTimeNow().time > endDayTime}}")
            val diaryEntryDate = if (
                localDateTimeNow().time > endDayTime
            ) {
                localDateToday()
            } else {
                localDateToday().minus(1, DateTimeUnit.DAY)
            }
            diaryEntry = diaryEntryRepository.getDiaryEntryByDate(diaryEntryDate)
        } catch (ex: Exception) {
            if (ex is SocketTimeoutException) {
                _errorLoading.value = true
                return
            }
        }

        getComponents()
    }

    private fun getComponents() {
        if (diaryEntry == null && diaryOrder == null) {
            _isLoading.value = false
            return
        }

        if (diaryEntry != null && diaryOrder != null) {

            val components = mutableListOf<DiaryEntryComponent>()
            diaryOrder!!.diaryEntryOrder.forEach { component ->
                var added = false
                diaryEntry!!.components.find { it.id == component.id }?.let {
                    val componentToAdd = it
                    componentToAdd.title = component.title
                    components.add(it)

                    added = true
                }
                if (!added) {
                    components.add(component)
                }
            }

            _components.value = components

            _isLoading.value = false
            return
        }

        if (diaryEntry != null) {
            _components.value = diaryEntry!!.components
        } else {
            _components.value = diaryOrder!!.diaryEntryOrder
        }
        _isLoading.value = false
    }

    private fun saveDiaryEntry(endDayTime: LocalTime) {
        if (isDiaryEntryEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newDiaryEntryDate =
                    if (localDateTimeNow().time > endDayTime) {
                        localDateToday()
                    } else {
                        localDateToday().minus(1, DateTimeUnit.DAY)
                    }

                if (diaryEntryDate == null) {
                    diaryEntryDate = diaryEntry?.createdDateTime ?: Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                }

                diaryEntryRepository.saveTodayEntry(
                    id = diaryEntry?.id ?: "diaryEntry-${newDiaryEntryDate}",
                    components = _components.value,
                    date = newDiaryEntryDate,
                    createdDateTime = diaryEntryDate!!,
                    updatedDateTime = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                )
                globalViewModel.notifyDiaryChanged()
            } catch (ex: Exception) {
                Napier.e { "TodayScreenViewModel.saveDiaryEntry: ${ex.message}" }
                _errorSaving.value = true
            }
        }
    }

    private fun isDiaryEntryEmpty(): Boolean {
        var isEmpty = true

        components.value.forEach { component ->
            when (component) {
                is ShortTextDiaryComponent -> {
                    if (component.text.isNotEmpty()) {
                        isEmpty = false
                    }
                }

                is LongTextDiaryComponent -> {
                    if (component.text.isNotEmpty()) {
                        isEmpty = false
                    }
                }

                is BooleanDiaryComponent -> {
                    if (component.isChecked != null) {
                        isEmpty = false
                    }
                }

                is FiveOptionDiaryComponent -> {
                    if (component.index != null) {
                        isEmpty = false
                    }
                }
            }
        }
        return isEmpty
    }

    private fun empty() {
        extraTime = 0L
        _alertNextDay.value = false
        isInitialized = false
        checkTomorrowCoroutine = null
        extraTime = 0L
        _alertNextDay.value = false
        _isLoading.value = true
        _errorLoading.value = false
        diaryEntry = null
        diaryOrder = null
        _components.value = emptyList()
        _errorSaving.value = false
    }
}
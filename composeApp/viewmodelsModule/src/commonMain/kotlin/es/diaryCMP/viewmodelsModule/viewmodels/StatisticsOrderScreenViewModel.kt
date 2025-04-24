package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.register_time
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import es.diaryCMP.modelsModule.models.StatisticsHelper
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.repositoriesModule.repositories.StatisticsOrderRepository
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.sqlDelight.db.StatisticsOrder
import es.diaryCMP.utilsModule.utils.calendar.localDateTimeNow
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * Every piece of information in this ViewModel, that could change in time is scoped to the last 7 days.
 */
@OptIn(FlowPreview::class)
class StatisticsOrderScreenViewModel(
    private val globalViewModel: GlobalViewModel,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val statisticsOrderRepository: StatisticsOrderRepository
) : ViewModel() {
    private var isInitialized = false

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorLoading = MutableStateFlow(false)
    val errorLoading: StateFlow<Boolean> = _errorLoading.asStateFlow()

    fun dismissLoadingError() {
        _errorLoading.value = false
    }

    private val _errorSaving = MutableStateFlow(false)
    val errorSaving: StateFlow<Boolean> get() = _errorSaving.asStateFlow()

    fun dismissErrorSaving() {
        _errorSaving.value = false
    }

    private val eventFlow = MutableStateFlow<Unit?>(null)

    private val _isAddingStatistics = MutableStateFlow(false)
    val isAddingStatistics: StateFlow<Boolean> get() = _isAddingStatistics.asStateFlow()

    fun setAddingStatistics(value: Boolean) {
        _isAddingStatistics.value = value
    }

    private val _statistics = MutableStateFlow(emptyList<StatisticsHelper>())
    val statistics: StateFlow<List<StatisticsHelper>> get() = _statistics.asStateFlow()

    private val _archivedStatistics = MutableStateFlow(emptyList<StatisticsHelper>())
    val archivedStatistics: StateFlow<List<StatisticsHelper>> get() = _archivedStatistics.asStateFlow()


    fun unarchiveStatistic(id: String) {
        _statistics.value = statistics.value.toMutableList().apply {
            val componentToAdd = archivedStatistics.value.find { it.componentId == id }!!

            add(componentToAdd)

            _archivedStatistics.value = archivedStatistics.value.toMutableList().apply {
                remove(componentToAdd)
            }
        }
        eventFlow.value = Unit
    }

    fun reorderStatistic(fromIndex: Int, toIndex: Int) {
        _statistics.value = statistics.value.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        eventFlow.value = Unit
    }

    fun archiveStatistic(id: String) {
        _statistics.value = statistics.value.toMutableList().apply {
            val componentToRemove = find { it.componentId == id }!!

            remove(componentToRemove)

            _archivedStatistics.value = archivedStatistics.value.toMutableList().apply {
                add(componentToRemove)
            }
        }
        eventFlow.value = Unit
    }


    init {
        viewModelScope.launch {
            launch {
                globalViewModel.eventLogout.collect {
                    empty()
                }
            }
            launch {
                globalViewModel.eventDiaryChanged.collect {
                    empty()
                }
            }
            launch(Dispatchers.IO) {
                eventFlow.filterNotNull().debounce { 50 }
                    .collect {
                        eventFlow.value = null
                        saveOrder()
                    }
            }
        }
    }

    fun fakeInit() {
        if (isInitialized) return
        isInitialized = true

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            loadDiaryEntriesStatisticsOrder()
        }
    }

    private suspend fun loadDiaryEntriesStatisticsOrder() {
        var diaryEntries: List<DiaryEntry> = emptyList()
        var statisticsOrder: StatisticsOrder? = null
        try {
            statisticsOrder = statisticsOrderRepository.getLatest()
        } catch (ex: Exception) {
            Napier.e { "StatisticsScreenViewModel.loadDiaryEntries1: $ex" }
            if (ex is SocketTimeoutException) {
                _errorLoading.value = true
                return
            }
        }

        try {
            diaryEntries = diaryEntryRepository.getEntriesLastSevenDays()

        } catch (ex: Exception) {
            Napier.e { "StatisticsScreenViewModel.loadDiaryEntries2: $ex" }
            _errorLoading.value = true
        } finally {
            _isLoading.value = false
        }

        preparations(diaryEntries = diaryEntries, statisticsOrder = statisticsOrder)
    }

    private suspend fun preparations(
        diaryEntries: List<DiaryEntry>,
        statisticsOrder: StatisticsOrder?
    ) {
        val statisticsInDiaryEntries = getOneStatisticHelperPerComponent(diaryEntries)

        statisticsInDiaryEntries.add(
            StatisticsHelper(
                componentId = "TIME_AGAINST",
                componentTitle = getString(Res.string.register_time),
                componentType = null,
                componentValues = null,
                canBeArchived = false
            )
        )

        val unarchivedStatistics: MutableSet<StatisticsHelper> = mutableSetOf()
        val archivedStatistics: MutableSet<StatisticsHelper> = mutableSetOf()

        if (statisticsOrder == null) {
            statisticsInDiaryEntries.forEach { archivedStatistic ->
                unarchivedStatistics.add(archivedStatistic)
            }

            _statistics.value = unarchivedStatistics.toList()
            _archivedStatistics.value = archivedStatistics.toList()

            eventFlow.value = Unit

            return
        }


        statisticsOrder.statisticsOrder.forEach { id ->
            statisticsInDiaryEntries.find { it.componentId == id }?.let {
                unarchivedStatistics.add(it)
                return@forEach
            }
        }

        statisticsInDiaryEntries.forEach { statisticsHelper ->
            if (statisticsHelper.componentId in statisticsOrder.statisticsOrder) return@forEach
            archivedStatistics.add(statisticsHelper)
        }

        _statistics.value = unarchivedStatistics.toList()
        _archivedStatistics.value = archivedStatistics.toList()
    }

    private fun saveOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                statisticsOrderRepository.saveNew(
                    statisticsOrder = statistics.value.map { it.componentId },
                    updatedDateTime = localDateTimeNow()
                )
                globalViewModel.notifyStatisticsOrderChanged()
            } catch (ex: Exception) {
                Napier.e { "StatisticsScreenViewModel.saveOrder: $ex" }
                _errorSaving.value = true
            }
        }
    }

    private fun getOneStatisticHelperPerComponent(
        diaryEntries: List<DiaryEntry>,
    ): MutableSet<StatisticsHelper> {
        val statisticsInDiaryEntries: MutableSet<StatisticsHelper> = mutableSetOf()
        diaryEntries.reversed().forEach { diaryEntry ->
            diaryEntry.components.forEach componentsForEach@{ diaryComponent ->
                if (diaryComponent is ShortTextDiaryComponent) return@componentsForEach

                if (statisticsInDiaryEntries.any { it.componentId == diaryComponent.id }) return@componentsForEach

                statisticsInDiaryEntries.add(
                    StatisticsHelper(
                        componentId = diaryComponent.id,
                        componentTitle = diaryComponent.title,
                        componentType = diaryComponent::class,
                        componentValues = null
                    )
                )
            }
        }
        return statisticsInDiaryEntries
    }

    private fun empty() {
        isInitialized = false
        _isLoading.value = true
        _errorLoading.value = false
        _errorSaving.value = false
        eventFlow.value = null
        _isAddingStatistics.value = false
        _statistics.value = emptyList()
        _archivedStatistics.value = emptyList()
    }
}
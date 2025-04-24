package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.register_time
import diary.composeapp.utilsmodule.generated.resources.title_not_found
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import es.diaryCMP.modelsModule.models.StatisticsHelper
import es.diaryCMP.repositoriesModule.repositories.DiaryEntryRepository
import es.diaryCMP.repositoriesModule.repositories.StatisticsOrderRepository
import es.diaryCMP.sqlDelight.db.DiaryEntry
import es.diaryCMP.sqlDelight.db.StatisticsOrder
import es.diaryCMP.utilsModule.utils.calendar.localDateToday
import es.diaryCMP.utilsModule.utils.calendar.toMillis
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.getString
import kotlin.reflect.KClass

/**
 * Every piece of information in this ViewModel, that could change in time is scoped to the last 7 days.
 */
class StatisticsScreenViewModel(
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

    private var isStarting = false

    private var navigateToStatisticsOrderScreen: (() -> Unit)? = null

    private var diaryEntries: List<DiaryEntry> = emptyList()

    private var statisticsOrder: StatisticsOrder? = null

    private val _statisticsHelperList = MutableStateFlow<List<StatisticsHelper>>(emptyList())
    val statisticsHelperList = _statisticsHelperList.asStateFlow()

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
                    fakeInit()
                }
            }
            launch(Dispatchers.IO) {
                globalViewModel.eventStatisticsOrderChanged.collect {
                    empty()
                    fakeInit()
                }
            }
        }
    }

    fun fakeInit(navigationOrder: (() -> Unit)? = null) {
        if (isInitialized) return
        isInitialized = true

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            navigateToStatisticsOrderScreen = navigationOrder

            loadStatisticsOrder()

            if (isStarting) {
                navigateToStatisticsOrderScreen?.invoke()
                empty()
                return@launch
            }

            loadDiaryEntries()

            _isLoading.value = false
        }
    }

    private suspend fun loadStatisticsOrder() {
        try {
            statisticsOrder = statisticsOrderRepository.getLatest()
        } catch (ex: Exception) {
            Napier.e { "StatisticsScreenViewModel.loadStatisticsOrder: $ex" }
            if (ex is SocketTimeoutException) {
                _errorLoading.value = true
                return
            } else {
                isStarting = true
            }
        }
    }

    private suspend fun loadDiaryEntries() {
        try {
            diaryEntries = diaryEntryRepository.getEntriesLastSevenDays()
            getValuesFromDiaryEntries()
        } catch (ex: Exception) {
            Napier.e { "StatisticsScreenViewModel.loadDiaryEntries: $ex ${ex.message}" }
            _errorLoading.value = true
        }
    }

    private suspend fun getValuesFromDiaryEntries() {
        val diaryEntriesOrdered = getDiaryEntriesOrdered()

        val statisticsHelperList = mutableListOf<StatisticsHelper>()

        statisticsOrder!!.statisticsOrder.forEach { id ->
            if (id == "01") return@forEach

            if (id == "TIME_AGAINST") {
                val componentValues: List<Int?> = diaryEntriesOrdered.map { diaryEntry ->
                    if (diaryEntry == null) return@map null

                    val time =
                        diaryEntry.updatedDateTime.toMillis() - diaryEntry.createdDateTime.toMillis()
                    if (time > 0)
                        return@map (time / 1000).toInt()

                    return@map (1 * 60 * 60 * 24) + (time / 1000).toInt()
                }

                statisticsHelperList.add(
                    StatisticsHelper(
                        componentId = id,
                        componentTitle = getString(Res.string.register_time),
                        componentType = null,
                        componentValues = componentValues
                    )
                )
                return@forEach
            }

            var title: String? = null
            var type: KClass<out DiaryEntryComponent>? = null

            val componentValues = mutableListOf<Int?>()

            diaryEntriesOrdered.reversed().forEach entriesForEach@{ diaryEntry ->
                val component = diaryEntry?.components?.firstOrNull { it.id == id }

                if (component == null) {
                    componentValues.add(null)
                    return@entriesForEach
                }

                if (type == null) {
                    type = component::class
                    if (type == ShortTextDiaryComponent::class) {
                        return@forEach
                    }
                }

                if (title == null)
                    title = component.title

                componentValues.add(
                    when (type) {
                        LongTextDiaryComponent::class -> {
                            (component as LongTextDiaryComponent).text.split(" ")
                                .count { it.isNotBlank() }
                        }

                        BooleanDiaryComponent::class -> {
                            if ((component as BooleanDiaryComponent).isChecked == true) 1 else 0
                        }

                        FiveOptionDiaryComponent::class -> {
                            (component as FiveOptionDiaryComponent).index
                        }

                        else -> {
                            null
                        }
                    }
                )
            }

            statisticsHelperList.add(
                StatisticsHelper(
                    componentId = id,
                    componentTitle = title ?: getString(Res.string.title_not_found),
                    componentType = type,
                    componentValues = componentValues.reversed()
                )
            )
        }
        _statisticsHelperList.value = statisticsHelperList
    }

    private fun getDiaryEntriesOrdered(): List<DiaryEntry?> {
        val sixDaysBeforeToday = localDateToday().minus(6, DAY)
        val latestSevenEntriesIds = (0..6).map { day ->
            "diaryEntry-${sixDaysBeforeToday.plus(day.toLong(), DAY)}"
        }

        return latestSevenEntriesIds.map { id ->
            diaryEntries.firstOrNull { it.id == id }
        }
    }

    private fun empty() {
        isInitialized = false
        _isLoading.value = true
        _errorLoading.value = false
        isStarting = false
        diaryEntries = emptyList()
        statisticsOrder = null
        _statisticsHelperList.value = emptyList()
        navigateToStatisticsOrderScreen = null
    }
}
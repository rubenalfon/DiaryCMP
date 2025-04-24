package es.diaryCMP.viewmodelsModule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.change_title_component
import diary.composeapp.utilsmodule.generated.resources.long_component_text_example
import diary.composeapp.utilsmodule.generated.resources.short_component_text_example
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import es.diaryCMP.repositoriesModule.repositories.DiaryOrderRepository
import es.diaryCMP.utilsModule.utils.calendar.localDateTimeNow
import es.diaryCMP.utilsModule.utils.getAllExampleComponents
import es.diaryCMP.utilsModule.utils.getStartingComponents
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

@OptIn(FlowPreview::class)
class TodayOrderScreenViewModel(
    private val globalViewModel: GlobalViewModel,
    private val diaryOrderRepository: DiaryOrderRepository
) : ViewModel() {

    private var isInitialized = false

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorLoading = MutableStateFlow(false)
    val errorLoading: StateFlow<Boolean> = _errorLoading.asStateFlow()

    fun dismissLoadingError() {
        _errorLoading.value = false
    }

    private val eventFlow = MutableStateFlow<Unit?>(null)

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> get() = _showTutorial.asStateFlow()

    fun dismissTutorial() {
        _showTutorial.value = false
    }

    var allComponents: List<DiaryEntryComponent> = emptyList()

    private val _isAddingComponents = MutableStateFlow(false)
    val isAddingComponents: StateFlow<Boolean> get() = _isAddingComponents.asStateFlow()

    fun setAddingComponents(value: Boolean) {
        _isAddingComponents.value = value
    }

    private val _components = MutableStateFlow(emptyList<DiaryEntryComponent>())
    val components: StateFlow<List<DiaryEntryComponent>> get() = _components.asStateFlow()

    fun editEntryTitle(id: String, title: String) {
        _components.value = components.value.toMutableList().apply {
            find { it.id == id }?.let {
                if (it.title == title) {
                    return@apply
                }
                it.title = title
                eventFlow.value = Unit
            }
        }
    }

    fun deleteEntry(id: String) {
        _components.value = components.value.toMutableList().apply {
            remove(find { it.id == id })
        }
        eventFlow.value = Unit
    }

    fun reorderItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex)
            return
        _components.value = components.value.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        eventFlow.value = Unit
    }

    fun addEntry(id: String) {
        _components.value = components.value.toMutableList().apply {
            viewModelScope.launch {
                val componentToAdd: DiaryEntryComponent?

                when (allComponents.find { it.id == id }!!) {
                    is ShortTextDiaryComponent -> {
                        componentToAdd = ShortTextDiaryComponent(
                            id = uuid4().toString(),
                            title = getString(Res.string.change_title_component),
                            text = getString(Res.string.short_component_text_example)
                        )
                    }

                    is LongTextDiaryComponent -> {
                        componentToAdd = LongTextDiaryComponent(
                            id = uuid4().toString(),
                            title = getString(Res.string.change_title_component),
                            text = getString(Res.string.long_component_text_example)
                        )
                    }

                    is BooleanDiaryComponent -> {
                        componentToAdd = BooleanDiaryComponent(
                            id = uuid4().toString(),
                            title = getString(Res.string.change_title_component),
                            isChecked = true
                        )
                    }

                    is FiveOptionDiaryComponent -> {
                        componentToAdd = FiveOptionDiaryComponent(
                            id = uuid4().toString(),
                            title = getString(Res.string.change_title_component),
                            index = 4
                        )

                    }
                }
                add(componentToAdd)
            }
        }
        eventFlow.value = Unit
    }

    private val _errorSaving = MutableStateFlow(false)
    val errorSaving: StateFlow<Boolean> get() = _errorSaving.asStateFlow()

    fun dismissErrorSaving() {
        _errorSaving.value = false
    }

    init {
        viewModelScope.launch {
            launch {
                globalViewModel.eventLogout.collect {
                    empty()
                }
            }
            launch {
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
            allComponents = getAllExampleComponents()
            getOrder()
        }
    }

    private suspend fun getOrder() {
        try {
            _components.value = diaryOrderRepository.getLatest()!!.diaryEntryOrder
        } catch (ex: Exception) { // no order saved yet
            Napier.e { "TodayOrderScreenViewModel.getOrder (no order saved yet): $ex" }
            if (ex is SocketTimeoutException) {
                _errorLoading.value = true
            } else {
                _showTutorial.value = true
                _components.value = getStartingComponents()
                saveOrder() // Save default
                globalViewModel.notifyDiaryOrderChanged()
            }
        }
        _isLoading.value = false
    }

    private fun saveOrder() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                diaryOrderRepository.saveNew(
                    diaryEntryOrder = components.value.map {
                        when (it) {
                            is ShortTextDiaryComponent -> {
                                it.copy(text = "")
                            }

                            is LongTextDiaryComponent -> {
                                it.copy(text = "")
                            }

                            is BooleanDiaryComponent -> {
                                it.copy(isChecked = null)
                            }

                            is FiveOptionDiaryComponent -> {
                                it.copy(index = null)
                            }
                        }
                    },
                    updatedDateTime = localDateTimeNow()
                )
                globalViewModel.notifyDiaryOrderChanged()
            }
        } catch (ex: Exception) {
            Napier.e { "TodayOrderScreenViewModel.saveOrder: $ex" }
            _errorSaving.value = true
        }
    }

    private fun empty() {
        isInitialized = false
        _isLoading.value = true
        _errorLoading.value = false
        allComponents = emptyList()
        _isAddingComponents.value = false
        _components.value = emptyList()
        _errorSaving.value = false
    }
}
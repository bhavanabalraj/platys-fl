package com.example.platys.tagcontext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.platys.data.Event
import com.example.platys.data.PlatysRepository
import com.example.platys.navigation.NavigationDestination
import com.example.platys.utils.ContextTagModel
import com.example.platys.utils.PlatysCacheData
import com.example.platys.utils.WORKER_ID
import com.example.platys.worker.wifi.WifiWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContextTagViewModel @Inject constructor(
    private val platysRepository: PlatysRepository,
    private val platysCacheData: PlatysCacheData,
    application: Application
): AndroidViewModel(application) {

    private var suggestionsAsList: List<String> = mutableListOf()
    private var suggestionsList: HashSet<String> = HashSet()

    private val _suggestions = MutableLiveData<Event<List<ChipModel>>>()
    val suggestions: LiveData<Event<List<ChipModel>>> = _suggestions

    private val _addChip = MutableLiveData<Event<ChipModel>>()
    val addChip: LiveData<Event<ChipModel>> = _addChip

    private val _clearText = MutableLiveData<Event<Unit>>()
    val clearTextField: LiveData<Event<Unit>> = _clearText

    private val _backPressed = MutableLiveData<Event<Unit>>()
    val backPressed: LiveData<Event<Unit>> = _backPressed

    private val _chipGroupInvalidate = MutableLiveData<Event<Unit>>()
    val chipGroupInvalidate: LiveData<Event<Unit>> = _chipGroupInvalidate

    private val _navigationEvent = MutableLiveData<Event<NavigationDestination>>()
    val navigationEvent: LiveData<Event<NavigationDestination>> = _navigationEvent

    private val _hint = MutableLiveData<Event<String>>()
    val hint: LiveData<Event<String>> = _hint

    private val _checkChip = MutableLiveData<Event<Int>>()
    val checkChip: LiveData<Event<Int>> = _checkChip

    private val _hideInputLabel = MutableLiveData<Event<Unit>>()
    val hideInputLabel: LiveData<Event<Unit>> = _hideInputLabel

    private val _setError = MutableLiveData<Event<String>>()
    val setError: LiveData<Event<String>> = _setError

    private val _setCarousel = MutableLiveData<Event<Int>>()
    val setCarousel: LiveData<Event<Int>> = _setCarousel

    private var chipId = 1

    private var cardId: Int = -1

    private var loadingData = false

    private val workManager = WorkManager.getInstance(application)
    internal val outputWorkInfo: LiveData<List<WorkInfo>>

    init {
        // This transformation makes sure that whenever the current work Id changes the WorkInfo
        // the UI is listening to changes
        outputWorkInfo = workManager.getWorkInfosByTagLiveData("OUTPUT")
    }

    private fun startWorkerThread() {

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workReq = OneTimeWorkRequestBuilder<WifiWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("OUTPUT")
            .build()

        val continuation = workManager
            .beginUniqueWork(
                WORKER_ID,
                ExistingWorkPolicy.REPLACE,
                workReq
            )

        continuation.enqueue()
    }

    fun viewLoaded(cardIndex: Int) {
        cardId = cardIndex
    }

    private fun getSuggestion(contextType: ContextType) {
        val sList = platysRepository.getSuggestions(contextType)

        if(sList.isNotEmpty()) {
            val chipModelList = mutableListOf<ChipModel>()
            for(text in sList) {
                chipModelList.add(ChipModel(-1, text, chipId))
                chipId++
            }

            _suggestions.value = Event(chipModelList)
            suggestionsAsList = sList
            suggestionsList = sList.toHashSet()
        }
    }

    fun loadSuggestions() {

        when(cardId) {
            0 -> {
                getSuggestion(ContextType.ACTIVITY)
                _hint.value = Event("Activity tag")
            }
            1 -> {
                getSuggestion(ContextType.PLACE)
                _hint.value = Event("Place tag")
            }
            2 -> {
                getSuggestion(ContextType.PEOPLE)
                _hint.value = Event("People tag")
            }
            3 -> {
                getSuggestion(ContextType.EMOTION)
                _hint.value = Event("Emotion tag")
            }
        }
    }

    fun chipCloseClicked(option: String) {
        val removedOption: ContextTagModel? = platysCacheData.contextTagList[option]
        platysCacheData.contextTagList.remove(option)

        if(removedOption != null && platysCacheData.countMap.containsKey(removedOption.contextTagKey)) {
            platysCacheData.countMap[removedOption.contextTagKey] = platysCacheData.countMap[removedOption.contextTagKey]!! - 1

            if(platysCacheData.countMap[removedOption.contextTagKey] == 0) {
                platysCacheData.countMap.remove(removedOption.contextTagKey)
            }
        }

        if(platysCacheData.contextTagList.size == 0) {
            _hideInputLabel.value = Event(Unit)
        }
    }

    private fun getContextType() : String {
        when(cardId) {
            0 -> {
                return platysCacheData.activityConstant
            }
            1 -> {
                return platysCacheData.placeConstant
            }
            2 -> {
                return platysCacheData.peopleConstant
            }
            3 -> {
                return platysCacheData.emotionConstant
            }
            else -> {
                return "NA"
            }
        }
    }

    fun optionAdded(option: String, clearText: Boolean, id: Int) {
        if(option.isNotEmpty() && !platysCacheData.contextTagList.containsKey(option)) {
            platysCacheData.contextTagList[option] = ContextTagModel(getContextType(), option)

            var currentChipID = id

            if(suggestionsList.contains(option)) {
                var i = 0
                while(i < suggestionsAsList.size) {
                    if(suggestionsAsList[i] == option) {
                        currentChipID = i + 1
                        break
                    }
                    i++
                }
                _checkChip.value = Event(currentChipID)
            } else if(id == -1) {
                currentChipID = chipId
                chipId++
            }

            if(!platysCacheData.countMap.containsKey(getContextType())) {
                platysCacheData.countMap[getContextType()] = 0;
            }

            platysCacheData.countMap[getContextType()] = platysCacheData.countMap[getContextType()]!! + 1
            _addChip.value = Event(ChipModel(0, option, currentChipID))
        }

        if(clearText) {
            _clearText.value = Event(Unit)
        }
    }

    fun tagWindowClosed() {
        chipId = 1
        _backPressed.value = Event(Unit)
    }

    fun tagIconClicked() {
        _navigationEvent.value = Event(NavigationDestination.CONTEXT_TO_TAG)
    }

    fun loadData() {
        if(loadingData) {
            return
        }

        loadingData = true
        _chipGroupInvalidate.value = Event(Unit)
        if(platysCacheData.contextTagList.size > 0) {
            for((_, value) in platysCacheData.contextTagList) {
                if(!platysCacheData.countMap.containsKey(value.contextTagKey)) {
                    platysCacheData.countMap[value.contextTagKey] = 0;
                }

                //countMap[value.contextTagKey] = countMap[value.contextTagKey]!! + 1
                _addChip.value = Event(ChipModel(-1, value.contextTagValue, -1))
            }
        }

        loadingData = false
    }

    fun submitButtonClicked() {
        if(platysCacheData.countMap.size < 4) {
            if(!platysCacheData.countMap.containsKey(platysCacheData.activityConstant)) {
                _setCarousel.value = Event(0)
                return
            }

            if(!platysCacheData.countMap.containsKey(platysCacheData.placeConstant)) {
                _setCarousel.value = Event(1)
                return
            }

            if(!platysCacheData.countMap.containsKey(platysCacheData.peopleConstant)) {
                _setCarousel.value = Event(2)
                return
            }

            if(!platysCacheData.countMap.containsKey(platysCacheData.emotionConstant)) {
                _setCarousel.value = Event(3)
                return
            }
        }

        startWorkerThread()
    }
}
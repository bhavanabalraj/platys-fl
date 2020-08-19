package com.example.platys.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.platys.data.Event
import com.example.platys.data.PlatysRepository
import com.example.platys.navigation.NavigationDestination
import com.example.platys.utils.PlatysCacheData
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val platysRepository: PlatysRepository
) : ViewModel() {

    private val _dataLoaded = MutableLiveData<Boolean>()
    val dataLoaded: LiveData<Boolean> = _dataLoaded

    private val _navigationEvent = MutableLiveData<Event<NavigationDestination>>()
    val navigationEvent: LiveData<Event<NavigationDestination>> = _navigationEvent

    init {
        _dataLoaded.value = false
    }

    fun start() {
        if(_dataLoaded.value == true) {
            return
        }

        _dataLoaded.value = true
        viewModelScope.launch {
            platysRepository.getUserDetails().let {
                _dataLoaded.value = false
                _navigationEvent.value = Event(NavigationDestination.SPLASH_TO_LOGIN)
//                if(it == null) {
//                    _navigationEvent.value = Event(NavigationDestination.SPLASH_TO_LOGIN)
//                } else {
//                    platysCacheData.userDetails = it
//                }
            }
        }
    }
}
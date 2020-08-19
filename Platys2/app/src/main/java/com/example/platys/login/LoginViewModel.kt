package com.example.platys.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.platys.data.Event
import com.example.platys.data.PlatysRepository
import com.example.platys.data.Result
import com.example.platys.navigation.NavigationDestination
import com.example.platys.utils.ErrorMessage
import com.example.platys.utils.PlatysCacheData
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val platysRepository: PlatysRepository
) : ViewModel(){

    private val _dataLoaded = MutableLiveData<Event<Boolean>>()
    val dataLoaded: LiveData<Event<Boolean>> = _dataLoaded

    private val _navigationEvent = MutableLiveData<Event<NavigationDestination>>()
    val navigationEvent: LiveData<Event<NavigationDestination>> = _navigationEvent

    private var email: String
    private var password: String

    private var emailValidationError = false
    private var passwordValidationError = false

    private val _emailError = MutableLiveData<Event<String>>()
    val emailError: LiveData<Event<String>> = _emailError

    private val _clearEmailError = MutableLiveData<Event<Unit>>()
    val clearEmailError: LiveData<Event<Unit>> = _clearEmailError

    private val _passwordError = MutableLiveData<Event<String>>()
    val passwordError: LiveData<Event<String>> = _passwordError

    private val _clearPasswordError = MutableLiveData<Event<Unit>>()
    val clearPasswordError: LiveData<Event<Unit>> = _clearPasswordError

    private val _fieldErrors = MutableLiveData<Event<String>>()
    val fieldErrors: LiveData<Event<String>> = _fieldErrors

    init {
        email = ""
        password = ""
    }

    fun registerButtonClicked() {
        _navigationEvent.value = Event(NavigationDestination.LOGIN_TO_REGISTER)
    }

    fun loginButtonClicked() {
        if(_dataLoaded.value != null && _dataLoaded.value!!.peekContent()) {
            return
        }

        var currentEmailError = false
        var currentPasswordError = false

        if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailValidationError = true
            currentEmailError = true
        }

        if(!currentEmailError && emailValidationError) {
            emailValidationError = false
            _clearEmailError.value = Event(Unit)
        }

        if(password.isEmpty() || password.length <= 5) {
            passwordValidationError = true
            currentPasswordError = true
        }

        if(!currentPasswordError && passwordValidationError) {
            passwordValidationError = false
            _clearPasswordError.value = Event(Unit)
        }

        if(currentEmailError && currentPasswordError) {
            _fieldErrors.value = Event(ErrorMessage.EMAIL_INVALID_MESSAGE.errorMessage + "$" + ErrorMessage.PASSWORD_INVALID_MESSAGE.errorMessage)
        } else if(currentEmailError) {
            _emailError.value = Event(ErrorMessage.EMAIL_INVALID_MESSAGE.errorMessage)
        } else if(currentPasswordError) {
            _passwordError.value = Event(ErrorMessage.PASSWORD_INVALID_MESSAGE.errorMessage)
        }

        if(currentEmailError || currentPasswordError) {
            return
        }

        _dataLoaded.value = Event(true)

        viewModelScope.launch {
            platysRepository.signInUser(email, password).let { result ->
                _dataLoaded.value = Event(false)

                if(result is Result.Success) {
                    _navigationEvent.value = Event(NavigationDestination.LOGIN_SUCCESS)
                } else {
                    _navigationEvent.value = Event(NavigationDestination.LOGIN_FAILURE)
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        this.email = email
    }

    fun onPasswordChanged(password: String) {
        this.password = password
    }

    fun onResume() {
        _clearEmailError.value = Event(Unit)
        _clearPasswordError.value = Event(Unit)
    }
}
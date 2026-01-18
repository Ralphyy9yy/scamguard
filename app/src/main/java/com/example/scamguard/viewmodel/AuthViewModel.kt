package com.example.scamguard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.preferences.ThemePreferences
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _currentUser = MutableLiveData(authRepository.currentUser())
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isDarkMode = MutableLiveData(false)
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.postValue(firebaseAuth.currentUser)
    }

    init {
        authRepository.addAuthStateListener(authStateListener)
        viewModelScope.launch {
            themePreferences.isDarkMode.collect { enabled ->
                _isDarkMode.postValue(enabled)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _message.value = "Email and password are required."
            return
        }
        viewModelScope.launch {
            runAuthCall { authRepository.login(email.trim(), password) }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _message.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            runAuthCall { authRepository.register(email.trim(), password) }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            runAuthCall { authRepository.signInAnonymously() }
        }
    }

    fun logout() {
        authRepository.logout()
        _message.value = "Signed out."
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkMode(enabled)
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun runAuthCall(action: suspend () -> FirebaseUser) {
        try {
            _isLoading.value = true
            val user = action()
            _currentUser.value = user
            _message.value = "Hello ${user.email ?: "Guest"}"
        } catch (ex: Exception) {
            _message.value = ex.message ?: "Authentication failed"
        } finally {
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        authRepository.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}


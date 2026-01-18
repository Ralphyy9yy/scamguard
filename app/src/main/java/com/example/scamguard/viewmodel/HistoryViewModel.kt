package com.example.scamguard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamguard.data.model.ScanRecord
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.example.scamguard.data.repository.FirestoreScanRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val scanRepository: FirestoreScanRepository,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _history = MutableLiveData<List<ScanRecord>>(emptyList())
    val history: LiveData<List<ScanRecord>> = _history

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var registration: ListenerRegistration? = null

    fun startListening() {
        registration?.remove()
        val userId = authRepository.currentUser()?.uid ?: run {
            _history.value = emptyList()
            return
        }
        registration = scanRepository.listenToHistory(
            userId = userId,
            onChange = { _history.postValue(it) },
            onError = { _error.postValue(it.message) }
        )
    }

    fun clearHistory() {
        val userId = authRepository.currentUser()?.uid ?: run {
            _error.value = "Login required"
            return
        }
        viewModelScope.launch {
            try {
                scanRepository.clearUserScans(userId)
            } catch (ex: Exception) {
                _error.value = ex.message
            }
        }
    }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }
}


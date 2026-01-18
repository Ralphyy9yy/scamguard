package com.example.scamguard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scamguard.data.model.ScanRecord
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.example.scamguard.data.repository.FirestoreScanRepository
import com.google.firebase.firestore.ListenerRegistration

class HomeViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val scanRepository: FirestoreScanRepository
) : ViewModel() {

    private val _greeting = MutableLiveData("")
    val greeting: LiveData<String> = _greeting

    private val _latestScan = MutableLiveData<ScanRecord?>()
    val latestScan: LiveData<ScanRecord?> = _latestScan

    private val _tips = MutableLiveData(
        listOf(
            "Never click shortened links from unknown senders.",
            "Banks will never ask for full passwords over SMS.",
            "Verify prizes by calling official hotlines."
        )
    )
    val tips: LiveData<List<String>> = _tips

    private var latestRegistration: ListenerRegistration? = null

    init {
        updateGreeting()
        attachLatestScan()
    }

    fun refreshUserData() {
        updateGreeting()
        attachLatestScan()
    }

    private fun updateGreeting() {
        val email = authRepository.currentUser()?.email
        val name = email?.substringBefore("@").orEmpty().ifBlank { "Explorer" }
        _greeting.value = "Welcome back, ${name.replaceFirstChar { it.uppercaseChar() }}"
    }

    private fun attachLatestScan() {
        latestRegistration?.remove()
        val userId = authRepository.currentUser()?.uid ?: run {
            _latestScan.postValue(null)
            return
        }
        latestRegistration = scanRepository.listenToLatest(userId) { latest ->
            _latestScan.postValue(latest)
        }
    }

    override fun onCleared() {
        latestRegistration?.remove()
        super.onCleared()
    }
}


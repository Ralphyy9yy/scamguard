package com.example.scamguard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scamguard.data.preferences.ThemePreferences
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.example.scamguard.data.repository.FirestoreArticleRepository
import com.example.scamguard.data.repository.FirestoreScanRepository
import com.example.scamguard.viewmodel.AuthViewModel
import com.example.scamguard.viewmodel.HistoryViewModel
import com.example.scamguard.viewmodel.HomeViewModel
import com.example.scamguard.viewmodel.LearnViewModel
import com.example.scamguard.viewmodel.ScanViewModel

class ScamGuardViewModelFactory(
    private val authRepository: FirebaseAuthRepository,
    private val scanRepository: FirestoreScanRepository,
    private val articleRepository: FirestoreArticleRepository,
    private val themePreferences: ThemePreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepository, scanRepository) as T
            }

            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                ScanViewModel(scanRepository, authRepository) as T
            }

            modelClass.isAssignableFrom(LearnViewModel::class.java) -> {
                LearnViewModel(articleRepository) as T
            }

            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(scanRepository, authRepository) as T
            }

            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, themePreferences) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}


package com.example.scamguard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.scamguard.R
import com.example.scamguard.data.preferences.ThemePreferences
import com.example.scamguard.data.repository.FirebaseAuthRepository
import com.example.scamguard.data.repository.FirestoreArticleRepository
import com.example.scamguard.data.repository.FirestoreScanRepository
import com.example.scamguard.databinding.ActivityMainBinding
import com.example.scamguard.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authRepository: FirebaseAuthRepository
    lateinit var viewModelFactory: ScamGuardViewModelFactory
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance())
        val firestore = FirebaseFirestore.getInstance()
        ensureAuthenticatedUser()
        viewModelFactory = ScamGuardViewModelFactory(
            authRepository = authRepository,
            scanRepository = FirestoreScanRepository(firestore),
            articleRepository = FirestoreArticleRepository(firestore),
            themePreferences = ThemePreferences(applicationContext)
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        val topDestinations = setOf(
            R.id.homeFragment,
            R.id.historyFragment,
            R.id.settingsFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in topDestinations
        }

        val authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
        authViewModel.isDarkMode.observe(this) { enabled ->
            val mode = if (enabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun ensureAuthenticatedUser() {
        lifecycleScope.launch {
            try {
                authRepository.ensureUser()
            } catch (error: Exception) {
                Log.w(TAG, "Unable to create anonymous Firebase user", error)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}


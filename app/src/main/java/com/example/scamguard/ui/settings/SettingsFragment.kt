package com.example.scamguard.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentSettingsBinding
import com.example.scamguard.viewmodel.AuthViewModel
import com.example.scamguard.viewmodel.HistoryViewModel
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var suppressDarkToggle = false

    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }
    private val historyViewModel: HistoryViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.manageAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
        }
        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
        }
        binding.clearHistoryButton.setOnClickListener {
            historyViewModel.clearHistory()
        }
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressDarkToggle) {
                authViewModel.setDarkMode(isChecked)
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.userStatus.text = user?.email ?: "Anonymous session"
            binding.logoutButton.isVisible = user != null
        }
        authViewModel.isDarkMode.observe(viewLifecycleOwner) { enabled ->
            suppressDarkToggle = true
            binding.darkModeSwitch.isChecked = enabled
            suppressDarkToggle = false
        }
        historyViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


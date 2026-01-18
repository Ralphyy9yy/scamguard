package com.example.scamguard.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentAuthBinding
import com.example.scamguard.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.authLoginButton.setOnClickListener {
            authViewModel.login(
                binding.authEmailInput.text?.toString().orEmpty(),
                binding.authPasswordInput.text?.toString().orEmpty()
            )
        }
        binding.authRegisterButton.setOnClickListener {
            authViewModel.register(
                binding.authEmailInput.text?.toString().orEmpty(),
                binding.authPasswordInput.text?.toString().orEmpty()
            )
        }
        binding.authAnonButton.setOnClickListener {
            authViewModel.signInAnonymously()
        }
        binding.authLogoutButton.setOnClickListener {
            authViewModel.logout()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.authUserStatus.text = user?.email ?: getString(R.string.anonymous_login)
        }
        authViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.authProgress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.authLoginButton.isEnabled = !loading
            binding.authRegisterButton.isEnabled = !loading
            binding.authAnonButton.isEnabled = !loading
        }
        authViewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                authViewModel.clearMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


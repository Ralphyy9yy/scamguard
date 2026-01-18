package com.example.scamguard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentHomeBinding
import com.example.scamguard.util.ScamVerdict
import com.example.scamguard.viewmodel.AuthViewModel
import com.example.scamguard.viewmodel.HomeViewModel
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        binding.quickScanButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_quickScanFragment)
        }
        binding.scanSmsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_smsScanFragment)
        }
        binding.scanQrButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_qrScanFragment)
        }
        binding.learnButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_learnFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.greeting.observe(viewLifecycleOwner) { greeting ->
            binding.greetingText.text = greeting
        }
        viewModel.latestScan.observe(viewLifecycleOwner) { record ->
            if (record == null) {
                binding.lastScanCard.visibility = View.GONE
            } else {
                val verdict = ScamVerdict.fromFirestoreValue(record.result)
                binding.lastScanCard.visibility = View.VISIBLE
                binding.lastScanResult.text = verdict.displayName
                binding.lastScanType.text = record.type.uppercase(Locale.getDefault())
                binding.lastScanSnippet.text = record.content.take(90)
            }
        }
        viewModel.tips.observe(viewLifecycleOwner) { tips ->
            binding.tipOne.text = tips.getOrNull(0)
            binding.tipTwo.text = tips.getOrNull(1)
            binding.tipThree.text = tips.getOrNull(2)
        }

        authViewModel.currentUser.observe(viewLifecycleOwner) {
            viewModel.refreshUserData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


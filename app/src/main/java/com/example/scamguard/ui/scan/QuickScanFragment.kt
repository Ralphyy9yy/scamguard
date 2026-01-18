package com.example.scamguard.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentQuickScanBinding
import com.example.scamguard.util.ScamVerdict
import com.example.scamguard.viewmodel.ScanViewModel
import com.google.android.material.snackbar.Snackbar

class QuickScanFragment : Fragment() {

    private var _binding: FragmentQuickScanBinding? = null
    private val binding get() = _binding!!

    private val scanViewModel: ScanViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quickScanAction.setOnClickListener {
            val customMessage = binding.quickScanInput.text?.toString()
            scanViewModel.runQuickScan(customMessage)
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        scanViewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            binding.quickScanAction.isEnabled = !isScanning
            binding.scanProgress.isVisible = isScanning
        }
        scanViewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (result == null || result.type != "quick") {
                binding.scanResultCard.isVisible = false
                return@observe
            }
            binding.scanResultCard.isVisible = true
            binding.scanResultTitle.text = result.verdict.displayName
            binding.scanResultReason.text = result.reason
            binding.scanResultContent.text = result.content
            val colorRes = when (result.verdict) {
                ScamVerdict.SAFE -> R.color.status_safe
                ScamVerdict.SUSPICIOUS -> R.color.status_suspicious
                ScamVerdict.DANGER -> R.color.status_danger
            }
            binding.scanResultTitle.setTextColor(
                ContextCompat.getColor(requireContext(), colorRes)
            )
        }
        scanViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                scanViewModel.clearStatusMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


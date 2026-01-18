package com.example.scamguard.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scamguard.MainActivity
import com.example.scamguard.databinding.FragmentSmsScanBinding
import com.example.scamguard.ui.adapters.SmsAdapter
import com.example.scamguard.viewmodel.ScanViewModel
import com.google.android.material.snackbar.Snackbar

class SmsScanFragment : Fragment() {

    private var _binding: FragmentSmsScanBinding? = null
    private val binding get() = _binding!!
    private var lastHandledResultTimestamp: Long = 0

    private val scanViewModel: ScanViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    private lateinit var smsAdapter: SmsAdapter

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val rootBinding = _binding ?: return@registerForActivityResult
            if (granted) {
                loadSms()
            } else {
                Snackbar.make(rootBinding.root, "SMS permission is required", Snackbar.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmsScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        smsAdapter = SmsAdapter { message ->
            scanViewModel.scanSms(message)
        }
        binding.smsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.smsRecycler.adapter = smsAdapter
        binding.swipeRefresh.setOnRefreshListener {
            loadSms()
        }
        binding.requestPermissionButton.setOnClickListener {
            requestSmsPermission()
        }
        observeViewModel()
        loadSms()
    }

    private fun loadSms() {
        val context = context ?: return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            scanViewModel.loadDeviceSms(context.contentResolver)
        } else {
            binding.requestPermissionContainer.isVisible = true
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    private fun requestSmsPermission() {
        permissionLauncher.launch(Manifest.permission.READ_SMS)
    }

    private fun observeViewModel() {
        scanViewModel.smsMessages.observe(viewLifecycleOwner) { messages ->
            binding.swipeRefresh.isRefreshing = false
            binding.requestPermissionContainer.isVisible = false
            smsAdapter.submitList(messages)
            binding.emptyState.isVisible = messages.isEmpty()
        }
        scanViewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (result != null && result.type == "sms" && result.timestamp != lastHandledResultTimestamp) {
                lastHandledResultTimestamp = result.timestamp
                Snackbar.make(binding.root, "${result.verdict.displayName}: ${result.reason}", Snackbar.LENGTH_LONG).show()
            }
        }
        scanViewModel.isScanning.observe(viewLifecycleOwner) { loading ->
            binding.scanLoading.isVisible = loading
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


package com.example.scamguard.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scamguard.MainActivity
import com.example.scamguard.databinding.FragmentHistoryBinding
import com.example.scamguard.ui.adapters.ScanHistoryAdapter
import com.example.scamguard.viewmodel.AuthViewModel
import com.example.scamguard.viewmodel.HistoryViewModel
import com.google.android.material.snackbar.Snackbar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }
    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    private lateinit var adapter: ScanHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ScanHistoryAdapter()
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener {
            historyViewModel.startListening()
        }
        observeViewModel()
        authViewModel.currentUser.observe(viewLifecycleOwner) {
            historyViewModel.startListening()
        }
    }

    override fun onResume() {
        super.onResume()
        historyViewModel.startListening()
    }

    private fun observeViewModel() {
        historyViewModel.history.observe(viewLifecycleOwner) { items ->
            binding.swipeRefresh.isRefreshing = false
            adapter.submitList(items)
            binding.emptyState.isVisible = items.isEmpty()
        }
        historyViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                binding.swipeRefresh.isRefreshing = false
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


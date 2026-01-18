package com.example.scamguard.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentLearnBinding
import com.example.scamguard.ui.adapters.ArticleAdapter
import com.example.scamguard.viewmodel.LearnViewModel
import com.google.android.material.snackbar.Snackbar

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    private val learnViewModel: LearnViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleAdapter = ArticleAdapter { article ->
            val args = bundleOf(
                "title" to article.title,
                "summary" to article.summary,
                "content" to article.content,
                "iconEmoji" to article.iconEmoji
            )
            findNavController().navigate(R.id.action_learnFragment_to_articleDetailFragment, args)
        }
        binding.articlesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.articlesRecycler.adapter = articleAdapter
        observeViewModel()
    }

    private fun observeViewModel() {
        learnViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter.submitList(articles)
            binding.emptyState.isVisible = articles.isEmpty()
        }
        learnViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressIndicator.isVisible = loading
        }
        learnViewModel.error.observe(viewLifecycleOwner) { error ->
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


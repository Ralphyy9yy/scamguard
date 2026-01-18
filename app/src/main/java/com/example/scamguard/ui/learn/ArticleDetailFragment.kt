package com.example.scamguard.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.scamguard.databinding.FragmentArticleDetailBinding

class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title").orEmpty()
        val summary = arguments?.getString("summary").orEmpty()
        val content = arguments?.getString("content").orEmpty()
        val iconEmoji = arguments?.getString("iconEmoji").orEmpty().ifBlank { "💡" }

        binding.iconEmoji.text = iconEmoji
        binding.articleTitle.text = title
        binding.articleSummary.text = summary
        binding.articleContent.text = content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


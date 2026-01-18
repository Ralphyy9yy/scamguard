package com.example.scamguard.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.scamguard.data.model.Article
import com.example.scamguard.databinding.ItemArticleBinding

class ArticleAdapter(
    private val onItemClicked: (Article) -> Unit
) : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.iconEmoji.text = article.iconEmoji.ifBlank { "💡" }
            binding.articleTitle.text = article.title
            binding.articleSummary.text = article.summary
            binding.root.setOnClickListener { onItemClicked(article) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem == newItem
    }
}


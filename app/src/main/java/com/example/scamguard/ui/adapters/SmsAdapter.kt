package com.example.scamguard.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.scamguard.data.model.SmsMessage
import com.example.scamguard.databinding.ItemSmsMessageBinding

class SmsAdapter(
    private val onScanClicked: (SmsMessage) -> Unit
) : ListAdapter<SmsMessage, SmsAdapter.SmsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val binding = ItemSmsMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SmsViewHolder(
        private val binding: ItemSmsMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: SmsMessage) {
            binding.smsSender.text = message.address ?: "Unknown sender"
            binding.smsPreview.text = message.body
            binding.scanSmsButton.setOnClickListener { onScanClicked(message) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SmsMessage>() {
        override fun areItemsTheSame(oldItem: SmsMessage, newItem: SmsMessage): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SmsMessage, newItem: SmsMessage): Boolean =
            oldItem == newItem
    }
}


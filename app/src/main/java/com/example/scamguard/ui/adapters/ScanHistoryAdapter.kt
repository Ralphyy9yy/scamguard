package com.example.scamguard.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.scamguard.R
import com.example.scamguard.data.model.ScanRecord
import com.example.scamguard.databinding.ItemScanHistoryBinding
import com.example.scamguard.util.ScamVerdict
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter : ListAdapter<ScanRecord, ScanHistoryAdapter.HistoryViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemScanHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemScanHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: ScanRecord) {
            val context = binding.root.context
            val verdict = ScamVerdict.fromFirestoreValue(record.result)
            val colorRes = when (verdict) {
                ScamVerdict.SAFE -> R.color.status_safe
                ScamVerdict.SUSPICIOUS -> R.color.status_suspicious
                ScamVerdict.DANGER -> R.color.status_danger
            }

            binding.scanType.text = record.type.uppercase(Locale.getDefault())
            binding.scanTimestamp.text = dateFormat.format(Date(record.timestamp))
            binding.scanResult.text = verdict.displayName
            binding.scanResult.setTextColor(ContextCompat.getColor(context, colorRes))
            binding.scanSnippet.text = record.content.take(80)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ScanRecord>() {
        override fun areItemsTheSame(oldItem: ScanRecord, newItem: ScanRecord): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ScanRecord, newItem: ScanRecord): Boolean =
            oldItem == newItem
    }
}


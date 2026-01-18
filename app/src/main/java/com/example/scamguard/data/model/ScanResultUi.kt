package com.example.scamguard.data.model

import com.example.scamguard.util.ScamVerdict

/**
 * UI-friendly representation of a scan verdict with optional reason.
 */
data class ScanResultUi(
    val type: String,
    val content: String,
    val verdict: ScamVerdict,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)


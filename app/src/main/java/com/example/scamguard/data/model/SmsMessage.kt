package com.example.scamguard.data.model

/**
 * Lightweight representation of an SMS row pulled from the content provider.
 */
data class SmsMessage(
    val id: String,
    val address: String?,
    val body: String,
    val timestamp: Long
)


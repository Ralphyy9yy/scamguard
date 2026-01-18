package com.example.scamguard.data.model

/**
 * Persisted scan document aligned with Firestore schema.
 */
data class ScanRecord(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val content: String = "",
    val result: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


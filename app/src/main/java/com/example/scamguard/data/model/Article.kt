package com.example.scamguard.data.model

/**
 * Represents a learn article stored in Firestore.
 */
data class Article(
    val id: String = "",
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val iconEmoji: String = "💡"
)


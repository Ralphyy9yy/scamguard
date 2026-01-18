package com.example.scamguard.data.repository

import com.example.scamguard.data.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FirestoreArticleRepository(
    firestore: FirebaseFirestore
) {

    private val articlesCollection = firestore.collection("articles")

    fun listenToArticles(
        onChange: (List<Article>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return articlesCollection
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    onChange(FALLBACK_ARTICLES)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents
                if (documents.isNullOrEmpty()) {
                    onChange(FALLBACK_ARTICLES)
                    return@addSnapshotListener
                }

                val articles = documents.map { doc ->
                    Article(
                        id = doc.id,
                        title = doc.getString("title").orEmpty(),
                        summary = doc.getString("summary").orEmpty(),
                        content = doc.getString("content").orEmpty(),
                        iconEmoji = doc.getString("iconEmoji").orEmpty().ifEmpty { "💡" }
                    )
                }
                onChange(articles)
            }
    }

    companion object {
        private val FALLBACK_ARTICLES = listOf(
            Article(
                id = "phishing-basics",
                title = "Spotting Phishing SMS",
                summary = "Look for urgent tone, suspicious links, and spelling mistakes.",
                content = """
                    Fraudsters send phishing SMS to trick you into revealing banking details.
                    • Never tap shortened links from unknown senders.
                    • Banks never ask you to confirm PINs, OTPs, or passwords over SMS.
                    • If the message mentions locked accounts, call your bank using the official number.
                """.trimIndent(),
                iconEmoji = "📱"
            ),
            Article(
                id = "delivery-scams",
                title = "Fake Delivery Notices",
                summary = "Scammers pose as couriers asking for small 're-delivery fees'.",
                content = """
                    Delivery scams surge around holidays. Warning signs include:
                    • Requests for payment via unfamiliar gateways.
                    • Links that redirect to sites without HTTPS.
                    • Messages that don't mention your name or order number.
                """.trimIndent(),
                iconEmoji = "📦"
            ),
            Article(
                id = "investment-redflags",
                title = "Investment Red Flags",
                summary = "Guaranteed returns or pressure to invest fast = scam.",
                content = """
                    Before investing:
                    • Verify the company registration with regulators.
                    • Be wary of testimonials sent via SMS or messaging apps.
                    • Talk to someone you trust if you feel rushed.
                """.trimIndent(),
                iconEmoji = "💰"
            )
        )
    }
}


package com.example.scamguard.data.repository

import com.example.scamguard.data.model.ScanRecord
import com.example.scamguard.util.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FirestoreScanRepository(
    firestore: FirebaseFirestore
) {

    private val db = firestore
    private val scansCollection = db.collection("scans")

    suspend fun saveScan(record: ScanRecord) {
        scansCollection.add(record.toMap()).await()
    }

    suspend fun clearUserScans(userId: String) {
        val snapshots = scansCollection.whereEqualTo("userId", userId).get().await()
        if (snapshots.isEmpty) return
        db.runBatch { batch ->
            snapshots.documents.forEach { batch.delete(it.reference) }
        }.await()
    }

    fun listenToHistory(
        userId: String,
        onChange: (List<ScanRecord>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return scansCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toScanRecord()
                }
                onChange(records)
            }
    }

    fun listenToLatest(
        userId: String,
        onChange: (ScanRecord?) -> Unit
    ): ListenerRegistration {
        return scansCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                val latest = snapshot?.documents?.firstOrNull()?.toScanRecord()
                onChange(latest)
            }
    }

    private fun ScanRecord.toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "type" to type,
            "content" to content,
            "result" to result,
            "timestamp" to timestamp
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toScanRecord(): ScanRecord? {
        return try {
            ScanRecord(
                id = id,
                userId = getString("userId").orEmpty(),
                type = getString("type").orEmpty(),
                content = getString("content").orEmpty(),
                result = getString("result").orEmpty(),
                timestamp = getLong("timestamp") ?: System.currentTimeMillis()
            )
        } catch (ex: Exception) {
            null
        }
    }
}


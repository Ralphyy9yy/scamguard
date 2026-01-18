package com.example.scamguard.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Minimal Task.await implementation to avoid bringing an extra dependency.
 */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}


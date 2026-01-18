package com.example.scamguard.data.repository

import com.example.scamguard.util.await
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) {

    fun currentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.removeAuthStateListener(listener)
    }

    suspend fun login(email: String, password: String): FirebaseUser {
        return firebaseAuth.signInWithEmailAndPassword(email, password).awaitAuthUser()
    }

    suspend fun register(email: String, password: String): FirebaseUser {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).awaitAuthUser()
    }

    suspend fun signInAnonymously(): FirebaseUser {
        return firebaseAuth.signInAnonymously().awaitAuthUser()
    }

    suspend fun ensureUser(): FirebaseUser {
        return currentUser() ?: signInAnonymously()
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private suspend fun com.google.android.gms.tasks.Task<AuthResult>.awaitAuthUser(): FirebaseUser {
        return await().user ?: throw IllegalStateException("Firebase user is null")
    }
}


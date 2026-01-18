package com.example.scamguard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scamguard.data.model.Article
import com.example.scamguard.data.repository.FirestoreArticleRepository
import com.google.firebase.firestore.ListenerRegistration

class LearnViewModel(
    private val articleRepository: FirestoreArticleRepository
) : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>(emptyList())
    val articles: LiveData<List<Article>> = _articles

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var registration: ListenerRegistration? = null

    init {
        observeArticles()
    }

    private fun observeArticles() {
        _isLoading.value = true
        registration = articleRepository.listenToArticles(
            onChange = {
                _articles.value = it
                _isLoading.value = false
            },
            onError = { throwable ->
                _error.value = throwable.message
                _isLoading.value = false
            }
        )
    }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }
}


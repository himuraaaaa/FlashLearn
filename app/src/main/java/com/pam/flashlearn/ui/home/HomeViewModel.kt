package com.pam.flashlearn.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.model.FlashcardSet
import com.pam.flashlearn.data.repository.AuthRepository
import com.pam.flashlearn.data.repository.FlashcardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _recentSets = mutableStateListOf<FlashcardSet>()
    val recentSets: List<FlashcardSet> = _recentSets

    private val _searchResults = mutableStateListOf<FlashcardSet>()
    val searchResults: List<FlashcardSet> = _searchResults

    var isLoading by mutableStateOf(false)
        private set

    var isSearching by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    fun loadRecentSets() {
        val userId = authRepository.currentUser?.uid ?: return

        isLoading = true
        viewModelScope.launch {
            flashcardRepository.getRecentSets(userId)
                .collectLatest { sets ->
                    _recentSets.clear()
                    _recentSets.addAll(sets)
                    isLoading = false
                }
        }
    }

    fun searchSets(query: String) {
        if (query.isBlank()) {
            _searchResults.clear()
            return
        }

        val userId = authRepository.currentUser?.uid ?: return

        // Debounce search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            isSearching = true
            delay(300) // Debounce delay

            flashcardRepository.searchSets(userId, query)
                .collectLatest { sets ->
                    _searchResults.clear()
                    _searchResults.addAll(sets)
                    isSearching = false
                }
        }
    }
}

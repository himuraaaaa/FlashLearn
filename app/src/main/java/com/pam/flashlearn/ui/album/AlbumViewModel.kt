package com.pam.flashlearn.ui.album

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.model.FlashcardSet
import com.pam.flashlearn.data.repository.AuthRepository
import com.pam.flashlearn.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _sets = mutableStateListOf<FlashcardSet>()
    val sets: List<FlashcardSet> = _sets

    var isLoading by mutableStateOf(false)
        private set

    fun loadAllSets() {
        val userId = authRepository.currentUser?.uid ?: return

        isLoading = true
        viewModelScope.launch {
            flashcardRepository.getUserSets(userId)
                .collectLatest { userSets ->
                    _sets.clear()
                    _sets.addAll(userSets)
                    isLoading = false
                }
        }
    }
}

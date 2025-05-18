package com.pam.flashlearn.ui.flashcard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.model.Flashcard
import com.pam.flashlearn.data.model.FlashcardSet
import com.pam.flashlearn.data.repository.AuthRepository
import com.pam.flashlearn.data.repository.FlashcardRepository
import com.pam.flashlearn.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var currentSet by mutableStateOf<FlashcardSet?>(null)
        private set

    private val _flashcards = mutableStateListOf<Flashcard>()
    val flashcards: List<Flashcard> = _flashcards

    var createdSetId by mutableStateOf<String?>(null)
        private set

    var flashcardCreated by mutableStateOf(false)
        private set

    fun loadSet(setId: String) {
        isLoading = true
        viewModelScope.launch {
            println("Loading set with ID: $setId")
            flashcardRepository.getSetById(setId)
                .onSuccess { set ->
                    currentSet = set
                    println("Set loaded successfully: ${set.title}")

                    // Update recent sets for the user
                    val userId = authRepository.currentUser?.uid
                    if (userId != null) {
                        userRepository.updateRecentSets(userId, setId)
                    }
                }
                .onFailure { error ->
                    println("Error loading set: ${error.message}")
                }
            isLoading = false
        }
    }

    fun loadFlashcards(setId: String) {
        isLoading = true
        viewModelScope.launch {
            println("Loading flashcards for set ID: $setId")
            flashcardRepository.getFlashcardsBySetId(setId)
                .collectLatest { cards ->
                    println("Received ${cards.size} flashcards from repository")
                    _flashcards.clear()
                    _flashcards.addAll(cards)
                    isLoading = false
                }
        }
    }

    fun createSet(title: String, description: String) {
        val userId = authRepository.currentUser?.uid ?: return

        val set = FlashcardSet(
            title = title,
            description = description,
            userId = userId,
            cardCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastAccessed = System.currentTimeMillis()
        )

        viewModelScope.launch {
            isLoading = true
            println("Creating new set: $title")
            flashcardRepository.createSet(set)
                .onSuccess { id ->
                    createdSetId = id
                    println("Set created successfully with ID: $id")
                }
                .onFailure { error ->
                    println("Error creating set: ${error.message}")
                }
            isLoading = false
        }
    }

    fun resetCreatedSetId() {
        createdSetId = null
    }

    fun createFlashcard(setId: String, question: String, answer: String) {
        // Validate inputs
        if (setId.isBlank() || question.isBlank() || answer.isBlank()) {
            println("Invalid inputs for creating flashcard")
            return
        }

        val currentTime = System.currentTimeMillis()
        val flashcard = Flashcard(
            id = "", // ID will be assigned by Firebase
            question = question,
            answer = answer,
            setId = setId,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        viewModelScope.launch {
            isLoading = true
            println("Creating flashcard: Q: $question, A: $answer for set: $setId")

            flashcardRepository.createFlashcard(flashcard)
                .onSuccess { id ->
                    println("Flashcard created successfully with ID: $id")
                    flashcardCreated = true

                    // Reload flashcards to update the list
                    loadFlashcards(setId)

                    // Also reload the set to update the card count
                    loadSet(setId)
                }
                .onFailure { error ->
                    println("Error creating flashcard: ${error.message}")
                }

            isLoading = false
        }
    }

    fun resetFlashcardCreated() {
        flashcardCreated = false
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            isLoading = true
            println("Deleting flashcard with ID: ${flashcard.id}")

            flashcardRepository.deleteFlashcard(flashcard)
                .onSuccess {
                    println("Flashcard deleted successfully")

                    // Remove from local list
                    val index = _flashcards.indexOfFirst { it.id == flashcard.id }
                    if (index != -1) {
                        _flashcards.removeAt(index)
                    }

                    // Refresh the set to update card count
                    flashcard.setId.let { setId ->
                        loadSet(setId)
                    }
                }
                .onFailure { error ->
                    println("Error deleting flashcard: ${error.message}")
                }

            isLoading = false
        }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            isLoading = true
            println("Deleting set with ID: $setId")

            flashcardRepository.deleteSet(setId)
                .onSuccess {
                    println("Set deleted successfully")
                }
                .onFailure { error ->
                    println("Error deleting set: ${error.message}")
                }

            isLoading = false
        }
    }

    fun shuffleFlashcards() {
        println("Shuffling ${_flashcards.size} flashcards")
        val shuffled = _flashcards.shuffled()
        _flashcards.clear()
        _flashcards.addAll(shuffled)
    }

    fun updateFlashcard(flashcard: Flashcard, newQuestion: String, newAnswer: String) {
        if (newQuestion.isBlank() || newAnswer.isBlank()) return

        val updatedFlashcard = flashcard.copy(
            question = newQuestion,
            answer = newAnswer,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            isLoading = true
            println("Updating flashcard with ID: ${flashcard.id}")

            flashcardRepository.updateFlashcard(updatedFlashcard)
                .onSuccess {
                    println("Flashcard updated successfully")

                    // Update in local list
                    val index = _flashcards.indexOfFirst { it.id == flashcard.id }
                    if (index != -1) {
                        _flashcards[index] = updatedFlashcard
                    }
                }
                .onFailure { error ->
                    println("Error updating flashcard: ${error.message}")
                }

            isLoading = false
        }
    }
    // Tambahkan fungsi ini di FlashcardViewModel.kt
    fun forceRefresh(setId: String) {
        viewModelScope.launch {
            println("Force refreshing data for set: $setId")

            // Clear existing data
            _flashcards.clear()
            currentSet = null

            // Reload data
            loadSet(setId)
            loadFlashcards(setId)
        }
    }

}

package com.pam.flashlearn.ui.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.model.FlashcardSet
import com.pam.flashlearn.data.model.QuizQuestion
import com.pam.flashlearn.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AnsweredQuestion(
    val question: QuizQuestion,
    val selectedAnswer: String,
    val isCorrect: Boolean
)

class QuizViewModel(
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var currentSet by mutableStateOf<FlashcardSet?>(null)
        private set

    private val _questions = mutableStateListOf<QuizQuestion>()
    val questions: List<QuizQuestion> = _questions

    private val _answeredQuestions = mutableStateListOf<AnsweredQuestion>()
    val answeredQuestions: List<AnsweredQuestion> = _answeredQuestions

    fun loadSet(setId: String) {
        isLoading = true
        viewModelScope.launch {
            flashcardRepository.getSetById(setId)
                .onSuccess { set ->
                    currentSet = set
                }
                .onFailure {
                    // Handle error
                }
            isLoading = false
        }
    }

    fun generateQuiz(setId: String) {
        isLoading = true
        viewModelScope.launch {
            flashcardRepository.generateQuizQuestions(setId)
                .collectLatest { quizQuestions ->
                    _questions.clear()
                    _questions.addAll(quizQuestions)
                    isLoading = false
                }
        }
    }

    fun recordAnswer(question: QuizQuestion, selectedAnswer: String, isCorrect: Boolean) {
        val answeredQuestion = AnsweredQuestion(
            question = question,
            selectedAnswer = selectedAnswer,
            isCorrect = isCorrect
        )
        _answeredQuestions.add(answeredQuestion)
    }

    fun resetQuiz() {
        _questions.clear()
        _answeredQuestions.clear()
    }
}

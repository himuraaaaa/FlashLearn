package com.pam.flashlearn.data.model

data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val flashcardId: String = ""
)

data class QuizResult(
    val id: String = "",
    val setId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)

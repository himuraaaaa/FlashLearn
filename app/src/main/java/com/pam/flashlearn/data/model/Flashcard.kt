package com.pam.flashlearn.data.model

data class Flashcard(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val setId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

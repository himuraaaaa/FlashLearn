package com.pam.flashlearn.data.model

data class FlashcardSet(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val userId: String = "",
    val cardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)

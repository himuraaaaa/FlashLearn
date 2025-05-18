package com.pam.flashlearn.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val recentSets: List<String> = emptyList()
)

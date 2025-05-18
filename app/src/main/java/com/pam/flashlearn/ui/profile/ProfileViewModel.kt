package com.pam.flashlearn.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.model.User
import com.pam.flashlearn.data.repository.AuthRepository
import com.pam.flashlearn.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    fun loadUserProfile() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .collectLatest { profile ->
                    user = profile
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}


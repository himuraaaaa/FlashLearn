package com.pam.flashlearn.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pam.flashlearn.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(authRepository.currentUser != null)
        private set

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess {
                    isLoggedIn = true
                    callback(true, null)
                }
                .onFailure {
                    callback(false, it.message ?: "Login failed")
                }
            isLoading = false
        }
    }

    fun register(name: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            authRepository.register(name, email, password)
                .onSuccess {
                    isLoggedIn = true
                    callback(true, null)
                }
                .onFailure {
                    callback(false, it.message ?: "Registration failed")
                }
            isLoading = false
        }
    }

    fun logout() {
        authRepository.logout()
        isLoggedIn = false
    }
}

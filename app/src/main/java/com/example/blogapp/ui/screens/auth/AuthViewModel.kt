package com.example.blogapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.repository.AuthRepository
import com.example.blogapp.util.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository // This now injects the interface
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        // Now it correctly accesses the property on the injected repository
        val currentUser = authRepository.currentUser
        _authState.value = _authState.value.copy(user = currentUser)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signIn(email, password).collect { result ->
                processResult(result)
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            authRepository.signUp(email, password, displayName).collect { result ->
                processResult(result)
            }
        }
    }

    // Helper function to reduce repeated code
    private fun processResult(result: Resource<FirebaseUser>) {
        when (result) {
            is Resource.Loading -> {
                _authState.value = _authState.value.copy(isLoading = true, error = null)
            }
            is Resource.Success -> {
                _authState.value = AuthState(
                    isLoading = false,
                    user = result.data,
                    error = null
                )
            }
            is Resource.Error -> {
                _authState.value = AuthState(
                    isLoading = false,
                    user = null,
                    error = result.message
                )
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            authRepository.resetPassword(email).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _authState.value = _authState.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Password reset email sent" // Use a success message, not an error
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState() // Reset the state
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

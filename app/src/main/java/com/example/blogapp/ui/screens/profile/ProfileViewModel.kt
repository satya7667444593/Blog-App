package com.example.blogapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.model.User
import com.example.blogapp.data.repository.AuthRepository
import com.example.blogapp.data.repository.UserRepository
import com.example.blogapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val userPosts: List<BlogPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val userResult = userRepository.getCurrentUser()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        user = userResult.data,
                        isLoading = false
                    )

                    userResult.data?.let { user ->
                        userRepository.getUserPosts(user.userId).collectLatest { postsResource ->
                            when (postsResource) {
                                is Resource.Success -> {
                                    _state.value = _state.value.copy(
                                        userPosts = postsResource.data ?: emptyList()
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = userResult.message
                    )
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
package com.example.blogapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.repository.BlogRepository
import com.example.blogapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val posts: List<BlogPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blogRepository: BlogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            blogRepository.getPostsFlow().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            posts = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun filterByCategory(category: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                isLoading = true
            )

            if (category == null) {
                loadPosts()
            } else {
                blogRepository.getPostsByCategory(category).collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                posts = resource.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    fun searchPosts(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query, isLoading = true)

            if (query.isEmpty()) {
                loadPosts()
            } else {
                when (val resource = blogRepository.searchPosts(query)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            posts = resource.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun refresh() {
        loadPosts()
    }
}
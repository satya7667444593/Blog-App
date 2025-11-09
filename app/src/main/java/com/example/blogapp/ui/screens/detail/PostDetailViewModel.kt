package com.example.blogapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.model.Comment
import com.example.blogapp.data.repository.BlogRepository
import com.example.blogapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailState(
    val post: BlogPost? = null,
    val comments: List<Comment> = emptyList(),
    val isLiked: Boolean = false,
    val isFavorited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val blogRepository: BlogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Load post
            blogRepository.getPostById(postId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            post = resource.data,
                            isLoading = false
                        )

                        // Check if liked and favorited
                        val isLiked = blogRepository.isPostLiked(postId)
                        val isFavorited = blogRepository.isPostFavorited(postId)

                        _state.value = _state.value.copy(
                            isLiked = isLiked,
                            isFavorited = isFavorited
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    else -> {}
                }
            }

            // Load comments
            blogRepository.getComments(postId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            comments = resource.data ?: emptyList()
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            if (_state.value.isLiked) {
                when (val result = blogRepository.unlikePost(postId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLiked = false)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    else -> {}
                }
            } else {
                when (val result = blogRepository.likePost(postId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLiked = true)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            if (_state.value.isFavorited) {
                when (val result = blogRepository.unfavoritePost(postId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isFavorited = false)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    else -> {}
                }
            } else {
                when (val result = blogRepository.favoritePost(postId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isFavorited = true)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(error = result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            when (val result = blogRepository.addComment(postId, content)) {
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }
}
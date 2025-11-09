package com.example.blogapp.ui.screens.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.repository.BlogRepository
import com.example.blogapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePostState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val blogRepository: BlogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    fun createPost(
        title: String,
        content: String,
        excerpt: String,
        authorName: String,
        categories: List<String>,
        tags: List<String>,
        imageUri: Uri?,
        isPublished: Boolean
    ) {
        viewModelScope.launch {
            _state.value = CreatePostState(isLoading = true)

            val post = BlogPost(
                title = title,
                content = content,
                excerpt = excerpt,
                authorName = authorName,
                categories = categories,
                tags = tags,
                isPublished = isPublished
            )

            when (val result = blogRepository.createPost(post, imageUri)) {
                is Resource.Success -> {
                    _state.value = CreatePostState(isSuccess = true)
                }
                is Resource.Error -> {
                    _state.value = CreatePostState(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun updatePost(
        postId: String,
        title: String,
        content: String,
        excerpt: String,
        authorName: String,
        categories: List<String>,
        tags: List<String>,
        imageUri: Uri?,
        currentImageUrl: String,
        isPublished: Boolean
    ) {
        viewModelScope.launch {
            _state.value = CreatePostState(isLoading = true)

            val post = BlogPost(
                postId = postId,
                title = title,
                content = content,
                excerpt = excerpt,
                authorName = authorName,
                categories = categories,
                tags = tags,
                imageUrl = currentImageUrl,
                isPublished = isPublished
            )

            when (val result = blogRepository.updatePost(postId, post, imageUri)) {
                is Resource.Success -> {
                    _state.value = CreatePostState(isSuccess = true)
                }
                is Resource.Error -> {
                    _state.value = CreatePostState(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _state.value = CreatePostState()
    }
}
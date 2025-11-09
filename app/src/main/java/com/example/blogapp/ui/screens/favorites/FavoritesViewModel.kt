package com.example.blogapp.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.repository.BlogRepository
import com.example.blogapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesState(
    val favoritePosts: List<BlogPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val blogRepository: BlogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            blogRepository.getFavoritePosts().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            favoritePosts = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun refresh() {
        loadFavorites()
    }
}

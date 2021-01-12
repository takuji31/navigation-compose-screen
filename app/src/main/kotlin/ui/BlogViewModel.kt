package jp.takuji31.compose.navigation.example.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.takuji31.compose.navigation.KEY_SCREEN
import jp.takuji31.compose.navigation.example.model.Blog
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen
import jp.takuji31.compose.navigation.example.repository.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BlogViewModel @ViewModelInject constructor(
    private val blogRepository: BlogRepository,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val screen: ExampleScreen.Blog = checkNotNull(savedStateHandle.get(KEY_SCREEN))

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State>
        get() = _state

    init {
        viewModelScope.launch {
            load()
        }
    }

    private suspend fun load() {
        _state.value = State.Loading
        val blog = blogRepository.getBlogById(screen.blogId)
        _state.value = State.Loaded(blog)
    }

    fun reload() {
        viewModelScope.launch { load() }
    }

    sealed class State {
        object Loading : State()
        data class Loaded(val blog: Blog) : State()
    }
}

package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.takuji31.compose.navigation.example.model.Blog
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen
import jp.takuji31.compose.navigation.example.repository.BlogRepository
import jp.takuji31.compose.navigation.screen.screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blogRepository: BlogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val screen: ExampleScreen.Home by savedStateHandle.screen()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: MutableStateFlow<State>
        get() = _state

    val snackbarHostState = SnackbarHostState()

    init {
        viewModelScope.launch {
            load()
            if (screen.fromDeepLink) {
                showSnackBar(screen.deepLinkOnlyArg)
            }
        }
    }

    private suspend fun load() {
        val blogs = blogRepository.getAllBlogs()
        _state.value = State.Loaded(blogs)
    }

    fun reload() {
        viewModelScope.launch {
            _state.value = State.Loading
            load()
        }
    }

    private suspend fun showSnackBar(deepLinkOnlyArg: String?) {
        snackbarHostState.showSnackbar("Hello $deepLinkOnlyArg from Deep Link")
    }

    sealed class State {
        object Loading : State()
        data class Loaded(val blogs: List<Blog>) : State()
    }
}

package jp.takuji31.compose.navigation.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jp.takuji31.compose.navigation.example.model.Blog
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Home(
    state: HomeViewModel.State,
    snackbarHostState: SnackbarHostState,
    screen: ExampleScreen.Home,
    onReloadButtonClick: () -> Unit,
    onBottomSheetItemClicked: (ExampleScreen) -> Unit,
    onItemClick: (Blog) -> Unit,
) {
    BlogScaffold(
        currentScreen = screen,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(
                onClick = onReloadButtonClick,
                enabled = state !is HomeViewModel.State.Loading,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh home")
            }
        },
        onBottomSheetItemClicked = onBottomSheetItemClicked,
    ) {
        val blogs = (state as? HomeViewModel.State.Loaded)?.blogs
        if (blogs != null) {
            LazyColumn {
                items(blogs) { blog ->
                    ListItem(
                        icon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.clickable(onClick = { onItemClick(blog) }),
                    ) {
                        Text(text = blog.title)
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
    }
}

package jp.takuji31.compose.navigation.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jp.takuji31.compose.navigation.example.model.Entry
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen

@Composable
fun Blog(
    state: BlogViewModel.State,
    screen: ExampleScreen.Blog,
    onReloadButtonClick: () -> Unit,
    onItemClick: (Entry) -> Unit,
) {
    BlogScaffold(
        currentScreen = screen,
        actions = {
            IconButton(
                onClick = onReloadButtonClick,
                enabled = state !is BlogViewModel.State.Loading,
            ) {
                Icon(Icons.Default.Refresh)
            }
        },
    ) {
        val blog = (state as? BlogViewModel.State.Loaded)?.blog
        val entries = blog?.entries
        if (entries != null) {
            LazyColumn {
                items(entries) { entry ->
                    ListItem(
                        icon = {
                            Icon(Icons.Default.Book)
                        },
                        modifier = Modifier.clickable(onClick = { onItemClick(entry) }),
                    ) {
                        Text(text = entry.title)
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

package jp.takuji31.compose.navigation.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen

@Composable
fun Entry(
    state: EntryViewModel.State,
    screen: ExampleScreen.Entry,
    onReloadButtonClick: () -> Unit,
) {
    BlogScaffold(
        currentScreen = screen,
        title = if (state is EntryViewModel.State.Loaded) state.entry.title else screen.title,
        actions = {
            IconButton(
                onClick = onReloadButtonClick,
                enabled = state !is EntryViewModel.State.Loading,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh entries")
            }
        },
    ) {
        val entry = (state as? EntryViewModel.State.Loaded)?.entry
        if (entry != null) {
            Box(Modifier.padding(it)) {
                Text(text = entry.body)
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
    }
}

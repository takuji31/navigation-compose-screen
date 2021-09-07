package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun About(onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = onCancel) {
                Text(text = "Close")
            }
        },
        text = {
            Text(text = "This is navigation-compose-screen sample app")
        },
    )
}

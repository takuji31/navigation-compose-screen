package jp.takuji31.compose.navigation.screen

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

interface ScreenFactory<T : Screen<*>> {
    fun fromBundle(bundle: Bundle?): T
    fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): T
}

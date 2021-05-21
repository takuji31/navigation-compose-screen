package jp.takuji31.compose.navigation.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private const val SavedStateHandleScreenKey = "__navigation-compose-screen_screen__"

class SavedStateHandleScreenPropertyDelegate<T : Screen<*>>(private val screenClass: KClass<T>, private val savedStateHandle: SavedStateHandle) : ReadWriteProperty<ViewModel, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: ViewModel, property: KProperty<*>): T {
        return if (savedStateHandle.contains(SavedStateHandleScreenKey)) {
            checkNotNull(savedStateHandle[SavedStateHandleScreenKey]) as T
        } else {
            val screen = ScreenFactoryRegistry.findByClass<ScreenFactory<*>>(screenClass).fromSavedStateHandle(savedStateHandle)
            savedStateHandle.set(SavedStateHandleScreenKey, screen)
            screen as T
        }
    }

    override fun setValue(thisRef: ViewModel, property: KProperty<*>, value: T) {
        savedStateHandle.set(SavedStateHandleScreenKey, value)
    }
}

fun <T : Screen<*>> SavedStateHandle.screen(screenClass: KClass<T>): SavedStateHandleScreenPropertyDelegate<T> = SavedStateHandleScreenPropertyDelegate(screenClass, this)

inline fun <reified T : Screen<*>> SavedStateHandle.screen() = screen(T::class)

fun SavedStateHandle(
    screen: Screen<*>,
    initialState: Map<String, Any> = emptyMap(),
): SavedStateHandle = SavedStateHandle(
    initialState + (SavedStateHandleScreenKey to screen),
)

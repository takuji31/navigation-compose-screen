package jp.takuji31.compose.navigation.screen

import android.os.Build
import android.os.Bundle

inline fun <reified T : Enum<*>> Bundle.getEnum(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(key) as? T
    }

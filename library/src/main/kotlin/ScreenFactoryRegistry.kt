package jp.takuji31.compose.navigation.screen

import kotlin.reflect.KClass

object ScreenFactoryRegistry {
    private val routeMap: MutableMap<String, ScreenFactory<*>> = mutableMapOf()
    private val classMap: MutableMap<KClass<*>, ScreenFactory<*>> = mutableMapOf()
    fun register(route: String, screenClass: KClass<*>, factory: ScreenFactory<*>) {
        routeMap[route] = factory
        classMap[screenClass] = factory
    }

    fun <T : ScreenFactory<T>> findByRoute(route: String): T {
        @Suppress("UNCHECKED_CAST")
        return routeMap.getOrElse(route) {
            error("Screen factory for route[$route] not registered.")
        } as T
    }

    fun <T : ScreenFactory<T>> findByClass(screenClass: KClass<*>): T {
        @Suppress("UNCHECKED_CAST")
        return classMap.getOrElse(screenClass) {
            error("Screen factory for class[${screenClass.simpleName}] not registered.")
        } as T
    }
}

package jp.takuji31.compose.navigation.screen.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ScreenId(
    val screenClassName: String,
    val dynamicDeepLinkPrefix: Boolean = false,
)

@Deprecated(
    "Renamed to ScreenId",
    replaceWith = ReplaceWith("jp.takuji31.compose.navigation.screen.annotation.ScreenId"),
)
typealias AutoScreenId = ScreenId

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Route(
    val route: String,
    val deepLinks: Array<String> = [],
    val stringArguments: Array<StringArgument> = [],
    val intArguments: Array<IntArgument> = [],
    val longArguments: Array<LongArgument> = [],
    val booleanArguments: Array<BooleanArgument> = [],
    val floatArguments: Array<FloatArgument> = [],
    val enumArguments: Array<EnumArgument> = [],
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class DialogRoute(
    val route: String,
    val deepLinks: Array<String> = [],
    val stringArguments: Array<StringArgument> = [],
    val intArguments: Array<IntArgument> = [],
    val longArguments: Array<LongArgument> = [],
    val booleanArguments: Array<BooleanArgument> = [],
    val floatArguments: Array<FloatArgument> = [],
    val enumArguments: Array<EnumArgument> = [],
)

/**
 * [String] argument annotation.
 *
 * @param name Argument name
 * @param isNullable This argument is nullable if `true`
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Default value. If default value is null, pass @null
 */
annotation class StringArgument(
    val name: String,
    val isNullable: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: String = "@null",
)

/**
 * [Int] argument annotation
 * @param name Argument name
 * @param isNullable This argument is nullable if `true`
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Default value
 */
annotation class IntArgument(
    val name: String,
    val isNullable: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: Int = 0,
)

/**
 * [Long] argument annotation
 * @param name Argument name
 * @param isNullable This argument is nullable if `true`
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Default value
 */
annotation class LongArgument(
    val name: String,
    val isNullable: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: Long = 0L,
)

/**
 * [Boolean] argument annotation
 * @param name Argument name
 * @param isNullable This argument is nullable if `true`
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Default value
 */
annotation class BooleanArgument(
    val name: String,
    val isNullable: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: Boolean = false,
)

/**
 * [Float] argument annotation
 * @param name Argument name
 * @param isNullable This argument is nullable if `true`
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Default value
 */
annotation class FloatArgument(
    val name: String,
    val isNullable: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: Float = 0f,
)

/**
 * [Enum] argument annotation. this argument type cannot be nullable.
 * @param name Argument name
 * @param enumClass KClass instance of this argument type
 * @param hasDefaultValue If this argument has default value, should be true.
 * @param defaultValue Enum name of default. If default not needed, pass @null
 */
annotation class EnumArgument(
    val name: String,
    val enumClass: KClass<out Enum<*>>,
    val hasDefaultValue: Boolean = false,
    val defaultValue: String = "@null",
)

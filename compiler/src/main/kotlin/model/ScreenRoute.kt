package jp.takuji31.compose.navigation.compiler.model

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.Bundle
import jp.takuji31.compose.navigation.compiler.Parcelize
import jp.takuji31.compose.navigation.compiler.SavedStateHandle
import jp.takuji31.compose.navigation.compiler.ScreenFactory
import jp.takuji31.compose.navigation.compiler.toCamelCase
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.FloatArgument
import jp.takuji31.compose.navigation.screen.annotation.IntArgument
import jp.takuji31.compose.navigation.screen.annotation.LongArgument
import jp.takuji31.compose.navigation.screen.annotation.Route
import jp.takuji31.compose.navigation.screen.annotation.RouteType
import jp.takuji31.compose.navigation.screen.annotation.StringArgument

// TODO: convert to Visitor
sealed class ScreenRouteClass {
    abstract val baseClassName: ClassName
    abstract val name: String
    abstract val routeType: RouteType
    abstract val route: String
    abstract val deepLinks: List<String>
    abstract val typeSpec: TypeSpec

    private val bestTypeName: String by lazy { name.toCamelCase() }
    val className by lazy { baseClassName.nestedClass(bestTypeName) }

    protected val fromBundleSpecBuilder: FunSpec.Builder by lazy {
        FunSpec
            .builder("fromBundle")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("bundle", Bundle.copy(nullable = true))
            .returns(className)
    }

    protected val fromSavedStateHandleSpecBuilder: FunSpec.Builder by lazy {
        FunSpec
            .builder("fromSavedStateHandle")
            .addParameter("savedStateHandle", SavedStateHandle)
            .addModifiers(KModifier.OVERRIDE)
            .returns(className)
    }

    data class NonParameterized(
        override val baseClassName: ClassName,
        override val name: String,
        override val routeType: RouteType,
        override val route: String,
        override val deepLinks: List<String>,
        private val enumClassName: ClassName,
    ) : ScreenRouteClass() {
        override val typeSpec: TypeSpec by lazy {
            val spec = TypeSpec.objectBuilder(className)
                .superclass(baseClassName)
                .addAnnotation(Parcelize)
                .addSuperclassConstructorParameter(
                    "%T.%N",
                    enumClassName,
                    enumClassName.member(name),
                )
                .addSuperinterface(ScreenFactory.parameterizedBy(className))
                .addFunctions(
                    listOf(
                        fromBundleSpecBuilder.addCode("return this").build(),
                        fromSavedStateHandleSpecBuilder.addCode("return this").build(),
                    ),
                )
                .addProperty(
                    PropertySpec
                        .builder("route", STRING, KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return screenId.route").build())
                        .build(),
                )

            spec.build()
        }
    }

    data class Parameterized(
        override val baseClassName: ClassName,
        override val name: String,
        override val routeType: RouteType,
        override val route: String,
        override val deepLinks: List<String>,
        private val constructorAsInternal: Boolean,
        private val enumClassName: ClassName,
        private val argumentArgs: Set<Arg>,
    ) : ScreenRouteClass() {

        override val typeSpec: TypeSpec by lazy {
            val spec = TypeSpec.classBuilder(className)
                .superclass(baseClassName)
                .addAnnotation(Parcelize)
                .addSuperclassConstructorParameter(
                    "%T.%N",
                    enumClassName,
                    enumClassName.member(name),
                )

            spec.addProperty(
                PropertySpec
                    .builder("route", STRING, KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder().addCode("return screenId.route").build(),
                    )
                    .build(),
            )


            val parameterizedRouteProperty =
                PropertySpec.builder("parameterizedRoute", STRING, KModifier.OVERRIDE)
            val constructor = FunSpec.constructorBuilder()

            var route = route
            val constructorParameters = compiledArgs.map {
                ParameterSpec.builder(it.name, it.typeNameWithNullability).apply {
                    if (it.hasDefaultValue) {
                        defaultValue("%L", it.defaultValue)
                    }
                }.build()
            }
            constructor
                .addParameters(constructorParameters)
                .apply { if (constructorAsInternal) addModifiers(KModifier.INTERNAL) }

            val properties = compiledArgs.map { navArgument ->
                PropertySpec.builder(navArgument.name, navArgument.typeNameWithNullability)
                    .initializer(navArgument.name).build()
            }

            compiledArgs.forEach { route = route.replace("{${it.name}}", "\$${it.name}") }

            spec.primaryConstructor(constructor.build())
            spec.addProperties(properties)

            parameterizedRouteProperty.getter(
                FunSpec.getterBuilder().addCode("return %P", route)
                    .build(),
            )
            spec.addProperty(parameterizedRouteProperty.build())

            val companionObjectBuilder = TypeSpec.companionObjectBuilder()
                .addSuperinterface(ScreenFactory.parameterizedBy(className))
                .addFunctions(listOf(fromBundleFunSpec, fromSavedStateHandleFunSpec))

            spec.addType(companionObjectBuilder.build())
            spec.build()
        }

        private val routePathAndQuery by lazy { route.split("?", limit = 2) }
        private val routePath: String by lazy { routePathAndQuery[0] }
        private val routeQuery: String by lazy { routePathAndQuery.getOrNull(1) ?: "" }
        private val routePathArgNames: List<String> by lazy {
            routePath.extractParameters()
        }
        private val routeQueryArgNames: List<String> by lazy {
            routeQuery.extractParameters()
        }

        val compiledArgs: Set<Arg> by lazy {
            val definedArgs = argumentArgs
                .groupBy { it.name }
                .mapValues { entry ->
                    if (entry.value.size > 1) {
                        error("Argument key ${entry.key} is Duplicated in $name")
                    }
                    entry.value.first()
                }

            val definedKeys = definedArgs.keys
            check(routePathArgNames.none { routeQueryArgNames.contains(it) }) {
                "Argument key used twice in $name.route"
            }
            val generatedArgs = (
                (routePathArgNames - definedKeys).map {
                    it to Arg(
                        type = Arg.Type.String,
                        name = it,
                        isNullable = false,
                        hasDefaultValue = false,
                        typeName = STRING,
                    )
                } +
                    (routeQueryArgNames - definedKeys).map {
                        it to Arg(
                            type = Arg.Type.String,
                            name = it,
                            isNullable = true,
                            hasDefaultValue = true,
                            typeName = STRING,
                            defaultValue = null,
                        )
                    }
                ).toMap()

            val deepLinkArgNames =
                deepLinks.map { deepLink -> deepLink.extractParameters() }.flatten()
            val deepLinkArgs = (deepLinkArgNames - (definedKeys + generatedArgs.keys)).map {
                it to Arg(
                    type = Arg.Type.String,
                    name = it,
                    isNullable = true,
                    hasDefaultValue = true,
                    typeName = STRING,
                    defaultValue = null,
                )
            }.toMap()

            val navArgs: Map<String, Arg> = definedArgs + generatedArgs + deepLinkArgs

            // order map key by argument order
            (routePathArgNames + routeQueryArgNames + deepLinkArgNames)
                .map { checkNotNull(navArgs[it]) }
                .distinctBy { it.name }
                .toSet()
        }

        private val isAllArgsOptional: Boolean by lazy { compiledArgs.all { it.hasDefaultValue } }

        private fun String.extractParameters() =
            argPattern.findAll(this).map { it.groupValues[1] }.toList()

        /**
         * ScreenRoute.fromBundle(bundle) spec
         */
        private val fromBundleFunSpec: FunSpec
            get() {
                val fromBundleBuilder = fromBundleSpecBuilder
                if (isAllArgsOptional) {
                    fromBundleBuilder
                        .addStatement("bundle ?: return %T()", className)
                } else {
                    fromBundleBuilder
                        .beginControlFlow("checkNotNull(bundle)")
                        .addStatement(
                            "error(%S)",
                            "Screen $className has non-optional parameter",
                        )
                        .endControlFlow()
                }

                compiledArgs.forEach { arg ->
                    fromBundleBuilder.addCode(arg.bundleGetter)
                }

                return fromBundleBuilder
                    .addCode(initializerBlock)
                    .build()
            }

        /**
         * ScreenRoute.fromSavedStateHandle(savedStateHandle) spec
         */
        private val fromSavedStateHandleFunSpec: FunSpec
            get() {
                val fromSavedStateHandleBuilder = FunSpec
                    .builder("fromSavedStateHandle")
                    .addParameter("savedStateHandle", SavedStateHandle)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(className)

                compiledArgs.forEach { arg ->
                    when {
                        !arg.isNullable && !arg.hasDefaultValue -> {
                            // always not null
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T ?: error(%3S)",
                                arg.name,
                                arg.typeNameWithNullability,
                                "Screen $className requires parameter: ${arg.name}",
                            )
                        }
                        arg.hasDefaultValue && arg.defaultValue != null -> {
                            // with default value
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T ?: %3L",
                                arg.name,
                                arg.typeNameWithNullability,
                                arg.defaultValueLiteral,
                            )
                        }
                        else -> {
                            // with default value
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T",
                                arg.name,
                                arg.typeNameWithNullability,
                            )
                        }
                    }
                }

                return fromSavedStateHandleBuilder
                    .addCode(initializerBlock)
                    .build()
            }

        /**
         * Create ScreenRoute(params...) code block
         */
        private val initializerBlock: CodeBlock by lazy {
            val initializerBlockBuilder = CodeBlock
                .builder()
                .add("return %T(\n", className)
                .indent()

            compiledArgs.forEach { arg ->
                initializerBlockBuilder.add("%N = %N,\n", arg.name, arg.name)
            }

            initializerBlockBuilder.unindent().add(")").build()
        }

        // endregion


        companion object {
            internal val argPattern = """\{([^/}]+)}""".toRegex()
        }
    }

    companion object {
        @OptIn(KspExperimental::class)
        fun from(
            classDeclaration: KSClassDeclaration,
            enumClassName: ClassName,
            screenBaseClassName: ClassName,
        ): ScreenRouteClass? {
            require(classDeclaration.classKind == ClassKind.ENUM_ENTRY)
            val name = classDeclaration.simpleName.asString()


            val routeAnnotation =
                classDeclaration.getAnnotationsByType(Route::class).firstOrNull() ?: return null

            // https://github.com/google/ksp/issues/888
            @Suppress("UNCHECKED_CAST")
            val enumArgumentAnnotaions =
                classDeclaration
                    .annotations
                    .toList()
                    .first {
                        it.annotationType.resolve().toClassName() == Route::class.asClassName()
                    }
                    .arguments
                    .first { it.name?.asString() == "enumArguments" }
                    .value as List<KSAnnotation>

            val args: List<Arg> = createArgs(
                routeAnnotation.stringArguments,
                routeAnnotation.intArguments,
                routeAnnotation.longArguments,
                routeAnnotation.booleanArguments,
                routeAnnotation.floatArguments,
            ) + enumArgumentAnnotaions.map { Arg.createEnumArg(it) }

            return if (routeAnnotation.hasArgs) {
                Parameterized(
                    screenBaseClassName,
                    name,
                    routeAnnotation.type,
                    routeAnnotation.route,
                    routeAnnotation.deepLinks.toList(),
                    routeAnnotation.constructorAsInternal,
                    enumClassName,
                    args.toSet(),
                )
            } else {
                NonParameterized(
                    screenBaseClassName,
                    name,
                    routeAnnotation.type,
                    routeAnnotation.route,
                    routeAnnotation.deepLinks.toList(),
                    enumClassName,
                )
            }
        }

        private fun createArgs(
            stringArguments: Array<StringArgument>,
            intArguments: Array<IntArgument>,
            longArguments: Array<LongArgument>,
            booleanArguments: Array<BooleanArgument>,
            floatArguments: Array<FloatArgument>,
        ) =
            stringArguments.map { Arg.from(it) } +
                intArguments.map { Arg.from(it) } +
                longArguments.map { Arg.from(it) } +
                booleanArguments.map { Arg.from(it) } +
                floatArguments.map { Arg.from(it) }

        private val Route.hasArgs: Boolean
            get() = stringArguments.isNotEmpty() ||
                intArguments.isNotEmpty() ||
                longArguments.isNotEmpty() ||
                floatArguments.isNotEmpty() ||
                booleanArguments.isNotEmpty() ||
                enumArguments.isNotEmpty() ||
                route.contains(Parameterized.argPattern) ||
                deepLinks.any { it.contains(Parameterized.argPattern) }

    }
}


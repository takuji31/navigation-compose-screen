package jp.takuji31.compose.navigation.compiler.model

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.NavType
import jp.takuji31.compose.navigation.compiler.model.Arg.Type.Bool
import jp.takuji31.compose.navigation.compiler.navArgument
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.FloatArgument
import jp.takuji31.compose.navigation.screen.annotation.IntArgument
import jp.takuji31.compose.navigation.screen.annotation.LongArgument
import jp.takuji31.compose.navigation.screen.annotation.StringArgument

// TODO: convert to Visitor
data class Arg constructor(
    val type: Type,
    val name: String,
    val isNullable: Boolean,
    val hasDefaultValue: Boolean,
    private val typeName: TypeName,
    val defaultValue: Any? = null,
) {

    val typeNameWithNullability: TypeName by lazy { typeName.copy(nullable = isNullable) }

    val bundleGetter: CodeBlock by lazy {
        val codeBlock = if (type == Type.Enum) {
            CodeBlock.of(
                "val %1N = bundle.%2M<%3T>(%1S)",
                name,
                MemberName("jp.takuji31.compose.navigation.screen", "getEnum"),
                typeNameWithNullability,
            ).toBuilder()
        } else {
            val typeString = when (type) {
                Type.String -> "String"
                Type.Int -> "Int"
                Type.Long -> "Long"
                Bool -> "Boolean"
                Type.Float -> "Float"
                Type.Enum -> error("does not happen")
            }
            CodeBlock.of(
                "val %1N = bundle.get${typeString}(%1S)",
                name,
            ).toBuilder()
        }

        when {
            !isNullable && !hasDefaultValue -> {
                // always not null
                codeBlock.add("?: error(%S)", "Screen requires parameter: $name")
            }
            hasDefaultValue && defaultValue != null -> {
                // with default value
                codeBlock.add("?: %L", defaultValueLiteral)
            }
        }

        codeBlock.add("\n").build()
    }

    val defaultValueLiteral: CodeBlock by lazy {
        check(hasDefaultValue) { error("$this has no default value") }

        if (type == Type.Enum) {
            CodeBlock.builder()
                .add(
                    "%T.%M",
                    this.typeName,
                    MemberName(this.typeName as ClassName, defaultValue as String),
                )
                .build()
        } else {
            CodeBlock.builder()
                .add("%L", defaultValue)
                .build()
        }
    }

    val navArgsExtensionStatement: CodeBlock by lazy {
        val codeBlock = CodeBlock.builder()
            .addStatement("%M(%S) {", navArgument, name)
            .indent()

        val typeName = "${type.name}Type"

        if (type == Type.Enum) {
            val navType = MemberName(NavType, typeName)
            codeBlock.addStatement(
                "type = %T.%M(%T::class.java)", NavType, navType,
                this.typeName,
            )
        } else {
            codeBlock.addStatement("type = %T.%N", NavType, NavType.member(typeName))
        }
        if (hasDefaultValue) {
            check(type != Type.Enum || defaultValue != null) { "Enum default value cannot be null. at: $name" }
            check(defaultValue != null || isNullable) {
                "DefaultValue cannot be null when argument is not nullable. at: $name"
            }
            codeBlock
                .addStatement("defaultValue = %L", defaultValueLiteral)
        }

        codeBlock.addStatement("nullable = %L", isNullable)



        codeBlock.unindent().addStatement("},")
        codeBlock.build()
    }

    companion object {
        fun from(stringArgument: StringArgument): Arg = Arg(
            Type.String,
            stringArgument.name,
            stringArgument.isNullable,
            stringArgument.hasDefaultValue,
            STRING,
            stringArgument.defaultValue.takeIf { it != "@null" },
        )

        fun from(intArgument: IntArgument): Arg = Arg(
            Type.Int,
            intArgument.name,
            intArgument.isNullable,
            intArgument.hasDefaultValue,
            INT,
            intArgument.defaultValue,
        )

        fun from(longArgument: LongArgument): Arg = Arg(
            Type.Long,
            longArgument.name,
            longArgument.isNullable,
            longArgument.hasDefaultValue,
            LONG,
            longArgument.defaultValue,
        )

        fun from(booleanArgument: BooleanArgument): Arg = Arg(
            Bool,
            booleanArgument.name,
            booleanArgument.isNullable,
            booleanArgument.hasDefaultValue,
            BOOLEAN,
            booleanArgument.defaultValue,
        )

        fun from(floatArgument: FloatArgument): Arg = Arg(
            Type.Float,
            floatArgument.name,
            floatArgument.isNullable,
            floatArgument.hasDefaultValue,
            INT,
            floatArgument.defaultValue,
        )

        fun createEnumArg(annotation: KSAnnotation): Arg {
            val enumClass =
                annotation.arguments.first { it.name?.asString() == "enumClass" }.value as KSType
            val classDeclaration = enumClass.declaration as KSClassDeclaration

            val entries = classDeclaration
                .declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }

            val defaultValue =
                (annotation.arguments.first { it.name?.asString() == "defaultValue" }.value as String).takeIf { it != "@null" }
            val className = classDeclaration.toClassName()
            if (defaultValue != null) {
                check(entries.any { it.simpleName.asString() == defaultValue }) {
                    "Enum entry $className.${defaultValue} not found."
                }
            }

            val name =
                (annotation.arguments.first { it.name?.asString() == "name" }.value as String)
            val hasDefaultValue =
                (annotation.arguments.first { it.name?.asString() == "hasDefaultValue" }.value as Boolean)

            return Arg(
                Type.Enum,
                name,
                false,
                hasDefaultValue,
                className,
                defaultValue,
            )
        }
    }

    enum class Type {
        String, Int, Long, Bool, Float, Enum
    }
}

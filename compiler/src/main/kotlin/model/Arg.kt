package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import jp.takuji31.compose.navigation.compiler.NavType
import jp.takuji31.compose.navigation.compiler.navArgument
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.EnumArgument
import jp.takuji31.compose.navigation.screen.annotation.FloatArgument
import jp.takuji31.compose.navigation.screen.annotation.IntArgument
import jp.takuji31.compose.navigation.screen.annotation.LongArgument
import jp.takuji31.compose.navigation.screen.annotation.StringArgument
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.util.Elements

data class Arg constructor(
    val type: Type,
    val name: String,
    val isNullable: Boolean,
    val hasDefaultValue: Boolean,
    private val typeName: TypeName,
    val defaultValue: Any? = null,
) {

    val typeNameWithNullability: TypeName by lazy { typeName.copy(nullable = isNullable) }

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
            Type.Bool,
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

        @OptIn(KotlinPoetMetadataPreview::class)
        fun from(elements: Elements, enumArgument: EnumArgument): Arg {
            val kmClass = try {
                enumArgument.enumClass.toImmutableKmClass()
            } catch (e: MirroredTypeException) {
                elements.getTypeElement(e.typeMirror.toString()).toImmutableKmClass()
            }

            val defaultValue = enumArgument.defaultValue.takeIf { it != "@null" }
            val className = kmClass.name.replace("/", ".")
            if (defaultValue != null) {
                check(kmClass.enumEntries.any { it == enumArgument.defaultValue }) {
                    "Enum entry $className.${defaultValue} not found."
                }
            }

            return Arg(
                Type.Enum,
                enumArgument.name,
                false,
                enumArgument.hasDefaultValue,
                ClassName.bestGuess(className),
                defaultValue,
            )
        }
    }

    enum class Type {
        String, Int, Long, Bool, Float, Enum
    }
}

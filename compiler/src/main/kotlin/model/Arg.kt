package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
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
    val typeName: TypeName,
    val defaultValue: Any? = null,
) {

    val navArgsExtensionStatement: CodeBlock by lazy {
        val codeBlock = CodeBlock.builder()
            .addStatement("%M(%S) {", navArgument, name)
            .indent()

        if (type == Type.Enum) {
            codeBlock.addStatement("type = %T.%M(%T::class.java)", NavType, navType, typeName)
        } else {
            codeBlock.addStatement("type = %T.%M", NavType, navType)
        }

        codeBlock.addStatement("nullable = %L", isNullable)

        if (hasDefaultValue) {
            if (type == Type.Enum) {
                checkNotNull(defaultValue) { "Enum default value cannot be null. at: $name" }
                codeBlock.addStatement(
                    "defaultValue = %T.%M",
                    typeName,
                    MemberName(typeName as ClassName, defaultValue as String),
                )
            } else {
                check(defaultValue != null || isNullable) {
                    "DefaultValue cannot be null when argument is not nullable. at: $name"
                }
                codeBlock.addStatement("defaultValue = %L", defaultValue)
            }
        }


        codeBlock.unindent().addStatement("},")
        codeBlock.build()
    }

    private val navType by lazy { MemberName(NavType, "${type.name}Type") }

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

package jp.takuji31.compose.navigation.compiler.model

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.Parcelable
import jp.takuji31.compose.navigation.screen.Screen
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

// TODO: convert to Visitor
data class ScreenClass(
    val className: ClassName,
    val enumClassName: ClassName,
    val disableParcelize: Boolean,
) {

    companion object {
        @OptIn(KspExperimental::class)
        fun from(classDeclaration: KSClassDeclaration): ScreenClass {
            require(classDeclaration.classKind == ClassKind.ENUM_CLASS)
            val screenId = classDeclaration.getAnnotationsByType(ScreenId::class).first()
            return ScreenClass(
                ClassName(classDeclaration.packageName.asString(), screenId.screenClassName),
                classDeclaration.toClassName(),
                screenId.disableParcelize,
            )
        }
    }

    val typeSpecBuilder: TypeSpec.Builder by lazy {
        val screenIdParameter =
            ParameterSpec.builder("screenId", enumClassName).build()

        val spec = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(Screen::class.asClassName().parameterizedBy(enumClassName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        screenIdParameter,
                    )
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("screenId", enumClassName, KModifier.OVERRIDE)
                    .initializer("%N", screenIdParameter).build(),
            )

        if (!disableParcelize) {
            spec.addSuperinterface(Parcelable)
        }

        spec
    }
}

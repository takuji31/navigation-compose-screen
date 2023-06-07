package jp.takuji31.compose.navigation.compiler.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

fun ScreenId.createClassName(classDeclaration: KSClassDeclaration): ClassName =
    ClassName(classDeclaration.packageName.asString(), screenClassName)

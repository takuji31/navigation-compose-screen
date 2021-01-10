package jp.takuji31.compose.navigation.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.ImmutableSetMultimap
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import jp.takuji31.compose.screengenerator.annotation.AutoScreenId
import jp.takuji31.compose.screengenerator.annotation.Route
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.tools.Diagnostic

class ScreenGenerateStep(private val processingEnv: ProcessingEnvironment) :
    BasicAnnotationProcessor.Step {
    override fun annotations(): MutableSet<String> = mutableSetOf(AutoScreenId::class.java.name)

    override fun process(elementsByAnnotation: ImmutableSetMultimap<String, Element>?): MutableSet<out Element> {
        elementsByAnnotation?.forEach { _, element ->
            if (element.kind != ElementKind.ENUM) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "AutoScreenId can use only enum class.",
                )
                return@forEach
            }

            var enclosing = element
            while (enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            val packageElement: PackageElement = enclosing as PackageElement

            val packageName = packageElement.qualifiedName.toString()
            val screenClassSimpleName =
                element.getAnnotation(AutoScreenId::class.java).screenClassName
            val screenClassName = ClassName(
                packageName,
                screenClassSimpleName,
            )
            val fileSpec = FileSpec.builder(packageName, screenClassSimpleName)
            val enumClassName = ClassName(packageName, element.simpleName.toString())

            val values = mutableListOf<ScreenEnumValue>()
            element.enclosedElements.filter { it.kind == ElementKind.ENUM_CONSTANT }.forEach {
                val annotations = it.getAnnotationsByType(Route::class.java)
                if (annotations.isEmpty()) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${element.simpleName}.${it.simpleName} must have Screen annotation.",
                    )
                } else {
                    val annotation = annotations.first()
                    values += ScreenEnumValue(it.simpleName.toString(), annotation)
                }
            }

            val screenEnum = ScreenEnum(enumClassName, screenClassName, values.toList())

            fileSpec.addType(screenEnum.screenSpec)
            fileSpec.addFunction(screenEnum.composeFunctionSpec)
            fileSpec.addProperty(screenEnum.routeExtensionSpec)
            fileSpec.addProperty(screenEnum.navArgsExtensionSpec)
            fileSpec.addProperty(screenEnum.deepLinksExtensionSpec)

            val dest = processingEnv.options["kapt.kotlin.generated"]

            fileSpec.build().writeTo(File(dest))
        }
        return mutableSetOf()
    }
}

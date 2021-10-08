package jp.takuji31.compose.navigation.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.ImmutableSetMultimap
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isAbstract
import com.squareup.kotlinpoet.metadata.isInterface
import com.squareup.kotlinpoet.metadata.isOpen
import com.squareup.kotlinpoet.metadata.toKmClass
import jp.takuji31.compose.navigation.compiler.model.ComposeBuilderFunction
import jp.takuji31.compose.navigation.compiler.model.NavOptionsBuilderExtensions
import jp.takuji31.compose.navigation.compiler.model.ScreenClass
import jp.takuji31.compose.navigation.compiler.model.ScreenIdExtensions
import jp.takuji31.compose.navigation.compiler.model.ScreenRoute
import jp.takuji31.compose.navigation.screen.Screen
import jp.takuji31.compose.navigation.screen.annotation.AutoScreenId
import jp.takuji31.compose.navigation.screen.annotation.DialogRoute
import jp.takuji31.compose.navigation.screen.annotation.Route
import kotlinx.metadata.KmClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic

class ScreenGenerateStep(private val processingEnv: ProcessingEnvironment) :
    BasicAnnotationProcessor.Step {
    override fun annotations(): MutableSet<String> =
        mutableSetOf(AutoScreenId::class.java.name)

    @OptIn(KotlinPoetMetadataPreview::class)
    override fun process(elementsByAnnotation: ImmutableSetMultimap<String, Element>?): MutableSet<out Element> {
        elementsByAnnotation?.forEach { _, element ->
            if (element.kind != ElementKind.ENUM) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "AutoScreenId can use only enum class.",
                )
                return@forEach
            }

            val packageName = getPackageName(element)
            val annotation =
                element.getAnnotation(AutoScreenId::class.java)

            val screenBaseType = try {
                processingEnv.elementUtils.getTypeElement(annotation.screenBaseClass.qualifiedName)
            } catch (e: MirroredTypeException) {
                val classType = e.typeMirror
                processingEnv.elementUtils.getTypeElement(classType.toString())
            }

            val screenElement =
                processingEnv.elementUtils.getTypeElement(Screen::class.qualifiedName)
            val erasureType = processingEnv.typeUtils.erasure(screenBaseType.asType())
            if (!processingEnv.typeUtils.isAssignable(
                    erasureType,
                    screenElement.asType(),
                )
            ) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "AutoScreenId.screenBaseClass can use only subtype of ${Screen::class.qualifiedName} current: $screenBaseType",
                )
                return@forEach
            }

            val screenBaseClass: KmClass = screenBaseType.toKmClass()
            if ((!screenBaseClass.flags.isAbstract && !screenBaseClass.flags.isOpen) || screenBaseClass.typeParameters.size != 1) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "AutoScreenId.screenBaseClass can use only single parameterized open class",
                )
                return@forEach
            }

            val screenClassSimpleName =
                annotation.screenClassName
            val screenClassName = ClassName(
                packageName,
                screenClassSimpleName,
            )
            val fileSpec = FileSpec.builder(packageName, screenClassSimpleName)
            val idClassName = ClassName(packageName, element.simpleName.toString())

            val routes =
                element.enclosedElements.filter { it.kind == ElementKind.ENUM_CONSTANT }.map {
                    val annotations =
                        it.getAnnotationsByType(Route::class.java)
                    val dialogAnnotations =
                        it.getAnnotationsByType(DialogRoute::class.java)
                    if (annotations.isEmpty() && dialogAnnotations.isEmpty()) {
                        throw RuntimeException("${element.simpleName}.${it.simpleName} must have Route or DialogRoute annotation.")
                    } else if (annotations.isNotEmpty()) {
                        val valueAnnotation = annotations.first()
                        ScreenRoute(
                            processingEnv.elementUtils,
                            it.simpleName.toString(),
                            valueAnnotation,
                        )
                    } else {
                        val valueAnnotation = dialogAnnotations.first()
                        ScreenRoute(
                            processingEnv.elementUtils,
                            it.simpleName.toString(),
                            valueAnnotation,
                        )
                    }
                }.toList()

            val screenBaseClassName = ClassName.bestGuess(screenBaseClass.name.replace("/", "."))
            val composeBuilderClassName = screenClassName.nestedClass("ComposeDestinationBuilder")

            fileSpec.addType(
                ScreenClass(
                    screenClassName,
                    idClassName,
                    screenBaseClassName,
                    screenBaseClass.isInterface,
                    composeBuilderClassName,
                    annotation.dynamicDeepLinkPrefix,
                    routes,
                ).typeSpec,
            )

            val screenIdExtensions = ScreenIdExtensions(
                idClassName,
                annotation.dynamicDeepLinkPrefix,
                routes,
            )
            screenIdExtensions.propertySpecs.forEach { fileSpec.addProperty(it) }

            fileSpec.addFunction(screenIdExtensions.deepLinksExtensionSpec)

            fileSpec.addFunction(
                ComposeBuilderFunction(
                    screenClassName,
                    composeBuilderClassName,
                    annotation.dynamicDeepLinkPrefix,
                ).spec,
            )

            fileSpec.addFunction(NavOptionsBuilderExtensions(idClassName).spec)

            fileSpec.build().writeTo(processingEnv.filer)
        }
        return mutableSetOf()
    }

    private fun getPackageName(element: Element): String {
        var enclosing = element
        while (enclosing.kind != ElementKind.PACKAGE) {
            enclosing = enclosing.enclosingElement
        }
        val packageElement: PackageElement = enclosing as PackageElement

        return packageElement.qualifiedName.toString()
    }
}

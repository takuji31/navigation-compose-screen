package jp.takuji31.compose.navigation.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.ImmutableSetMultimap
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isAbstract
import com.squareup.kotlinpoet.metadata.isInterface
import com.squareup.kotlinpoet.metadata.isOpen
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import jp.takuji31.compose.navigation.Screen
import jp.takuji31.compose.screengenerator.annotation.AutoScreenId
import jp.takuji31.compose.screengenerator.annotation.Route
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic

class ScreenGenerateStep(private val processingEnv: ProcessingEnvironment) :
    BasicAnnotationProcessor.Step {
    override fun annotations(): MutableSet<String> = mutableSetOf(AutoScreenId::class.java.name)

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

            var enclosing = element
            while (enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            val packageElement: PackageElement = enclosing as PackageElement

            val packageName = packageElement.qualifiedName.toString()
            val annotation = element.getAnnotation(AutoScreenId::class.java)

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

            val screenBaseClass: ImmutableKmClass = screenBaseType.toImmutableKmClass()
            if ((!screenBaseClass.isAbstract && !screenBaseClass.isOpen) || screenBaseClass.typeParameters.size != 1) {
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
                    val valueAnnotation = annotations.first()
                    values += ScreenEnumValue(it.simpleName.toString(), valueAnnotation)
                }
            }

            val screenEnum = ScreenEnum(
                enumClassName,
                screenClassName,
                ClassName.bestGuess(
                    screenBaseClass.name.replace("/", "."),
                ),
                screenBaseClass.isInterface,
                values.toList(),
            )

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

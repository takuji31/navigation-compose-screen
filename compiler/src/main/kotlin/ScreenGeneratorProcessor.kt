package jp.takuji31.compose.navigation.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.AGGREGATING
import javax.annotation.processing.Processor
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion

@IncrementalAnnotationProcessor(AGGREGATING)
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ScreenGeneratorProcessor : BasicAnnotationProcessor() {
    override fun steps(): MutableIterable<Step> {
        return mutableListOf(ScreenGenerateStep(processingEnv))
    }
}

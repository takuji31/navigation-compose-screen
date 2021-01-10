package jp.takuji31.compose.navigation.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import javax.annotation.processing.Processor
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ScreenGenerator : BasicAnnotationProcessor() {
    override fun steps(): MutableIterable<Step> {
        return mutableListOf(ScreenGenerateStep(processingEnv))
    }
}

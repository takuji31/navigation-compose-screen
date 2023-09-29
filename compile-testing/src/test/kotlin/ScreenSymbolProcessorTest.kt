package jp.takuji31.compose.navigation.compiler

import com.google.common.truth.Expect
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Rule
import org.junit.Test

class ScreenSymbolProcessorTest {
    @get:Rule
    val expect: Expect = Expect.create()

    @Test
    fun simple() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            symbolProcessorProviders = listOf(ScreenSymbolProcessorProvider())
            kspWithCompilation = true
        }
        val result = compilation.compile()
        expect
            .that(result.exitCode)
            .isEqualTo(KotlinCompilation.ExitCode.OK)

        val files = compilation.kspSourcesDir.walkTopDown()
            .filter { it.name == "TestingScreen.kt" }
            .toList()

        expect
            .that(files)
            .isNotEmpty()
    }

    @Test
    fun withEnum() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(sourceWithEnum)
            inheritClassPath = true
            symbolProcessorProviders = listOf(ScreenSymbolProcessorProvider())
            kspWithCompilation = true
        }
        val result = compilation.compile()
        expect
            .that(result.exitCode)
            .isEqualTo(KotlinCompilation.ExitCode.OK)

        val files = compilation.kspSourcesDir.walkTopDown()
            .filter { it.name == "EnumScreen.kt" }
            .toList()

        expect
            .that(files)
            .isNotEmpty()
    }

    @Test
    fun dialogRoute() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(sourceWithDialogRouteType)
            inheritClassPath = true
            symbolProcessorProviders = listOf(ScreenSymbolProcessorProvider())
            kspWithCompilation = true
        }
        val result = compilation.compile()
        expect
            .that(result.exitCode)
            .isEqualTo(KotlinCompilation.ExitCode.OK)

        val files = compilation.kspSourcesDir.walkTopDown()
            .filter { it.name == "TestingScreen.kt" }
            .toList()

        expect
            .that(files.first().readText()).contains("navGraphBuilder.dialog")
    }

    @Test
    fun dialogRouteWithDeepLinkPrefix() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(sourceWithDialogRouteTypeAndDeepLinkPrefix)
            inheritClassPath = true
            symbolProcessorProviders = listOf(ScreenSymbolProcessorProvider())
            kspWithCompilation = true
        }
        val result = compilation.compile()
        expect
            .that(result.exitCode)
            .isEqualTo(KotlinCompilation.ExitCode.OK)

        val files = compilation.kspSourcesDir.walkTopDown()
            .filter { it.name == "TestingScreen.kt" }
            .toList()

        expect
            .that(files.first().readText())
            .contains("navGraphBuilder.dialog")
    }

    @Test
    fun nestedGraph() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(sourceWithNestedGraph)
            inheritClassPath = true
            symbolProcessorProviders = listOf(ScreenSymbolProcessorProvider())
            kspWithCompilation = true
            jvmTarget = "11"
        }
        val result = compilation.compile()
        expect
            .that(result.exitCode)
            .isEqualTo(KotlinCompilation.ExitCode.OK)

        val files = compilation.kspSourcesDir.walkTopDown()
            .filter { it.name == "NestedGraphTesting.kt" }
            .toList()

        expect
            .that(files)
            .isNotEmpty()
    }

    companion object {
        val source = SourceFile.kotlin(
            "TestScreenId.kt",
            """
            package jp.takuji31.compose.navigation

            import jp.takuji31.compose.navigation.screen.annotation.Route
            import jp.takuji31.compose.navigation.screen.annotation.ScreenId

            @ScreenId("TestingScreen", disableParcelize = true)
            enum class TestingScreenId {
                @Route("/")
                Home,

                @Route("/subscreen")
                SubScreen,

                @Route("/parameterized/{id}")
                Parameterized
            }
        """.trimIndent(),
        )
        val sourceWithEnum = SourceFile.kotlin(
            "TestScreenId.kt",
            """
            package jp.takuji31.compose.navigation

            import jp.takuji31.compose.navigation.screen.annotation.Route
            import jp.takuji31.compose.navigation.screen.annotation.ScreenId
            import jp.takuji31.compose.navigation.screen.annotation.EnumArgument

            enum class EnumArg {
                Default, Another
            }
            @ScreenId("EnumScreen", disableParcelize = true)
            enum class EnumScreenId {
                @Route("/{enumArg}", enumArguments = [EnumArgument(name = "enumArg", enumClass = EnumArg::class)])
                EnumArgRoute,
            }
        """.trimIndent(),
        )
        val sourceWithDialogRouteType = SourceFile.kotlin(
            "TestScreenId.kt",
            """
            package jp.takuji31.compose.navigation

            import jp.takuji31.compose.navigation.screen.annotation.Route
            import jp.takuji31.compose.navigation.screen.annotation.RouteType
            import jp.takuji31.compose.navigation.screen.annotation.ScreenId

            @ScreenId("TestingScreen", dynamicDeepLinkPrefix = false, disableParcelize = true)
            enum class TestingScreenId {
                @Route("/", RouteType.Dialog)
                Home,
            }
        """.trimIndent(),
        )
        val sourceWithDialogRouteTypeAndDeepLinkPrefix = SourceFile.kotlin(
            "TestScreenId.kt",
            """
            package jp.takuji31.compose.navigation

            import jp.takuji31.compose.navigation.screen.annotation.Route
            import jp.takuji31.compose.navigation.screen.annotation.RouteType
            import jp.takuji31.compose.navigation.screen.annotation.ScreenId

            @ScreenId("TestingScreen", dynamicDeepLinkPrefix = true, disableParcelize = true)
            enum class TestingScreenId {
                @Route("/", RouteType.Dialog)
                Home,
            }
        """.trimIndent(),

            )

        val sourceWithNestedGraph = SourceFile.kotlin(
            "NestedGraph.kt",
            """
            package jp.takuji31.compose.navigation

            import jp.takuji31.compose.navigation.screen.annotation.Route
            import jp.takuji31.compose.navigation.screen.annotation.ScreenId
            import jp.takuji31.compose.navigation.screen.annotation.RouteType

            @ScreenId("NestedGraphTesting", disableParcelize = true)
            enum class NestedGraphTestingScreenId {
                @Route("/", type = RouteType.NestedGraph)
                NestedGraph,
            }
        """.trimIndent(),

            )
    }
}

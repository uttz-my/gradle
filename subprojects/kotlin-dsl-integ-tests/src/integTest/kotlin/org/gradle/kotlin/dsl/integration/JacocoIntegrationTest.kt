package org.gradle.kotlin.dsl.integration

import org.gradle.integtests.fixtures.RepoScriptBlockUtil
import org.gradle.test.fixtures.dsl.GradleDsl
import org.gradle.test.fixtures.file.LeaksFileHandles
import org.gradle.util.TestPrecondition
import org.junit.Assume.assumeTrue
import org.junit.Test


@LeaksFileHandles("Kotlin Compiler Daemon working directory")
class JacocoIntegrationTest : AbstractPluginIntegrationTest() {

    @Test
    fun `jacoco ignore codegen`() {
        assumeTrue("JaCoCo does not support JDK 18 yet", TestPrecondition.JDK17_OR_EARLIER.isFulfilled)
        withBuildScript(
            """
            plugins {
                `kotlin-dsl`
                jacoco
            }
            ${RepoScriptBlockUtil.mavenCentralRepository(GradleDsl.KOTLIN)}
            dependencies {
                testImplementation("junit:junit:4.12")
            }
            tasks {
                test {
                    useJUnit()
                }
                jacocoTestCoverageVerification {
                    violationRules {
                        rule {
                            element = "CLASS"
                            includes = listOf("org.gradle.*", "gradle.*")
                            limit {
                                minimum = 1.toBigDecimal()
                            }
                        }
                        rule {
                            element = "METHOD"
                            includes = listOf("org.gradle.*", "gradle.*")
                            limit {
                                minimum = 1.toBigDecimal()
                            }
                        }
                    }
                }
            }
            """
        )

        withFile("src/main/kotlin/foo.gradle.kts", "plugins { base }")
        withFile(
            "src/test/kotlin/fooTest.kt",
            """
            import org.junit.Test
            class FooTest {
                @Test fun testFoo() { }
            }
            """.trimIndent()
        )

        build("test", "jacocoTestCoverageVerification")
    }
}

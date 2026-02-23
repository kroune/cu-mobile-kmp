package io.github.kroune.detekt_rules

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.createEnvironment
import org.junit.Test
import kotlin.test.assertEquals

class RedundantNamedArgumentTest {
    private val env: KotlinEnvironmentContainer = createEnvironment()
    private val rule = RedundantNamedArgument(Config.empty)

    @Test
    fun `reports single named argument where value equals name in single-param function`() {
        val code = """
            fun foo(bar: Int) {}
            val bar = 1
            val x = foo(bar = bar)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `does not report single named argument in multi-param function`() {
        val code = """
            fun foo(a: Int = 0, bar: Int) {}
            val bar = 1
            val x = foo(bar = bar)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `does not report single positional argument`() {
        val code = """
            fun foo(bar: Int) {}
            val bar = 1
            val x = foo(bar)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `does not report when argument value differs from argument name`() {
        val code = """
            fun foo(bar: Int) {}
            val baz = 1
            val x = foo(bar = baz)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `does not report multiple named arguments`() {
        val code = """
            fun foo(bar: Int, baz: Int) {}
            val bar = 1
            val baz = 2
            val x = foo(bar = bar, baz = baz)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `does not report call with no arguments`() {
        val code = """
            fun foo() {}
            val x = foo()
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `reports finding with correct message`() {
        val code = """
            fun foo(bar: Int) {}
            val bar = 1
            val x = foo(bar = bar)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(1, findings.size)
        assertEquals("Redundant named argument: 'bar = bar'. Use just 'bar'.", findings.first().message)
    }

    @Test
    fun `does not report when single-param lambda function called with named matching arg but function has 2 params`() {
        val code = """
            fun someFunc2(a: Int = 0, someLambda: () -> Unit) {}
            val someLambda = { }
            val x = someFunc2(someLambda = someLambda)
        """.trimIndent()
        val findings = rule.lintWithContext(env, code)
        assertEquals(0, findings.size)
    }
}

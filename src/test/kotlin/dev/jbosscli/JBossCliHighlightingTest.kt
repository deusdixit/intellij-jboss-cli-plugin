package dev.jbosscli

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Colors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter
import dev.jbosscli.psi.JBossCliTypes.*

class JBossCliHighlightingTest : BasePlatformTestCase() {
    fun testLexicalColors() {
        val highlighter = JBossCliSyntaxHighlighter()
        assertEquals(JBossCliSyntaxHighlighter.COMMENT_COLOR, highlighter.getTokenHighlights(COMMENT).single())
        assertEquals(JBossCliSyntaxHighlighter.VARIABLE_COLOR, highlighter.getTokenHighlights(VARIABLE).single())
        assertEquals(JBossCliSyntaxHighlighter.STRING_COLOR, highlighter.getTokenHighlights(STRING).single())
        assertEquals(JBossCliSyntaxHighlighter.BRACES_COLOR, highlighter.getTokenHighlights(LBRACE).single())
        assertEquals(JBossCliSyntaxHighlighter.VARIABLE_COLOR, highlighter.getTokenHighlights(EXPRESSION).single())
        for (token in listOf(ECHO, SET, UNSET)) {
            assertEquals(JBossCliSyntaxHighlighter.KEYWORD_COLOR, highlighter.getTokenHighlights(token).single())
        }
        for (token in listOf(TRUE, FALSE)) {
            assertEquals(Colors.KEYWORD, highlighter.getTokenHighlights(token).single().fallbackAttributeKey)
        }
    }

    fun testThemeAwareDefaults() {
        assertEquals(Colors.KEYWORD, JBossCliSyntaxHighlighter.KEYWORD_COLOR.fallbackAttributeKey)
        assertEquals(Colors.KEYWORD, JBossCliSyntaxHighlighter.COMMAND_COLOR.fallbackAttributeKey)
        assertEquals(Colors.INSTANCE_FIELD, JBossCliSyntaxHighlighter.PATH_COLOR.fallbackAttributeKey)
        assertEquals(Colors.PARAMETER, JBossCliSyntaxHighlighter.PARAMETER_COLOR.fallbackAttributeKey)
        assertEquals(Colors.LOCAL_VARIABLE, JBossCliSyntaxHighlighter.VARIABLE_COLOR.fallbackAttributeKey)
    }

    fun testLoggingAddressParametersAndVariables() {
        val text = """
            /subsystem=logging/logging-profile=${'$'}loggingProfile/periodic-size-rotating-file-handler=${'$'}file:add(category=${'$'}logger,autoflush="true",append=false,enabled=true)
            /host=primary/server=main:read-resource(recursive=true)
            echo ${'$'}{prop_logging_profile}
        """.trimIndent()
        assertColors(text, buildMap {
            put("echo", JBossCliSyntaxHighlighter.COMMAND_COLOR)
            for (name in listOf("subsystem", "logging", "logging-profile", "periodic-size-rotating-file-handler", "host", "primary", "server", "main")) {
                put(name, JBossCliSyntaxHighlighter.PATH_COLOR)
            }
            for (name in listOf("category", "autoflush", "append", "enabled", "recursive")) {
                put(name, JBossCliSyntaxHighlighter.PARAMETER_COLOR)
            }
            for (name in listOf("${'$'}loggingProfile", "${'$'}file", "${'$'}logger", "${'$'}{prop_logging_profile}")) {
                put(name, JBossCliSyntaxHighlighter.VARIABLE_COLOR)
            }
            put("add", JBossCliSyntaxHighlighter.OPERATION_COLOR)
            put("false", JBossCliSyntaxHighlighter.CONSTANT_COLOR)
            put("\"true\"", JBossCliSyntaxHighlighter.STRING_COLOR)
        })
    }

    fun testInterpolationsInsideStringsAndBareValues() {
        val text = """
            echo "Profile ${'$'}loggingProfile: ${'$'}{outer:${'$'}{inner:default}} ${'$'}first${'$'}second"
            :add(path="${'$'}{app_log_directory}/${'$'}file.log",category=prefix-${'$'}logger,other='${'$'}{single:default}')
        """.trimIndent()
        assertColors(text, listOf(
            "${'$'}loggingProfile", "${'$'}{outer:${'$'}{inner:default}}", "${'$'}first", "${'$'}second",
            "${'$'}{app_log_directory}", "${'$'}file", "${'$'}logger", "${'$'}{single:default}",
        ).associateWith { JBossCliSyntaxHighlighter.VARIABLE_COLOR })
    }

    fun testEscapedDollarsAndIncompleteStringExpressions() {
        val text = """
            echo "\${'$'}escaped \${'$'}{literal} \\${'$'}active \"${'$'}quoted\" ${'$'}{unfinished"
            echo "${'$'}{open
            :read-resource
        """.trimIndent()
        myFixture.configureByText("strings.cli", text)
        val variables = myFixture.doHighlighting()
            .filter { it.forcedTextAttributesKey == JBossCliSyntaxHighlighter.VARIABLE_COLOR }
            .map { text.substring(it.startOffset, it.endOffset) }
        assertEquals(listOf("${'$'}active", "${'$'}quoted", "${'$'}{unfinished", "${'$'}{open"), variables)
    }

    fun testEchoRemainsAnIdentifierInOperationRoles() {
        assertColors("/echo=example:echo(echo=echo)", mapOf(
            "example" to JBossCliSyntaxHighlighter.PATH_COLOR,
        ))
        for ((script, color) in listOf(
            "/echo=example:read-resource" to JBossCliSyntaxHighlighter.PATH_COLOR,
            ":echo" to JBossCliSyntaxHighlighter.OPERATION_COLOR,
            ":add(echo=value)" to JBossCliSyntaxHighlighter.PARAMETER_COLOR,
            ":add(value=echo)" to JBossCliSyntaxHighlighter.VALUE_COLOR,
        )) assertColors(script, mapOf("echo" to color))
    }

    private fun assertColors(text: String, expected: Map<String, TextAttributesKey>) {
        myFixture.configureByText("colors.cli", text)
        val highlights = myFixture.doHighlighting()
        val highlighter = JBossCliSyntaxHighlighter()
        for ((fragment, key) in expected) {
            val start = text.indexOf(fragment)
            assertTrue("Missing test fragment: $fragment", start >= 0)
            val semantic = highlights.lastOrNull { it.startOffset <= start && it.endOffset >= start + fragment.length && it.forcedTextAttributesKey != null }
            val lexer = highlighter.highlightingLexer
            lexer.start(text)
            while (lexer.tokenType != null && lexer.tokenEnd <= start) lexer.advance()
            val actual = semantic?.forcedTextAttributesKey ?: lexer.tokenType?.let { highlighter.getTokenHighlights(it).singleOrNull() }
            assertEquals("Color of $fragment in $text", key, actual)
        }
    }

    fun testSemanticRolesAndKeywordAsValue() {
        val text = "/system-property=example:add(value=if)\n"
        myFixture.configureByText("colors.cli", text)
        val highlights = myFixture.doHighlighting()
        fun hasColor(fragment: String, key: com.intellij.openapi.editor.colors.TextAttributesKey): Boolean {
            val start = text.indexOf(fragment)
            return highlights.any { it.startOffset == start && it.endOffset == start + fragment.length && it.forcedTextAttributesKey == key }
        }
        assertTrue(hasColor("add", JBossCliSyntaxHighlighter.OPERATION_COLOR))
        assertTrue(hasColor("system-property", JBossCliSyntaxHighlighter.PATH_COLOR))
        assertTrue(hasColor("value", JBossCliSyntaxHighlighter.PARAMETER_COLOR))
        assertTrue(hasColor("if", JBossCliSyntaxHighlighter.VALUE_COLOR))
    }
}

package dev.jbosscli

import com.intellij.psi.TokenType
import dev.jbosscli.lexer.JBossCliLexerAdapter
import dev.jbosscli.psi.JBossCliTypes.*
import junit.framework.TestCase

class JBossCliLexerTest : TestCase() {
    fun testEscapingExpressionsAndComments() {
        val text = """
            # comment
              # indented comment
            /system-property=demo:add(value="a,#:{}",other=hello\ world)
            echo value#fragment ${'$'}target ${'$'}{outer:${'$'}{inner:fallback}}
        """.trimIndent()
        val tokens = tokens(text)
        assertEquals(2, tokens.count { it.first == COMMENT })
        assertTrue(tokens.contains(STRING to "\"a,#:{}\""))
        assertTrue(tokens.contains(WORD to "hello\\ world"))
        assertTrue(tokens.contains(EXPRESSION to "${'$'}{outer:${'$'}{inner:fallback}}"))
        assertTrue(tokens.contains(VARIABLE to "${'$'}target"))
        assertFalse(tokens.any { it.first == TokenType.BAD_CHARACTER })
        assertEquals(text, tokens.joinToString("") { it.second })
    }

    fun testRestartAtEveryToken() {
        val text = "# comment\r\nif (outcome != success) of /x=y:add(value=[\"a,b\",${'$'}{x:${'$'}{y:z}}]) \\\r\n {roles=[admin];blocking-timeout=30}\nend-if\n"
        val lexer = JBossCliLexerAdapter()
        lexer.start(text)
        while (lexer.tokenType != null) {
            val restarted = JBossCliLexerAdapter()
            restarted.start(text, lexer.tokenStart, text.length, lexer.state)
            assertEquals(lexer.tokenType, restarted.tokenType)
            assertEquals(lexer.tokenEnd, restarted.tokenEnd)
            assertTrue(lexer.tokenEnd > lexer.tokenStart)
            lexer.advance()
        }
    }

    fun testIncompleteTokensStopBeforeNextStatement() {
        for (prefix in listOf("\"unterminated", "'unterminated", "${'$'}{unterminated", "`unterminated")) {
            val tokens = tokens("$prefix\n:read-resource\n")
            assertTrue(tokens.any { it.first == NEWLINE })
            assertTrue(tokens.any { it.first == COLON })
        }
    }

    fun testNestedExpressionHasNoDepthLimit() {
        val expression = "${'$'}{".repeat(40) + "x" + "}".repeat(40)
        assertEquals(listOf(EXPRESSION to expression), tokens(expression))
    }

    fun testAdjacentVariableReferencesStaySeparate() {
        assertEquals(listOf(VARIABLE to "${'$'}first", VARIABLE to "${'$'}second"), tokens("${'$'}first${'$'}second"))
    }

    private fun tokens(text: String) = buildList {
        val lexer = JBossCliLexerAdapter()
        lexer.start(text)
        while (lexer.tokenType != null) {
            add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
    }
}

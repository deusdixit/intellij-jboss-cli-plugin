package dev.jbosscli

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

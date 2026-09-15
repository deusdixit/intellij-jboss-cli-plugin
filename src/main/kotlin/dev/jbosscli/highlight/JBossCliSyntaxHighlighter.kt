package dev.jbosscli.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Colors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.jbosscli.lexer.JBossCliLexerAdapter
import dev.jbosscli.psi.JBossCliTypes.*

class JBossCliSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = JBossCliLexerAdapter()
    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = pack(when (tokenType) {
        COMMENT -> COMMENT_COLOR
        STRING, SUBSTITUTION -> STRING_COLOR
        VARIABLE, EXPRESSION -> VARIABLE_COLOR
        NUMBER -> NUMBER_COLOR
        TRUE, FALSE, UNDEFINED -> CONSTANT_COLOR
        IF, ELSE, END_IF, OF, TRY, CATCH, FINALLY, END_TRY, FOR, IN, DONE,
        BATCH, RUN_BATCH, DISCARD_BATCH, HOLDBACK_BATCH, SET, UNSET, ROLLOUT,
        ROLLBACK_ACROSS_GROUPS, BYTES, EXPRESSION_TYPE -> KEYWORD_COLOR
        LPAREN, RPAREN -> PARENTHESES_COLOR
        LBRACKET, RBRACKET -> BRACKETS_COLOR
        LBRACE, RBRACE -> BRACES_COLOR
        COMMA, SEMICOLON -> SEPARATOR_COLOR
        COLON, EQ, ARROW, EQEQ, NE, GE, LE, GT, LT, MATCH, AND, OR, PIPE, APPEND, CARET, BANG -> OPERATOR_COLOR
        CONTINUATION -> CONTINUATION_COLOR
        TokenType.BAD_CHARACTER -> HighlighterColors.BAD_CHARACTER
        else -> null
    })

    companion object {
        private fun key(name: String, fallback: TextAttributesKey) = TextAttributesKey.createTextAttributesKey("JBOSS_CLI.$name", fallback)
        @JvmField val KEYWORD_COLOR = key("KEYWORD", Colors.KEYWORD)
        @JvmField val OPERATION_COLOR = key("OPERATION", Colors.FUNCTION_CALL)
        @JvmField val PATH_COLOR = key("PATH", Colors.CLASS_REFERENCE)
        @JvmField val COMMAND_COLOR = key("COMMAND", Colors.FUNCTION_CALL)
        @JvmField val PARAMETER_COLOR = key("PARAMETER", Colors.INSTANCE_FIELD)
        @JvmField val STRING_COLOR = key("STRING", Colors.STRING)
        @JvmField val VARIABLE_COLOR = key("VARIABLE", Colors.LOCAL_VARIABLE)
        @JvmField val NUMBER_COLOR = key("NUMBER", Colors.NUMBER)
        @JvmField val CONSTANT_COLOR = key("CONSTANT", Colors.KEYWORD)
        @JvmField val COMMENT_COLOR = key("COMMENT", Colors.LINE_COMMENT)
        @JvmField val OPERATOR_COLOR = key("OPERATOR", Colors.OPERATION_SIGN)
        @JvmField val PARENTHESES_COLOR = key("PARENTHESES", Colors.PARENTHESES)
        @JvmField val BRACKETS_COLOR = key("BRACKETS", Colors.BRACKETS)
        @JvmField val BRACES_COLOR = key("BRACES", Colors.BRACES)
        @JvmField val SEPARATOR_COLOR = key("SEPARATOR", Colors.COMMA)
        @JvmField val CONTINUATION_COLOR = key("CONTINUATION", Colors.OPERATION_SIGN)
        @JvmField val VALUE_COLOR = key("VALUE", Colors.IDENTIFIER)
    }
}

package dev.jbosscli.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.COMMAND_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.OPERATION_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.PARAMETER_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.PATH_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.VALUE_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.VARIABLE_COLOR
import dev.jbosscli.lexer.JBossCliLexerAdapter
import dev.jbosscli.psi.JBossCliTypes.*

/** Color roles from PSI, so a colon in java:/jdbc is not mistaken for an operation. */
class JBossCliAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.firstChild != null) return
        val type = element.node?.elementType ?: return
        if (type == STRING) {
            annotateInterpolations(element, holder)
            return
        }
        if (type in setOf(VARIABLE, EXPRESSION, STRING, SUBSTITUTION, COMMENT)) return
        val ancestors = generateSequence(element.parent) { it.parent }.take(5).mapNotNull { it.node?.elementType }.toList()
        val color = when {
            OPERATION_NAME in ancestors || type == COLON && element.parent?.node?.elementType == OPERATION -> OPERATION_COLOR
            ADDRESS in ancestors -> PATH_COLOR
            COMMAND_NAME in ancestors -> COMMAND_COLOR
            element.parent?.node?.elementType == IDENTIFIER && element.parent.parent?.node?.elementType in setOf(
                ARGUMENT, MAP_ENTRY, HEADER_ENTRY, NAMED_ARGUMENT, VARIABLE_ASSIGNMENT,
            ) -> PARAMETER_COLOR
            type !in setOf(NUMBER, TRUE, FALSE, UNDEFINED) && element.parent?.node?.elementType in setOf(IDENTIFIER, SCALAR) -> VALUE_COLOR
            else -> return
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(color).create()
    }

    /** Keep strings intact for parsing, and reuse CLI tokens for their substitution ranges. */
    private fun annotateInterpolations(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        val lexer = JBossCliLexerAdapter()
        // Bound incomplete expressions by the next unescaped closing quote.
        var end = 1
        while (end < text.length && text[end] != text[0]) {
            end = (end + if (text[end] == '\\') 2 else 1).coerceAtMost(text.length)
        }
        var offset = 1 // Skip the opening quote.
        while (offset < end) {
            when (text[offset]) {
                '\\' -> offset += 2 // Escaped dollars and quotes remain string text.
                '$' -> {
                    lexer.start(text, offset, end)
                    if (lexer.tokenType == VARIABLE || lexer.tokenType == EXPRESSION) {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                            .range(TextRange(element.textOffset + offset, element.textOffset + lexer.tokenEnd))
                            .textAttributes(VARIABLE_COLOR).create()
                        offset = lexer.tokenEnd
                    } else {
                        offset++
                    }
                }
                else -> offset++
            }
        }
    }
}

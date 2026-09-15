package dev.jbosscli.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import dev.jbosscli.psi.JBossCliTypes

class JBossCliParserUtil : GeneratedParserUtilBase() {
    companion object {
        private val TRIVIA = setOf(
            com.intellij.psi.TokenType.WHITE_SPACE, JBossCliTypes.COMMENT, JBossCliTypes.CONTINUATION,
        )
        /** The previous raw token must touch this token (no whitespace, comment or continuation). */
        @JvmStatic fun adjacent(builder: PsiBuilder, level: Int): Boolean {
            val previous = builder.rawLookup(-1) ?: return false
            return previous !in TRIVIA && previous != JBossCliTypes.NEWLINE
        }

        @JvmStatic fun batchStart(builder: PsiBuilder, level: Int): Boolean {
            if (builder.tokenType != JBossCliTypes.BATCH) return false
            return nextTokenText(builder) !in setOf("-l", "--help")
        }

        @JvmStatic fun operationOption(builder: PsiBuilder, level: Int): Boolean =
            builder.tokenText == "--operation"

        @JvmStatic fun helpCommand(builder: PsiBuilder, level: Int): Boolean {
            return !builder.eof() && nextTokenText(builder) == "--help"
        }

        private fun nextTokenText(builder: PsiBuilder): String? {
            var offset = 1
            while (builder.rawLookup(offset) in TRIVIA) offset++
            if (builder.rawLookup(offset) == null) return null
            val end = builder.rawTokenTypeStart(offset + 1).takeIf { it >= 0 } ?: builder.originalText.length
            return builder.originalText.subSequence(builder.rawTokenTypeStart(offset), end).toString()
        }

        /** Always consume a bad logical line, and leave the next statement available to the grammar. */
        @JvmStatic fun recoverLine(builder: PsiBuilder, level: Int): Boolean {
            if (builder.eof() || builder.tokenType == JBossCliTypes.NEWLINE) return false
            val error = builder.mark()
            do {
                builder.advanceLexer()
            } while (!builder.eof() && builder.tokenType != JBossCliTypes.NEWLINE)
            error.error("Expected a CLI command or operation")
            return true
        }
    }
}

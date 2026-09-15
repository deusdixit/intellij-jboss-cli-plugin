package dev.jbosscli

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.jbosscli.psi.JBossCliTypes.*

class JBossCliBraceMatcher : PairedBraceMatcher {
    override fun getPairs() = arrayOf(BracePair(LPAREN, RPAREN, false), BracePair(LBRACKET, RBRACKET, false), BracePair(LBRACE, RBRACE, false))
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) =
        contextType == null || contextType in setOf(NEWLINE, COMMA, SEMICOLON, RPAREN, RBRACKET, RBRACE, com.intellij.psi.TokenType.WHITE_SPACE)
    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) = openingBraceOffset
}

package dev.jbosscli

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.TokenType
import dev.jbosscli.lexer.JBossCliLexerAdapter
import dev.jbosscli.parser.JBossCliParser
import dev.jbosscli.psi.JBossCliFile
import dev.jbosscli.psi.JBossCliTypes

class JBossCliParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = JBossCliLexerAdapter()
    override fun createParser(project: Project?) = JBossCliParser()
    override fun getFileNodeType() = FILE
    override fun getWhitespaceTokens() = TokenSet.create(TokenType.WHITE_SPACE, JBossCliTypes.CONTINUATION)
    override fun getCommentTokens() = TokenSet.create(JBossCliTypes.COMMENT)
    override fun getStringLiteralElements() = TokenSet.create(JBossCliTypes.STRING)
    override fun createElement(node: ASTNode) = JBossCliTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider) = JBossCliFile(viewProvider)
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = ParserDefinition.SpaceRequirements.MAY

    companion object { @JvmField val FILE = IFileElementType(JBossCliLanguage) }
}

package dev.jbosscli.format

import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import dev.jbosscli.psi.JBossCliTypes.*

class JBossCliBlock(node: ASTNode, private val blockIndent: Indent = Indent.getNoneIndent()) : AbstractBlock(node, null, null) {
    override fun buildChildren(): List<Block> = generateSequence(myNode.firstChildNode) { it.treeNext }
        .filter { it.textLength > 0 && !it.text.all(Char::isWhitespace) }
        .map { child -> JBossCliBlock(child, childIndent(child)) }.toList()

    private fun childIndent(child: ASTNode): Indent = when {
        child.elementType == BODY -> Indent.getNormalIndent()
        child.elementType == COMMENT && myNode.elementType in CONTROL_BLOCKS -> Indent.getNormalIndent()
        myNode.elementType in CONTAINERS && child.elementType !in DELIMITERS -> Indent.getNormalIndent()
        else -> Indent.getNoneIndent()
    }

    override fun getIndent() = blockIndent
    // Bare CLI values and command arguments can depend on spaces. Only adjust indentation.
    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null
    override fun isLeaf() = myNode.firstChildNode == null
    override fun getChildAttributes(newChildIndex: Int) = ChildAttributes(
        if (myNode.elementType in CONTAINERS || myNode.elementType in CONTROL_BLOCKS) Indent.getNormalIndent()
        else Indent.getNoneIndent(), null,
    )

    companion object {
        private val CONTAINERS = setOf(ARGUMENT_LIST, LIST_VALUE, OBJECT_VALUE, HEADER_BLOCK, PROPERTY_VALUE, BYTE_LITERAL)
        private val DELIMITERS = setOf(LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, COMMA, SEMICOLON, CONTINUATION)
        private val CONTROL_BLOCKS = setOf(IF_BLOCK, ELSE_BRANCH, TRY_BLOCK, CATCH_BRANCH, FINALLY_BRANCH, FOR_BLOCK, BATCH_BLOCK)
    }
}

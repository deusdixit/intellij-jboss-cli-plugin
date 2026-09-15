package dev.jbosscli.format

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.psi.formatter.DocumentBasedFormattingModel

class JBossCliFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel =
        // NEWLINE is a grammar token. Edit whitespace in the document so the PSI
        // formatter cannot leave an old NEWLINE token beside inserted whitespace.
        DocumentBasedFormattingModel(
            JBossCliBlock(formattingContext.node),
            formattingContext.codeStyleSettings,
            formattingContext.containingFile,
        )
}

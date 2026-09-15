package dev.jbosscli.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import dev.jbosscli.JBossCliFileType
import dev.jbosscli.JBossCliLanguage

class JBossCliFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JBossCliLanguage) {
    override fun getFileType() = JBossCliFileType.INSTANCE
    override fun toString() = "JBoss CLI file"
}

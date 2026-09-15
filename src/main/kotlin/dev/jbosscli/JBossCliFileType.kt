package dev.jbosscli

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

class JBossCliFileType private constructor() : LanguageFileType(JBossCliLanguage) {
    override fun getName() = "JBoss CLI"
    override fun getDescription() = "JBoss EAP / WildFly CLI script"
    override fun getDefaultExtension() = "cli"
    override fun getIcon() = ICON

    companion object {
        @JvmField val INSTANCE = JBossCliFileType()
        @JvmField val ICON = IconLoader.getIcon("/icons/cli.svg", JBossCliFileType::class.java)
    }
}

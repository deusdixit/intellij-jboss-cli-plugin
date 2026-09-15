package dev.jbosscli.format

import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import dev.jbosscli.JBossCliLanguage

class JBossCliCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = JBossCliLanguage
    override fun getCodeSample(settingsType: SettingsType) = """
        if (outcome == success) of /:read-resource
            batch
                /system-property=sample:add(value=true)
            run-batch
        end-if
    """.trimIndent()
    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 4
        indentOptions.CONTINUATION_INDENT_SIZE = 4
        indentOptions.TAB_SIZE = 4
        indentOptions.USE_TAB_CHARACTER = false
    }
}

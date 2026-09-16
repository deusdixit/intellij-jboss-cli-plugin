package dev.jbosscli.highlight

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.jbosscli.JBossCliFileType
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.BRACES_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.BRACKETS_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.COMMAND_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.COMMENT_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.CONSTANT_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.CONTINUATION_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.KEYWORD_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.NUMBER_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.OPERATION_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.OPERATOR_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.PARAMETER_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.PARENTHESES_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.PATH_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.SEPARATOR_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.STRING_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.VALUE_COLOR
import dev.jbosscli.highlight.JBossCliSyntaxHighlighter.Companion.VARIABLE_COLOR

class JBossCliColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = "JBoss / WildFly CLI"
    override fun getIcon() = JBossCliFileType.ICON
    override fun getHighlighter() = JBossCliSyntaxHighlighter()
    override fun getAdditionalHighlightingTagToDescriptorMap() = mapOf(
        "path" to PATH_COLOR, "op" to OPERATION_COLOR, "param" to PARAMETER_COLOR, "cmd" to COMMAND_COLOR,
        "var" to VARIABLE_COLOR,
    )
    override fun getAttributeDescriptors() = arrayOf(
        AttributesDescriptor("Keyword", KEYWORD_COLOR), AttributesDescriptor("Operation", OPERATION_COLOR),
        AttributesDescriptor("Resource address", PATH_COLOR), AttributesDescriptor("Command", COMMAND_COLOR),
        AttributesDescriptor("Parameter / map key", PARAMETER_COLOR), AttributesDescriptor("String", STRING_COLOR),
        AttributesDescriptor("Variable / expression", VARIABLE_COLOR), AttributesDescriptor("Number", NUMBER_COLOR),
        AttributesDescriptor("Constant", CONSTANT_COLOR), AttributesDescriptor("Comment", COMMENT_COLOR),
        AttributesDescriptor("Operator", OPERATOR_COLOR), AttributesDescriptor("Parentheses", PARENTHESES_COLOR),
        AttributesDescriptor("List brackets", BRACKETS_COLOR), AttributesDescriptor("Object / header braces", BRACES_COLOR),
        AttributesDescriptor("Separator", SEPARATOR_COLOR), AttributesDescriptor("Line continuation", CONTINUATION_COLOR),
        AttributesDescriptor("Bare value", VALUE_COLOR),
    )
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDemoText() = """
        # Configure a management resource
        set target=/system-property=demo
        if (outcome != success) of ${'$'}target<op>:read-resource</op>
            ${'$'}target<op>:add</op>(<param>value</param>="hello")
        else
            <cmd>echo</cmd> "Configured <var>${'$'}target</var> in <var>${'$'}{app_log_directory}</var>"
        end-if
        batch
            <path>/subsystem=undertow/server=default-server</path><op>:read-resource</op>(<param>recursive</param>=true)
            <path>/system-property=demo</path><op>:write-attribute</op>(name=value,value=${'$'}{demo:default})
        run-batch
        <path>/subsystem=example</path><op>:add</op>(options=[{name="first",timeout=30}]) \
            {allow-resource-service-restart=true;blocking-timeout=60}
    """.trimIndent()
}

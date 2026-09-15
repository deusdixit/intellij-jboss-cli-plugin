package dev.jbosscli

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JBossCliFormattingTest : BasePlatformTestCase() {
    fun testNestedControlFlowAndBranches() = checkFormatting(
        """
            for item in /:read-children-names(child-type=system-property)
            if (outcome == success) of /system-property=${'$'}item:read-resource
            echo Found ${'$'}item
            else
            echo Missing
            end-if
            done
            try
            echo try
            catch
            echo catch
            finally
            echo finally
            end-try
        """,
        """
            for item in /:read-children-names(child-type=system-property)
                if (outcome == success) of /system-property=${'$'}item:read-resource
                    echo Found ${'$'}item
                else
                    echo Missing
                end-if
            done
            try
                echo try
            catch
                echo catch
            finally
                echo finally
            end-try
        """,
    )

    fun testBatchesAndInformationalCommands() = checkFormatting(
        """
            batch -l
            echo outside
            batch
            /system-property=a:add(value=one)
            holdback-batch work
            echo outside
            batch work
            /system-property=b:add(value=two)
            run-batch
            echo outside
        """,
        """
            batch -l
            echo outside
            batch
                /system-property=a:add(value=one)
            holdback-batch work
            echo outside
            batch work
                /system-property=b:add(value=two)
            run-batch
            echo outside
        """,
    )

    fun testContinuedCompositeValues() = checkFormatting(
        """
            /subsystem=example:add( \
            options={ \
            handlers=[ \
            {name=one}, \
            {name=two} \
            ] \
            } \
            )
        """,
        """
            /subsystem=example:add( \
                options={ \
                    handlers=[ \
                        {name=one}, \
                        {name=two} \
                    ] \
                } \
            )
        """,
    )

    fun testCommentIndentation() = checkFormatting(
        """
            if (outcome == success) of /:read-resource
            # inside
            echo yes
            # still inside
            else
            # other branch
            echo no
            end-if
        """,
        """
            if (outcome == success) of /:read-resource
                # inside
                echo yes
                # still inside
            else
                # other branch
                echo no
            end-if
        """,
    )

    fun testFormattingDoesNotChangeLiteralWhitespaceOrContinuation() {
        val text = "set value=\"a  b\"\n:write-attribute(name=value,value=\"first \\\n  second\")\necho a  b\\ c\n"
        myFixture.configureByText("literal.cli", text)
        reformat()
        assertEquals(text, myFixture.file.text)
    }

    fun testIncompleteBlockFormattingDoesNotCrash() {
        myFixture.configureByText("partial.cli", "if (outcome == success) of /:read-resource\necho partial\n")
        reformat()
        assertTrue(myFixture.file.text.contains("    echo partial"))
    }

    private fun checkFormatting(before: String, after: String) {
        myFixture.configureByText("format.cli", before.trimIndent() + "\n")
        reformat()
        assertEquals(after.trimIndent() + "\n", myFixture.file.text)
        val once = myFixture.file.text
        reformat()
        assertEquals("Formatting must be idempotent", once, myFixture.file.text)
    }

    private fun reformat() {
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
    }
}

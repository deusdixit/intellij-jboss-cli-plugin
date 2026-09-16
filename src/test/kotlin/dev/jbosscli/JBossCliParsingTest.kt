package dev.jbosscli

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.jbosscli.psi.JBossCliTypes.*
import java.nio.file.Files
import java.nio.file.Path

class JBossCliParsingTest : BasePlatformTestCase() {
    fun testLoggingConfigurationWithSubstitutions() {
        assertParses("""
            set loggingProfile=${'$'}{prop_logging_profile}
            set file=application.log
            echo "Configuring ${'$'}loggingProfile in ${'$'}{app_log_directory}"
            /subsystem=logging/logging-profile=${'$'}loggingProfile:add()
            /subsystem=logging/logging-profile=${'$'}loggingProfile/logger=${'$'}logger:add(category=${'$'}logger,autoflush="true")
            /subsystem=logging/logging-profile=${'$'}loggingProfile/periodic-size-rotating-file-handler=${'$'}file:add(file={path="${'$'}{app_log_directory}/${'$'}file"},append=true,autoflush=false)
            unset file loggingProfile
        """.trimIndent())
        // Built-in commands remain valid identifiers and values in management operations.
        assertParses("/system-property=echo:add(echo=echo,value=\"${'$'}{outer:${'$'}{inner:default}}\")")
        assertParses("echo --help")
    }

    fun testOperationAndValueCorpus() {
        val scripts = listOf(
            ":read-resource", "/:read-resource()", ":read-resource(recursive,!include-runtime)",
            "/subsystem=undertow/server=default-server:read-resource(recursive=true)",
            "../server=default-server:read-resource", "/deployment=*:read-resource",
            "/system-property=\"a/b:c\":add(value=\"a,b:{}\")",
            "/system-property=a\\/b:add(value=hello\\ world)",
            "/system-property=demo:add(value=java:/jdbc/Test)",
            "/system-property=demo:add(value=jdbc:h2:mem:test?MODE=PostgreSQL)",
            ":add(value=[one,two,{name=three,roles=[admin,user]}])",
            ":add(value={\"one\" => 1,\"two\" => [(\"key\" => \"value\")]})",
            ":add(value={Hello World})", ":add(value=bytes{0x01,0xff})",
            ":add(value=expression \"${'$'}{property:default}\")",
            ":add(value=[1L,-2,3.14,1e3,42BI,3.2BD,true,false,undefined])",
            ":remove(){allow-resource-service-restart=true;blocking-timeout=60;roles=[admin]}",
            ":read-resource{rollout main(rolling-to-servers=true)^other,max(max-failed-servers=1) rollback-across-groups}",
            ":read-resource{rollout id=my-plan}",
            "${'$'}target:${'$'}operation(${ '$'}parameter=${'$'}value)",
            "/host=${'$'}{host:primary}:read-resource",
            ":read-resource | grep success > output.txt",
            ":read-resource >> output.txt",
        )
        for (script in scripts) assertParses(script)
    }

    fun testCommandsAndAssignments() {
        for (script in listOf(
            "set target=/subsystem=undertow", "set one=first two=second", "set", "unset target",
            "set result=`/:read-attribute(name=server-state)`",
            "connect --controller=remote+http://localhost:9990", "cd ../subsystem=undertow", "pwd",
            "deployment deploy-file /tmp/demo.war --force --headers={blocking-timeout=60}",
            "embed-server --server-config=standalone.xml --std-out=echo", "stop-embedded-server",
            "alias inspect='/subsystem=undertow:read-resource'", "inspect", "${'$'}cmd --${'$'}arg=${'$'}value",
            "attachment display --operation=/deployment=demo.war:read-content(path=META-INF/MANIFEST.MF)",
            "batch -l\necho after", "batch --help\necho after", "run-batch --file=setup.cli",
            "if --help", "for --help", "try --help", "set --help", "unset --help",
            "echo if else done finally #literal", "command add --node-type=subsystem=threads --command-name=thread",
        )) assertParses(script)
    }

    fun testControlFlowAndBatchFiles() {
        Files.list(Path.of("examples")).use { paths ->
            paths.filter { it.toString().endsWith(".cli") }.forEach { assertParses(Files.readString(it)) }
        }
        assertParses("if result.value==true of /:read-resource\necho yes\nend-if")
        assertParses("try\necho attempt\nfinally\necho cleanup\nend-try")
        assertParses("batch\n:read-resource\nholdback-batch work\necho outside\nbatch work\n:read-resource\ndiscard-batch")
        assertParses("if ((outcome == success) && (result.value >= 3 || result.name ~= \"demo.*\")) of /:read-resource\necho yes\nend-if")
    }

    fun testIncompleteRequestRecoversAtNextLogicalLine() {
        for (broken in listOf(":", "/subsystem=", ":add(value=[one,", ":add(value={key=", ":add(value=\"unfinished")) {
            val file = myFixture.configureByText("broken.cli", "$broken\n/system-property=after:read-resource\n")
            val operations = PsiTreeUtil.collectElements(file) { it.node.elementType == OPERATION }
            assertTrue("Lost following request: $broken", operations.any { it.text == "/system-property=after:read-resource" })
        }
    }

    fun testAllPrefixesRemainLosslessAndTerminate() {
        val script = Files.readString(Path.of("examples/control-flow.cli"))
        for (length in 0..script.length step 11) {
            val prefix = script.take(length)
            assertEquals(prefix, myFixture.configureByText("partial.cli", prefix).node.text)
        }
        for (text in listOf("}".repeat(200), "[".repeat(100), "if\nelse\nend-if\n", "try\ncatch\nfinally\n", "for item in\n")) {
            assertEquals(text, myFixture.configureByText("partial.cli", text).node.text)
        }
    }

    fun testFileTypeRegistration() {
        assertSame(JBossCliFileType.INSTANCE, myFixture.configureByText("registered.cli", "# CLI\n").fileType)
    }

    private fun assertParses(script: String) {
        val file = myFixture.configureByText("corpus.cli", script)
        val errors = PsiTreeUtil.collectElementsOfType(file, PsiErrorElement::class.java)
        assertTrue("$script\n" + errors.joinToString("\n") { "${it.textOffset}: ${it.errorDescription}" }, errors.isEmpty())
        assertEquals(script, file.node.text)
    }
}

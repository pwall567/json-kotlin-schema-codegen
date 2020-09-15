package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import net.pwall.json.schema.codegen.log.ConsoleLog

class CodeGeneratorEnumTest {

    @Test fun `should output deeply nested class`() {
        val input = File("src/test/resources/test-enum.schema.json")
        val codeGenerator = CodeGenerator(log = ConsoleLog)
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestEnum", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

enum class TestEnum {
    FIRST,
    SECOND,
    THIRD
}
"""

    }

}

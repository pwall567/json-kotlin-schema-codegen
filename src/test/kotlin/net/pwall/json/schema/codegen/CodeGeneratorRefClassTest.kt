package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails

class CodeGeneratorRefClassTest {

    @Test fun `should generate class with reference to other generated class`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterOuter = StringWriter()
        val outputDetailsOuter = OutputDetails("dummy1", emptyList(), "TestRefClassOuter", "kt", stringWriterOuter)
        val stringWriterInner = StringWriter()
        val outputDetailsInner = OutputDetails("dummy1", emptyList(), "TestRefClassInner", "kt", stringWriterInner)
        codeGenerator.outputResolver = CodeGeneratorTestUtil.outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedOuter) { stringWriterOuter.toString() }
        expect(expectedInner) { stringWriterInner.toString() }
    }

    companion object {

        const val expectedOuter =
"""package com.example

import java.util.UUID

data class TestRefClassOuter(
        val id: UUID,
        val single: TestRefClassInner,
        val multiple: List<TestRefClassInner>
)
"""

        const val expectedInner =
"""package com.example

data class TestRefClassInner(
        val name: String
)
"""

    }

}

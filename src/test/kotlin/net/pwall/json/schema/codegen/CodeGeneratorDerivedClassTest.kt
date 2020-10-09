package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.log.LoggerFactory

class CodeGeneratorDerivedClassTest {

    @Test fun `should generate base class and derived class`() {
        val input = File("src/test/resources/test-derived-class")
        val codeGenerator = CodeGenerator(templateName = "open_class", loggerFactory = LoggerFactory.getDefault())
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterBase = StringWriter()
        val outputDetailsBase = OutputDetails("dummy1", emptyList(), "TestBaseClass", "kt", stringWriterBase)
        val stringWriterDerived = StringWriter()
        val outputDetailsDerived = OutputDetails("dummy1", emptyList(), "TestDerivedClass", "kt", stringWriterDerived)
        codeGenerator.outputResolver = CodeGeneratorTestUtil.outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedBase) { stringWriterBase.toString() }
        expect(expectedDerived) { stringWriterDerived.toString() }
    }

    companion object {

        const val expectedBase =
"""package com.example

import java.util.UUID

open class TestBaseClass(
        val id: UUID
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseClass &&
            id == other.id

    override fun hashCode(): Int = 
            id.hashCode()

}
"""

        const val expectedDerived =
"""package com.example

import java.util.UUID

open class TestDerivedClass(
        id: UUID,
        val name: String
) : TestBaseClass(id) {

    override fun equals(other: Any?): Boolean = this === other || other is TestDerivedClass && super.equals(other) &&
            name == other.name

    override fun hashCode(): Int = super.hashCode() xor
            name.hashCode()

}
"""

    }

}

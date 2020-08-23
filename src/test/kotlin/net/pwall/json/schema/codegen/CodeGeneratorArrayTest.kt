package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

class CodeGeneratorArrayTest {

    @Test fun `should generate nested class for array of object`() {
        val input = File("src/test/resources/test-array")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy1", emptyList(), "TestArray", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import java.util.UUID

data class TestArray(
        val aaa: List<Person>
) {

    data class Person(
            val id: UUID,
            val name: String
    )

}
"""
    }

}

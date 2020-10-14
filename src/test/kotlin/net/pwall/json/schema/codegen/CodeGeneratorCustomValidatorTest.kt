package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter
import net.pwall.json.JSONString
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.validation.StringValidator

class CodeGeneratorCustomValidatorTest {

    @Test fun `should generate correct code for custom validation`() {
        val input = File("src/test/resources/test-custom-validator.schema.json")
        val parser = Parser()
        parser.customValidationHandler = { key, uri, location, value ->
            when (key) {
                "x-test" -> {
                    if (value is JSONString && value.get() == "not-empty")
                        StringValidator(uri, location, StringValidator.ValidationType.MIN_LENGTH, 1)
                    else
                        throw RuntimeException("Unknown type")
                }
                else -> null
            }
        }
        val schema = parser.parse(input)
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestCustom", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateClass(schema, "TestCustom")
        expect(expected) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

data class TestCustom(
        val aaa: String
) {

    init {
        require(aaa.isNotEmpty()) { "aaa length < minimum 1 - ${'$'}{aaa.length}" }
    }

}
"""

    }

}

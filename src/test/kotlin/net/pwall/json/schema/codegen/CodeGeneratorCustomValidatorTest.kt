/*
 * @(#) CodeGeneratorCustomValidatorTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.JSONString
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
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
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestCustom", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateClass(schema, "TestCustom")
        expect(createHeader("TestCustom") + expected) { stringWriter.toString() }
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

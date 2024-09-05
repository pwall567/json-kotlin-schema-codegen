/*
 * @(#) CodeGeneratorNonstandardFormatTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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

import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.json.schema.validation.StringValidator

class CodeGeneratorNonstandardFormatTest {

    @Test fun `should generate correct code for custom validation`() {
        val input = File("src/test/resources/test-nonstandard-format.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            FormatValidator.DelegatingFormatChecker(keyword,
                    StringValidator(null, JSONPointer.root, StringValidator.ValidationType.MIN_LENGTH, 1))
        }
        val schema = parser.parse(input)
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestCustom", "kt", dirs), stringWriter)
        codeGenerator.generateClass(schema, "TestCustom")
        expect(createHeader("TestCustom.kt") + expected1) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for format delegating to another format`() {
        val input = File("src/test/resources/test-nonstandard-format-delegating.schema.json")
        val parser = Parser()
        parser.nonstandardFormatHandler = { keyword ->
            when (keyword) {
                "guid" -> FormatValidator.DelegatingFormatChecker(keyword,
                        FormatValidator(null, JSONPointer.root, FormatValidator.UUIDFormatChecker))
                else -> null
            }
        }
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser = parser
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDelegating", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestDelegating.kt") + expected2) { stringWriter.toString() }
    }

    companion object {

        const val expected1 =
"""package com.example

/**
 * Test non-standard format.
 */
data class TestCustom(
    val ggg: String
) {

    init {
        require(ggg.isNotEmpty()) { "ggg length < minimum 1 - ${'$'}{ggg.length}" }
    }

}
"""

        const val expected2 =
"""package com.example

import java.util.UUID

/**
 * Test non-standard format (delegating).
 */
data class TestDelegating(
    val aaa: UUID,
    val bbb: UUID? = null
)
"""

    }

}

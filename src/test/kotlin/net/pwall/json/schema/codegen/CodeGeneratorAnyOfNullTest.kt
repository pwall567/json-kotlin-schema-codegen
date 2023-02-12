/*
 * @(#) CodeGeneratorAnyOfNullTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2023 Peter Wall
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

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorAnyOfNullTest {

    @Test fun `should generate nested class for array of object`() {
        val input = File("src/test/resources/test-anyof-null.json")
        val personInput = File("src/test/resources/simple/simple.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAnyofNull", "kt", dirs))
        val personOutputDetails = OutputDetails(TargetFileName("TestPerson", "kt", dirs))
        CodeGenerator().apply {
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetails, personOutputDetails)
            schemaParser.preLoad(File("src/test/resources/test1/utility.schema.json"))
            generate(listOf(input, personInput))
        }
        expect(createHeader("TestAnyofNull.kt") + expected) { outputDetails.output() }
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test use of anyOf to make property nullable.
 */
data class TestAnyofNull(
    val aaa: TestPerson?,
    val bbb: TestPerson,
    val ccc: List<TestPerson?>,
    val ddd: List<TestPerson>
)
"""

    }

}

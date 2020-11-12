/*
 * @(#) CodeGeneratorDerivedClassTest.kt
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

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDerivedClassTest {

    @Test fun `should generate base class and derived class`() {
        val input = File("src/test/resources/test-derived-class")
        val codeGenerator = CodeGenerator(templateName = "open_class")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterBase = StringWriter()
        val outputDetailsBase = OutputDetails("dummy1", emptyList(), "TestBaseClass", "kt", stringWriterBase)
        val stringWriterDerived = StringWriter()
        val outputDetailsDerived = OutputDetails("dummy1", listOf("derived"), "TestDerivedClass", "kt",
                stringWriterDerived)
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestBaseClass") + expectedBase) { stringWriterBase.toString() }
        expect(createHeader("TestDerivedClass") + expectedDerived) { stringWriterDerived.toString() }
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
"""package com.example.derived

import java.util.UUID

import com.example.TestBaseClass

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

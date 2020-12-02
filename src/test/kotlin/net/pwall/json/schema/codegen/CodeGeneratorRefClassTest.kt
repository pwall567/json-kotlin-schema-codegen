/*
 * @(#) CodeGeneratorRefClassTest.kt
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
import java.nio.file.FileSystems

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorRefClassTest {

    @Test fun `should generate class with reference to other generated class`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterOuter = StringWriter()
        val outputDetailsOuter = OutputDetails("dummy1", emptyList(), "TestRefClassOuter", "kt", stringWriterOuter)
        val stringWriterInner = StringWriter()
        val outputDetailsInner = OutputDetails("dummy1", emptyList(), "TestRefClassInner", "kt", stringWriterInner)
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestRefClassOuter") + expectedOuter) { stringWriterOuter.toString() }
        expect(createHeader("TestRefClassInner") + expectedInner) { stringWriterInner.toString() }
    }

    @Test fun `should generate class with reference to other generated class using Path`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterOuter = StringWriter()
        val outputDetailsOuter = OutputDetails("dummy1", emptyList(), "TestRefClassOuter", "kt", stringWriterOuter)
        val stringWriterInner = StringWriter()
        val outputDetailsInner = OutputDetails("dummy1", emptyList(), "TestRefClassInner", "kt", stringWriterInner)
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateFromPaths(input)
        expect(createHeader("TestRefClassOuter") + expectedOuter) { stringWriterOuter.toString() }
        expect(createHeader("TestRefClassInner") + expectedInner) { stringWriterInner.toString() }
    }

    @Test fun `should generate class with reference to other generated class using Path list`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterOuter = StringWriter()
        val outputDetailsOuter = OutputDetails("dummy1", emptyList(), "TestRefClassOuter", "kt", stringWriterOuter)
        val stringWriterInner = StringWriter()
        val outputDetailsInner = OutputDetails("dummy1", emptyList(), "TestRefClassInner", "kt", stringWriterInner)
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateFromPaths(listOf(input))
        expect(createHeader("TestRefClassOuter") + expectedOuter) { stringWriterOuter.toString() }
        expect(createHeader("TestRefClassInner") + expectedInner) { stringWriterInner.toString() }
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

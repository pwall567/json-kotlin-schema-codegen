/*
 * @(#) CodeGeneratorCompanionObjectTest.kt
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

class CodeGeneratorCompanionObjectTest {

    @Test fun `should generate companion object when global flag set`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/companion-object-all-config.json"))
        val outputDetails = OutputDetails(TargetFileName("TestGenerateAny", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.kt") + expectedWithCO) { outputDetails.output() }
    }

    @Test fun `should generate companion object when individual class selected`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.companionObjectForClasses.add("TestGenerateAny")
        val outputDetails = OutputDetails(TargetFileName("TestGenerateAny", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.kt") + expectedWithCO) { outputDetails.output() }
    }

    @Test fun `should not generate companion object when individual class not selected`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.companionObjectForClasses.add("TestGenerateOther")
        val outputDetails = OutputDetails(TargetFileName("TestGenerateAny", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.kt") + expectedWithoutCO) { outputDetails.output() }
    }

    companion object {

        const val expectedWithCO =
"""package com.example

data class TestGenerateAny(
    /** No details, so should generate type Any */
    val aaa: Any
) {

    companion object

}
"""

        const val expectedWithoutCO =
"""package com.example

data class TestGenerateAny(
    /** No details, so should generate type Any */
    val aaa: Any
)
"""

    }

}

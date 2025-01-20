/*
 * @(#) CodeGeneratorHTTPTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022, 2025 Peter Wall
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

import java.net.URI

import io.kjson.pointer.JSONPointer

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorHTTPTest {

    @Test fun `should process schema from HTTP URI`() {
        val input = URI("http://kjson.io/json/http/testhttp1.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser.setExtendedResolver(codeGenerator.schemaParser.defaultExtendedResolver)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input)
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Testhttp1", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe CodeGeneratorTestUtil.createHeader("Testhttp1.kt") + expected1
    }

    @Test fun `should process schema from HTTP URI with subpackage name`() {
        val input = URI("http://kjson.io/json/http/testhttp1.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser.setExtendedResolver(codeGenerator.schemaParser.defaultExtendedResolver)
        codeGenerator.basePackageName = "com"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input, "example")
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Testhttp1", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe CodeGeneratorTestUtil.createHeader("Testhttp1.kt") + expected1
    }

    @Test fun `should process composite from HTTP URI`() {
        val input = URI("http://kjson.io/json/http/testhttp2.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.schemaParser.setExtendedResolver(codeGenerator.schemaParser.defaultExtendedResolver)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addCompositeTargets(input, JSONPointer("/\$defs"))
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Def1", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe CodeGeneratorTestUtil.createHeader("Def1.kt") + expected2
    }

    @Suppress("ConstPropertyName")
    companion object {

        const val expected1 =
"""package com.example

data class Testhttp1(
    val xxx: Def1? = null
) {

    data class Def1(
        val aaa: Long
    )

}
"""

        const val expected2 =
"""package com.example

data class Def1(
    val aaa: Long
)
"""

    }

}

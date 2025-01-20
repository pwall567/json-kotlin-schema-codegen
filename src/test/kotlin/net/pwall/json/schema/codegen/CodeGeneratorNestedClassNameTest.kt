/*
 * @(#) CodeGeneratorNestedClassNameTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024 Peter Wall
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

import java.io.File

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageDirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageName
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultFile

class CodeGeneratorNestedClassNameTest {

    @Test fun `should use supplied name for nested class`() {
        val input = File("src/test/resources/test-ap-false-pattern-object.schema.json")
        val config = File("src/test/resources/config/class-name-nested-config.json")
        val outputDetails = OutputDetails(TargetFileName("TestClassNaming", "kt", packageDirs))
        CodeGenerator().apply {
            configure(config, config.toURI())
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestClassNaming")
    }

}

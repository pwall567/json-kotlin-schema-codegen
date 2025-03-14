/*
 * @(#) CodeGeneratorValidationTest.kt
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

import io.kstuff.test.shouldThrow

import io.kstuff.log.LogList
import io.kstuff.log.shouldHaveWarning

import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageDirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageName

class CodeGeneratorValidationTest {

    @Test fun `should warn on validation errors`() {
        LogList().use { logList ->
            val input = File("src/test/resources/test-validation-errors.schema.json")
            val outputDetails = OutputDetails(TargetFileName("TestValidationErrors", "kt", packageDirs))
            CodeGenerator().apply {
                examplesValidationOption = CodeGenerator.ValidationOption.BLOCK
                basePackageName = packageName
                outputResolver = outputCapture(outputDetails)
                shouldThrow<JSONSchemaException>("Validation errors encountered") {
                    generate(input)
                }
            }
            logList shouldHaveWarning "http://pwall.net/test-validation-errors#/minimum: " +
                    "Number fails check: minimum 10, was 5, at #/examples/1"
        }
    }

    @Test fun `should warn on validation errors involving nonstandard format`() {
        LogList().use { logList ->
            val input = File("src/test/resources/test-validation-errors-format.schema.json")
            val configFile = File("src/test/resources/config/nonstandard-format-config.json")
            val outputDetails = OutputDetails(TargetFileName("TestValidationErrorsFormat", "kt", packageDirs))
            CodeGenerator().apply {
                examplesValidationOption = CodeGenerator.ValidationOption.BLOCK
                configure(configFile)
                basePackageName = packageName
                outputResolver = outputCapture(outputDetails)
                shouldThrow<JSONSchemaException>("Validation errors encountered") {
                    generate(input)
                }
            }
            val expectedError = "http://pwall.net/test-validation-errors-format#/format/money/pattern: " +
                    "String doesn't match pattern ^[0-9]{1,16}\\.[0-9]{2}\$ - \"wrong\", at #/examples/1"
            logList shouldHaveWarning expectedError
        }
    }

}

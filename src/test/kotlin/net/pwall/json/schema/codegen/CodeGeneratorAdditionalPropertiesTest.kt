/*
 * @(#) CodeGeneratorAdditionalPropertiesTest.kt
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
import kotlin.test.expect

import java.io.File

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageDirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageName
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultFile

class CodeGeneratorAdditionalPropertiesTest {

    @Test fun `should generate code for additionalProperties false`() {
        val input = File("src/test/resources/test-additional-properties-false.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesFalse", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesFalse")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties true`() {
        val input = File("src/test/resources/test-additional-properties-true.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesTrue", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesTrue")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties with schema`() {
        val input = File("src/test/resources/test-additional-properties-schema.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchema1", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesSchema1")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties with valid schema`() {
        val input = File("src/test/resources/test-additional-properties-schema-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesSchemaValid")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties false with extra`() {
        val input = File("src/test/resources/test-additional-properties-false-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesFalseExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesFalseExtra")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties true with extra`() {
        val input = File("src/test/resources/test-additional-properties-true-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesTrueExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesTrueExtra")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties with schema with extra`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesSchemaExtra")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties with schema with extra 2`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra2.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra2", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesSchemaExtra2")) { outputDetails.output() }
    }

    @Test fun `should generate code for additionalProperties with schema with extra 3`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra3.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra3", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.MAP
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        expect(resultFile("TestAdditionalPropertiesSchemaExtra3")) { outputDetails.output() }
    }

    // TODO - also check that all of this works for nested classes

    // TODO - check that when a delegating class uses a nested class, it is generated correctly

    // TODO - patternProperties

}

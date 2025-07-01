/*
 * @(#) CodeGeneratorAdditionalPropertiesTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024, 2025 Peter Wall
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

class CodeGeneratorAdditionalPropertiesTest {

    @Test fun `should generate code for additionalProperties false`() {
        // type: object, no properties, no patternProperties, additionalProperties: false
        val input = File("src/test/resources/test-additional-properties-false.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesFalse", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesFalse")
    }

    @Test fun `should generate code for additionalProperties true`() {
        val input = File("src/test/resources/test-additional-properties-true.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesTrue", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesTrue")
    }

    @Test fun `should generate code for additionalProperties with schema`() {
        val input = File("src/test/resources/test-additional-properties-schema.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchema1", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesSchema1")
    }

    @Test fun `should generate code for additionalProperties with valid schema`() {
        val input = File("src/test/resources/test-additional-properties-schema-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesSchemaValid")
    }

    @Test fun `should generate code for additionalProperties false with extra`() {
        val input = File("src/test/resources/test-additional-properties-false-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesFalseExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesFalseExtra")
    }

    @Test fun `should generate code for aP false with extra valid`() {
        val input = File("src/test/resources/test-ap-false-extra-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApFalseExtraValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApFalseExtraValid")
    }

    @Test fun `should generate code for aP true with extra`() {
        val input = File("src/test/resources/test-ap-true-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtra")
    }

    @Test fun `should generate code for aP true with optional extra`() {
        val input = File("src/test/resources/test-ap-true-extra-optional.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraOptional", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraOptional")
    }

    @Test fun `should generate code for aP true with extra with validation`() {
        val input = File("src/test/resources/test-ap-true-extra-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraValid")
    }

    @Test fun `should generate code for aP true with optional extra with validation`() {
        val input = File("src/test/resources/test-ap-true-extra-opt-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraOptValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraOptValid")
    }

    @Test fun `should generate code for aP true with extra array`() {
        val input = File("src/test/resources/test-ap-true-extra-array.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraArray", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraArray")
    }

    @Test fun `should generate code for aP true with optional extra array`() {
        val input = File("src/test/resources/test-ap-true-extra-array-opt.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraArrayOpt", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraArrayOpt")
    }

    @Test fun `should generate code for aP true with optional extra array with validation`() {
        val input = File("src/test/resources/test-ap-true-extra-array-opt-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraArrayOptValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraArrayOptValid")
    }

    @Test fun `should generate code for aP true with extra with default value`() {
        val input = File("src/test/resources/test-ap-true-extra-default.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraDefault", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraDefault")
    }

    @Test fun `should generate code for additionalProperties with schema with extra`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesSchemaExtra")
    }

    @Test fun `should generate code for additionalProperties with schema with extra 2`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra2.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra2", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesSchemaExtra2")
    }

    @Test fun `should generate code for additionalProperties with schema with extra 3`() {
        val input = File("src/test/resources/test-additional-properties-schema-extra3.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestAdditionalPropertiesSchemaExtra3", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestAdditionalPropertiesSchemaExtra3")
    }

    @Test fun `should generate code for aP false with pattern`() {
        val input = File("src/test/resources/test-ap-false-pattern.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApFalsePattern", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApFalsePattern")
    }

    @Test fun `should generate code for aP false with pattern and extra`() {
        val input = File("src/test/resources/test-ap-false-pattern-extra.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApFalsePatternExtra", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApFalsePatternExtra")
    }

    @Test fun `should generate code for aP false with pattern and optional extra`() {
        val input = File("src/test/resources/test-ap-false-pattern-extra-opt.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApFalsePatternExtraOpt", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApFalsePatternExtraOpt")
    }

    @Test fun `should generate code for aP false with pattern specifying object`() {
        val input = File("src/test/resources/test-ap-false-pattern-object.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApFalsePatternObject", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApFalsePatternObject")
    }

    @Test fun `should generate code for aP true with pattern`() {
        val input = File("src/test/resources/test-ap-true-pattern.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTruePattern", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTruePattern")
    }

    @Test fun `should generate code for aP true with pattern with validation`() {
        val input = File("src/test/resources/test-ap-true-pattern-valid.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTruePatternValid", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTruePatternValid")
    }

    @Test fun `should generate code for aP true with pattern and extra fields`() {
        val input = File("src/test/resources/test-ap-true-extra-pattern.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraPattern", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraPattern")
    }

    @Test fun `should generate code for nested aP false`() {
        val input = File("src/test/resources/test-ap-nested-false.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApNestedFalse", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApNestedFalse")
    }

    @Test fun `should generate code for nested aP true`() {
        val input = File("src/test/resources/test-ap-nested-true.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApNestedTrue", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApNestedTrue")
    }

    @Test fun `should generate code for aP true with extra nested object`() {
        val input = File("src/test/resources/test-ap-true-extra-nested.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueExtraNested", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueExtraNested")
    }

    @Test fun `should generate code for aP true with minimum`() {
        val input = File("src/test/resources/test-ap-true-min.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueMin", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueMin")
    }

    @Test fun `should generate code for aP true with minimum 1`() {
        val input = File("src/test/resources/test-ap-true-min-1.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueMin1", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueMin1")
    }

    @Test fun `should generate code for aP true with minimum 0`() {
        val input = File("src/test/resources/test-ap-true-min-0.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueMin0", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueMin0")
    }

    @Test fun `should generate code for aP true with maximum`() {
        val input = File("src/test/resources/test-ap-true-max.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueMax", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueMax")
    }

    @Test fun `should generate code for aP true with minimum and maximum`() {
        val input = File("src/test/resources/test-ap-true-min-max.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueMinMax", "kt", packageDirs))
        CodeGenerator().apply {
            additionalPropertiesOption = CodeGenerator.AdditionalPropertiesOption.STRICT
            basePackageName = packageName
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueMinMax")
    }

    @Test fun `should generate code for aP true with const number`() {
        val input = File("src/test/resources/test-ap-true-const.schema.json")
        val outputDetails = OutputDetails(TargetFileName("TestApTrueConst", "kt", packageDirs))
        CodeGenerator().apply {
            configure(File("src/test/resources/config/ap-config.json"))
            outputResolver = outputCapture(outputDetails)
            generate(input)
        }
        outputDetails.output() shouldBe resultFile("TestApTrueConst")
    }

    // TODO - more tests of nested classes?

    // TODO - propertyNames

}

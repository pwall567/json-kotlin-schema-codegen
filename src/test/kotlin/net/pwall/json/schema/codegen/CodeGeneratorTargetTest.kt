/*
 * @(#) CodeGeneratorTargetTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022 Peter Wall
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
import java.nio.file.FileSystems

import io.kstuff.test.shouldBe

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorTargetTest {

    @Test fun `should add to target list`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        val parser = Parser()
        codeGenerator.addTarget(parser.parse(input), "Test")
        codeGenerator.numTargets shouldBe 1
        with(codeGenerator.targets[0]) {
            className shouldBe "Test"
            packageName shouldBe "com.example"
        }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample
    }

    @Test fun `should add to target list by File`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input)
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample
    }

    @Test fun `should add to target list by File with subpackage name`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input, "example")
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample
    }

    @Test fun `should add to target list by Path`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input.toPath())
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample
    }

    @Test fun `should add to target list by Path with subpackage name`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com"
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTarget(input.toPath(), "example")
        codeGenerator.numTargets shouldBe 1
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        outputDetails.output() shouldBe createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample
    }

    @Test fun `should add targets for multiple schemata`() {
        val input = File("src/test/resources/test-multiple.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addCompositeTargets(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        codeGenerator.numTargets shouldBe 2
        codeGenerator.generateAllTargets()
        outputDetailsA.output() shouldBe createHeader("TypeA.kt") + CodeGeneratorMultipleTest.expectedTypeA
        outputDetailsB.output() shouldBe createHeader("TypeB.kt") + CodeGeneratorMultipleTest.expectedTypeB
    }

    @Test fun `should add targets for multiple files in directory`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTargets(listOf(input))
        codeGenerator.numTargets shouldBe 2
        codeGenerator.generateAllTargets()
        outputDetailsOuter.output() shouldBe createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter
        outputDetailsInner.output() shouldBe createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner
    }

    @Test fun `should add targets for multiple files in directory with subpackage name`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTargets(listOf(input), "example")
        codeGenerator.numTargets shouldBe 2
        codeGenerator.generateAllTargets()
        outputDetailsOuter.output() shouldBe createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter
        outputDetailsInner.output() shouldBe createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner
    }

    @Test fun `should add targets for multiple files in directory specified by path`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTargetsByPath(listOf(input))
        codeGenerator.numTargets shouldBe 2
        codeGenerator.generateAllTargets()
        outputDetailsOuter.output() shouldBe createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter
        outputDetailsInner.output() shouldBe createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner
    }

    @Test fun `should add targets for multiple files in directory specified by path with subpackage name`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        codeGenerator.numTargets shouldBe 0
        codeGenerator.addTargetsByPath(listOf(input), "example")
        codeGenerator.numTargets shouldBe 2
        codeGenerator.generateAllTargets()
        outputDetailsOuter.output() shouldBe createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter
        outputDetailsInner.output() shouldBe createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner
    }

}

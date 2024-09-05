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
import kotlin.test.expect

import java.io.File
import java.nio.file.FileSystems

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
        expect(0) { codeGenerator.numTargets }
        val parser = Parser()
        codeGenerator.addTarget(parser.parse(input), "Test")
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

    @Test fun `should add to target list by File`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTarget(input)
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

    @Test fun `should add to target list by File with subpackage name`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com"
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTarget(input, "example")
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

    @Test fun `should add to target list by Path`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTarget(input.toPath())
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

    @Test fun `should add to target list by Path with subpackage name`() {
        val input = File("src/test/resources/example.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com"
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTarget(input.toPath(), "example")
        expect(1) { codeGenerator.numTargets }
        val outputDetails = OutputDetails(TargetFileName("Test", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generateAllTargets()
        expect(createHeader("Test.kt") + CodeGeneratorExampleTest.expectedExample) { outputDetails.output() }
    }

    @Test fun `should add targets for multiple schemata`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addCompositeTargets(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(2) { codeGenerator.numTargets }
        codeGenerator.generateAllTargets()
        expect(createHeader("TypeA.kt") + CodeGeneratorMultipleTest.expectedTypeA) { outputDetailsA.output() }
        expect(createHeader("TypeB.kt") + CodeGeneratorMultipleTest.expectedTypeB) { outputDetailsB.output() }
    }

    @Test fun `should add targets for multiple files in directory`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTargets(listOf(input))
        expect(2) { codeGenerator.numTargets }
        codeGenerator.generateAllTargets()
        expect(createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter) {
            outputDetailsOuter.output()
        }
        expect(createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner) {
            outputDetailsInner.output()
        }
    }

    @Test fun `should add targets for multiple files in directory with subpackage name`() {
        val input = File("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTargets(listOf(input), "example")
        expect(2) { codeGenerator.numTargets }
        codeGenerator.generateAllTargets()
        expect(createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter) {
            outputDetailsOuter.output()
        }
        expect(createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner) {
            outputDetailsInner.output()
        }
    }

    @Test fun `should add targets for multiple files in directory specified by path`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTargetsByPath(listOf(input))
        expect(2) { codeGenerator.numTargets }
        codeGenerator.generateAllTargets()
        expect(createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter) {
            outputDetailsOuter.output()
        }
        expect(createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner) {
            outputDetailsInner.output()
        }
    }

    @Test fun `should add targets for multiple files in directory specified by path with subpackage name`() {
        val input = FileSystems.getDefault().getPath("src/test/resources/test-ref-class")
        val codeGenerator = CodeGenerator()
        val outputDetailsOuter = OutputDetails(TargetFileName("TestRefClassOuter", "kt", dirs))
        val outputDetailsInner = OutputDetails(TargetFileName("TestRefClassInner", "kt", dirs))
        codeGenerator.basePackageName = "com"
        codeGenerator.outputResolver = outputCapture(outputDetailsOuter, outputDetailsInner)
        codeGenerator.clearTargets()
        expect(0) { codeGenerator.numTargets }
        codeGenerator.addTargetsByPath(listOf(input), "example")
        expect(2) { codeGenerator.numTargets }
        codeGenerator.generateAllTargets()
        expect(createHeader("TestRefClassOuter.kt") + CodeGeneratorRefClassTest.expectedOuter) {
            outputDetailsOuter.output()
        }
        expect(createHeader("TestRefClassInner.kt") + CodeGeneratorRefClassTest.expectedInner) {
            outputDetailsInner.output()
        }
    }

}

/*
 * @(#) CodeGeneratorOneOf2Test.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2025 Peter Wall
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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultFile
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultJava

class CodeGeneratorOneOf2Test {

    @Test fun `should generate classes for complex multiple oneOf schemata`() {
        val input = File("src/test/resources/test-oneof-2.schema.json")
        val packageName = "net.pwall.json.schema.codegen.test.kotlin.oneof"
        val packageDirs = packageName.split('.')
        val codeGenerator = CodeGenerator()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", packageDirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", packageDirs))
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "kt", packageDirs))
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        outputDetailsA.output() shouldBe resultFile("oneof/TypeA")
        outputDetailsB.output() shouldBe resultFile("oneof/TypeB")
        outputDetailsC.output() shouldBe resultFile("oneof/TypeC")
    }

    @Test fun `should generate classes for complex multiple oneOf schemata in Java`() {
        val input = File("src/test/resources/test-oneof-2.schema.json")
        val packageName = "net.pwall.json.schema.codegen.test.java.oneof"
        val packageDirs = packageName.split('.')
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "java", packageDirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "java", packageDirs))
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "java", packageDirs))
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        outputDetailsA.output() shouldBe resultJava("oneof/TypeA")
        outputDetailsB.output() shouldBe resultJava("oneof/TypeB")
        outputDetailsC.output() shouldBe resultJava("oneof/TypeC")
    }

}

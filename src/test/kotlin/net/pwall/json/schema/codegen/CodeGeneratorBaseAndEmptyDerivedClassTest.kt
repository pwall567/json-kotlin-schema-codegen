/*
 * @(#) CodeGeneratorBaseAndEmptyDerivedClassTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.javaPackageDirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.javaPackageName
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageDirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.packageName
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultFile
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.resultJava

class CodeGeneratorBaseAndEmptyDerivedClassTest {

    @Test fun `should generate base and derived classes`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-empty-derived.schema.json")
        val localDirs = packageDirs + "basederived"
        val localPackage = "$packageName.basederived"
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "kt", localDirs))
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseEmptyDerived", "kt", localDirs))
        CodeGenerator().apply {
            basePackageName = localPackage
            outputResolver = outputCapture(outputDetailsA, outputDetailsB)
            generate(inputA, inputB)
        }
        outputDetailsA.output() shouldBe resultFile("basederived/TestBase")
        outputDetailsB.output() shouldBe resultFile("basederived/TestBaseEmptyDerived")
    }

    @Test fun `should generate base and derived classes in Java`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-empty-derived.schema.json")
        val localDirs = javaPackageDirs + "basederived"
        val localPackage = "$javaPackageName.basederived"
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "java", localDirs))
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseEmptyDerived", "java", localDirs))
        CodeGenerator(TargetLanguage.JAVA).apply {
            basePackageName = localPackage
            outputResolver = outputCapture(outputDetailsA, outputDetailsB)
            generate(inputA, inputB)
        }
        outputDetailsA.output() shouldBe resultJava("basederived/TestBase")
        outputDetailsB.output() shouldBe resultJava("basederived/TestBaseEmptyDerived")
    }

}

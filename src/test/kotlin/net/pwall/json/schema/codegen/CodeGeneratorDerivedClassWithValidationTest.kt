/*
 * @(#) CodeGeneratorNestedClassOptionTest.kt
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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDerivedClassWithValidationTest {

    @Test fun `should generate init check for const on derived class`() {
        val input = File("src/test/resources/test-derived-with-validation.schema.json")
        val codeGenerator = CodeGenerator()
        val packageName = "net.pwall.json.schema.codegen.test.kotlin"
        val dirs = packageName.split('.')
        val baseOutputDetails = OutputDetails(TargetFileName("TestBaseWithValidation", "kt", dirs))
        val derivedOutputDetails0 = OutputDetails(TargetFileName("TestDerivedWithValidation0", "kt", dirs))
        val derivedOutputDetails1 = OutputDetails(TargetFileName("TestDerivedWithValidation1", "kt", dirs))
        val baseExpected = File("src/test/kotlin/${dirs.joinToString("/")}/TestBaseWithValidation.kt")
        val derivedExpected0 = File("src/test/kotlin/${dirs.joinToString("/")}/TestDerivedWithValidation0.kt")
        val derivedExpected1 = File("src/test/kotlin/${dirs.joinToString("/")}/TestDerivedWithValidation1.kt")
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(baseOutputDetails, derivedOutputDetails0, derivedOutputDetails1)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(baseExpected.readText()) { baseOutputDetails.output() }
        expect(derivedExpected0.readText()) { derivedOutputDetails0.output() }
        expect(derivedExpected1.readText()) { derivedOutputDetails1.output() }
    }

    @Test fun `should generate init check for const on derived class in Java`() {
        val input = File("src/test/resources/test-derived-with-validation.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val packageName = "net.pwall.json.schema.codegen.test.java"
        val dirs = packageName.split('.')
        val baseOutputDetails = OutputDetails(TargetFileName("TestBaseWithValidation", "java", dirs))
        val derivedOutputDetails0 = OutputDetails(TargetFileName("TestDerivedWithValidation0", "java", dirs))
        val derivedOutputDetails1 = OutputDetails(TargetFileName("TestDerivedWithValidation1", "java", dirs))
        val baseExpected = File("src/test/kotlin/${dirs.joinToString("/")}/TestBaseWithValidation.java")
        val derivedExpected0 = File("src/test/kotlin/${dirs.joinToString("/")}/TestDerivedWithValidation0.java")
        val derivedExpected1 = File("src/test/kotlin/${dirs.joinToString("/")}/TestDerivedWithValidation1.java")
        codeGenerator.basePackageName = packageName
        codeGenerator.outputResolver = outputCapture(baseOutputDetails, derivedOutputDetails0, derivedOutputDetails1)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(baseExpected.readText()) { baseOutputDetails.output() }
        expect(derivedExpected0.readText()) { derivedOutputDetails0.output() }
        expect(derivedExpected1.readText()) { derivedOutputDetails1.output() }
    }

}

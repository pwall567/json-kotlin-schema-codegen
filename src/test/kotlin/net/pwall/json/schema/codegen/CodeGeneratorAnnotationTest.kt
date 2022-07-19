/*
 * @(#) CodeGeneratorAnnotationTest.kt
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

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorAnnotationTest {

    @Test fun `should generate correct code for annotations`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/annotation-config.yaml"))
        val packageName = "net.pwall.json.schema.codegen.test.kotlin"
        codeGenerator.basePackageName = packageName
        val dirs = packageName.split('.')
        val outputDetails = OutputDetails(TargetFileName("Person", "kt", dirs + "person"))
        val expected = File("src/test/kotlin/${dirs.joinToString("/")}/person/Person.kt")
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(expected.readText()) { outputDetails.output() }
    }

    @Test fun `should generate correct code for annotations in Java`() {
        val input = File("src/test/resources/test1")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.configure(File("src/test/resources/config/annotation-config.yaml"))
        val packageName = "net.pwall.json.schema.codegen.test.java"
        codeGenerator.basePackageName = packageName
        val dirs = packageName.split('.')
        val outputDetails = OutputDetails(TargetFileName("Person", "java", dirs + "person"))
        val expected = File("src/test/kotlin/${dirs.joinToString("/")}/person/Person.java")
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        expect(expected.readText()) { outputDetails.output() }
    }

}

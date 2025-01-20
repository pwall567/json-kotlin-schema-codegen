/*
 * @(#) CodeGeneratorEnumTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021 Peter Wall
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
import java.io.StringWriter

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorEnumTest {

    @Test fun `should output enum class`() {
        val input = File("src/test/resources/test-enum.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEnum", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEnum.kt") + expected
    }

    @Test fun `should output enum class as Java`() {
        val input = File("src/test/resources/test-enum.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEnum", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEnum.java") + expectedJava
    }

    @Test fun `should output enum class as TypeScript`() {
        val input = File("src/test/resources/test-enum.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEnum", "ts"), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEnum.ts") + expectedTypeScript
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test enum.
 */
enum class TestEnum {
    FIRST,
    SECOND,
    THIRD
}
"""

        const val expectedJava =
"""package com.example;

/**
 * Test enum.
 */
public enum TestEnum {
    FIRST,
    SECOND,
    THIRD
}
"""

        const val expectedTypeScript =
"""
/**
 * Test enum.
 */
export type TestEnum =
    "FIRST" |
    "SECOND" |
    "THIRD";
"""

    }

}

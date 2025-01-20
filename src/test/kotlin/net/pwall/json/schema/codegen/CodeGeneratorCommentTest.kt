/*
 * @(#) CodeGeneratorCommentTest.kt
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

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorCommentTest {

    @Test fun `should output generator comment`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.generatorComment = "Created for test purposes"
        codeGenerator.basePackageName = "com.example"
        val outputDetails = OutputDetails(TargetFileName("TestEmpty", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        outputDetails.output() shouldBe createHeader("TestEmpty.kt", "Created for test purposes") + expected
    }

    @Test fun `should output generator comment in Java`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.generatorComment = "Created for test purposes"
        codeGenerator.basePackageName = "com.example"
        val outputDetails = OutputDetails(TargetFileName("TestEmpty", "java", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        outputDetails.output() shouldBe createHeader("TestEmpty.java", "Created for test purposes") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test empty object.
 */
open class TestEmpty
"""

        const val expectedJava =
"""package com.example;

/**
 * Test empty object.
 */
public class TestEmpty {

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestEmpty))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static class Builder {

        public TestEmpty build() {
            return new TestEmpty(
            );
        }

    }

}
"""

    }

}

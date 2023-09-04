/*
 * @(#) CodeGeneratorIntegerTest.kt
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
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorIntegerTest {

    @Test fun `should generate correct code for integer formats`() {
        val input = File("src/test/resources/test-integer-format.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerFormat", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestIntegerFormat.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for integer formats in Java`() {
        val input = File("src/test/resources/test-integer-format.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerFormat", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestIntegerFormat.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test integer formats.
 */
data class TestIntegerFormat(
    val little: Int,
    val big: Long
)
"""

        const val expectedJava =
"""package com.example;

/**
 * Test integer formats.
 */
public class TestIntegerFormat {

    private final int little;
    private final long big;

    public TestIntegerFormat(
            int little,
            long big
    ) {
        this.little = little;
        this.big = big;
    }

    public int getLittle() {
        return little;
    }

    public long getBig() {
        return big;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestIntegerFormat))
            return false;
        TestIntegerFormat cg_typedOther = (TestIntegerFormat)cg_other;
        if (little != cg_typedOther.little)
            return false;
        return big == cg_typedOther.big;
    }

    @Override
    public int hashCode() {
        int hash = little;
        return hash ^ (int)big;
    }

    public static class Builder {

        private int little;
        private long big;

        public Builder withLittle(int little) {
            this.little = little;
            return this;
        }

        public Builder withBig(long big) {
            this.big = big;
            return this;
        }

        public TestIntegerFormat build() {
            return new TestIntegerFormat(
                    little,
                    big
            );
        }

    }

}
"""

    }

}

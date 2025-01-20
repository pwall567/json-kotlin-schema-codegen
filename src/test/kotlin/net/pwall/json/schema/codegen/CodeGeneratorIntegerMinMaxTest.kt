/*
 * @(#) CodeGeneratorIntegerMinMaxTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2022 Peter Wall
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

class CodeGeneratorIntegerMinMaxTest {

    @Test fun `should generate correct code for integers with min and max near limits`() {
        val input = File("src/test/resources/test-integer-min-max.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerMinMax", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestIntegerMinMax.kt") + expected
    }

    @Test fun `should generate correct code for integers with min and max near limits in Java`() {
        val input = File("src/test/resources/test-integer-min-max.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerMinMax", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestIntegerMinMax.java") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test integer minimum and maximum.
 */
data class TestIntegerMinMax(
    val aaa: Int,
    val bbb: Int,
    val ccc: Long,
    val ddd: Long
) {

    init {
        require(aaa >= 0) { "aaa < minimum 0 - ${'$'}aaa" }
        require(ccc >= 0L) { "ccc < minimum 0 - ${'$'}ccc" }
    }

}
"""

        const val expectedJava =
"""package com.example;

/**
 * Test integer minimum and maximum.
 */
public class TestIntegerMinMax {

    private final int aaa;
    private final int bbb;
    private final long ccc;
    private final long ddd;

    public TestIntegerMinMax(
            int aaa,
            int bbb,
            long ccc,
            long ddd
    ) {
        if (aaa < 0)
            throw new IllegalArgumentException("aaa < minimum 0 - " + aaa);
        this.aaa = aaa;
        this.bbb = bbb;
        if (ccc < 0L)
            throw new IllegalArgumentException("ccc < minimum 0 - " + ccc);
        this.ccc = ccc;
        this.ddd = ddd;
    }

    public int getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    public long getCcc() {
        return ccc;
    }

    public long getDdd() {
        return ddd;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestIntegerMinMax))
            return false;
        TestIntegerMinMax cg_typedOther = (TestIntegerMinMax)cg_other;
        if (aaa != cg_typedOther.aaa)
            return false;
        if (bbb != cg_typedOther.bbb)
            return false;
        if (ccc != cg_typedOther.ccc)
            return false;
        return ddd == cg_typedOther.ddd;
    }

    @Override
    public int hashCode() {
        int hash = aaa;
        hash ^= bbb;
        hash ^= (int)ccc;
        return hash ^ (int)ddd;
    }

    public static class Builder {

        private int aaa;
        private int bbb;
        private long ccc;
        private long ddd;

        public Builder withAaa(int aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(int bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(long ccc) {
            this.ccc = ccc;
            return this;
        }

        public Builder withDdd(long ddd) {
            this.ddd = ddd;
            return this;
        }

        public TestIntegerMinMax build() {
            return new TestIntegerMinMax(
                    aaa,
                    bbb,
                    ccc,
                    ddd
            );
        }

    }

}
"""

    }

}

/*
 * @(#) CodeGeneratorConstantTest.kt
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
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorConstantTest {

    @Test fun `should generate correct code for constants`() {
        val input = File("src/test/resources/test-const.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestConst", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestConst.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for constants in Java`() {
        val input = File("src/test/resources/test-const.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestConst", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestConst.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import java.math.BigDecimal

/**
 * Test const.
 */
data class TestConst(
    val aaa: Int,
    val bbb: Long,
    val ccc: String,
    val ddd: BigDecimal
) {

    init {
        require(aaa == 8) { "aaa not constant value 8 - ${'$'}aaa" }
        require(bbb == 123456789123456789L) { "bbb not constant value 123456789123456789 - ${'$'}bbb" }
        require(ccc == cg_str0) { "ccc not constant value ${'$'}cg_str0 - ${'$'}ccc" }
        require(ddd.compareTo(cg_dec1) == 0) { "ddd not constant value 1.5 - ${'$'}ddd" }
    }

    companion object {
        private const val cg_str0 = "Hello!"
        private val cg_dec1 = BigDecimal("1.5")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.math.BigDecimal;

/**
 * Test const.
 */
public class TestConst {

    private static final String cg_str0 = "Hello!";
    private static final BigDecimal cg_dec1 = new BigDecimal("1.5");

    private final int aaa;
    private final long bbb;
    private final String ccc;
    private final BigDecimal ddd;

    public TestConst(
            int aaa,
            long bbb,
            String ccc,
            BigDecimal ddd
    ) {
        if (aaa != 8)
            throw new IllegalArgumentException("aaa not constant value 8 - " + aaa);
        this.aaa = aaa;
        if (bbb != 123456789123456789L)
            throw new IllegalArgumentException("bbb not constant value 123456789123456789 - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        if (!cg_str0.equals(ccc))
            throw new IllegalArgumentException("ccc not constant value " + cg_str0 + " - " + ccc);
        this.ccc = ccc;
        if (ddd == null)
            throw new IllegalArgumentException("Must not be null - ddd");
        if (ddd.compareTo(cg_dec1) != 0)
            throw new IllegalArgumentException("ddd not constant value 1.5 - " + ddd);
        this.ddd = ddd;
    }

    public int getAaa() {
        return aaa;
    }

    public long getBbb() {
        return bbb;
    }

    public String getCcc() {
        return ccc;
    }

    public BigDecimal getDdd() {
        return ddd;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestConst))
            return false;
        TestConst typedOther = (TestConst)other;
        if (aaa != typedOther.aaa)
            return false;
        if (bbb != typedOther.bbb)
            return false;
        if (!ccc.equals(typedOther.ccc))
            return false;
        return ddd.equals(typedOther.ddd);
    }

    @Override
    public int hashCode() {
        int hash = aaa;
        hash ^= (int)bbb;
        hash ^= ccc.hashCode();
        return hash ^ ddd.hashCode();
    }

    public static class Builder {

        private int aaa;
        private long bbb;
        private String ccc;
        private BigDecimal ddd;

        public Builder withAaa(int aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(long bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(String ccc) {
            this.ccc = ccc;
            return this;
        }

        public Builder withDdd(BigDecimal ddd) {
            this.ddd = ddd;
            return this;
        }

        public TestConst build() {
            return new TestConst(
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

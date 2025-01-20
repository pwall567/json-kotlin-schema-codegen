/*
 * @(#) CodeGeneratorDecimalMinMaxTest.kt
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

class CodeGeneratorDecimalMinMaxTest {

    @Test fun `should generate correct code for decimals with min and max constraints`() {
        val input = File("src/test/resources/test-decimal-min-max.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDecimalMinMax", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestDecimalMinMax.kt") + expected
    }

    @Test fun `should generate correct code for decimals with min and max constraints in Java`() {
        val input = File("src/test/resources/test-decimal-min-max.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDecimalMinMax", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestDecimalMinMax.java") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

import java.math.BigDecimal

/**
 * Test decimal minimum and maximum.
 */
data class TestDecimalMinMax(
    val aaa: BigDecimal,
    val bbb: BigDecimal,
    val ccc: BigDecimal
) {

    init {
        require(aaa in cg_dec0..cg_dec1) { "aaa not in range 0..1000.00 - ${'$'}aaa" }
        require(bbb >= cg_dec2) { "bbb < minimum -10000.00 - ${'$'}bbb" }
        require(bbb < BigDecimal.ZERO) { "bbb >= exclusive maximum 0 - ${'$'}bbb" }
        require(ccc in cg_dec3..cg_dec4) { "ccc not in range -1..1 - ${'$'}ccc" }
    }

    companion object {
        private val cg_dec0 = BigDecimal.ZERO
        private val cg_dec1 = BigDecimal("1000.00")
        private val cg_dec2 = BigDecimal("-10000.00")
        private val cg_dec3 = BigDecimal("-1")
        private val cg_dec4 = BigDecimal.ONE
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.math.BigDecimal;

/**
 * Test decimal minimum and maximum.
 */
public class TestDecimalMinMax {

    private static final BigDecimal cg_dec0 = BigDecimal.ZERO;
    private static final BigDecimal cg_dec1 = new BigDecimal("1000.00");
    private static final BigDecimal cg_dec2 = new BigDecimal("-10000.00");
    private static final BigDecimal cg_dec3 = new BigDecimal("-1");
    private static final BigDecimal cg_dec4 = BigDecimal.ONE;

    private final BigDecimal aaa;
    private final BigDecimal bbb;
    private final BigDecimal ccc;

    public TestDecimalMinMax(
            BigDecimal aaa,
            BigDecimal bbb,
            BigDecimal ccc
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (aaa.compareTo(cg_dec0) < 0 || aaa.compareTo(cg_dec1) > 0)
            throw new IllegalArgumentException("aaa not in range 0..1000.00 - " + aaa);
        this.aaa = aaa;
        if (bbb == null)
            throw new IllegalArgumentException("Must not be null - bbb");
        if (bbb.compareTo(cg_dec2) < 0)
            throw new IllegalArgumentException("bbb < minimum -10000.00 - " + bbb);
        if (bbb.compareTo(BigDecimal.ZERO) >= 0)
            throw new IllegalArgumentException("bbb >= exclusive maximum 0 - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        if (ccc.compareTo(cg_dec3) < 0 || ccc.compareTo(cg_dec4) > 0)
            throw new IllegalArgumentException("ccc not in range -1..1 - " + ccc);
        this.ccc = ccc;
    }

    public BigDecimal getAaa() {
        return aaa;
    }

    public BigDecimal getBbb() {
        return bbb;
    }

    public BigDecimal getCcc() {
        return ccc;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestDecimalMinMax))
            return false;
        TestDecimalMinMax cg_typedOther = (TestDecimalMinMax)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        if (!bbb.equals(cg_typedOther.bbb))
            return false;
        return ccc.equals(cg_typedOther.ccc);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= bbb.hashCode();
        return hash ^ ccc.hashCode();
    }

    public static class Builder {

        private BigDecimal aaa;
        private BigDecimal bbb;
        private BigDecimal ccc;

        public Builder withAaa(BigDecimal aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(BigDecimal bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(BigDecimal ccc) {
            this.ccc = ccc;
            return this;
        }

        public TestDecimalMinMax build() {
            return new TestDecimalMinMax(
                    aaa,
                    bbb,
                    ccc
            );
        }

    }

}
"""

    }

}

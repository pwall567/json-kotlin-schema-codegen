/*
 * @(#) CodeGeneratorNotTest.kt
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

import java.io.File

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorNotTest {

    @Test fun `should output negated format tests`() {
        val input = File("src/test/resources/test-not.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        val outputDetails = OutputDetails(TargetFileName("TestNot", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        outputDetails.output() shouldBe createHeader("TestNot.kt") + expected
    }

    @Test fun `should output negated format tests in Java`() {
        val input = File("src/test/resources/test-not.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.basePackageName = "com.example"
        val outputDetails = OutputDetails(TargetFileName("TestNot", "java", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails)
        codeGenerator.generate(input)
        outputDetails.output() shouldBe createHeader("TestNot.java") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

import net.pwall.json.validation.JSONValidation

/**
 * Test "not" validations.
 */
data class TestNot(
    val aaa: String,
    val bbb: Int,
    val ccc: String
) {

    init {
        require(!JSONValidation.isDateTime(aaa)) { "aaa matches format date-time - ${'$'}aaa" }
        require(!JSONValidation.isDate(aaa)) { "aaa matches format date - ${'$'}aaa" }
        require(bbb in 0..20) { "bbb not in range 0..20 - ${'$'}bbb" }
        require(bbb !in 5..8) { "bbb in range 5..8 - ${'$'}bbb" }
        require(ccc !in cg_array0) { "ccc in enumerated values - ${'$'}ccc" }
    }

    companion object {
        private val cg_array0 = setOf(
            "abc",
            "def",
            "ghi"
        )
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.Arrays;
import java.util.List;
import net.pwall.json.validation.JSONValidation;

/**
 * Test "not" validations.
 */
public class TestNot {

    private static final List<String> cg_array0 = Arrays.asList(
            "abc",
            "def",
            "ghi"
    );

    private final String aaa;
    private final int bbb;
    private final String ccc;

    public TestNot(
            String aaa,
            int bbb,
            String ccc
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (JSONValidation.isDateTime(aaa))
            throw new IllegalArgumentException("aaa matches format date-time - " + aaa);
        if (JSONValidation.isDate(aaa))
            throw new IllegalArgumentException("aaa matches format date - " + aaa);
        this.aaa = aaa;
        if (bbb < 0 || bbb > 20)
            throw new IllegalArgumentException("bbb not in range 0..20 - " + bbb);
        if (bbb >= 5 && bbb <= 8)
            throw new IllegalArgumentException("bbb in range 5..8 - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        if (cg_array0.contains(ccc))
            throw new IllegalArgumentException("ccc in enumerated values - " + ccc);
        this.ccc = ccc;
    }

    public String getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    public String getCcc() {
        return ccc;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestNot))
            return false;
        TestNot cg_typedOther = (TestNot)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        if (bbb != cg_typedOther.bbb)
            return false;
        return ccc.equals(cg_typedOther.ccc);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= bbb;
        return hash ^ ccc.hashCode();
    }

    public static class Builder {

        private String aaa;
        private int bbb;
        private String ccc;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(int bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(String ccc) {
            this.ccc = ccc;
            return this;
        }

        public TestNot build() {
            return new TestNot(
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

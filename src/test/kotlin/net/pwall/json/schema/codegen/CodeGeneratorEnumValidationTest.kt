/*
 * @(#) CodeGeneratorEnumValidationTest.kt
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

class CodeGeneratorEnumValidationTest {

    @Test fun `should generate correct code for string enum validations`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEnumValidation", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEnumValidation.kt") + expected
    }

    @Test fun `should generate correct code for string enum validations in Java`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEnumValidation", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEnumValidation.java") + expectedJava
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test enum validation.
 */
data class TestEnumValidation(
    val aaa: String,
    val bbb: Int,
    val ccc: Ccc
) {

    init {
        require(aaa in cg_array0) { "aaa not in enumerated values - ${'$'}aaa" }
        require(bbb in cg_array1) { "bbb not in enumerated values - ${'$'}bbb" }
    }

    enum class Ccc {
        xxx,
        yyy,
        zzz
    }

    companion object {
        private val cg_array0 = setOf(
            "ABCDE",
            "FGHIJ",
            "!@#*%"
        )
        private val cg_array1 = setOf(123, 456, 789)
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Test enum validation.
 */
public class TestEnumValidation {

    private static final List<String> cg_array0 = Arrays.asList(
            "ABCDE",
            "FGHIJ",
            "!@#*%"
    );
    private static final List<Integer> cg_array1 = Arrays.asList(123, 456, 789);

    private final String aaa;
    private final int bbb;
    private final Ccc ccc;

    public TestEnumValidation(
            String aaa,
            int bbb,
            Ccc ccc
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (!cg_array0.contains(aaa))
            throw new IllegalArgumentException("aaa not in enumerated values - " + aaa);
        this.aaa = aaa;
        if (!cg_array1.contains(bbb))
            throw new IllegalArgumentException("bbb not in enumerated values - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        this.ccc = ccc;
    }

    public String getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    public Ccc getCcc() {
        return ccc;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestEnumValidation))
            return false;
        TestEnumValidation cg_typedOther = (TestEnumValidation)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        if (bbb != cg_typedOther.bbb)
            return false;
        return ccc == cg_typedOther.ccc;
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
        private Ccc ccc;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(int bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(Ccc ccc) {
            this.ccc = ccc;
            return this;
        }

        public TestEnumValidation build() {
            return new TestEnumValidation(
                    aaa,
                    bbb,
                    ccc
            );
        }

    }

    public enum Ccc {
        xxx,
        yyy,
        zzz
    }

}
"""

    }

}

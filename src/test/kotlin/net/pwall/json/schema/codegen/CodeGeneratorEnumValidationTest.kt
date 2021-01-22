/*
 * @(#) CodeGeneratorEnumValidationTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020 Peter Wall
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
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorEnumValidationTest {

    @Test fun `should generate correct code for string enum validations`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEnumValidation", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestEnumValidation") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for string enum validations in Java`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy", emptyList(), "TestEnumValidation", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestEnumValidation") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example


/**
 * Test enum validation.
 */
data class TestEnumValidation(
    val aaa: String,
    val bbb: Int
) {

    init {
        require(aaa in cg_array0) { "aaa not in enumerated values - ${'$'}aaa" }
        require(bbb in cg_array1) { "bbb not in enumerated values - ${'$'}bbb" }
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

    public TestEnumValidation(
            String aaa,
            int bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (!cg_array0.contains(aaa))
            throw new IllegalArgumentException("aaa not in enumerated values - " + aaa);
        this.aaa = aaa;
        if (!cg_array1.contains(bbb))
            throw new IllegalArgumentException("bbb not in enumerated values - " + bbb);
        this.bbb = bbb;
    }

    public String getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestEnumValidation))
            return false;
        TestEnumValidation typedOther = (TestEnumValidation)other;
        if (!aaa.equals(typedOther.aaa))
            return false;
        return bbb == typedOther.bbb;
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        return hash ^ bbb;
    }

}
"""

    }

}

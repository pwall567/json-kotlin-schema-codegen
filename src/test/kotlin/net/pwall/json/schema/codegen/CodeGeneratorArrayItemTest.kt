/*
 * @(#) CodeGeneratorArrayItemTest.kt
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

class CodeGeneratorArrayItemTest {

    @Test fun `should generate correct validations for arrays of integer and string`() {
        val input = File("src/test/resources/test-array-items.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy1", emptyList(), "TestArrayItems", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestArrayItems") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct validations for arrays of integer and string in Java`() {
        val input = File("src/test/resources/test-array-items.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture("dummy1", emptyList(), "TestArrayItems", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestArrayItems") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example


data class TestArrayItems(
        val aaa: List<Int>,
        val bbb: List<String>? = null
) {

    init {
        aaa.forEach {
            require(it >= 0) { "aaa item < minimum 0 - ${'$'}it" }
            require(it <= 9999) { "aaa item > maximum 9999 - ${'$'}it" }
        }
        require(aaa.isNotEmpty()) { "aaa length < minimum 1 - ${'$'}{aaa.size}" }
        require(aaa.size <= 5) { "aaa length > maximum 5 - ${'$'}{aaa.size}" }
        bbb?.forEach {
            require(cg_regex0 matches it) { "bbb item does not match pattern ${'$'}cg_regex0 - ${'$'}it" }
        }
    }

    companion object {
        private val cg_regex0 = Regex("^[A-Z]{3}\${'$'}")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.List;
import java.util.regex.Pattern;

public class TestArrayItems {

    private static final Pattern cg_regex0 = Pattern.compile("^[A-Z]{3}${'$'}");

    private final List<Integer> aaa;
    private final List<String> bbb;

    public TestArrayItems(
            List<Integer> aaa,
            List<String> bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        for (int it : aaa) {
            if (it < 0)
                throw new IllegalArgumentException("aaa item < minimum 0 - " + it);
            if (it > 9999)
                throw new IllegalArgumentException("aaa item > maximum 9999 - " + it);
        }
        if (aaa.size() < 1)
            throw new IllegalArgumentException("aaa length < minimum 1 - " + aaa.size());
        if (aaa.size() > 5)
            throw new IllegalArgumentException("aaa length > maximum 5 - " + aaa.size());
        this.aaa = aaa;
        if (bbb != null) {
            for (String it : bbb) {
                if (!cg_regex0.matcher(it).matches())
                    throw new IllegalArgumentException("bbb item does not match pattern " + cg_regex0 + " - " + it);
            }
        }
        this.bbb = bbb;
    }

    public List<Integer> getAaa() {
        return aaa;
    }

    public List<String> getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestArrayItems))
            return false;
        TestArrayItems typedOther = (TestArrayItems)other;
        if (!aaa.equals(typedOther.aaa))
            return false;
        return bbb == null ? typedOther.bbb == null : bbb.equals(typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= bbb != null ? bbb.hashCode() : 0;
        return hash;
    }

}
"""

    }

}

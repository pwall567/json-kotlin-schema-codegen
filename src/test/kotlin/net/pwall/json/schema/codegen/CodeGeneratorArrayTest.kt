/*
 * @(#) CodeGeneratorArrayTest.kt
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

class CodeGeneratorArrayTest {

    @Test fun `should generate nested class for array of object`() {
        val input = File("src/test/resources/test-array")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy1", emptyList(), "TestArray", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    @Test fun `should generate nested class for array of object in Java`() {
        val input = File("src/test/resources/test-array")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy1", emptyList(), "TestArray", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import java.util.UUID

data class TestArray(
        val aaa: List<Person>
) {

    init {
        require(aaa.size <= 2) { "aaa length > maximum - ${'$'}{aaa.size}" }
    }

    data class Person(
            val id: UUID,
            val name: String
    ) {

        init {
            require(cg_regex0 matches name) { "name does not match pattern ${'$'}cg_regex0 - ${'$'}name" }
        }

    }

    companion object {
        private val cg_regex0 = Regex(""${'"'}^[A-Z][A-Za-z]*${'$'}""${'"'})
    }

}
"""

        const val expectedJava = // NOTE: Does not expect pattern validation
"""package com.example;

import java.util.List;
import java.util.UUID;

public class TestArray {

    private final List<Person> aaa;

    public TestArray(
            List<Person> aaa
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (aaa.size() > 2)
            throw new IllegalArgumentException("aaa length > maximum - " + aaa.size());
        this.aaa = aaa;
    }

    public List<Person> getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestArray))
            return false;
        TestArray typedOther = (TestArray)other;
        if (!aaa.equals(typedOther.aaa))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash ^= aaa.hashCode();
        return hash;
    }

    public static class Person {

        private final UUID id;
        private final String name;

        public Person(
                UUID id,
                String name
        ) {
            if (id == null)
                throw new IllegalArgumentException("Must not be null - id");
            this.id = id;
            if (name == null)
                throw new IllegalArgumentException("Must not be null - name");
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof Person))
                return false;
            Person typedOther = (Person)other;
            if (!id.equals(typedOther.id))
                return false;
            if (!name.equals(typedOther.name))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash ^= id.hashCode();
            hash ^= name.hashCode();
            return hash;
        }

    }

}
"""
    }

}

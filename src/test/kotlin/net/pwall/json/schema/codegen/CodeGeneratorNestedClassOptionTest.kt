/*
 * @(#) CodeGeneratorNestedClassOptionTest.kt
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

class CodeGeneratorNestedClassOptionTest {

    // this is just a copy of CodeGeneratorArrayTest, repurposed to test NestedClassNameOption

    @Test fun `should generate nested class for array of object`() {
        val input = File("src/test/resources/test-array")
        val codeGenerator = CodeGenerator()
        codeGenerator.nestedClassNameOption = CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestArray", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestArray.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate nested class for array of object in Java`() {
        val input = File("src/test/resources/test-array")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        codeGenerator.nestedClassNameOption = CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestArray", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestArray.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import java.util.UUID

/**
 * Test generation of arrays.
 */
data class TestArray(
    val aaa: List<Aaa>,
    val bbb: Set<String> = setOf()
) {

    init {
        require(aaa.size in 1..5) { "aaa length not in range 1..5 - ${'$'}{aaa.size}" }
    }

    data class Aaa(
        val id: UUID,
        val name: String
    ) {

        init {
            require(cg_regex0.containsMatchIn(name)) { "name does not match pattern ${'$'}cg_regex0 - ${'$'}name" }
        }

    }

    companion object {
        private val cg_regex0 = Regex("^[A-Z][A-Za-z]*\${'$'}")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Test generation of arrays.
 */
public class TestArray {

    private static final Pattern cg_regex0 = Pattern.compile("^[A-Z][A-Za-z]*${'$'}");

    private final List<Aaa> aaa;
    private final Set<String> bbb;

    public TestArray(
            List<Aaa> aaa,
            Set<String> bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (aaa.size() < 1 || aaa.size() > 5)
            throw new IllegalArgumentException("aaa length not in range 1..5 - " + aaa.size());
        this.aaa = aaa;
        if (bbb == null)
            throw new IllegalArgumentException("Must not be null - bbb");
        this.bbb = bbb;
    }

    public List<Aaa> getAaa() {
        return aaa;
    }

    public Set<String> getBbb() {
        return bbb;
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
        return bbb.equals(typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        return hash ^ bbb.hashCode();
    }

    public static class Builder {

        private List<Aaa> aaa;
        private Set<String> bbb;

        public Builder withAaa(List<Aaa> aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(Set<String> bbb) {
            this.bbb = bbb;
            return this;
        }

        public TestArray build() {
            return new TestArray(
                    aaa,
                    bbb
            );
        }

    }

    public static class Aaa {

        private final UUID id;
        private final String name;

        public Aaa(
                UUID id,
                String name
        ) {
            if (id == null)
                throw new IllegalArgumentException("Must not be null - id");
            this.id = id;
            if (name == null)
                throw new IllegalArgumentException("Must not be null - name");
            if (!cg_regex0.matcher(name).find())
                throw new IllegalArgumentException("name does not match pattern " + cg_regex0 + " - " + name);
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
            if (!(other instanceof Aaa))
                return false;
            Aaa typedOther = (Aaa)other;
            if (!id.equals(typedOther.id))
                return false;
            return name.equals(typedOther.name);
        }

        @Override
        public int hashCode() {
            int hash = id.hashCode();
            return hash ^ name.hashCode();
        }

    }

}
"""

    }

}

/*
 * @(#) CodeGeneratorNestedClassTest.kt
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

class CodeGeneratorNestedClassTest {

    @Test fun `should output deeply nested class`() {
        val input = File("src/test/resources/test-nested-object.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestNestedObject", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestNestedObject.kt") + expectedNested) { stringWriter.toString() }
    }

    @Test fun `should output deeply nested class in Java`() {
        val input = File("src/test/resources/test-nested-object.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestNestedObject", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestNestedObject.java") + expectedNestedJava) { stringWriter.toString() }
    }

    companion object {

        const val expectedNested =
"""package com.example

/**
 * Test nested objects.
 */
data class TestNestedObject(
    /** Test nested object - nested. */
    val nested: Nested
) {

    /**
     * Test nested object - nested.
     */
    data class Nested(
        /** Test nested object - deeper. */
        val deeper: Deeper
    )

    /**
     * Test nested object - deeper.
     */
    data class Deeper(
        val deepest: String
    ) {

        init {
            require(deepest.isNotEmpty()) { "deepest length < minimum 1 - ${'$'}{deepest.length}" }
        }

    }

}
"""

        const val expectedNestedJava =
"""package com.example;

/**
 * Test nested objects.
 */
public class TestNestedObject {

    private final Nested nested;

    public TestNestedObject(
            Nested nested
    ) {
        if (nested == null)
            throw new IllegalArgumentException("Must not be null - nested");
        this.nested = nested;
    }

    /**
     * Test nested object - nested.
     */
    public Nested getNested() {
        return nested;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestNestedObject))
            return false;
        TestNestedObject typedOther = (TestNestedObject)other;
        return nested.equals(typedOther.nested);
    }

    @Override
    public int hashCode() {
        return nested.hashCode();
    }

    public static class Builder {

        private Nested nested;

        public Builder withNested(Nested nested) {
            this.nested = nested;
            return this;
        }

        public TestNestedObject build() {
            return new TestNestedObject(
                    nested
            );
        }

    }

    /**
     * Test nested object - nested.
     */
    public static class Nested {

        private final Deeper deeper;

        public Nested(
                Deeper deeper
        ) {
            if (deeper == null)
                throw new IllegalArgumentException("Must not be null - deeper");
            this.deeper = deeper;
        }

        /**
         * Test nested object - deeper.
         */
        public Deeper getDeeper() {
            return deeper;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof Nested))
                return false;
            Nested typedOther = (Nested)other;
            return deeper.equals(typedOther.deeper);
        }

        @Override
        public int hashCode() {
            return deeper.hashCode();
        }

    }

    /**
     * Test nested object - deeper.
     */
    public static class Deeper {

        private final String deepest;

        public Deeper(
                String deepest
        ) {
            if (deepest == null)
                throw new IllegalArgumentException("Must not be null - deepest");
            if (deepest.length() < 1)
                throw new IllegalArgumentException("deepest length < minimum 1 - " + deepest.length());
            this.deepest = deepest;
        }

        public String getDeepest() {
            return deepest;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof Deeper))
                return false;
            Deeper typedOther = (Deeper)other;
            return deepest.equals(typedOther.deepest);
        }

        @Override
        public int hashCode() {
            return deepest.hashCode();
        }

    }

}
"""

    }

}

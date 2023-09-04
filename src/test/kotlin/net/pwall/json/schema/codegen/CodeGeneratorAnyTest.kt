/*
 * @(#) CodeGeneratorAnyTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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

class CodeGeneratorAnyTest {

    @Test fun `should generate property type Any when no details supplied`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestGenerateAny", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate property type Object when no details supplied in Java`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestGenerateAny", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

data class TestGenerateAny(
    /** No details, so should generate type Any */
    val aaa: Any
)
"""

        const val expectedJava =
"""package com.example;

public class TestGenerateAny {

    private final Object aaa;

    public TestGenerateAny(
            Object aaa
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
    }

    /**
     * No details, so should generate type Any
     */
    public Object getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestGenerateAny))
            return false;
        TestGenerateAny cg_typedOther = (TestGenerateAny)cg_other;
        return aaa.equals(cg_typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return aaa.hashCode();
    }

    public static class Builder {

        private Object aaa;

        public Builder withAaa(Object aaa) {
            this.aaa = aaa;
            return this;
        }

        public TestGenerateAny build() {
            return new TestGenerateAny(
                    aaa
            );
        }

    }

}
"""

    }

}

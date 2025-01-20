/*
 * @(#) CodeGeneratorClassNameTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2022, 2023 Peter Wall
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
import java.net.URI

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorClassNameTest {

    @Test fun `should generate class with supplied name`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.addClassNameMapping(URI("urn:test:example"), "Supplied")
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Supplied.kt") + expected
    }

    @Test fun `should generate class with supplied name in Java`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.addClassNameMapping(URI("urn:test:example"), "Supplied")
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Supplied.java") + expectedJava
    }

    @Test fun `should generate class with supplied name using config file`() {
        val input = File("src/test/resources/test-class-name.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("src/test/resources/config/class-name-config.json"))
        val stringWriter = StringWriter()
        codeGenerator.outputResolver = outputCapture(TargetFileName("Supplied", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("Supplied.kt") + expected
    }

    companion object {

        const val expected =
"""package com.example

data class Supplied(
    val id: String
)
"""

        const val expectedJava =
"""package com.example;

public class Supplied {

    private final String id;

    public Supplied(
            String id
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Supplied))
            return false;
        Supplied cg_typedOther = (Supplied)cg_other;
        return id.equals(cg_typedOther.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Builder {

        private String id;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Supplied build() {
            return new Supplied(
                    id
            );
        }

    }

}
"""

    }

}

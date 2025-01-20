/*
 * @(#) CodeGeneratorMarkerInterfaceTest.kt
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

class CodeGeneratorMarkerInterfaceTest {

    @Test fun `should output marker interface`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = ClassName("Marker", "com.example")
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEmpty", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEmpty.kt") + expected
    }

    @Test fun `should output marker interface in Java`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = ClassName("Marker", "com.example")
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEmpty", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEmpty.java") + expectedJava
    }

    @Test fun `should output marker interface in different package`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = ClassName.of("com.example.other.Marker")
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEmpty", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEmpty.kt") + expectedExternal
    }

    @Test fun `should output marker interface in different package in Java`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.markerInterface = ClassName("Marker", "com.example.other")
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEmpty", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEmpty.java") + expectedExternalJava
    }

    @Test fun `should output marker interface when specified in config file`() {
        val input = File("src/test/resources/test-empty-object.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.configure(File("src/test/resources/config/marker-interface-config.json"))
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestEmpty", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        stringWriter.toString() shouldBe createHeader("TestEmpty.kt") + expected
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test empty object.
 */
open class TestEmpty : Marker
"""

        const val expectedJava =
"""package com.example;

/**
 * Test empty object.
 */
public class TestEmpty implements Marker {

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestEmpty))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static class Builder {

        public TestEmpty build() {
            return new TestEmpty(
            );
        }

    }

}
"""

        const val expectedExternal =
"""package com.example

import com.example.other.Marker

/**
 * Test empty object.
 */
open class TestEmpty : Marker
"""

        const val expectedExternalJava =
"""package com.example;

import com.example.other.Marker;

/**
 * Test empty object.
 */
public class TestEmpty implements Marker {

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestEmpty))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static class Builder {

        public TestEmpty build() {
            return new TestEmpty(
            );
        }

    }

}
"""

    }

}

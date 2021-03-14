/*
 * @(#) CodeGeneratorDerivedClassTest.kt
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

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDerivedClassTest {

    @Test fun `should generate base class and derived class`() {
        val input = File("src/test/resources/test-derived-class")
        val codeGenerator = CodeGenerator(templateName = "open_class")
        val stringWriterBase = StringWriter()
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "kt", dirs), stringWriterBase)
        val stringWriterDerived = StringWriter()
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "kt", dirs + "derived"),
                stringWriterDerived)
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(createHeader("TestBaseClass.kt") + expectedBase) { stringWriterBase.toString() }
        expect(createHeader("TestDerivedClass.kt") + expectedDerived) { stringWriterDerived.toString() }
    }

    @Test fun `should generate base class and derived class in Java`() {
        val input = File("src/test/resources/test-derived-class")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        val stringWriterBase = StringWriter()
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "java", dirs), stringWriterBase)
        val stringWriterDerived = StringWriter()
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "java", dirs + "derived"),
                stringWriterDerived)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.generate(input)
        expect(createHeader("TestBaseClass.java") + expectedBaseJava) { stringWriterBase.toString() }
        expect(createHeader("TestDerivedClass.java") + expectedDerivedJava) { stringWriterDerived.toString() }
    }

    companion object {

        const val expectedBase =
"""package com.example

import java.util.UUID

/**
 * Test base class.
 */
open class TestBaseClass(
    val id: UUID
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseClass &&
            id == other.id

    override fun hashCode(): Int = 
            id.hashCode()

}
"""

        const val expectedDerived =
"""package com.example.derived

import java.util.UUID

import com.example.TestBaseClass

/**
 * Test derived class.
 */
open class TestDerivedClass(
    id: UUID,
    val name: String
) : TestBaseClass(id) {

    override fun equals(other: Any?): Boolean = this === other || other is TestDerivedClass && super.equals(other) &&
            name == other.name

    override fun hashCode(): Int = super.hashCode() xor
            name.hashCode()

}
"""

        const val expectedBaseJava =
"""package com.example;

import java.util.UUID;

/**
 * Test base class.
 */
public class TestBaseClass {

    private final UUID id;

    public TestBaseClass(
            UUID id
    ) {
        if (id == null)
            throw new IllegalArgumentException("Must not be null - id");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestBaseClass))
            return false;
        TestBaseClass typedOther = (TestBaseClass)other;
        return id.equals(typedOther.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
"""

        const val expectedDerivedJava =
"""package com.example.derived;

import java.util.UUID;

import com.example.TestBaseClass;

/**
 * Test derived class.
 */
public class TestDerivedClass extends TestBaseClass {

    private final String name;

    public TestDerivedClass(
            UUID id,
            String name
    ) {
        super(id);
        if (name == null)
            throw new IllegalArgumentException("Must not be null - name");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestDerivedClass))
            return false;
        if (!super.equals(other))
            return false;
        TestDerivedClass typedOther = (TestDerivedClass)other;
        return name.equals(typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ name.hashCode();
    }

}
"""

    }

}

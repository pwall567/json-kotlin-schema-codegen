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

import java.io.File

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDerivedClassTest {

    @Test fun `should generate base class and derived class`() {
        val input = File("src/test/resources/test-derived-class")
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "kt", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "kt", dirs + "derived"))
        CodeGenerator(templateName = "open_class").apply {
            outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
            basePackageName = "com.example"
            generate(input)
        }
        outputDetailsBase.output() shouldBe createHeader("TestBaseClass.kt") + expectedBase
        outputDetailsDerived.output() shouldBe createHeader("TestDerivedClass.kt") + expectedDerived
    }

    @Test fun `should generate base class and derived class in Java`() {
        val input = File("src/test/resources/test-derived-class")
        val outputDetailsBase = OutputDetails(TargetFileName("TestBaseClass", "java", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("TestDerivedClass", "java", dirs + "derived"))
        CodeGenerator(TargetLanguage.JAVA).apply {
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
            generate(input)
        }
        outputDetailsBase.output() shouldBe createHeader("TestBaseClass.java") + expectedBaseJava
        outputDetailsDerived.output() shouldBe createHeader("TestDerivedClass.java") + expectedDerivedJava
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

    override fun equals(other: Any?): Boolean = this === other || other is TestDerivedClass &&
            super.equals(other) &&
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
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestBaseClass))
            return false;
        TestBaseClass cg_typedOther = (TestBaseClass)cg_other;
        return id.equals(cg_typedOther.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class Builder {

        private UUID id;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public TestBaseClass build() {
            return new TestBaseClass(
                    id
            );
        }

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
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestDerivedClass))
            return false;
        if (!super.equals(cg_other))
            return false;
        TestDerivedClass cg_typedOther = (TestDerivedClass)cg_other;
        return name.equals(cg_typedOther.name);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ name.hashCode();
    }

    public static class Builder {

        private UUID id;
        private String name;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public TestDerivedClass build() {
            return new TestDerivedClass(
                    id,
                    name
            );
        }

    }

}
"""

    }

}

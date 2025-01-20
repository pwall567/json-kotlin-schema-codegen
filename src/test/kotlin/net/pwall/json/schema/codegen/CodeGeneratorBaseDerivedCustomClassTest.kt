/*
 * @(#) CodeGeneratorBaseDerivedCustomClassTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2023 Peter Wall
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

class CodeGeneratorBaseDerivedCustomClassTest {

    @Test fun `should generate base and derived classes with custom class properties`() {
        val inputA = File("src/test/resources/test-base-custom-class.schema.json")
        val inputB = File("src/test/resources/test-base-derived-custom-class.schema.json")
        val outputDetailsA = OutputDetails(TargetFileName("TestBaseCustomClass", "kt", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerivedCustomClass", "kt", dirs))
        CodeGenerator().apply {
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetailsA, outputDetailsB)
            addCustomClassByFormat("date-time", "java.time.Instant")
            generate(inputA, inputB)
        }
        outputDetailsA.output() shouldBe createHeader("TestBaseCustomClass.kt") + expectedA
        outputDetailsB.output() shouldBe createHeader("TestBaseDerivedCustomClass.kt") + expectedB
    }

    @Test fun `should generate base and derived classes with custom class properties in Java`() {
        val inputA = File("src/test/resources/test-base-custom-class.schema.json")
        val inputB = File("src/test/resources/test-base-derived-custom-class.schema.json")
        val outputDetailsA = OutputDetails(TargetFileName("TestBaseCustomClass", "java", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerivedCustomClass", "java", dirs))
        CodeGenerator(TargetLanguage.JAVA).apply {
            basePackageName = "com.example"
            outputResolver = outputCapture(outputDetailsA, outputDetailsB)
            addCustomClassByFormat("date-time", "java.time.Instant")
            generate(inputA, inputB)
        }
        outputDetailsA.output() shouldBe createHeader("TestBaseCustomClass.java") + expectedAJava
        outputDetailsB.output() shouldBe createHeader("TestBaseDerivedCustomClass.java") + expectedBJava
    }

    companion object {

        const val expectedA =
"""package com.example

import java.time.Instant

/**
 * Test base class with custom type.
 */
open class TestBaseCustomClass(
    val aaa: Instant
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseCustomClass &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

    override fun toString() = "TestBaseCustomClass(aaa=${'$'}aaa)"

    open fun copy(
        aaa: Instant = this.aaa
    ) = TestBaseCustomClass(aaa)

    operator fun component1() = aaa

}
"""

        const val expectedAJava =
"""package com.example;

import java.time.Instant;

/**
 * Test base class with custom type.
 */
public class TestBaseCustomClass {

    private final Instant aaa;

    public TestBaseCustomClass(
            Instant aaa
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
    }

    public Instant getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestBaseCustomClass))
            return false;
        TestBaseCustomClass cg_typedOther = (TestBaseCustomClass)cg_other;
        return aaa.equals(cg_typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return aaa.hashCode();
    }

    public static class Builder {

        private Instant aaa;

        public Builder withAaa(Instant aaa) {
            this.aaa = aaa;
            return this;
        }

        public TestBaseCustomClass build() {
            return new TestBaseCustomClass(
                    aaa
            );
        }

    }

}
"""

        const val expectedB =
"""package com.example

import java.time.Instant

/**
 * Test base derived class with custom type.
 */
class TestBaseDerivedCustomClass(
    aaa: Instant,
    val bbb: String
) : TestBaseCustomClass(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseDerivedCustomClass &&
            super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

    override fun toString() = "TestBaseDerivedCustomClass(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    fun copy(
        aaa: Instant = this.aaa,
        bbb: String = this.bbb
    ) = TestBaseDerivedCustomClass(aaa, bbb)

    operator fun component2() = bbb

}
"""

        const val expectedBJava =
"""package com.example;

import java.time.Instant;

/**
 * Test base derived class with custom type.
 */
public class TestBaseDerivedCustomClass extends TestBaseCustomClass {

    private final String bbb;

    public TestBaseDerivedCustomClass(
            Instant aaa,
            String bbb
    ) {
        super(aaa);
        if (bbb == null)
            throw new IllegalArgumentException("Must not be null - bbb");
        this.bbb = bbb;
    }

    public String getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestBaseDerivedCustomClass))
            return false;
        if (!super.equals(cg_other))
            return false;
        TestBaseDerivedCustomClass cg_typedOther = (TestBaseDerivedCustomClass)cg_other;
        return bbb.equals(cg_typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ bbb.hashCode();
    }

    public static class Builder {

        private Instant aaa;
        private String bbb;

        public Builder withAaa(Instant aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(String bbb) {
            this.bbb = bbb;
            return this;
        }

        public TestBaseDerivedCustomClass build() {
            return new TestBaseDerivedCustomClass(
                    aaa,
                    bbb
            );
        }

    }

}
"""

    }

}

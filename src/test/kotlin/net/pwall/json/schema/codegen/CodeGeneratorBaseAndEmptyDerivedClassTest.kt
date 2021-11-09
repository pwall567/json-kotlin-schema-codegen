/*
 * @(#) CodeGeneratorBaseAndEmptyDerivedClassTest.kt
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
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorBaseAndEmptyDerivedClassTest {

    @Test fun `should generate base and derived classes`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-empty-derived.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseEmptyDerived", "kt", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.kt") + expectedA) { stringWriterA.toString() }
        expect(createHeader("TestBaseEmptyDerived.kt") + expectedB) { stringWriterB.toString() }
    }

    @Test fun `should generate base and derived classes in Java`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-empty-derived.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseEmptyDerived", "java", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.java") + expectedAJava) { stringWriterA.toString() }
        expect(createHeader("TestBaseEmptyDerived.java") + expectedBJava) { stringWriterB.toString() }
    }

    companion object {

        const val expectedA =
"""package com.example

/**
 * Test base class.
 */
open class TestBase(
    val aaa: String
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBase &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

    override fun toString() = "TestBase(aaa=${'$'}aaa)"

    open fun copy(
        aaa: String = this.aaa
    ) = TestBase(aaa)

    operator fun component1() = aaa

}
"""

        const val expectedB =
"""package com.example

/**
 * Test base with empty derived class.
 */
class TestBaseEmptyDerived(
    aaa: String
) : TestBase(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseEmptyDerived &&
            super.equals(other)

    @Suppress("unused")
    override fun hashCode(): Int = super.hashCode()

    override fun toString() = "TestBaseEmptyDerived(aaa=${'$'}aaa)"

    override fun copy(
        aaa: String
    ) = TestBaseEmptyDerived(aaa)

}
"""

        const val expectedAJava =
"""package com.example;

/**
 * Test base class.
 */
public class TestBase {

    private final String aaa;

    public TestBase(
            String aaa
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
    }

    public String getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestBase))
            return false;
        TestBase typedOther = (TestBase)other;
        return aaa.equals(typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return aaa.hashCode();
    }

}
"""

        const val expectedBJava =
"""package com.example;

/**
 * Test base with empty derived class.
 */
public class TestBaseEmptyDerived extends TestBase {

    public TestBaseEmptyDerived(
            String aaa
    ) {
        super(aaa);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestBaseEmptyDerived))
            return false;
        if (!super.equals(other))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
"""

    }

}

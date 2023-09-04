/*
 * @(#) CodeGeneratorBaseAndDerivedClassTest.kt
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

class CodeGeneratorBaseAndDerivedClassTest {

    @Test fun `should generate base and derived classes`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-derived.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerived", "kt", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.kt") + expectedA) { stringWriterA.toString() }
        expect(createHeader("TestBaseDerived.kt") + expectedB) { stringWriterB.toString() }
    }

    @Test fun `should generate base and derived classes in Java`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-derived.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerived", "java", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.java") + expectedAJava) { stringWriterA.toString() }
        expect(createHeader("TestBaseDerived.java") + expectedBJava) { stringWriterB.toString() }
    }

    @Test fun `should generate base and derived classes alternative form`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-derived-2.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerived", "kt", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBaseDerived.kt") + expectedB) { stringWriterB.toString() }
        expect(createHeader("TestBase.kt") + expectedA) { stringWriterA.toString() }
    }

    @Test fun `should generate base and derived classes in Java alternative form`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-derived-2.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerived", "java", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.java") + expectedAJava) { stringWriterA.toString() }
        expect(createHeader("TestBaseDerived.java") + expectedBJava) { stringWriterB.toString() }
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
 * Test base derived class.
 */
class TestBaseDerived(
    aaa: String,
    val bbb: String
) : TestBase(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseDerived &&
            super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

    override fun toString() = "TestBaseDerived(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    fun copy(
        aaa: String = this.aaa,
        bbb: String = this.bbb
    ) = TestBaseDerived(aaa, bbb)

    operator fun component2() = bbb

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
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestBase))
            return false;
        TestBase cg_typedOther = (TestBase)cg_other;
        return aaa.equals(cg_typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return aaa.hashCode();
    }

    public static class Builder {

        private String aaa;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public TestBase build() {
            return new TestBase(
                    aaa
            );
        }

    }

}
"""

        const val expectedBJava =
"""package com.example;

/**
 * Test base derived class.
 */
public class TestBaseDerived extends TestBase {

    private final String bbb;

    public TestBaseDerived(
            String aaa,
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
        if (!(cg_other instanceof TestBaseDerived))
            return false;
        if (!super.equals(cg_other))
            return false;
        TestBaseDerived cg_typedOther = (TestBaseDerived)cg_other;
        return bbb.equals(cg_typedOther.bbb);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ bbb.hashCode();
    }

    public static class Builder {

        private String aaa;
        private String bbb;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(String bbb) {
            this.bbb = bbb;
            return this;
        }

        public TestBaseDerived build() {
            return new TestBaseDerived(
                    aaa,
                    bbb
            );
        }

    }

}
"""

    }

}

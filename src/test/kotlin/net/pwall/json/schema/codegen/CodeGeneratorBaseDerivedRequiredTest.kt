/*
 * @(#) CodeGeneratorBaseDerivedRequiredTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022 Peter Wall
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

import net.pwall.json.JSON
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorBaseDerivedRequiredTest {

    @Test fun `should generate base and derived classes where derived adds required`() {
        val input = File("src/test/resources/test-base-derived-required.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetailsBase = OutputDetails(TargetFileName("Base", "kt", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("Derived", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("Base.kt") + expectedBase) { outputDetailsBase.output() }
        expect(createHeader("Derived.kt") + expectedDerived) { outputDetailsDerived.output() }
    }

    @Test fun `should generate base and derived classes where derived adds required in Java`() {
        val input = File("src/test/resources/test-base-derived-required.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val outputDetailsBase = OutputDetails(TargetFileName("Base", "java", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("Derived", "java", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("Base.java") + expectedBaseJava) { outputDetailsBase.output() }
        expect(createHeader("Derived.java") + expectedDerivedJava) { outputDetailsDerived.output() }
    }

    companion object {

        const val expectedBase =
"""package com.example

open class Base(
    aaa: String? = null
) {

    open val aaa: String? = aaa

    override fun equals(other: Any?): Boolean = this === other || other is Base &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

    override fun toString() = "Base(aaa=${'$'}aaa)"

    open fun copy(
        aaa: String? = this.aaa
    ) = Base(aaa)

    operator fun component1() = aaa

}
"""

        const val expectedDerived =
"""package com.example

class Derived(
    override val aaa: String,
    val bbb: String
) : Base(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is Derived &&
            super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

    override fun toString() = "Derived(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    fun copy(
        aaa: String = this.aaa,
        bbb: String = this.bbb
    ) = Derived(aaa, bbb)

    operator fun component2() = bbb

}
"""

        const val expectedBaseJava =
"""package com.example;

public class Base {

    private final String aaa;

    public Base(
            String aaa
    ) {
        this.aaa = aaa;
    }

    public String getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Base))
            return false;
        Base cg_typedOther = (Base)cg_other;
        return aaa == null ? cg_typedOther.aaa == null : aaa.equals(cg_typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return (aaa != null ? aaa.hashCode() : 0);
    }

    public static class Builder {

        private String aaa;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Base build() {
            return new Base(
                    aaa
            );
        }

    }

}
"""

        const val expectedDerivedJava =
"""package com.example;

public class Derived extends Base {

    private final String bbb;

    public Derived(
            String aaa,
            String bbb
    ) {
        super(validateAaa(aaa));
        if (bbb == null)
            throw new IllegalArgumentException("Must not be null - bbb");
        this.bbb = bbb;
    }

    private static String validateAaa(String aaa) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        return aaa;
    }

    public String getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Derived))
            return false;
        if (!super.equals(cg_other))
            return false;
        Derived cg_typedOther = (Derived)cg_other;
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

        public Derived build() {
            return new Derived(
                    aaa,
                    bbb
            );
        }

    }

}
"""

    }

}

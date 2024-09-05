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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

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
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
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
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("Base.java") + expectedBaseJava) { outputDetailsBase.output() }
        expect(createHeader("Derived.java") + expectedDerivedJava) { outputDetailsDerived.output() }
    }

    companion object {

        const val expectedBase =
"""package com.example

open class Base(
    aaa: String? = null,
    val bbb: Int
) {

    init {
        require(bbb in 0..99) { "bbb not in range 0..99 - ${'$'}bbb" }
    }

    open val aaa: String? = aaa

    override fun equals(other: Any?): Boolean = this === other || other is Base &&
            aaa == other.aaa &&
            bbb == other.bbb

    override fun hashCode(): Int =
            aaa.hashCode() xor
            bbb.hashCode()

    override fun toString() = "Base(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    open fun copy(
        aaa: String? = this.aaa,
        bbb: Int = this.bbb
    ) = Base(aaa, bbb)

    operator fun component1() = aaa

    operator fun component2() = bbb

}
"""

        const val expectedDerived =
"""package com.example

class Derived(
    override val aaa: String,
    bbb: Int,
    val ccc: String
) : Base(aaa, bbb) {

    init {
        require(aaa.length <= 30) { "aaa length > maximum 30 - ${'$'}{aaa.length}" }
    }

    override fun equals(other: Any?): Boolean = this === other || other is Derived &&
            super.equals(other) &&
            ccc == other.ccc

    override fun hashCode(): Int = super.hashCode() xor
            ccc.hashCode()

    override fun toString() = "Derived(aaa=${'$'}aaa, bbb=${'$'}bbb, ccc=${'$'}ccc)"

    fun copy(
        aaa: String = this.aaa,
        bbb: Int = this.bbb,
        ccc: String = this.ccc
    ) = Derived(aaa, bbb, ccc)

    operator fun component3() = ccc

}
"""

        const val expectedBaseJava =
"""package com.example;

public class Base {

    private final String aaa;
    private final int bbb;

    public Base(
            String aaa,
            int bbb
    ) {
        this.aaa = aaa;
        if (bbb < 0 || bbb > 99)
            throw new IllegalArgumentException("bbb not in range 0..99 - " + bbb);
        this.bbb = bbb;
    }

    public String getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof Base))
            return false;
        Base cg_typedOther = (Base)cg_other;
        if (aaa == null ? cg_typedOther.aaa != null : !aaa.equals(cg_typedOther.aaa))
            return false;
        return bbb == cg_typedOther.bbb;
    }

    @Override
    public int hashCode() {
        int hash = (aaa != null ? aaa.hashCode() : 0);
        return hash ^ bbb;
    }

    public static class Builder {

        private String aaa;
        private int bbb;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(int bbb) {
            this.bbb = bbb;
            return this;
        }

        public Base build() {
            return new Base(
                    aaa,
                    bbb
            );
        }

    }

}
"""

        const val expectedDerivedJava =
"""package com.example;

public class Derived extends Base {

    private final String ccc;

    public Derived(
            String aaa,
            int bbb,
            String ccc
    ) {
        super(cg_valid_aaa(aaa), bbb);
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        this.ccc = ccc;
    }

    private static String cg_valid_aaa(String aaa) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (aaa.length() > 30)
            throw new IllegalArgumentException("aaa length > maximum 30 - " + aaa.length());
        return aaa;
    }

    public String getCcc() {
        return ccc;
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
        return ccc.equals(cg_typedOther.ccc);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return hash ^ ccc.hashCode();
    }

    public static class Builder {

        private String aaa;
        private int bbb;
        private String ccc;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(int bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(String ccc) {
            this.ccc = ccc;
            return this;
        }

        public Derived build() {
            return new Derived(
                    aaa,
                    bbb,
                    ccc
            );
        }

    }

}
"""

    }

}

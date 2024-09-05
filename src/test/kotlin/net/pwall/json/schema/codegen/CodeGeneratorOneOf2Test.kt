/*
 * @(#) CodeGeneratorOneOf2Test.kt
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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorOneOf2Test {

    @Test fun `should generate classes for complex multiple oneOf schemata`() {
        val input = File("src/test/resources/test-oneof-2.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs), stringWriterB)
        val stringWriterC = StringWriter()
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "kt", dirs), stringWriterC)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.kt") + expectedTypeA) { stringWriterA.toString() }
        expect(createHeader("TypeB.kt") + expectedTypeB) { stringWriterB.toString() }
        expect(createHeader("TypeC.kt") + expectedTypeC) { stringWriterC.toString() }
    }

    @Test fun `should generate classes for complex multiple oneOf schemata in Java`() {
        val input = File("src/test/resources/test-oneof-2.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "java", dirs), stringWriterB)
        val stringWriterC = StringWriter()
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "java", dirs), stringWriterC)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.java") + expectedTypeAJava) { stringWriterA.toString() }
        expect(createHeader("TypeB.java") + expectedTypeBJava) { stringWriterB.toString() }
        expect(createHeader("TypeC.java") + expectedTypeCJava) { stringWriterC.toString() }
    }

    companion object {

        const val expectedTypeA =
"""package com.example

open class TypeA(
    val aaa: Long? = null
) {

    override fun equals(other: Any?): Boolean = this === other || other is TypeA &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

    override fun toString() = "TypeA(aaa=${'$'}aaa)"

    open fun copy(
        aaa: Long? = this.aaa
    ) = TypeA(aaa)

    operator fun component1() = aaa

    class A(
        aaa: Long? = null,
        val xxx: String
    ) : TypeA(aaa) {

        override fun equals(other: Any?): Boolean = this === other || other is A &&
                super.equals(other) &&
                xxx == other.xxx

        override fun hashCode(): Int = super.hashCode() xor
                xxx.hashCode()

        override fun toString() = "A(aaa=${'$'}aaa, xxx=${'$'}xxx)"

        fun copy(
            aaa: Long? = this.aaa,
            xxx: String = this.xxx
        ) = A(aaa, xxx)

        operator fun component2() = xxx

    }

    class B(
        aaa: Long? = null,
        val yyy: String
    ) : TypeA(aaa) {

        override fun equals(other: Any?): Boolean = this === other || other is B &&
                super.equals(other) &&
                yyy == other.yyy

        override fun hashCode(): Int = super.hashCode() xor
                yyy.hashCode()

        override fun toString() = "B(aaa=${'$'}aaa, yyy=${'$'}yyy)"

        fun copy(
            aaa: Long? = this.aaa,
            yyy: String = this.yyy
        ) = B(aaa, yyy)

        operator fun component2() = yyy

    }

    class C(
        aaa: Long? = null,
        val zzz: String? = null
    ) : TypeA(aaa) {

        override fun equals(other: Any?): Boolean = this === other || other is C &&
                super.equals(other) &&
                zzz == other.zzz

        override fun hashCode(): Int = super.hashCode() xor
                zzz.hashCode()

        override fun toString() = "C(aaa=${'$'}aaa, zzz=${'$'}zzz)"

        fun copy(
            aaa: Long? = this.aaa,
            zzz: String? = this.zzz
        ) = C(aaa, zzz)

        operator fun component2() = zzz

    }

    class D(
        aaa: Long? = null,
        val qqq: String? = null
    ) : TypeA(aaa) {

        init {
            if (qqq != null)
                require(qqq.isNotEmpty()) { "qqq length < minimum 1 - ${'$'}{qqq.length}" }
        }

        override fun equals(other: Any?): Boolean = this === other || other is D &&
                super.equals(other) &&
                qqq == other.qqq

        override fun hashCode(): Int = super.hashCode() xor
                qqq.hashCode()

        override fun toString() = "D(aaa=${'$'}aaa, qqq=${'$'}qqq)"

        fun copy(
            aaa: Long? = this.aaa,
            qqq: String? = this.qqq
        ) = D(aaa, qqq)

        operator fun component2() = qqq

    }

}
"""

        const val expectedTypeB =
"""package com.example

data class TypeB(
    val xxx: String
)
"""

        const val expectedTypeC =
"""package com.example

data class TypeC(
    val yyy: String
)
"""

        const val expectedTypeAJava =
"""package com.example;

public class TypeA {

    private final long aaa;

    public TypeA(
            long aaa
    ) {
        this.aaa = aaa;
    }

    public long getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TypeA))
            return false;
        TypeA cg_typedOther = (TypeA)cg_other;
        return aaa == cg_typedOther.aaa;
    }

    @Override
    public int hashCode() {
        return (int)aaa;
    }

    public static class Builder {

        private long aaa;

        public Builder withAaa(long aaa) {
            this.aaa = aaa;
            return this;
        }

        public TypeA build() {
            return new TypeA(
                    aaa
            );
        }

    }

    public static class A extends TypeA {

        private final String xxx;

        public A(
                long aaa,
                String xxx
        ) {
            super(aaa);
            if (xxx == null)
                throw new IllegalArgumentException("Must not be null - xxx");
            this.xxx = xxx;
        }

        public String getXxx() {
            return xxx;
        }

        @Override
        public boolean equals(Object cg_other) {
            if (this == cg_other)
                return true;
            if (!(cg_other instanceof A))
                return false;
            if (!super.equals(cg_other))
                return false;
            A cg_typedOther = (A)cg_other;
            return xxx.equals(cg_typedOther.xxx);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            return hash ^ xxx.hashCode();
        }

    }

    public static class B extends TypeA {

        private final String yyy;

        public B(
                long aaa,
                String yyy
        ) {
            super(aaa);
            if (yyy == null)
                throw new IllegalArgumentException("Must not be null - yyy");
            this.yyy = yyy;
        }

        public String getYyy() {
            return yyy;
        }

        @Override
        public boolean equals(Object cg_other) {
            if (this == cg_other)
                return true;
            if (!(cg_other instanceof B))
                return false;
            if (!super.equals(cg_other))
                return false;
            B cg_typedOther = (B)cg_other;
            return yyy.equals(cg_typedOther.yyy);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            return hash ^ yyy.hashCode();
        }

    }

    public static class C extends TypeA {

        private final String zzz;

        public C(
                long aaa,
                String zzz
        ) {
            super(aaa);
            this.zzz = zzz;
        }

        public String getZzz() {
            return zzz;
        }

        @Override
        public boolean equals(Object cg_other) {
            if (this == cg_other)
                return true;
            if (!(cg_other instanceof C))
                return false;
            if (!super.equals(cg_other))
                return false;
            C cg_typedOther = (C)cg_other;
            return zzz == null ? cg_typedOther.zzz == null : zzz.equals(cg_typedOther.zzz);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            return hash ^ (zzz != null ? zzz.hashCode() : 0);
        }

    }

    public static class D extends TypeA {

        private final String qqq;

        public D(
                long aaa,
                String qqq
        ) {
            super(aaa);
            if (qqq != null && qqq.length() < 1)
                throw new IllegalArgumentException("qqq length < minimum 1 - " + qqq.length());
            this.qqq = qqq;
        }

        public String getQqq() {
            return qqq;
        }

        @Override
        public boolean equals(Object cg_other) {
            if (this == cg_other)
                return true;
            if (!(cg_other instanceof D))
                return false;
            if (!super.equals(cg_other))
                return false;
            D cg_typedOther = (D)cg_other;
            return qqq == null ? cg_typedOther.qqq == null : qqq.equals(cg_typedOther.qqq);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            return hash ^ (qqq != null ? qqq.hashCode() : 0);
        }

    }

}
"""

        const val expectedTypeBJava =
"""package com.example;

public class TypeB {

    private final String xxx;

    public TypeB(
            String xxx
    ) {
        if (xxx == null)
            throw new IllegalArgumentException("Must not be null - xxx");
        this.xxx = xxx;
    }

    public String getXxx() {
        return xxx;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TypeB))
            return false;
        TypeB cg_typedOther = (TypeB)cg_other;
        return xxx.equals(cg_typedOther.xxx);
    }

    @Override
    public int hashCode() {
        return xxx.hashCode();
    }

    public static class Builder {

        private String xxx;

        public Builder withXxx(String xxx) {
            this.xxx = xxx;
            return this;
        }

        public TypeB build() {
            return new TypeB(
                    xxx
            );
        }

    }

}
"""

        const val expectedTypeCJava =
"""package com.example;

public class TypeC {

    private final String yyy;

    public TypeC(
            String yyy
    ) {
        if (yyy == null)
            throw new IllegalArgumentException("Must not be null - yyy");
        this.yyy = yyy;
    }

    public String getYyy() {
        return yyy;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TypeC))
            return false;
        TypeC cg_typedOther = (TypeC)cg_other;
        return yyy.equals(cg_typedOther.yyy);
    }

    @Override
    public int hashCode() {
        return yyy.hashCode();
    }

    public static class Builder {

        private String yyy;

        public Builder withYyy(String yyy) {
            this.yyy = yyy;
            return this;
        }

        public TypeC build() {
            return new TypeC(
                    yyy
            );
        }

    }

}
"""

    }

}

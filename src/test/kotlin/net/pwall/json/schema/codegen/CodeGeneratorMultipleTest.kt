/*
 * @(#) CodeGeneratorMultipleTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2023 Peter Wall
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
import net.pwall.mustache.Template

class CodeGeneratorMultipleTest {

    @Test fun `should generate classes for multiple schemata`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.commentTemplate = Template.parse("Generated from {{\$comment}}")
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.kt", "Generated from Multiple schema") + expectedTypeA) { outputDetailsA.output() }
        expect(createHeader("TypeB.kt", "Generated from Multiple schema") + expectedTypeB) { outputDetailsB.output() }
    }

    @Test fun `should generate classes for multiple schemata in Java`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "java", dirs))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "java", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.java") + expectedTypeAJava) { outputDetailsA.output() }
        expect(createHeader("TypeB.java") + expectedTypeBJava) { outputDetailsB.output() }
    }

    @Test fun `should generate classes for multiple schemata in TypeScript`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "ts"))
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "ts"))
        val outputDetailsIndex = OutputDetails(TargetFileName("index", "d.ts"))
        codeGenerator.indexFileName = TargetFileName("index", "d.ts")
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsIndex)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.ts") + expectedTypeATypeScript) { outputDetailsA.output() }
        expect(createHeader("TypeB.ts") + expectedTypeBTypeScript) { outputDetailsB.output() }
        expect(createHeader("index.d.ts") + expectedIndexTypeScript) { outputDetailsIndex.output() }
    }

    companion object {

        const val expectedTypeA =
"""package com.example

data class TypeA(
    val aaa: String,
    val bbb: Long,
    val ccc: TypeB,
    val ddd: Ddd
) {

    enum class Ddd {
        AAAA,
        BBBB,
        CCCC
    }

}
"""

        const val expectedTypeB =
"""package com.example

data class TypeB(
    val xxx: String,
    val yyy: Boolean
)
"""

        const val expectedTypeAJava =
"""package com.example;

public class TypeA {

    private final String aaa;
    private final long bbb;
    private final TypeB ccc;
    private final Ddd ddd;

    public TypeA(
            String aaa,
            long bbb,
            TypeB ccc,
            Ddd ddd
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        this.ccc = ccc;
        if (ddd == null)
            throw new IllegalArgumentException("Must not be null - ddd");
        this.ddd = ddd;
    }

    public String getAaa() {
        return aaa;
    }

    public long getBbb() {
        return bbb;
    }

    public TypeB getCcc() {
        return ccc;
    }

    public Ddd getDdd() {
        return ddd;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TypeA))
            return false;
        TypeA typedOther = (TypeA)other;
        if (!aaa.equals(typedOther.aaa))
            return false;
        if (bbb != typedOther.bbb)
            return false;
        if (!ccc.equals(typedOther.ccc))
            return false;
        return ddd == typedOther.ddd;
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= (int)bbb;
        hash ^= ccc.hashCode();
        return hash ^ ddd.hashCode();
    }

    public static class Builder {

        private String aaa;
        private long bbb;
        private TypeB ccc;
        private Ddd ddd;

        public Builder withAaa(String aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(long bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(TypeB ccc) {
            this.ccc = ccc;
            return this;
        }

        public Builder withDdd(Ddd ddd) {
            this.ddd = ddd;
            return this;
        }

        public TypeA build() {
            return new TypeA(
                    aaa,
                    bbb,
                    ccc,
                    ddd
            );
        }

    }

    public enum Ddd {
        AAAA,
        BBBB,
        CCCC
    }

}
"""

        const val expectedTypeBJava =
"""package com.example;

public class TypeB {

    private final String xxx;
    private final boolean yyy;

    public TypeB(
            String xxx,
            boolean yyy
    ) {
        if (xxx == null)
            throw new IllegalArgumentException("Must not be null - xxx");
        this.xxx = xxx;
        this.yyy = yyy;
    }

    public String getXxx() {
        return xxx;
    }

    public boolean getYyy() {
        return yyy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TypeB))
            return false;
        TypeB typedOther = (TypeB)other;
        if (!xxx.equals(typedOther.xxx))
            return false;
        return yyy == typedOther.yyy;
    }

    @Override
    public int hashCode() {
        int hash = xxx.hashCode();
        return hash ^ (yyy ? 1 : 0);
    }

    public static class Builder {

        private String xxx;
        private boolean yyy;

        public Builder withXxx(String xxx) {
            this.xxx = xxx;
            return this;
        }

        public Builder withYyy(boolean yyy) {
            this.yyy = yyy;
            return this;
        }

        public TypeB build() {
            return new TypeB(
                    xxx,
                    yyy
            );
        }

    }

}
"""

        const val expectedTypeATypeScript =
"""
import { TypeB } from "./TypeB";

export interface TypeA {
    aaa: string;
    bbb: number;
    ccc: TypeB;
    ddd: Ddd;
}

export type Ddd =
    "AAAA" |
    "BBBB" |
    "CCCC";
"""

        const val expectedTypeBTypeScript =
"""
export interface TypeB {
    xxx: string;
    yyy: boolean;
}
"""

        const val expectedIndexTypeScript =
"""
import { TypeA, Ddd } from "./TypeA";
import { TypeB } from "./TypeB";

export { TypeA };
export { Ddd };
export { TypeB };
"""
    }

}

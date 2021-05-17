/*
 * @(#) CodeGeneratorMultipleTest.kt
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

import net.pwall.json.JSON
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorMultipleTest {

    @Test fun `should generate classes for multiple schemata`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.kt") + expectedTypeA) { stringWriterA.toString() }
        expect(createHeader("TypeB.kt") + expectedTypeB) { stringWriterB.toString() }
    }

    @Test fun `should generate classes for multiple schemata in Java`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "java", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.java") + expectedTypeAJava) { stringWriterA.toString() }
        expect(createHeader("TypeB.java") + expectedTypeBJava) { stringWriterB.toString() }
    }

    @Test fun `should generate classes for multiple schemata in TypeScript`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.TYPESCRIPT)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "ts"), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "ts"), stringWriterB)
        val stringWriterIndex = StringWriter()
        val outputDetailsIndex = OutputDetails(TargetFileName("index", "d.ts"), stringWriterIndex)
        codeGenerator.indexFileName = TargetFileName("index", "d.ts")
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsIndex)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.ts") + expectedTypeATypeScript) { stringWriterA.toString() }
        expect(createHeader("TypeB.ts") + expectedTypeBTypeScript) { stringWriterB.toString() }
        expect(createHeader("index.d.ts") + expectedIndexTypeScript) { stringWriterIndex.toString() }
    }

    companion object {

        const val expectedTypeA =
"""package com.example

data class TypeA(
    val aaa: String,
    val bbb: Long,
    val ccc: TypeB
)
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

    public TypeA(
            String aaa,
            long bbb,
            TypeB ccc
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        this.ccc = ccc;
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
        return ccc.equals(typedOther.ccc);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= (int)bbb;
        return hash ^ ccc.hashCode();
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

}
"""

        const val expectedTypeATypeScript =
"""
import { TypeB } from "./TypeB";

export interface TypeA {
    aaa: string;
    bbb: number;
    ccc: TypeB;
}
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
import { TypeA } from "./TypeA";
import { TypeB } from "./TypeB";

export { TypeA };
export { TypeB };
"""
    }

}

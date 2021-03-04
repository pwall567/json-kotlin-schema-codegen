package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter
import net.pwall.json.JSON
import net.pwall.json.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorMultipleTest {

    @Test fun `should generate classes for multiple schemata`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails("dummy1", emptyList(), "TypeA", "kt", stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails("dummy1", emptyList(), "TypeB", "kt", stringWriterB)
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA") + expectedTypeA) { stringWriterA.toString() }
        expect(createHeader("TypeB") + expectedTypeB) { stringWriterB.toString() }
    }

    @Test fun `should generate classes for multiple schemata in Java`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails("dummy1", emptyList(), "TypeA", "java", stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails("dummy1", emptyList(), "TypeB", "java", stringWriterB)
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA") + expectedTypeAJava) { stringWriterA.toString() }
        expect(createHeader("TypeB") + expectedTypeBJava) { stringWriterB.toString() }
    }

    @Test fun `should generate classes for multiple schemata in TypeScript`() {
        val input = File("src/test/resources/test-multiple-schema.json")
        val codeGenerator = CodeGenerator(templates = "typescript", suffix = "ts")
        codeGenerator.baseDirectoryName = "dummy1"
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails("dummy1", emptyList(), "TypeA", "ts", stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails("dummy1", emptyList(), "TypeB", "ts", stringWriterB)
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA") + expectedTypeATypeScript) { stringWriterA.toString() }
        expect(createHeader("TypeB") + expectedTypeBTypeScript) { stringWriterB.toString() }
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

    }

}

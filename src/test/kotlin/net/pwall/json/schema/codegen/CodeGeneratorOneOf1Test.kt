/*
 * @(#) CodeGeneratorOneOf1Test.kt
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

class CodeGeneratorOneOf1Test {

    @Test fun `should generate classes for multiple oneOf schemata`() {
        val input = File("src/test/resources/test-oneof-1.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "kt", dirs), stringWriterB)
        val stringWriterC = StringWriter()
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "kt", dirs), stringWriterC)
        val stringWriterD = StringWriter()
        val outputDetailsD = OutputDetails(TargetFileName("TypeD", "kt", dirs), stringWriterD)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC, outputDetailsD)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.kt") + expectedTypeA) { stringWriterA.toString() }
        expect(createHeader("TypeB.kt") + expectedTypeB) { stringWriterB.toString() }
        expect(createHeader("TypeC.kt") + expectedTypeC) { stringWriterC.toString() }
        expect(createHeader("TypeD.kt") + expectedTypeD) { stringWriterD.toString() }
    }

    @Test fun `should generate classes for multiple oneOf schemata in Java`() {
        val input = File("src/test/resources/test-oneof-1.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TypeA", "java", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TypeB", "java", dirs), stringWriterB)
        val stringWriterC = StringWriter()
        val outputDetailsC = OutputDetails(TargetFileName("TypeC", "java", dirs), stringWriterC)
        val stringWriterD = StringWriter()
        val outputDetailsD = OutputDetails(TargetFileName("TypeD", "java", dirs), stringWriterD)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC, outputDetailsD)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.java") + expectedTypeAJava) { stringWriterA.toString() }
        expect(createHeader("TypeB.java") + expectedTypeBJava) { stringWriterB.toString() }
        expect(createHeader("TypeC.java") + expectedTypeCJava) { stringWriterC.toString() }
        expect(createHeader("TypeD.java") + expectedTypeDJava) { stringWriterD.toString() }
    }

    companion object {

        const val expectedTypeA =
"""package com.example

interface TypeA
"""

        const val expectedTypeB =
"""package com.example

data class TypeB(
    val xxx: String
) : TypeA
"""

        const val expectedTypeC =
"""package com.example

data class TypeC(
    val yyy: String
) : TypeA
"""

        const val expectedTypeD =
"""package com.example

data class TypeD(
    val qqq: TypeA
)
"""

        const val expectedTypeAJava =
"""package com.example;

public interface TypeA {

}
"""

        const val expectedTypeBJava =
"""package com.example;

public class TypeB implements TypeA {

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

public class TypeC implements TypeA {

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

        const val expectedTypeDJava =
"""package com.example;

public class TypeD {

    private final TypeA qqq;

    public TypeD(
            TypeA qqq
    ) {
        if (qqq == null)
            throw new IllegalArgumentException("Must not be null - qqq");
        this.qqq = qqq;
    }

    public TypeA getQqq() {
        return qqq;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TypeD))
            return false;
        TypeD cg_typedOther = (TypeD)cg_other;
        return qqq.equals(cg_typedOther.qqq);
    }

    @Override
    public int hashCode() {
        return qqq.hashCode();
    }

    public static class Builder {

        private TypeA qqq;

        public Builder withQqq(TypeA qqq) {
            this.qqq = qqq;
            return this;
        }

        public TypeD build() {
            return new TypeD(
                    qqq
            );
        }

    }

}
"""

    }

}

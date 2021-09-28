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
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.kt") + expectedTypeA) { stringWriterA.toString() }
        expect(createHeader("TypeB.kt") + expectedTypeB) { stringWriterB.toString() }
        expect(createHeader("TypeC.kt") + expectedTypeC) { stringWriterC.toString() }
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
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB, outputDetailsC)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("TypeA.java") + expectedTypeAJava) { stringWriterA.toString() }
        expect(createHeader("TypeB.java") + expectedTypeBJava) { stringWriterB.toString() }
        expect(createHeader("TypeC.java") + expectedTypeCJava) { stringWriterC.toString() }
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
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TypeB))
            return false;
        TypeB typedOther = (TypeB)other;
        return xxx.equals(typedOther.xxx);
    }

    @Override
    public int hashCode() {
        return xxx.hashCode();
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
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TypeC))
            return false;
        TypeC typedOther = (TypeC)other;
        return yyy.equals(typedOther.yyy);
    }

    @Override
    public int hashCode() {
        return yyy.hashCode();
    }

}
"""

    }

}

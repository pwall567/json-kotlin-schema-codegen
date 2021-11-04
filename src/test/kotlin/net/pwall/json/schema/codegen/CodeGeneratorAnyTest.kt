package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorAnyTest {

    @Test fun `should generate property type Any when no details supplied`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestGenerateAny", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate property type Object when no details supplied in Java`() {
        val input = File("src/test/resources/test-generate-any.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestGenerateAny", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestGenerateAny.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

data class TestGenerateAny(
    /** No details, so should generate type Any */
    val aaa: Any
)
"""

        const val expectedJava =
"""package com.example;

public class TestGenerateAny {

    private final Object aaa;

    public TestGenerateAny(
            Object aaa
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        this.aaa = aaa;
    }

    /**
     * No details, so should generate type Any
     */
    public Object getAaa() {
        return aaa;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestGenerateAny))
            return false;
        TestGenerateAny typedOther = (TestGenerateAny)other;
        return aaa.equals(typedOther.aaa);
    }

    @Override
    public int hashCode() {
        return aaa.hashCode();
    }

}
"""

    }

}

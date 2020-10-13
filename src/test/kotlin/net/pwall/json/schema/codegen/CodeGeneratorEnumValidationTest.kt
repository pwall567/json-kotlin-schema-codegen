package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

class CodeGeneratorEnumValidationTest {

    @Test fun `should generate correct code for string enum validations`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestEnumValidation", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for string enum validations in Java`() {
        val input = File("src/test/resources/test-enum-validation.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java")
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestEnumValidation", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example


data class TestEnumValidation(
        val aaa: String,
        val bbb: Int
) {

    init {
        require(aaa in cg_array0) { "aaa not in enumerated values - ${'$'}aaa" }
        require(bbb in cg_array1) { "bbb not in enumerated values - ${'$'}bbb" }
    }

    companion object {
        private val cg_array0 = setOf(
                "ABCDE",
                "FGHIJ",
                "!@#*%"
        )
        private val cg_array1 = setOf(123, 456, 789)
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.util.Arrays;
import java.util.List;

public class TestEnumValidation {

    private static final List<String> cg_array0 = Arrays.asList(
            "ABCDE",
            "FGHIJ",
            "!@#*%"
    );
    private static final List<Integer> cg_array1 = Arrays.asList(123, 456, 789);

    private final String aaa;
    private final int bbb;

    public TestEnumValidation(
            String aaa,
            int bbb
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (!cg_array0.contains(aaa))
            throw new IllegalArgumentException("aaa not in enumerated values - " + aaa);
        this.aaa = aaa;
        if (!cg_array1.contains(bbb))
            throw new IllegalArgumentException("bbb not in enumerated values - " + bbb);
        this.bbb = bbb;
    }

    public String getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestEnumValidation))
            return false;
        TestEnumValidation typedOther = (TestEnumValidation)other;
        if (!aaa.equals(typedOther.aaa))
            return false;
        return bbb == typedOther.bbb;
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= bbb;
        return hash;
    }

}
"""

    }

}

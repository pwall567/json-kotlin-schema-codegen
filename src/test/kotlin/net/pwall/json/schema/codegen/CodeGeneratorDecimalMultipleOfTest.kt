package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDecimalMultipleOfTest {

    @Test fun `should generate correct code for decimals with multipleOf constraints`() {
        val input = File("src/test/resources/test-decimal-multiple-of.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDecimalMultipleOf", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestDecimalMultipleOf.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for decimals with multipleOf constraints in Java`() {
        val input = File("src/test/resources/test-decimal-multiple-of.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestDecimalMultipleOf", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestDecimalMultipleOf.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
            """package com.example

import java.math.BigDecimal

/**
 * Test decimal multipleOf.
 */
data class TestDecimalMultipleOf(
    val aaa: BigDecimal,
    val bbb: BigDecimal,
    val ccc: BigDecimal
) {

    init {
        require(aaa.rem(cg_dec0).compareTo(BigDecimal.ZERO) == 0) { "aaa not a multiple of 0.01 - ${'$'}aaa" }
        require(bbb.rem(cg_dec1).compareTo(BigDecimal.ZERO) == 0) { "bbb not a multiple of 0.02 - ${'$'}bbb" }
        require(ccc.rem(cg_dec2).compareTo(BigDecimal.ZERO) == 0) { "ccc not a multiple of 0.03 - ${'$'}ccc" }
    }

    companion object {
        private val cg_dec0 = BigDecimal("0.01")
        private val cg_dec1 = BigDecimal("0.02")
        private val cg_dec2 = BigDecimal("0.03")
    }

}
"""

        const val expectedJava =
            """package com.example;

import java.math.BigDecimal;

/**
 * Test decimal multipleOf.
 */
public class TestDecimalMultipleOf {

    private static final BigDecimal cg_dec0 = new BigDecimal("0.01");
    private static final BigDecimal cg_dec1 = new BigDecimal("0.02");
    private static final BigDecimal cg_dec2 = new BigDecimal("0.03");

    private final BigDecimal aaa;
    private final BigDecimal bbb;
    private final BigDecimal ccc;

    public TestDecimalMultipleOf(
            BigDecimal aaa,
            BigDecimal bbb,
            BigDecimal ccc
    ) {
        if (aaa == null)
            throw new IllegalArgumentException("Must not be null - aaa");
        if (aaa.remainder(cg_dec0).compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalArgumentException("aaa not a multiple of 0.01 - " + aaa);
        this.aaa = aaa;
        if (bbb == null)
            throw new IllegalArgumentException("Must not be null - bbb");
        if (bbb.remainder(cg_dec1).compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalArgumentException("bbb not a multiple of 0.02 - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        if (ccc.remainder(cg_dec2).compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalArgumentException("ccc not a multiple of 0.03 - " + ccc);
        this.ccc = ccc;
    }

    public BigDecimal getAaa() {
        return aaa;
    }

    public BigDecimal getBbb() {
        return bbb;
    }

    public BigDecimal getCcc() {
        return ccc;
    }

    @Override
    public boolean equals(Object cg_other) {
        if (this == cg_other)
            return true;
        if (!(cg_other instanceof TestDecimalMultipleOf))
            return false;
        TestDecimalMultipleOf cg_typedOther = (TestDecimalMultipleOf)cg_other;
        if (!aaa.equals(cg_typedOther.aaa))
            return false;
        if (!bbb.equals(cg_typedOther.bbb))
            return false;
        return ccc.equals(cg_typedOther.ccc);
    }

    @Override
    public int hashCode() {
        int hash = aaa.hashCode();
        hash ^= bbb.hashCode();
        return hash ^ ccc.hashCode();
    }

    public static class Builder {

        private BigDecimal aaa;
        private BigDecimal bbb;
        private BigDecimal ccc;

        public Builder withAaa(BigDecimal aaa) {
            this.aaa = aaa;
            return this;
        }

        public Builder withBbb(BigDecimal bbb) {
            this.bbb = bbb;
            return this;
        }

        public Builder withCcc(BigDecimal ccc) {
            this.ccc = ccc;
            return this;
        }

        public TestDecimalMultipleOf build() {
            return new TestDecimalMultipleOf(
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

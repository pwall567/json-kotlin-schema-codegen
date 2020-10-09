package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect

import java.io.File
import java.io.StringWriter

import net.pwall.log.LoggerFactory

class CodeGeneratorConstantTest {

    @Test fun `should generate correct code for constants`() {
        val input = File("src/test/resources/test-const.schema.json")
        val codeGenerator = CodeGenerator(loggerFactory = LoggerFactory.getDefault())
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestConst", "kt", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for constants in Java`() {
        val input = File("src/test/resources/test-const.schema.json")
        val codeGenerator = CodeGenerator(templates = "java", suffix = "java",
                loggerFactory = LoggerFactory.getDefault())
        codeGenerator.baseDirectoryName = "dummy"
        val stringWriter = StringWriter()
        codeGenerator.outputResolver =
                CodeGeneratorTestUtil.outputCapture("dummy", emptyList(), "TestConst", "java", stringWriter)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(input)
        expect(expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

import java.math.BigDecimal

data class TestConst(
        val aaa: Int,
        val bbb: Long,
        val ccc: String,
        val ddd: BigDecimal
) {

    init {
        require(aaa == 8) { "aaa not constant value 8 - ${'$'}aaa" }
        require(bbb == 123456789123456789L) { "bbb not constant value 123456789123456789 - ${'$'}bbb" }
        require(ccc == cg_str0) { "ccc not constant value ${'$'}cg_str0 - ${'$'}ccc" }
        require(ddd == cg_dec1) { "ddd not constant value 1.5 - ${'$'}ddd" }
    }

    companion object {
        private const val cg_str0 = "Hello!"
        private val cg_dec1 = BigDecimal("1.5")
    }

}
"""

        const val expectedJava =
"""package com.example;

import java.math.BigDecimal;

public class TestConst {

    private static final String cg_str0 = "Hello!";
    private static final BigDecimal cg_dec1 = new BigDecimal("1.5");

    private final int aaa;
    private final long bbb;
    private final String ccc;
    private final BigDecimal ddd;

    public TestConst(
            int aaa,
            long bbb,
            String ccc,
            BigDecimal ddd
    ) {
        if (aaa != 8)
            throw new IllegalArgumentException("aaa not constant value 8 - " + aaa);
        this.aaa = aaa;
        if (bbb != 123456789123456789L)
            throw new IllegalArgumentException("bbb not constant value 123456789123456789 - " + bbb);
        this.bbb = bbb;
        if (ccc == null)
            throw new IllegalArgumentException("Must not be null - ccc");
        if (!cg_str0.equals(ccc))
            throw new IllegalArgumentException("ccc not constant value " + cg_str0 + " - " + ccc);
        this.ccc = ccc;
        if (ddd == null)
            throw new IllegalArgumentException("Must not be null - ddd");
        if (!ddd.equals(cg_dec1))
            throw new IllegalArgumentException("ddd not constant value 1.5 - " + ddd);
        this.ddd = ddd;
    }

    public int getAaa() {
        return aaa;
    }

    public long getBbb() {
        return bbb;
    }

    public String getCcc() {
        return ccc;
    }

    public BigDecimal getDdd() {
        return ddd;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestConst))
            return false;
        TestConst typedOther = (TestConst)other;
        if (aaa != typedOther.aaa)
            return false;
        if (bbb != typedOther.bbb)
            return false;
        if (!ccc.equals(typedOther.ccc))
            return false;
        return ddd.equals(typedOther.ddd);
    }

    @Override
    public int hashCode() {
        int hash = aaa;
        hash ^= (int)bbb;
        hash ^= ccc.hashCode();
        hash ^= ddd.hashCode();
        return hash;
    }

}
"""

    }

}

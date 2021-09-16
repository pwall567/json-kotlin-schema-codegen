package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorIntegerMinMaxTest {

    @Test fun `should generate correct code for integers with min and max near limits`() {
        val input = File("src/test/resources/test-integer-min-max.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerMinMax", "kt", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestIntegerMinMax.kt") + expected) { stringWriter.toString() }
    }

    @Test fun `should generate correct code for integer formats in Java`() {
        val input = File("src/test/resources/test-integer-min-max.schema.json")
        val codeGenerator = CodeGenerator(TargetLanguage.JAVA)
        val stringWriter = StringWriter()
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(TargetFileName("TestIntegerMinMax", "java", dirs), stringWriter)
        codeGenerator.generate(input)
        expect(createHeader("TestIntegerMinMax.java") + expectedJava) { stringWriter.toString() }
    }

    companion object {

        const val expected =
"""package com.example

/**
 * Test integer minimum and maximum.
 */
data class TestIntegerMinMax(
    val aaa: Int,
    val bbb: Int,
    val ccc: Long,
    val ddd: Long
) {

    init {
        require(aaa >= 0) { "aaa < minimum 0 - ${'$'}aaa" }
        require(ccc >= 0L) { "ccc < minimum 0 - ${'$'}ccc" }
    }

}
"""

        const val expectedJava =
"""package com.example;

/**
 * Test integer minimum and maximum.
 */
public class TestIntegerMinMax {

    private final int aaa;
    private final int bbb;
    private final long ccc;
    private final long ddd;

    public TestIntegerMinMax(
            int aaa,
            int bbb,
            long ccc,
            long ddd
    ) {
        if (aaa < 0)
            throw new IllegalArgumentException("aaa < minimum 0 - " + aaa);
        this.aaa = aaa;
        this.bbb = bbb;
        if (ccc < 0L)
            throw new IllegalArgumentException("ccc < minimum 0 - " + ccc);
        this.ccc = ccc;
        this.ddd = ddd;
    }

    public int getAaa() {
        return aaa;
    }

    public int getBbb() {
        return bbb;
    }

    public long getCcc() {
        return ccc;
    }

    public long getDdd() {
        return ddd;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TestIntegerMinMax))
            return false;
        TestIntegerMinMax typedOther = (TestIntegerMinMax)other;
        if (aaa != typedOther.aaa)
            return false;
        if (bbb != typedOther.bbb)
            return false;
        if (ccc != typedOther.ccc)
            return false;
        return ddd == typedOther.ddd;
    }

    @Override
    public int hashCode() {
        int hash = aaa;
        hash ^= bbb;
        hash ^= (int)ccc;
        return hash ^ (int)ddd;
    }

}
"""

    }

}

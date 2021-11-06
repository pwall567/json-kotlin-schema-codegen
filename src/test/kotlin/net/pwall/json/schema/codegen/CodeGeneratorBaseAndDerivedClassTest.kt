package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import java.io.StringWriter
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorBaseAndDerivedClassTest {

    @Test fun `should generate base and derived classes`() {
        val inputA = File("src/test/resources/test-base.schema.json")
        val inputB = File("src/test/resources/test-base-derived.schema.json")
        val codeGenerator = CodeGenerator()
        val stringWriterA = StringWriter()
        val outputDetailsA = OutputDetails(TargetFileName("TestBase", "kt", dirs), stringWriterA)
        val stringWriterB = StringWriter()
        val outputDetailsB = OutputDetails(TargetFileName("TestBaseDerived", "kt", dirs), stringWriterB)
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsA, outputDetailsB)
        codeGenerator.generate(inputA, inputB)
        expect(createHeader("TestBase.kt") + expectedA) { stringWriterA.toString() }
        expect(createHeader("TestBaseDerived.kt") + expectedB) { stringWriterB.toString() }
    }

    companion object {

        const val expectedA =
"""package com.example

/**
 * Test base class.
 */
open class TestBase(
    val aaa: String
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBase &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

}
"""

        const val expectedB =
"""package com.example

/**
 * Test base derived class.
 */
class TestBaseDerived(
    aaa: String,
    val bbb: String
) : TestBase(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseDerived && super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

}
"""

    }

}

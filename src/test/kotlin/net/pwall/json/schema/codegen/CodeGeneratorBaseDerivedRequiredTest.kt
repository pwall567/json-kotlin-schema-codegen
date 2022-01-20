package net.pwall.json.schema.codegen

import kotlin.test.Test
import kotlin.test.expect
import java.io.File
import net.pwall.json.JSON
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorBaseDerivedRequiredTest {

    @Test fun `should generate base and derived classes where derived adds required`() {
        val input = File("src/test/resources/test-base-derived-required.schema.json")
        val codeGenerator = CodeGenerator()
        val outputDetailsBase = OutputDetails(TargetFileName("Base", "kt", dirs))
        val outputDetailsDerived = OutputDetails(TargetFileName("Derived", "kt", dirs))
        codeGenerator.basePackageName = "com.example"
        codeGenerator.outputResolver = outputCapture(outputDetailsBase, outputDetailsDerived)
        codeGenerator.generateAll(JSON.parse(input), JSONPointer("/\$defs"))
        expect(createHeader("Base.kt") + expectedBase) { outputDetailsBase.output() }
        expect(createHeader("Derived.kt") + expectedDerived) { outputDetailsDerived.output() }
    }

    companion object {

        const val expectedBase =
"""package com.example

open class Base(
    val aaa: String? = null
) {

    override fun equals(other: Any?): Boolean = this === other || other is Base &&
            aaa == other.aaa

    override fun hashCode(): Int =
            aaa.hashCode()

    override fun toString() = "Base(aaa=${'$'}aaa)"

    open fun copy(
        aaa: String? = this.aaa
    ) = Base(aaa)

    operator fun component1() = aaa

}
"""

        const val expectedDerived =
"""package com.example

class Derived(
    aaa: String,
    val bbb: String
) : Base(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is Derived &&
            super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

    override fun toString() = "Derived(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    fun copy(
        aaa: String = this.aaa!!,
        bbb: String = this.bbb
    ) = Derived(aaa, bbb)

    operator fun component2() = bbb

}
"""
    }

}

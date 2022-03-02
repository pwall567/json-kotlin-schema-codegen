/*
 * @(#) CodeGeneratorBaseDerivedRequiredTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2022 Peter Wall
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
    aaa: String? = null
) {

    open val aaa: String? = aaa

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
    override val aaa: String,
    val bbb: String
) : Base(aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is Derived &&
            super.equals(other) &&
            bbb == other.bbb

    override fun hashCode(): Int = super.hashCode() xor
            bbb.hashCode()

    override fun toString() = "Derived(aaa=${'$'}aaa, bbb=${'$'}bbb)"

    fun copy(
        aaa: String = this.aaa,
        bbb: String = this.bbb
    ) = Derived(aaa, bbb)

    operator fun component2() = bbb

}
"""
    }

}

/*
 * @(#) CodeGeneratorDerivedClassWithArrayTest.kt
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

import io.kjson.JSON
import io.kjson.pointer.JSONPointer

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorDerivedClassWithArrayTest {

    @Test fun `should generate base and derived classes correctly`() {
        val input = File("src/test/resources/test-derived-class-with-array.schema.json")
        val codeGenerator = CodeGenerator()
        codeGenerator.basePackageName = "com.example"
        val outputDetails1 = OutputDetails(TargetFileName("ItemType", "kt", dirs))
        val outputDetails2 = OutputDetails(TargetFileName("Base", "kt", dirs))
        val outputDetails3 = OutputDetails(TargetFileName("Derived", "kt", dirs))
        codeGenerator.outputResolver = outputCapture(outputDetails1, outputDetails2, outputDetails3)
        codeGenerator.generateAll(JSON.parseNonNull(input.readText()), JSONPointer("/\$defs"))
        expect(createHeader("ItemType.kt") + expected1) { outputDetails1.output() }
        expect(createHeader("Base.kt") + expected2) { outputDetails2.output() }
        expect(createHeader("Derived.kt") + expected3) { outputDetails3.output() }
    }

    companion object {

        const val expected1 =
"""package com.example

data class ItemType(
    val abc: String? = null
)
"""

        const val expected2 =
"""package com.example

open class Base(
    val array: List<ItemType>? = null
) {

    override fun equals(other: Any?): Boolean = this === other || other is Base &&
            array == other.array

    override fun hashCode(): Int =
            array.hashCode()

    override fun toString() = "Base(array=${'$'}array)"

    open fun copy(
        array: List<ItemType>? = this.array
    ) = Base(array)

    operator fun component1() = array

}
"""

        const val expected3 =
"""package com.example

class Derived(
    array: List<ItemType>? = null,
    val extra: Boolean? = null
) : Base(array) {

    override fun equals(other: Any?): Boolean = this === other || other is Derived &&
            super.equals(other) &&
            extra == other.extra

    override fun hashCode(): Int = super.hashCode() xor
            extra.hashCode()

    override fun toString() = "Derived(array=${'$'}array, extra=${'$'}extra)"

    fun copy(
        array: List<ItemType>? = this.array,
        extra: Boolean? = this.extra
    ) = Derived(array, extra)

    operator fun component2() = extra

}
"""

    }

}

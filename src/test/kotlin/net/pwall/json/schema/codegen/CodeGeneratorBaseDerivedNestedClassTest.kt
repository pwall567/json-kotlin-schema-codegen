/*
 * @(#) CodeGeneratorBaseDerivedNestedClassTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2023 Peter Wall
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

import java.io.File

import io.kstuff.test.shouldBe

import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.OutputDetails
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.createHeader
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.dirs
import net.pwall.json.schema.codegen.CodeGeneratorTestUtil.outputCapture

class CodeGeneratorBaseDerivedNestedClassTest {

    @Test fun `should output base and derived classes with nested class`() {
        val input = File("src/test/resources/test-derived-class-extra")
        CodeGenerator().apply {
            basePackageName = dirs.joinToString(".")
            val outputA = OutputDetails(TargetFileName("TestBaseNested", "kt", dirs))
            val outputB = OutputDetails(TargetFileName("TestBaseNestedDerived", "kt", dirs + "derived"))
            val outputC = OutputDetails(TargetFileName("TestBaseNestedExtra", "kt", dirs))
            outputResolver = outputCapture(outputA, outputB, outputC)
            generate(input)
            outputA.output() shouldBe createHeader("TestBaseNested.kt") + expectedA
            outputB.output() shouldBe createHeader("TestBaseNestedDerived.kt") + expectedB
        }
    }

    companion object {

        const val expectedA =
"""package com.example

/**
 * Test base class with nested class.
 */
open class TestBaseNested(
    val xxx: List<TestBaseNestedExtra>,
    val aaa: List<Aaa>
) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseNested &&
            xxx == other.xxx &&
            aaa == other.aaa

    override fun hashCode(): Int =
            xxx.hashCode() xor
            aaa.hashCode()

    override fun toString() = "TestBaseNested(xxx=${'$'}xxx, aaa=${'$'}aaa)"

    open fun copy(
        xxx: List<TestBaseNestedExtra> = this.xxx,
        aaa: List<Aaa> = this.aaa
    ) = TestBaseNested(xxx, aaa)

    operator fun component1() = xxx

    operator fun component2() = aaa

    data class Aaa(
        val alpha: String? = null
    )

}
"""

        const val expectedB =
"""package com.example.derived

import com.example.TestBaseNested
import com.example.TestBaseNestedExtra

/**
 * Test base with nested class and empty derived class.
 */
class TestBaseNestedDerived(
    xxx: List<TestBaseNestedExtra>,
    aaa: List<Aaa>
) : TestBaseNested(xxx, aaa) {

    override fun equals(other: Any?): Boolean = this === other || other is TestBaseNestedDerived &&
            super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    override fun toString() = "TestBaseNestedDerived(xxx=${'$'}xxx, aaa=${'$'}aaa)"

    override fun copy(
        xxx: List<TestBaseNestedExtra>,
        aaa: List<Aaa>
    ) = TestBaseNestedDerived(xxx, aaa)

}
"""

    }

}

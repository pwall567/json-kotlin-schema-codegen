/*
 * @(#) TargetFileNameTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021 Peter Wall
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

import io.kstuff.test.shouldBe

class TargetFileNameTest {

    @Test fun `should create TargetFileName with just name`() {
        val targetFileName = TargetFileName("Testing")
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe null
        targetFileName.dirs shouldBe emptyList()
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "Testing"
        targetFileName.extendedName shouldBe "Testing"
        targetFileName.toString() shouldBe "Testing"
    }

    @Test fun `should create TargetFileName with name and extension`() {
        val targetFileName = TargetFileName("Testing", "kt")
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe "kt"
        targetFileName.dirs shouldBe emptyList()
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "Testing"
        targetFileName.extendedName shouldBe "Testing.kt"
        targetFileName.toString() shouldBe "Testing.kt"
    }

    @Test fun `should create TargetFileName with name and single directory`() {
        val targetFileName = TargetFileName("Testing", dirs = listOf("test"))
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe null
        targetFileName.dirs shouldBe listOf("test")
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "test.Testing"
        targetFileName.extendedName shouldBe "Testing"
        targetFileName.toString() shouldBe "test/Testing"
    }

    @Test fun `should create TargetFileName with name and extension and single directory`() {
        val targetFileName = TargetFileName("Testing", "kt", dirs = listOf("test"))
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe "kt"
        targetFileName.dirs shouldBe listOf("test")
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "test.Testing"
        targetFileName.extendedName shouldBe "Testing.kt"
        targetFileName.toString() shouldBe "test/Testing.kt"
    }

    @Test fun `should create TargetFileName with name and multiple directories`() {
        val targetFileName = TargetFileName("Testing", dirs = listOf("net", "pwall", "test"))
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe null
        targetFileName.dirs shouldBe listOf("net", "pwall", "test")
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "net.pwall.test.Testing"
        targetFileName.extendedName shouldBe "Testing"
        targetFileName.toString() shouldBe "net/pwall/test/Testing"
    }

    @Test fun `should create TargetFileName with name and extension and multiple directories`() {
        val targetFileName = TargetFileName("Testing", "kt", dirs = listOf("net", "pwall", "test"))
        targetFileName.name shouldBe "Testing"
        targetFileName.ext shouldBe "kt"
        targetFileName.dirs shouldBe listOf("net", "pwall", "test")
        targetFileName.className shouldBe "Testing"
        targetFileName.qualifiedClassName shouldBe "net.pwall.test.Testing"
        targetFileName.extendedName shouldBe "Testing.kt"
        targetFileName.toString() shouldBe "net/pwall/test/Testing.kt"
    }

}

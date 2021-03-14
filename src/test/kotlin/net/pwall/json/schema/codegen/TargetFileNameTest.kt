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
import kotlin.test.expect

class TargetFileNameTest {

    @Test fun `should create TargetFileName with just name`() {
        val targetFileName = TargetFileName("Testing")
        expect("Testing") { targetFileName.name }
        expect(null) { targetFileName.ext }
        expect(emptyList()) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("Testing") { targetFileName.qualifiedClassName }
        expect("Testing") { targetFileName.toString() }
    }

    @Test fun `should create TargetFileName with name and extension`() {
        val targetFileName = TargetFileName("Testing", "kt")
        expect("Testing") { targetFileName.name }
        expect("kt") { targetFileName.ext }
        expect(emptyList()) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("Testing") { targetFileName.qualifiedClassName }
        expect("Testing.kt") { targetFileName.toString() }
    }

    @Test fun `should create TargetFileName with name and single directory`() {
        val targetFileName = TargetFileName("Testing", dirs = listOf("test"))
        expect("Testing") { targetFileName.name }
        expect(null) { targetFileName.ext }
        expect(listOf("test")) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("test.Testing") { targetFileName.qualifiedClassName }
        expect("test/Testing") { targetFileName.toString() }
    }

    @Test fun `should create TargetFileName with name and extension and single directory`() {
        val targetFileName = TargetFileName("Testing", "kt", dirs = listOf("test"))
        expect("Testing") { targetFileName.name }
        expect("kt") { targetFileName.ext }
        expect(listOf("test")) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("test.Testing") { targetFileName.qualifiedClassName }
        expect("test/Testing.kt") { targetFileName.toString() }
    }

    @Test fun `should create TargetFileName with name and multiple directories`() {
        val targetFileName = TargetFileName("Testing", dirs = listOf("net", "pwall", "test"))
        expect("Testing") { targetFileName.name }
        expect(null) { targetFileName.ext }
        expect(listOf("net", "pwall", "test")) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("net.pwall.test.Testing") { targetFileName.qualifiedClassName }
        expect("net/pwall/test/Testing") { targetFileName.toString() }
    }

    @Test fun `should create TargetFileName with name and extension and multiple directories`() {
        val targetFileName = TargetFileName("Testing", "kt", dirs = listOf("net", "pwall", "test"))
        expect("Testing") { targetFileName.name }
        expect("kt") { targetFileName.ext }
        expect(listOf("net", "pwall", "test")) { targetFileName.dirs }
        expect("Testing") { targetFileName.className }
        expect("net.pwall.test.Testing") { targetFileName.qualifiedClassName }
        expect("net/pwall/test/Testing.kt") { targetFileName.toString() }
    }

}

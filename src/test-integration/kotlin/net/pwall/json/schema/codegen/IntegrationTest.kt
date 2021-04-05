/*
 * @(#) IntegrationTest.kt
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
import kotlin.test.assertFailsWith
import kotlin.test.expect

import java.util.UUID

import net.pwall.json.schema.codegen.test.TestArrayItems
import net.pwall.json.schema.test.person.Person

class IntegrationTest {

    @Test fun `should compile simple generated code`() {
        val uuid = UUID.fromString("c9cfc320-9591-11eb-920e-a78a48655abd")
        val name = "Test Name"
        val person = Person(uuid, name)
        expect(uuid) { person.id }
        expect(name) { person.name }
    }

    @Test fun `should compile complex code with array validations`() {
        val testAAA = listOf(123, 456)
        val testBBB = listOf("AUD", "NZD")
        with (TestArrayItems(testAAA, testBBB, listOf(testBBB))) {
            expect(testAAA) { aaa }
            expect(testBBB) { bbb }
            expect(1) { ccc?.size }
            expect(testBBB) { ccc?.first() }
        }
        with (TestArrayItems(testAAA, testBBB)) {
            expect(testAAA) { aaa }
            expect(testBBB) { bbb }
            expect(null) { ccc }
        }
        with (TestArrayItems(testAAA)) {
            expect(testAAA) { aaa }
            expect(null) { bbb }
            expect(null) { ccc }
        }
    }

    @Test fun `should compile complex code with array validations - error cases`() {
        val testAAA = listOf(123, 456)
        val testBBB = listOf("AUD", "NZD")
        val testAAAError1 = listOf(123, 123456)
        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAAError1, testBBB, listOf(testBBB)) }.let {
            expect("aaa item not in range 0..9999 - 123456") { it.message }
        }

        val testAAAError2 = listOf(123, 456, 789, 987, 765, 432)
        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAAError2, testBBB, listOf(testBBB)) }.let {
            expect("aaa length not in range 1..5 - 6") { it.message }
        }

        val testBBBError1 = listOf("ABCD", "USD")
        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAA, testBBBError1, listOf(testBBB)) }.let {
            expect("bbb item does not match pattern ^[A-Z]{3}\$ - ABCD") { it.message }
        }

        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAA, testBBB, listOf(testBBBError1)) }.let {
            expect("ccc item item length > maximum 3 - 4") { it.message }
        }

        val testBBBError2 = listOf("GBP", "USD", "EUR")
        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAA, testBBB, listOf(testBBBError2)) }.let {
            expect("ccc item length not constant value 2 - 3") { it.message }
        }

        assertFailsWith<IllegalArgumentException> { TestArrayItems(testAAA, testBBB, emptyList()) }.let {
            expect("ccc length < minimum 1 - 0") { it.message }
        }
    }

}

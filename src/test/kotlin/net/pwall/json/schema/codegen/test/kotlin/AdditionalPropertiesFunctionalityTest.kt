/*
 * @(#) AdditionalPropertiesFunctionalityTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024 Peter Wall
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

package net.pwall.json.schema.codegen.test.kotlin

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.expect

import java.time.LocalDate

class AdditionalPropertiesFunctionalityTest {

    @Test fun `should generate functional TestAdditionalPropertiesFalse`() {
        val testClass = TestAdditionalPropertiesFalse()
        expect("TestAdditionalPropertiesFalse()") { testClass.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesTrue`() {
        val testClass1 = TestAdditionalPropertiesTrue(emptyMap())
        assertNull(testClass1["field1"])
        expect("TestAdditionalPropertiesTrue()") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesTrue(mapOf(
            "field1" to "TEST STRING",
        ))
        expect("TEST STRING") { testClass2["field1"] }
        assertNull(testClass2["field2"])
        expect("TestAdditionalPropertiesTrue(field1=TEST STRING)") { testClass2.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchema1`() {
        val testClass = TestAdditionalPropertiesSchema1(mapOf(
            "date1" to LocalDate.parse("2024-01-28"),
            "date2" to LocalDate.parse("2024-01-29"),
        ))
        expect(LocalDate.parse("2024-01-28")) { testClass["date1"] }
        assertNull(testClass["date99"])
        expect("TestAdditionalPropertiesSchema1(date1=2024-01-28, date2=2024-01-29)") { testClass.toString() }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaValid`() {
        val testClass = TestAdditionalPropertiesSchemaValid(mapOf(
            "field1" to "ABC",
            "field2" to "xxx",
        ))
        expect("ABC") { testClass["field1"] }
        assertNull(testClass["field99"])
        expect("TestAdditionalPropertiesSchemaValid(field1=ABC, field2=xxx)") { testClass.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaValid(mapOf(
                "field1" to "ABC",
                "field2" to "",
            ))
        }.let {
            expect("field2 length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesFalseExtra`() {
        val testClass = TestAdditionalPropertiesFalseExtra("content")
        expect("content") { testClass.extra }
        expect("TestAdditionalPropertiesFalseExtra(extra=content)") { testClass.toString() }
    }

    @Test fun `should generate functional TestApFalseExtraValid`() {
        val testClass = TestApFalseExtraValid("content")
        expect("content") { testClass.extra }
        expect("TestApFalseExtraValid(extra=content)") { testClass.toString() }

        assertFailsWith<IllegalArgumentException> { TestApFalseExtraValid("") }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtra`() {
        val testClass1 = TestApTrueExtra(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestApTrueExtra(extra=content)") { testClass1.toString() }

        val testClass2 = TestApTrueExtra(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestApTrueExtra(extra=content, anything=another)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtra(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtra(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraOptional`() {
        val testClass1 = TestApTrueExtraOptional(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestApTrueExtraOptional(extra=content)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraOptional(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestApTrueExtraOptional(extra=content, anything=another)") { testClass2.toString() }

        val testClass3 = TestApTrueExtraOptional(mapOf(
            "extra" to null,
        ))
        assertNull(testClass3.extra)
        assertNull(testClass3["whatever"])
        expect("TestApTrueExtraOptional(extra=null)") { testClass3.toString() }

        val testClass4 = TestApTrueExtraOptional(emptyMap())
        assertNull(testClass4.extra)
        assertNull(testClass4["whatever"])
        expect("TestApTrueExtraOptional()") { testClass4.toString() }

        val testClass5 = TestApTrueExtraOptional(mapOf(
            "anything" to "another",
        ))
        assertNull(testClass5.extra)
        expect("another") { testClass5["anything"] }
        assertNull(testClass5["whatever"])
        expect("TestApTrueExtraOptional(anything=another)") { testClass5.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraOptional(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String?") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraValid`() {
        val testClass1 = TestApTrueExtraValid(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestApTrueExtraValid(extra=content)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraValid(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestApTrueExtraValid(extra=content, anything=another)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraValid(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraValid(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraValid(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraOptValid`() {
        val testClass1 = TestApTrueExtraOptValid(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestApTrueExtraOptValid(extra=content)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraOptValid(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestApTrueExtraOptValid(extra=content, anything=another)") { testClass2.toString() }

        val testClass3 = TestApTrueExtraOptValid(mapOf(
            "extra" to null,
        ))
        assertNull(testClass3.extra)
        assertNull(testClass3["whatever"])
        expect("TestApTrueExtraOptValid(extra=null)") { testClass3.toString() }

        val testClass4 = TestApTrueExtraOptValid(emptyMap())
        assertNull(testClass4.extra)
        assertNull(testClass4["whatever"])
        expect("TestApTrueExtraOptValid()") { testClass4.toString() }

        val testClass5 = TestApTrueExtraOptValid(mapOf(
            "anything" to "another",
        ))
        assertNull(testClass5.extra)
        expect("another") { testClass5["anything"] }
        assertNull(testClass5["whatever"])
        expect("TestApTrueExtraOptValid(anything=another)") { testClass5.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraOptValid(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String?") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraOptValid(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraDefault`() {
        val testClass1 = TestApTrueExtraDefault(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestApTrueExtraDefault(extra=content)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraDefault(mapOf(
            "extra" to "content",
            "anything" to "another",
        ))
        expect("content") { testClass2.extra }
        expect("another") { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestApTrueExtraDefault(extra=content, anything=another)") { testClass2.toString() }

        val testClass3 = TestApTrueExtraDefault(mapOf(
            "anything" to "another",
        ))
        expect("default-value") { testClass3.extra }
        expect("another") { testClass3["anything"] }
        assertNull(testClass3["whatever"])
        expect("TestApTrueExtraDefault(anything=another)") { testClass3.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraDefault(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraDefault(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraArray`() {
        with(TestApTrueExtraArray(mapOf(
            "extra" to listOf("alpha", "beta"),
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArray(extra=[alpha, beta])") { toString() }
        }

        with(TestApTrueExtraArray(mapOf(
            "extra" to listOf("alpha", "beta"),
            "anything" to "another",
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            expect("another") { this["anything"] }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArray(extra=[alpha, beta], anything=another)") { toString() }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArray(emptyMap())
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArray(mapOf(
                "extra" to "wrong",
            ))
        }.let {
            expect("extra is not the correct type, expecting List<String>") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArray(mapOf(
                "extra" to listOf(123, 456),
            ))
        }.let {
            expect("extra item is not the correct type, expecting String") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraArrayOpt`() {
        with(TestApTrueExtraArrayOpt(mapOf(
            "extra" to listOf("alpha", "beta"),
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOpt(extra=[alpha, beta])") { toString() }
        }

        with(TestApTrueExtraArrayOpt(mapOf(
            "extra" to listOf("alpha", "beta"),
            "anything" to "another",
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            expect("another") { this["anything"] }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOpt(extra=[alpha, beta], anything=another)") { toString() }
        }

        with(TestApTrueExtraArrayOpt(emptyMap())) {
            assertNull(this["extra"])
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOpt()") { toString() }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArrayOpt(mapOf(
                "extra" to "wrong",
            ))
        }.let {
            expect("extra is not the correct type, expecting List<String>?") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArrayOpt(mapOf(
                "extra" to listOf(123, 456),
            ))
        }.let {
            expect("extra item is not the correct type, expecting String") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraArrayOptValid`() {
        with(TestApTrueExtraArrayOptValid(mapOf(
            "extra" to listOf("alpha", "beta"),
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOptValid(extra=[alpha, beta])") { toString() }
        }

        with(TestApTrueExtraArrayOptValid(mapOf(
            "extra" to listOf("alpha", "beta"),
            "anything" to "another",
        ))) {
            with(extra) {
                assertIs<List<String>>(this)
                expect(2) { size }
                expect("alpha") { this[0] }
                expect("beta") { this[1] }
            }
            expect("another") { this["anything"] }
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOptValid(extra=[alpha, beta], anything=another)") { toString() }
        }

        with(TestApTrueExtraArrayOptValid(emptyMap())) {
            assertNull(extra)
            assertNull(this["whatever"])
            expect("TestApTrueExtraArrayOptValid()") { toString() }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArrayOptValid(mapOf(
                "extra" to "wrong",
            ))
        }.let {
            expect("extra is not the correct type, expecting List<String>?") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArrayOptValid(mapOf(
                "extra" to listOf(123, 456),
            ))
        }.let {
            expect("extra item is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraArrayOptValid(mapOf(
                "extra" to listOf("alpha", ""),
            ))
        }.let {
            expect("extra item length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra`() {
        val testClass1 = TestAdditionalPropertiesSchemaExtra(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass1.extra }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra(extra=content)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra(mapOf(
            "extra" to "content",
            "anything" to LocalDate.parse("2024-01-28"),
        ))
        expect("content") { testClass2.extra }
        expect(LocalDate.parse("2024-01-28")) { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra(extra=content, anything=2024-01-28)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra(mapOf(
                "extra" to "OK",
                "anything" to "another",
            ))
        }.let {
            expect("anything is not the correct type, expecting LocalDate") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra2`() { // TODO - next (deserialization)
        val testClass1 = TestAdditionalPropertiesSchemaExtra2(mapOf(
            "extra" to "content",
            "extra2" to 12,
        ))
        expect("content") { testClass1.extra }
        expect(12) { testClass1.extra2 }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra2(mapOf(
            "extra" to "content",
            "extra2" to 12,
            "anything" to 999,
        ))
        expect("content") { testClass2.extra }
        expect(12) { testClass2.extra2 }
        expect(999) { testClass2["anything"] }
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12, anything=999)") {
            testClass2.toString()
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra2") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 999,
            ))
        }.let {
            expect("extra2 not in range 0..99 - 999") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to "wrong",
            ))
        }.let {
            expect("extra3 is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra2(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to 99999,
            ))
        }.let {
            expect("extra3 not in range 0..9999 - 99999") { it.message }
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra3`() {
        val testClass1 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
            "extra2" to 12,
        ))
        expect("content") { testClass1.extra }
        expect(12) { testClass1.extra2 }
        assertNull(testClass1["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12)") { testClass1.toString() }

        val testClass2 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
        ))
        expect("content") { testClass2.extra }
        assertNull(testClass2.extra2)
        assertNull(testClass2["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content)") { testClass2.toString() }

        val testClass3 = TestAdditionalPropertiesSchemaExtra3(mapOf(
            "extra" to "content",
            "extra2" to 12,
            "anything" to 999,
        ))
        expect("content") { testClass3.extra }
        expect(12) { testClass3.extra2 }
        expect(999) { testClass3["anything"] }
        assertNull(testClass3["whatever"])
        expect("TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12, anything=999)") {
            testClass3.toString()
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "anything" to "another",
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to 123,
            ))
        }.let {
            expect("extra is not the correct type, expecting String") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 999,
            ))
        }.let {
            expect("extra2 not in range 0..99 - 999") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to "wrong",
            ))
        }.let {
            expect("extra3 is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to 99999,
            ))
        }.let {
            expect("extra3 not in range 0..9999 - 99999") { it.message }
        }
    }

    @Test fun `should generate functional TestApFalsePattern`() {
        val testClass = TestApFalsePattern(mapOf(
            "ABC" to 22,
            "CAT" to 55,
        ))
        expect(22) { testClass["ABC"] }
        expect(55) { testClass["CAT"] }
        expect("TestApFalsePattern(ABC=22, CAT=55)") { testClass.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePattern(mapOf(
                "XYZ" to -1,
            ))
        }.let {
            expect("XYZ not in range 0..99 - -1") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePattern(mapOf(
                "XYZ" to "wrong",
            ))
        }.let {
            expect("XYZ is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePattern(mapOf(
                "WRONG" to 33,
            ))
        }.let {
            expect("Unexpected field WRONG") { it.message }
        }
    }

    @Test fun `should generate functional TestApFalsePatternExtra`() {
        val testClass = TestApFalsePatternExtra(mapOf(
            "extra" to "yes",
            "ABC" to 22,
            "CAT" to 55,
        ))
        expect("yes") { testClass.extra }
        expect(22) { testClass["ABC"] }
        expect(55) { testClass["CAT"] }
        expect("TestApFalsePatternExtra(extra=yes, ABC=22, CAT=55)") { testClass.toString() }
        val pp = testClass.entries.filter { Regex("^[A-Z]{3}\$").matches(it.key) }
        expect(2) { pp.size }
        with(pp[0]) {
            expect("ABC") { key }
            expect(22) { value }
        }
        with(pp[1]) {
            expect("CAT") { key }
            expect(55) { value }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "ABC" to 22,
                "CAT" to 55,
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "XYZ" to -1,
            ))
        }.let {
            expect("XYZ not in range 0..99 - -1") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "XYZ" to "wrong",
            ))
        }.let {
            expect("XYZ is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "WRONG" to 33,
            ))
        }.let {
            expect("Unexpected field WRONG") { it.message }
        }
    }

    @Test fun `should generate functional TestApFalsePatternExtraOpt`() {
        val testClass1 = TestApFalsePatternExtraOpt(mapOf(
            "extra" to "yes",
            "ABC" to 22,
            "CAT" to 55,
        ))
        expect("yes") { testClass1.extra }
        expect(22) { testClass1["ABC"] }
        expect(55) { testClass1["CAT"] }
        expect("TestApFalsePatternExtraOpt(extra=yes, ABC=22, CAT=55)") { testClass1.toString() }

        val testClass2 = TestApFalsePatternExtraOpt(mapOf(
            "ABC" to 22,
            "CAT" to 55,
        ))
        assertNull(testClass2.extra)
        expect(22) { testClass2["ABC"] }
        expect(55) { testClass2["CAT"] }
        expect("TestApFalsePatternExtraOpt(ABC=22, CAT=55)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "",
            ))
        }.let {
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "XYZ" to -1,
            ))
        }.let {
            expect("XYZ not in range 0..99 - -1") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "XYZ" to "wrong",
            ))
        }.let {
            expect("XYZ is not the correct type, expecting Int") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternExtra(mapOf(
                "extra" to "yes",
                "WRONG" to 33,
            ))
        }.let {
            expect("Unexpected field WRONG") { it.message }
        }
    }

    @Test fun `should generate functional TestApFalsePatternObject`() {
        val testClass = TestApFalsePatternObject(mapOf(
            "ABC" to TestApFalsePatternObject.PatternProperty(),
            "PIG" to TestApFalsePatternObject.PatternProperty(),
        ))
        expect(TestApFalsePatternObject.PatternProperty()) { testClass["ABC"] }
        expect(TestApFalsePatternObject.PatternProperty()) { testClass["PIG"] }
        expect("TestApFalsePatternObject(ABC=PatternProperty(), PIG=PatternProperty())") { testClass.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternObject(mapOf(
                "DOG" to "Wrong type",
            ))
        }.let {
            expect("DOG is not the correct type, expecting PatternProperty") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApFalsePatternObject(mapOf(
                "WOLF" to TestApFalsePatternObject.PatternProperty(),
            ))
        }.let {
            expect("Unexpected field WOLF") { it.message }
        }
    }

    @Test fun `should generate functional TestApTruePattern`() {
        val testClass1 = TestApTruePattern(mapOf(
            "ABC" to "first",
            "CAT" to "second",
        ))
        expect("first") { testClass1["ABC"] }
        expect("second") { testClass1["CAT"] }
        expect("TestApTruePattern(ABC=first, CAT=second)") { testClass1.toString() }

        val testClass2 = TestApTruePattern(mapOf(
            "ABC" to "first",
            "CAT" to "second",
            "extra" to 12345,
        ))
        expect("first") { testClass2["ABC"] }
        expect("second") { testClass2["CAT"] }
        expect(12345) { testClass2["extra"] }
        expect("TestApTruePattern(ABC=first, CAT=second, extra=12345)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTruePattern(mapOf(
                "XYZ" to 54321,
            ))
        }.let {
            expect("XYZ is not the correct type, expecting String") { it.message }
        }
    }

    @Test fun `should generate functional TestApTruePatternValid`() {
        val testClass1 = TestApTruePatternValid(mapOf(
            "ABC" to 22,
            "CAT" to 55,
        ))
        expect(22) { testClass1["ABC"] }
        expect(55) { testClass1["CAT"] }
        expect("TestApTruePatternValid(ABC=22, CAT=55)") { testClass1.toString() }

        val testClass2 = TestApTruePatternValid(mapOf(
            "ABC" to 22,
            "CAT" to 55,
            "extra" to "anything",
        ))
        expect(22) { testClass2["ABC"] }
        expect(55) { testClass2["CAT"] }
        expect("anything") { testClass2["extra"] }
        expect("TestApTruePatternValid(ABC=22, CAT=55, extra=anything)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTruePatternValid(mapOf(
                "XYZ" to -1,
            ))
        }.let {
            expect("XYZ not in range 0..99 - -1") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTruePatternValid(mapOf(
                "XYZ" to "wrong",
            ))
        }.let {
            expect("XYZ is not the correct type, expecting Int") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueExtraPattern`() {
        val testClass1 = TestApTrueExtraPattern(mapOf(
            "extra" to "anything",
        ))
        expect("anything") { testClass1["extra"] }
        expect("TestApTrueExtraPattern(extra=anything)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraPattern(mapOf(
            "ABC" to 123456,
            "CAT" to 987654321,
            "extra" to "anything",
        ))
        expect(123456) { testClass2["ABC"] }
        expect(987654321) { testClass2["CAT"] }
        expect("anything") { testClass2["extra"] }
        expect("TestApTrueExtraPattern(ABC=123456, CAT=987654321, extra=anything)") { testClass2.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueExtraPattern(mapOf(
                "XYZ" to 54321,
            ))
        }.let {
            expect("required property missing - extra") { it.message }
        }
    }

    @Test fun `should generate functional TestApNestedFalse`() {
        val testClass = TestApNestedFalse(TestApNestedFalse.Nested())
        expect("TestApNestedFalse(nested=Nested())") { testClass.toString() }
    }

    @Test fun `should generate functional TestApNestedTrue`() {
        val testClass1 = TestApNestedTrue(TestApNestedTrue.Nested(emptyMap()))
        expect("TestApNestedTrue(nested=Nested())") { testClass1.toString() }

        val testClass2 = TestApNestedTrue(TestApNestedTrue.Nested(mapOf(
            "testField" to "Test string",
        )))
        expect("Test string") { testClass2.nested["testField"] }
    }

    @Test fun `should generate functional TestApTrueExtraNested`() {
        val testClass1 = TestApTrueExtraNested(mapOf(
            "extra" to TestApTrueExtraNested.Extra(field1 = "ABC", field2 = true),
            "codes" to TestApTrueExtraNested.Codes.ALPHA,
        ))
        expect("ABC") { testClass1.extra.field1 }
        assertTrue(testClass1.extra.field2)
        expect(TestApTrueExtraNested.Codes.ALPHA) { testClass1.codes }
        assertNull(testClass1.empty)
        val name = "TestApTrueExtraNested"
        expect("$name(extra=Extra(field1=ABC, field2=true), codes=ALPHA)") { testClass1.toString() }

        val testClass2 = TestApTrueExtraNested(mapOf(
            "extra" to TestApTrueExtraNested.Extra(field1 = "ABC", field2 = true),
            "codes" to TestApTrueExtraNested.Codes.ALPHA,
            "empty" to TestApTrueExtraNested.Empty(mapOf(
                "abc" to 123,
                "def" to 456,
            )),
        ))
        expect("ABC") { testClass2.extra.field1 }
        assertTrue(testClass2.extra.field2)
        expect(TestApTrueExtraNested.Codes.ALPHA) { testClass2.codes }
        expect(123) { testClass2.empty?.get("abc") }
        expect(456) { testClass2.empty?.get("def") }
        expect("$name(extra=Extra(field1=ABC, field2=true), codes=ALPHA, empty=Empty(abc=123, def=456))") {
            testClass2.toString()
        }
    }

    @Test fun `should generate functional TestApTrueMin`() {
        val testClass1 = TestApTrueMin(mapOf(
            "field1" to "George",
            "field2" to "Henry",
        ))
        expect("George") { testClass1["field1"] }
        expect("Henry") { testClass1["field2"] }
        expect("TestApTrueMin(field1=George, field2=Henry)") { testClass1.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueMin(mapOf(
                "field1" to "George",
            ))
        }.let {
            expect("Number of properties < minimum 2 - 1") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueMin1`() {
        val testClass1 = TestApTrueMin1(mapOf(
            "field1" to "George",
            "field2" to "Henry",
        ))
        expect("George") { testClass1["field1"] }
        expect("Henry") { testClass1["field2"] }
        expect("TestApTrueMin1(field1=George, field2=Henry)") { testClass1.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueMin1(emptyMap())
        }.let {
            expect("Number of properties < minimum 1") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueMax`() {
        val testClass1 = TestApTrueMax(mapOf(
            "field1" to "George",
            "field2" to "Henry",
        ))
        expect("George") { testClass1["field1"] }
        expect("Henry") { testClass1["field2"] }
        expect("TestApTrueMax(field1=George, field2=Henry)") { testClass1.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueMax(mapOf(
                "field1" to "George",
                "field2" to "Henry",
                "field3" to "Arthur",
                "field4" to "Robert",
                "field5" to "Edward",
                "field6" to "Roger",
            ))
        }.let {
            expect("Number of properties > maximum 5 - 6") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueMinMax`() {
        val testClass1 = TestApTrueMinMax(mapOf(
            "field1" to "George",
            "field2" to "Henry",
        ))
        expect("George") { testClass1["field1"] }
        expect("Henry") { testClass1["field2"] }
        expect("TestApTrueMinMax(field1=George, field2=Henry)") { testClass1.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueMinMax(mapOf(
                "field1" to "George",
            ))
        }.let {
            expect("Number of properties not in range 2..5 - 1") { it.message }
        }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueMinMax(mapOf(
                "field1" to "George",
                "field2" to "Henry",
                "field3" to "Arthur",
                "field4" to "Robert",
                "field5" to "Edward",
                "field6" to "Roger",
            ))
        }.let {
            expect("Number of properties not in range 2..5 - 6") { it.message }
        }
    }

    @Test fun `should generate functional TestApTrueConst`() {
        val testClass1 = TestApTrueConst(mapOf(
            "field1" to "George",
            "field2" to "Henry",
            "field3" to "Arthur",
        ))
        expect("George") { testClass1["field1"] }
        expect("Henry") { testClass1["field2"] }
        expect("TestApTrueConst(field1=George, field2=Henry, field3=Arthur)") { testClass1.toString() }

        assertFailsWith<IllegalArgumentException> {
            TestApTrueConst(mapOf(
                "field1" to "George",
            ))
        }.let {
            expect("Number of properties != constant 3 - 1") { it.message }
        }
    }

}

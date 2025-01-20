/*
 * @(#) AdditionalPropertiesFunctionalityTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2024, 2025 Peter Wall
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

import java.time.LocalDate

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeNonNull
import io.kstuff.test.shouldBeType
import io.kstuff.test.shouldThrow

class AdditionalPropertiesFunctionalityTest {

    @Test fun `should generate functional TestAdditionalPropertiesFalse`() {
        with(TestAdditionalPropertiesFalse()) {
            toString() shouldBe "TestAdditionalPropertiesFalse()"
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesTrue`() {
        with(TestAdditionalPropertiesTrue(emptyMap())) {
            this["field1"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesTrue()"
        }

        with(TestAdditionalPropertiesTrue(mapOf("field1" to "TEST STRING"))) {
            this["field1"] shouldBe "TEST STRING"
            this["field2"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesTrue(field1=TEST STRING)"
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchema1`() {
        with(
            TestAdditionalPropertiesSchema1(
                mapOf(
                    "date1" to LocalDate.parse("2024-01-28"),
                    "date2" to LocalDate.parse("2024-01-29"),
                )
            )
        ) {
            this["date1"] shouldBe LocalDate.parse("2024-01-28")
            this["date99"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchema1(date1=2024-01-28, date2=2024-01-29)"
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaValid`() {
        with(
            TestAdditionalPropertiesSchemaValid(mapOf(
                "field1" to "ABC",
                "field2" to "xxx",
            ))
        ) {
            this["field1"] shouldBe "ABC"
            this["field99"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaValid(field1=ABC, field2=xxx)"
        }

        shouldThrow<IllegalArgumentException>("field2 length < minimum 1 - 0") {
            TestAdditionalPropertiesSchemaValid(
                mapOf(
                    "field1" to "ABC",
                    "field2" to "",
                )
            )
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesFalseExtra`() {
        with(TestAdditionalPropertiesFalseExtra("content")) {
            extra shouldBe "content"
            toString() shouldBe "TestAdditionalPropertiesFalseExtra(extra=content)"
        }
    }

    @Test fun `should generate functional TestApFalseExtraValid`() {
        with(TestApFalseExtraValid("content")) {
            extra shouldBe "content"
            toString() shouldBe "TestApFalseExtraValid(extra=content)"
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApFalseExtraValid("")
        }
    }

    @Test fun `should generate functional TestApTrueExtra`() {
        with(TestApTrueExtra(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtra(extra=content)"
        }

        with(
            TestApTrueExtra(
                mapOf(
                    "extra" to "content",
                    "anything" to "another",
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtra(extra=content, anything=another)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestApTrueExtra(mapOf("anything" to "another"))
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestApTrueExtra(mapOf("extra" to 123))
        }
    }

    @Test fun `should generate functional TestApTrueExtraOptional`() {
        with(TestApTrueExtraOptional(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptional(extra=content)"
        }

        with(
            TestApTrueExtraOptional(
                mapOf(
                    "extra" to "content",
                    "anything" to "another",
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptional(extra=content, anything=another)"
        }

        with(TestApTrueExtraOptional(mapOf("extra" to null))) {
            extra shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptional(extra=null)"
        }

        with(TestApTrueExtraOptional(emptyMap())) {
            extra shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptional()"
        }

        with(TestApTrueExtraOptional(mapOf("anything" to "another"))) {
            extra shouldBe null
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptional(anything=another)"
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String?") {
            TestApTrueExtraOptional(mapOf("extra" to 123))
        }
    }

    @Test fun `should generate functional TestApTrueExtraValid`() {
        with(TestApTrueExtraValid(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraValid(extra=content)"
        }

        with(
            TestApTrueExtraValid(
                mapOf(
                    "extra" to "content",
                    "anything" to "another",
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraValid(extra=content, anything=another)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestApTrueExtraValid(mapOf("anything" to "another"))
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestApTrueExtraValid(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApTrueExtraValid(mapOf("extra" to ""))
        }
    }

    @Test fun `should generate functional TestApTrueExtraOptValid`() {
        with(TestApTrueExtraOptValid(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptValid(extra=content)"
        }

        with(
            TestApTrueExtraOptValid(
                mapOf(
                    "extra" to "content",
                    "anything" to "another",
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptValid(extra=content, anything=another)"
        }

        with(TestApTrueExtraOptValid(mapOf("extra" to null))) {
            extra shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptValid(extra=null)"
        }

        with(TestApTrueExtraOptValid(emptyMap())) {
            extra shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptValid()"
        }

        with(TestApTrueExtraOptValid(mapOf("anything" to "another"))) {
            extra shouldBe null
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraOptValid(anything=another)"
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String?") {
            TestApTrueExtraOptValid(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApTrueExtraOptValid(mapOf("extra" to ""))
        }
    }

    @Test fun `should generate functional TestApTrueExtraDefault`() {
        with(TestApTrueExtraDefault(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraDefault(extra=content)"
        }

        with(
            TestApTrueExtraDefault(
                mapOf(
                    "extra" to "content",
                    "anything" to "another",
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraDefault(extra=content, anything=another)"
        }

        with(TestApTrueExtraDefault(mapOf("anything" to "another"))) {
            extra shouldBe "default-value"
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraDefault(anything=another)"
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestApTrueExtraDefault(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApTrueExtraDefault(mapOf("extra" to ""))
        }
    }

    @Test fun `should generate functional TestApTrueExtraArray`() {
        with(TestApTrueExtraArray(mapOf("extra" to listOf("alpha", "beta")))) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArray(extra=[alpha, beta])"
        }

        with(
            TestApTrueExtraArray(
                mapOf(
                    "extra" to listOf("alpha", "beta"),
                    "anything" to "another",
                )
            )
        ) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArray(extra=[alpha, beta], anything=another)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestApTrueExtraArray(emptyMap())
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting List<String>") {
            TestApTrueExtraArray(mapOf("extra" to "wrong"))
        }

        shouldThrow<IllegalArgumentException>("extra item is not the correct type, expecting String") {
            TestApTrueExtraArray(mapOf("extra" to listOf(123, 456)))
        }
    }

    @Test fun `should generate functional TestApTrueExtraArrayOpt`() {
        with(TestApTrueExtraArrayOpt(mapOf("extra" to listOf("alpha", "beta")))) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOpt(extra=[alpha, beta])"
        }

        with(
            TestApTrueExtraArrayOpt(
                mapOf(
                    "extra" to listOf("alpha", "beta"),
                    "anything" to "another",
                )
            )
        ) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOpt(extra=[alpha, beta], anything=another)"
        }

        with(TestApTrueExtraArrayOpt(emptyMap())) {
            this["extra"] shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOpt()"
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting List<String>?") {
            TestApTrueExtraArrayOpt(mapOf("extra" to "wrong"))
        }

        shouldThrow<IllegalArgumentException>("extra item is not the correct type, expecting String") {
            TestApTrueExtraArrayOpt(mapOf("extra" to listOf(123, 456)))
        }
    }

    @Test fun `should generate functional TestApTrueExtraArrayOptValid`() {
        with(TestApTrueExtraArrayOptValid(mapOf("extra" to listOf("alpha", "beta")))) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOptValid(extra=[alpha, beta])"
        }

        with(
            TestApTrueExtraArrayOptValid(
                mapOf(
                    "extra" to listOf("alpha", "beta"),
                    "anything" to "another",
                )
            )
        ) {
            with(extra) {
                shouldBeType<List<String>>()
                size shouldBe 2
                this[0] shouldBe "alpha"
                this[1] shouldBe "beta"
            }
            this["anything"] shouldBe "another"
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOptValid(extra=[alpha, beta], anything=another)"
        }

        with(TestApTrueExtraArrayOptValid(emptyMap())) {
            extra shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestApTrueExtraArrayOptValid()"
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting List<String>?") {
            TestApTrueExtraArrayOptValid(mapOf("extra" to "wrong"))
        }

        shouldThrow<IllegalArgumentException>("extra item is not the correct type, expecting String") {
            TestApTrueExtraArrayOptValid(mapOf("extra" to listOf(123, 456)))
        }

        shouldThrow<IllegalArgumentException>("extra item length < minimum 1 - 0") {
            TestApTrueExtraArrayOptValid(mapOf("extra" to listOf("alpha", "")))
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra`() {
        with(TestAdditionalPropertiesSchemaExtra(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra(extra=content)"
        }

        with(
            TestAdditionalPropertiesSchemaExtra(
                mapOf(
                    "extra" to "content",
                    "anything" to LocalDate.parse("2024-01-28"),
                )
            )
        ) {
            extra shouldBe "content"
            this["anything"] shouldBe LocalDate.parse("2024-01-28")
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra(extra=content, anything=2024-01-28)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestAdditionalPropertiesSchemaExtra(mapOf("anything" to "another"))
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestAdditionalPropertiesSchemaExtra(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestAdditionalPropertiesSchemaExtra(mapOf("extra" to ""))
        }

        shouldThrow<IllegalArgumentException>("anything is not the correct type, expecting LocalDate") {
            TestAdditionalPropertiesSchemaExtra(
                mapOf(
                    "extra" to "OK",
                    "anything" to "another",
                )
            )
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra2`() { // TODO - next (deserialization)
        with(
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "content",
                    "extra2" to 12,
                )
            )
        ) {
            extra shouldBe "content"
            extra2 shouldBe 12
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12)"
        }

        with(
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "content",
                    "extra2" to 12,
                    "anything" to 999,
                )
            )
        ) {
            extra shouldBe "content"
            extra2 shouldBe 12
            this["anything"] shouldBe 999
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra2(extra=content, extra2=12, anything=999)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestAdditionalPropertiesSchemaExtra2(mapOf("anything" to "another"))
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestAdditionalPropertiesSchemaExtra2(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestAdditionalPropertiesSchemaExtra2(mapOf("extra" to ""))
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra2") {
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "OK",
                    "anything" to "another",
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra2 not in range 0..99 - 999") {
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "OK",
                    "extra2" to 999,
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra3 is not the correct type, expecting Int") {
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "OK",
                    "extra2" to 18,
                    "extra3" to "wrong",
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra3 not in range 0..9999 - 99999") {
            TestAdditionalPropertiesSchemaExtra2(
                mapOf(
                    "extra" to "OK",
                    "extra2" to 18,
                    "extra3" to 99999,
                )
            )
        }
    }

    @Test fun `should generate functional TestAdditionalPropertiesSchemaExtra3`() {
        with(
            TestAdditionalPropertiesSchemaExtra3(
                mapOf(
                    "extra" to "content",
                    "extra2" to 12,
                )
            )
        ) {
            extra shouldBe "content"
            extra2 shouldBe 12
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12)"
        }

        with(TestAdditionalPropertiesSchemaExtra3(mapOf("extra" to "content"))) {
            extra shouldBe "content"
            extra2 shouldBe null
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra3(extra=content)"
        }

        with(
            TestAdditionalPropertiesSchemaExtra3(
                mapOf(
                    "extra" to "content",
                    "extra2" to 12,
                    "anything" to 999,
                )
            )
        ) {
            extra shouldBe "content"
            extra2 shouldBe 12
            this["anything"] shouldBe 999
            this["whatever"] shouldBe null
            toString() shouldBe "TestAdditionalPropertiesSchemaExtra3(extra=content, extra2=12, anything=999)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestAdditionalPropertiesSchemaExtra3(mapOf("anything" to "another"))
        }

        shouldThrow<IllegalArgumentException>("extra is not the correct type, expecting String") {
            TestAdditionalPropertiesSchemaExtra3(mapOf("extra" to 123))
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestAdditionalPropertiesSchemaExtra3(mapOf("extra" to ""))
        }

        shouldThrow<IllegalArgumentException>("extra2 not in range 0..99 - 999") {
            TestAdditionalPropertiesSchemaExtra3(
                mapOf(
                    "extra" to "OK",
                    "extra2" to 999,
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra3 is not the correct type, expecting Int") {
            TestAdditionalPropertiesSchemaExtra3(
                mapOf(
                    "extra" to "OK",
                    "extra2" to 18,
                    "extra3" to "wrong",
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra3 not in range 0..9999 - 99999") {
            TestAdditionalPropertiesSchemaExtra3(mapOf(
                "extra" to "OK",
                "extra2" to 18,
                "extra3" to 99999,
            ))
        }
    }

    @Test fun `should generate functional TestApFalsePattern`() {
        with(
            TestApFalsePattern(
                mapOf(
                    "ABC" to 22,
                    "CAT" to 55,
                )
            )
        ) {
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            toString() shouldBe "TestApFalsePattern(ABC=22, CAT=55)"
        }

        shouldThrow<IllegalArgumentException>("XYZ not in range 0..99 - -1") {
            TestApFalsePattern(mapOf("XYZ" to -1))
        }

        shouldThrow<IllegalArgumentException>("XYZ is not the correct type, expecting Int") {
            TestApFalsePattern(mapOf("XYZ" to "wrong"))
        }

        shouldThrow<IllegalArgumentException>("Unexpected field WRONG") {
            TestApFalsePattern(mapOf("WRONG" to 33))
        }
    }

    @Test fun `should generate functional TestApFalsePatternExtra`() {
        with(
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "ABC" to 22,
                    "CAT" to 55,
                )
            )
        ) {
            extra shouldBe "yes"
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            toString() shouldBe "TestApFalsePatternExtra(extra=yes, ABC=22, CAT=55)"
            with(entries.filter { Regex("^[A-Z]{3}\$").matches(it.key) }) {
                size shouldBe 2
                with(this[0]) {
                    key shouldBe "ABC"
                    value shouldBe 22
                }
                with(this[1]) {
                    key shouldBe "CAT"
                    value shouldBe 55
                }
            }
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestApFalsePatternExtra(
                mapOf(
                    "ABC" to 22,
                    "CAT" to 55,
                )
            )
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApFalsePatternExtra(mapOf( "extra" to ""))
        }

        shouldThrow<IllegalArgumentException>("XYZ not in range 0..99 - -1") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "XYZ" to -1,
                )
            )
        }

        shouldThrow<IllegalArgumentException>("XYZ is not the correct type, expecting Int") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "XYZ" to "wrong",
                )
            )
        }

        shouldThrow<IllegalArgumentException>("Unexpected field WRONG") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "WRONG" to 33,
                )
            )
        }
    }

    @Test fun `should generate functional TestApFalsePatternExtraOpt`() {
        with(
            TestApFalsePatternExtraOpt(mapOf(
                "extra" to "yes",
                "ABC" to 22,
                "CAT" to 55,
            ))
        ) {
            extra shouldBe "yes"
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            toString() shouldBe "TestApFalsePatternExtraOpt(extra=yes, ABC=22, CAT=55)"
        }

        with(
            TestApFalsePatternExtraOpt(
                mapOf(
                    "ABC" to 22,
                    "CAT" to 55,
                )
            )
        ) {
            extra shouldBe null
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            toString() shouldBe "TestApFalsePatternExtraOpt(ABC=22, CAT=55)"
        }

        shouldThrow<IllegalArgumentException>("extra length < minimum 1 - 0") {
            TestApFalsePatternExtra(mapOf("extra" to ""))
        }

        shouldThrow<IllegalArgumentException>("XYZ not in range 0..99 - -1") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "XYZ" to -1,
                )
            )
        }

        shouldThrow<IllegalArgumentException>("XYZ is not the correct type, expecting Int") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "XYZ" to "wrong",
                )
            )
        }

        shouldThrow<IllegalArgumentException>("Unexpected field WRONG") {
            TestApFalsePatternExtra(
                mapOf(
                    "extra" to "yes",
                    "WRONG" to 33,
                )
            )
        }
    }

    @Test fun `should generate functional TestApFalsePatternObject`() {
        with(
            TestApFalsePatternObject(
                mapOf(
                    "ABC" to TestApFalsePatternObject.PatternProperty(),
                    "PIG" to TestApFalsePatternObject.PatternProperty(),
                )
            )
        ) {
            this["ABC"] shouldBe TestApFalsePatternObject.PatternProperty()
            this["PIG"] shouldBe TestApFalsePatternObject.PatternProperty()
            toString() shouldBe "TestApFalsePatternObject(ABC=PatternProperty(), PIG=PatternProperty())"
        }

        shouldThrow<IllegalArgumentException>("DOG is not the correct type, expecting PatternProperty") {
            TestApFalsePatternObject(mapOf("DOG" to "Wrong type"))
        }

        shouldThrow<IllegalArgumentException>("Unexpected field WOLF") {
            TestApFalsePatternObject(mapOf("WOLF" to TestApFalsePatternObject.PatternProperty()))
        }
    }

    @Test fun `should generate functional TestApTruePattern`() {
        with(
            TestApTruePattern(
                mapOf(
                    "ABC" to "first",
                    "CAT" to "second",
                )
            )
        ) {
            this["ABC"] shouldBe "first"
            this["CAT"] shouldBe "second"
            toString() shouldBe "TestApTruePattern(ABC=first, CAT=second)"
        }

        with(
            TestApTruePattern(
                mapOf(
                    "ABC" to "first",
                    "CAT" to "second",
                    "extra" to 12345,
                )
            )
        ) {
            this["ABC"] shouldBe "first"
            this["CAT"] shouldBe "second"
            this["extra"] shouldBe 12345
            toString() shouldBe "TestApTruePattern(ABC=first, CAT=second, extra=12345)"
        }

        shouldThrow<IllegalArgumentException>("XYZ is not the correct type, expecting String") {
            TestApTruePattern(mapOf("XYZ" to 54321))
        }
    }

    @Test fun `should generate functional TestApTruePatternValid`() {
        with(
            TestApTruePatternValid(
                mapOf(
                    "ABC" to 22,
                    "CAT" to 55,
                )
            )
        ) {
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            toString() shouldBe "TestApTruePatternValid(ABC=22, CAT=55)"
        }

        with(
            TestApTruePatternValid(
                mapOf(
                    "ABC" to 22,
                    "CAT" to 55,
                    "extra" to "anything",
                )
            )
        ) {
            this["ABC"] shouldBe 22
            this["CAT"] shouldBe 55
            this["extra"] shouldBe "anything"
            toString() shouldBe "TestApTruePatternValid(ABC=22, CAT=55, extra=anything)"
        }

        shouldThrow<IllegalArgumentException>("XYZ not in range 0..99 - -1") {
            TestApTruePatternValid(mapOf("XYZ" to -1))
        }

        shouldThrow<IllegalArgumentException>("XYZ is not the correct type, expecting Int") {
            TestApTruePatternValid(mapOf("XYZ" to "wrong"))
        }
    }

    @Test fun `should generate functional TestApTrueExtraPattern`() {
        with(TestApTrueExtraPattern(mapOf("extra" to "anything"))) {
            this["extra"] shouldBe "anything"
            toString() shouldBe "TestApTrueExtraPattern(extra=anything)"
        }

        with(
            TestApTrueExtraPattern(
                    mapOf(
                    "ABC" to 123456,
                    "CAT" to 987654321,
                    "extra" to "anything",
                )
            )
        ) {
            this["ABC"] shouldBe 123456
            this["CAT"] shouldBe 987654321
            this["extra"] shouldBe "anything"
            toString() shouldBe "TestApTrueExtraPattern(ABC=123456, CAT=987654321, extra=anything)"
        }

        shouldThrow<IllegalArgumentException>("required property missing - extra") {
            TestApTrueExtraPattern(mapOf("XYZ" to 54321))
        }
    }

    @Test fun `should generate functional TestApNestedFalse`() {
        with(TestApNestedFalse(TestApNestedFalse.Nested())) {
            toString() shouldBe "TestApNestedFalse(nested=Nested())"
        }
    }

    @Test fun `should generate functional TestApNestedTrue`() {
        with(TestApNestedTrue(TestApNestedTrue.Nested(emptyMap()))) {
            toString() shouldBe "TestApNestedTrue(nested=Nested())"
        }

        with(TestApNestedTrue(TestApNestedTrue.Nested(mapOf("testField" to "Test string")))) {
            nested["testField"] shouldBe "Test string"
            toString() shouldBe "TestApNestedTrue(nested=Nested(testField=Test string))"
        }
    }

    @Test fun `should generate functional TestApTrueExtraNested`() {
        val name = "TestApTrueExtraNested"
        with(
            TestApTrueExtraNested(
                mapOf(
                    "extra" to TestApTrueExtraNested.Extra(field1 = "ABC", field2 = true),
                    "codes" to TestApTrueExtraNested.Codes.ALPHA,
                )
            )
        ) {
            extra.field1 shouldBe "ABC"
            extra.field2 shouldBe true
            codes shouldBe TestApTrueExtraNested.Codes.ALPHA
            empty shouldBe null
            toString() shouldBe "$name(extra=Extra(field1=ABC, field2=true), codes=ALPHA)"
        }

        with(
            TestApTrueExtraNested(
                mapOf(
                    "extra" to TestApTrueExtraNested.Extra(field1 = "ABC", field2 = true),
                    "codes" to TestApTrueExtraNested.Codes.ALPHA,
                    "empty" to TestApTrueExtraNested.Empty(
                        mapOf(
                            "abc" to 123,
                            "def" to 456,
                        )
                    ),
                )
            )
        ) {
            with(extra) {
                field1 shouldBe "ABC"
                field2 shouldBe true
            }
            codes shouldBe TestApTrueExtraNested.Codes.ALPHA
            with(empty) {
                shouldBeNonNull()
                this["abc"] shouldBe 123
                this["def"] shouldBe 456
            }
            toString() shouldBe
                    "$name(extra=Extra(field1=ABC, field2=true), codes=ALPHA, empty=Empty(abc=123, def=456))"
        }
    }

    @Test fun `should generate functional TestApTrueMin`() {
        with(
            TestApTrueMin(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                )
            )
        ) {
            this["field1"] shouldBe "George"
            this["field2"] shouldBe "Henry"
            toString() shouldBe "TestApTrueMin(field1=George, field2=Henry)"
        }

        shouldThrow<IllegalArgumentException>("Number of properties < minimum 2 - 1") {
            TestApTrueMin(mapOf("field1" to "George"))
        }
    }

    @Test fun `should generate functional TestApTrueMin1`() {
        with(
            TestApTrueMin1(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                )
            )
        ) {
            this["field1"] shouldBe "George"
            this["field2"] shouldBe "Henry"
            toString() shouldBe "TestApTrueMin1(field1=George, field2=Henry)"
        }

        shouldThrow<IllegalArgumentException>("Number of properties < minimum 1") {
            TestApTrueMin1(emptyMap())
        }
    }

    @Test fun `should generate functional TestApTrueMax`() {
        with(
            TestApTrueMax(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                )
            )
        ) {
            this["field1"] shouldBe "George"
            this["field2"] shouldBe "Henry"
            toString() shouldBe "TestApTrueMax(field1=George, field2=Henry)"
        }

        shouldThrow<IllegalArgumentException>("Number of properties > maximum 5 - 6") {
            TestApTrueMax(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                    "field3" to "Arthur",
                    "field4" to "Robert",
                    "field5" to "Edward",
                    "field6" to "Roger",
                )
            )
        }
    }

    @Test fun `should generate functional TestApTrueMinMax`() {
        with(
            TestApTrueMinMax(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                )
            )
        ) {
            this["field1"] shouldBe "George"
            this["field2"] shouldBe "Henry"
            toString() shouldBe "TestApTrueMinMax(field1=George, field2=Henry)"
        }

        shouldThrow<IllegalArgumentException>("Number of properties not in range 2..5 - 1") {
            TestApTrueMinMax(mapOf("field1" to "George"))
        }

        shouldThrow<IllegalArgumentException>("Number of properties not in range 2..5 - 6") {
            TestApTrueMinMax(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                    "field3" to "Arthur",
                    "field4" to "Robert",
                    "field5" to "Edward",
                    "field6" to "Roger",
                )
            )
        }
    }

    @Test fun `should generate functional TestApTrueConst`() {
        with(
            TestApTrueConst(
                mapOf(
                    "field1" to "George",
                    "field2" to "Henry",
                    "field3" to "Arthur",
                )
            )
        ) {
            this["field1"] shouldBe "George"
            this["field2"] shouldBe "Henry"
            toString() shouldBe "TestApTrueConst(field1=George, field2=Henry, field3=Arthur)"
        }

        shouldThrow<IllegalArgumentException>("Number of properties != constant 3 - 1") {
            TestApTrueConst(mapOf("field1" to "George"))
        }
    }

}

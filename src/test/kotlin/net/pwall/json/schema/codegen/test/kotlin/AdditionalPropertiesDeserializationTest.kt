/*
 * @(#) AdditionalPropertiesDeserializationTest.kt
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
import kotlin.test.expect

import java.time.LocalDate

import io.kjson.JSONKotlinException
import io.kjson.parseJSON

class AdditionalPropertiesDeserializationTest {

    @Test fun `should deserialize TestAdditionalPropertiesFalse`() {
        val json = "{}"
        val result = json.parseJSON<TestAdditionalPropertiesFalse>()
        expect(TestAdditionalPropertiesFalse()) { result }
    }

    @Test fun `should deserialize TestAdditionalPropertiesTrue`() {
        val json1 = "{}"
        val result1 = json1.parseJSON<TestAdditionalPropertiesTrue>()
        expect(TestAdditionalPropertiesTrue(emptyMap())) { result1 }

        val json2 = """{"data":"something"}"""
        val result2 = json2.parseJSON<TestAdditionalPropertiesTrue>()
        expect("something") { result2["data"] }
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchema1`() {
        val json = """{"date1":"2024-01-28","date2":"2024-01-29"}"""
        val result = json.parseJSON<TestAdditionalPropertiesSchema1>()
        expect(LocalDate.parse("2024-01-28")) { result["date1"] }
        assertNull(result["date99"])
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchemaValid`() {
        val json = """{"field1":"ABC","field2":"xxx"}"""
        val result = json.parseJSON<TestAdditionalPropertiesSchemaValid>()
        expect("ABC") { result["field1"] }
        assertNull(result["field99"])

        assertFailsWith<JSONKotlinException> {
            """{"field1":"ABC","field2":""}""".parseJSON<TestAdditionalPropertiesSchemaValid>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("field2 length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should deserialize TestAdditionalPropertiesFalseExtra`() {
        val json = """{"extra":"content"}"""
        val result = json.parseJSON<TestAdditionalPropertiesFalseExtra>()
        expect("content") { result.extra }
    }

    @Test fun `should deserialize TestApFalseExtraValid`() {
        val json = """{"extra":"content"}"""
        val result = json.parseJSON<TestApFalseExtraValid>()
        expect("content") { result.extra }

        assertFailsWith<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApFalseExtraValid>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should deserialize TestApTrueExtra`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestApTrueExtraValid>()
        expect("content") { result1.extra }
        assertNull(result1["whatever"])

        val json2 = """{"extra":"content","anything":"another"}"""
        val result2 = json2.parseJSON<TestApTrueExtraValid>()
        expect("content") { result2.extra }
        expect("another") { result2["anything"] }
        assertNull(result2["whatever"])

        assertFailsWith<JSONKotlinException> {
            """{"anything":"another"}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":123}""".parseJSON<TestApTrueExtraValid>()
        }.let {
            expect("Incorrect type, expected string but was 123, at /extra") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should deserialize TestApTrueExtraDefault`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestApTrueExtraDefault>()
        expect("content") { result1.extra }
        assertNull(result1["whatever"])

        val json2 = """{"extra":"content","anything":"another"}"""
        val result2 = json2.parseJSON<TestApTrueExtraDefault>()
        expect("content") { result2.extra }
        expect("another") { result2["anything"] }
        assertNull(result2["whatever"])

        val json3 = """{"anything":"another"}"""
        val result3 = json3.parseJSON<TestApTrueExtraDefault>()
        expect("default-value") { result3.extra }
        expect("another") { result3["anything"] }
        assertNull(result3["whatever"])

        assertFailsWith<JSONKotlinException> {
            """{"extra":123}""".parseJSON<TestApTrueExtraDefault>()
        }.let {
            expect("Incorrect type, expected string but was 123, at /extra") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("extra length < minimum 1 - 0") { it.message }
        }
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchemaExtra`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestAdditionalPropertiesSchemaExtra>()
        expect("content") { result1.extra }
        assertNull(result1["whatever"])

// TODO - can we find a way of deserializing into additionalProperties with a schema when there are other properties?
//      the test below fails with: anything is not the correct type, expecting LocalDate
//    Possible solutions:
//      1.  Change the generated constructor to take the specific properties AND a strongly-typed map for the
//          additionalProperties; then change kjson to recognise this case
//      2.  Change the generated init block to accept either the additionalProperties type OR a primitive type that can
//          be converted to it
//     Either of these would require a lot of effort; would that be justified for such a rare case?
//     (We can always tell people that classes like this will require custom deserialization.)

//        val json2 = """{"extra":"content","anything":"2024-01-28"}"""
//        val result2 = json2.parseJSON<TestAdditionalPropertiesSchemaExtra>()
//        expect("content") { result2.extra }
//        expect(LocalDate.parse("2024-01-28")) { result2["anything"] }
//        assertNull(result2["whatever"])

        assertFailsWith<JSONKotlinException> {
            """{"anything":"another"}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("required property missing - extra") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":123}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.let {
            expect("Incorrect type, expected string but was 123, at /extra") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("extra length < minimum 1 - 0") { it.message }
        }

        assertFailsWith<JSONKotlinException> {
            """{"extra":"OK","anything":"another"}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            assertIs<IllegalArgumentException>(it)
            expect("anything is not the correct type, expecting LocalDate") { it.message }
        }
    }

    // TODO - test every form of generated class

    // TODO - do the same for serialization?

}

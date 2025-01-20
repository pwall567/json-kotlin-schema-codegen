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

import java.time.LocalDate

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeType
import io.kstuff.test.shouldThrow

import io.kjson.JSONKotlinException
import io.kjson.parseJSON

class AdditionalPropertiesDeserializationTest {

    @Test fun `should deserialize TestAdditionalPropertiesFalse`() {
        val json = "{}"
        val result = json.parseJSON<TestAdditionalPropertiesFalse>()
        result shouldBe TestAdditionalPropertiesFalse()
    }

    @Test fun `should deserialize TestAdditionalPropertiesTrue`() {
        val json1 = "{}"
        val result1 = json1.parseJSON<TestAdditionalPropertiesTrue>()
        result1 shouldBe TestAdditionalPropertiesTrue(emptyMap())

        val json2 = """{"data":"something"}"""
        val result2 = json2.parseJSON<TestAdditionalPropertiesTrue>()
        result2["data"] shouldBe "something"
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchema1`() {
        val json = """{"date1":"2024-01-28","date2":"2024-01-29"}"""
        val result = json.parseJSON<TestAdditionalPropertiesSchema1>()
        result["date1"] shouldBe LocalDate.parse("2024-01-28")
        result["date99"] shouldBe null
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchemaValid`() {
        val json = """{"field1":"ABC","field2":"xxx"}"""
        val result = json.parseJSON<TestAdditionalPropertiesSchemaValid>()
        result["field1"] shouldBe "ABC"
        result["field99"] shouldBe null

        shouldThrow<JSONKotlinException> {
            """{"field1":"ABC","field2":""}""".parseJSON<TestAdditionalPropertiesSchemaValid>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "field2 length < minimum 1 - 0"
        }
    }

    @Test fun `should deserialize TestAdditionalPropertiesFalseExtra`() {
        val json = """{"extra":"content"}"""
        val result = json.parseJSON<TestAdditionalPropertiesFalseExtra>()
        result.extra shouldBe "content"
    }

    @Test fun `should deserialize TestApFalseExtraValid`() {
        val json = """{"extra":"content"}"""
        val result = json.parseJSON<TestApFalseExtraValid>()
        result.extra shouldBe "content"

        shouldThrow<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApFalseExtraValid>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "extra length < minimum 1 - 0"
        }
    }

    @Test fun `should deserialize TestApTrueExtra`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestApTrueExtraValid>()
        result1.extra shouldBe "content"
        result1["whatever"] shouldBe null

        val json2 = """{"extra":"content","anything":"another"}"""
        val result2 = json2.parseJSON<TestApTrueExtraValid>()
        result2.extra shouldBe "content"
        result2["anything"] shouldBe "another"
        result2["whatever"] shouldBe null

        shouldThrow<JSONKotlinException> {
            """{"anything":"another"}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "required property missing - extra"
        }

        shouldThrow<JSONKotlinException>("Incorrect type, expected string but was 123, at /extra") {
            """{"extra":123}""".parseJSON<TestApTrueExtraValid>()
        }

        shouldThrow<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "extra length < minimum 1 - 0"
        }
    }

    @Test fun `should deserialize TestApTrueExtraDefault`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestApTrueExtraDefault>()
        result1.extra shouldBe "content"
        result1["whatever"] shouldBe null

        val json2 = """{"extra":"content","anything":"another"}"""
        val result2 = json2.parseJSON<TestApTrueExtraDefault>()
        result2.extra shouldBe "content"
        result2["anything"] shouldBe "another"
        result2["whatever"] shouldBe null

        val json3 = """{"anything":"another"}"""
        val result3 = json3.parseJSON<TestApTrueExtraDefault>()
        result3.extra shouldBe "default-value"
        result3["anything"] shouldBe "another"
        result3["whatever"] shouldBe null

        shouldThrow<JSONKotlinException>("Incorrect type, expected string but was 123, at /extra") {
            """{"extra":123}""".parseJSON<TestApTrueExtraDefault>()
        }

        shouldThrow<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestApTrueExtraValid>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "extra length < minimum 1 - 0"
        }
    }

    @Test fun `should deserialize TestAdditionalPropertiesSchemaExtra`() {
        val json1 = """{"extra":"content"}"""
        val result1 = json1.parseJSON<TestAdditionalPropertiesSchemaExtra>()
        result1.extra shouldBe "content"
        result1["whatever"] shouldBe null

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

        shouldThrow<JSONKotlinException> {
            """{"anything":"another"}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "required property missing - extra"
        }

        shouldThrow<JSONKotlinException>("Incorrect type, expected string but was 123, at /extra") {
            """{"extra":123}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }

        shouldThrow<JSONKotlinException> {
            """{"extra":""}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "extra length < minimum 1 - 0"
        }

        shouldThrow<JSONKotlinException> {
            """{"extra":"OK","anything":"another"}""".parseJSON<TestAdditionalPropertiesSchemaExtra>()
        }.cause.let {
            it.shouldBeType<IllegalArgumentException>()
            it.message shouldBe "anything is not the correct type, expecting LocalDate"
        }
    }

    // TODO - test every form of generated class

    // TODO - do the same for serialization?

}

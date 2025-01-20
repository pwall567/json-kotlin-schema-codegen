/*
 * @(#) StringValueTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020 Peter Wall
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

class StringValueTest {

    @Test fun `should handle simple string`() {
        val testString = StringValue("abc")
        testString.rawString shouldBe "abc"
        testString.kotlinString shouldBe "\"abc\""
        testString.javaString shouldBe "\"abc\""
    }

    @Test fun `should handle special characters`() {
        val testString = StringValue("Fred's \$100 \"Special\"\n")
        testString.rawString shouldBe "Fred's \$100 \"Special\"\n"
        testString.kotlinString shouldBe "\"Fred's \\\$100 \\\"Special\\\"\\n\""
        testString.javaString shouldBe "\"Fred's \$100 \\\"Special\\\"\\n\""
    }

    @Test fun `should handle unicode sequences`() {
        val testString = StringValue("R\u00E9sum\u00E9 \u2014 CV")
        testString.rawString shouldBe "R\u00E9sum\u00E9 \u2014 CV"
        testString.kotlinString shouldBe "\"R\\u00E9sum\\u00E9 \\u2014 CV\""
        testString.javaString shouldBe "\"R\\u00E9sum\\u00E9 \\u2014 CV\""
    }

    @Test fun `should use raw string literal where appropriate`() {
        val testString = StringValue("""{"a":"apple","b":"bear","c":"cat"}""")
        testString.rawString shouldBe """{"a":"apple","b":"bear","c":"cat"}"""
        testString.kotlinString shouldBe """""${'"'}{"a":"apple","b":"bear","c":"cat"}""${'"'}"""
        testString.javaString shouldBe "\"{\\\"a\\\":\\\"apple\\\",\\\"b\\\":\\\"bear\\\",\\\"c\\\":\\\"cat\\\"}\""
    }

}

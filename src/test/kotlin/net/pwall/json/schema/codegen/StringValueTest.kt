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
import kotlin.test.expect

class StringValueTest {

    @Test fun `should handle simple string`() {
        val testString = StringValue("abc")
        expect("abc") { testString.rawString }
        expect("\"abc\"") { testString.kotlinString }
        expect("\"abc\"") { testString.javaString }
    }

    @Test fun `should handle special characters`() {
        val testString = StringValue("Fred's \$100 \"Special\"\n")
        expect("Fred's \$100 \"Special\"\n") { testString.rawString }
        expect("\"Fred's \\\$100 \\\"Special\\\"\\n\"") { testString.kotlinString }
        expect("\"Fred's \$100 \\\"Special\\\"\\n\"") { testString.javaString }
    }

    @Test fun `should handle unicode sequences`() {
        val testString = StringValue("R\u00E9sum\u00E9 \u2014 CV")
        expect("R\u00E9sum\u00E9 \u2014 CV") { testString.rawString }
        expect("\"R\\u00E9sum\\u00E9 \\u2014 CV\"") { testString.kotlinString }
        expect("\"R\\u00E9sum\\u00E9 \\u2014 CV\"") { testString.javaString }
    }

    @Test fun `should use raw string literal where appropriate`() {
        val testString = StringValue("""{"a":"apple","b":"bear","c":"cat"}""")
        expect("""{"a":"apple","b":"bear","c":"cat"}""") { testString.rawString }
        expect("""""${'"'}{"a":"apple","b":"bear","c":"cat"}""${'"'}""") { testString.kotlinString }
        expect("\"{\\\"a\\\":\\\"apple\\\",\\\"b\\\":\\\"bear\\\",\\\"c\\\":\\\"cat\\\"}\"") { testString.javaString }
    }

}

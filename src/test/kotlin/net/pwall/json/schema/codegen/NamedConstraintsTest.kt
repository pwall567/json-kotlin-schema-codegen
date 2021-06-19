/*
 * @(#) NamedConstraintsTest.kt
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

class NamedConstraintsTest {

    @Test fun `should detect and modify illegal Kotlin names`(){
        expect("name") { NamedConstraints.checkKotlinName("name") }
        expect("`hyphenated-name`") { NamedConstraints.checkKotlinName("hyphenated-name") }
        expect("`class`") { NamedConstraints.checkKotlinName("class") }
        expect("`123test123`") { NamedConstraints.checkKotlinName("123test123") }
    }

    @Test fun `should detect and modify illegal Java names`(){
        expect("name") { NamedConstraints.checkJavaName("name") }
        expect("hyphenated_name") { NamedConstraints.checkJavaName("hyphenated-name") }
        expect("_test123") { NamedConstraints.checkJavaName("123test123") }
    }

}

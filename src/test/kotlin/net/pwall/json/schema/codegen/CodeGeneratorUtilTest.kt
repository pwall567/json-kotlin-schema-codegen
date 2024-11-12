/*
 * @(#) CodeGeneratorUtilTest.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2023, 2024 Peter Wall
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
import kotlin.test.assertSame
import kotlin.test.expect

import net.pwall.json.schema.codegen.CodeGenerator.Companion.sanitiseName
import net.pwall.json.schema.codegen.CodeGenerator.Companion.toColumnId

class CodeGeneratorUtilTest {

    @Test fun `should return original name from sanitiseName when no changes needed`() {
        "Abc".let {
            assertSame(it, it.sanitiseName())
        }
        "data12345".let {
            assertSame(it, it.sanitiseName())
        }
    }

    @Test fun `should sanitise name correctly`() {
        expect("Abc123") { "Abc-123".sanitiseName() }
        expect("Abc123456") { "Abc-123-456".sanitiseName() }
    }

    @Test fun `should convert number to column id`() {
        expect("A") { 0.toColumnId() }
        expect("B") { 1.toColumnId() }
        expect("Z") { 25.toColumnId() }
        expect("AA") { 26.toColumnId() }
        expect("AB") { 27.toColumnId() }
        expect("AZ") { 51.toColumnId() }
        expect("BA") { 52.toColumnId() }
        expect("BB") { 53.toColumnId() }
        expect("ZA") { (26 * 26).toColumnId() }
        expect("ZZ") { (26 * 26 + 25).toColumnId() }
        expect("AAA") { (27 * 26).toColumnId() }
        expect("AAB") { (27 * 26 + 1).toColumnId() }
    }

}

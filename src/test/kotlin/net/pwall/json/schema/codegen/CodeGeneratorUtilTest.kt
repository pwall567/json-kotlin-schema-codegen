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

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeSameInstance

import net.pwall.json.schema.codegen.CodeGenerator.Companion.sanitiseName
import net.pwall.json.schema.codegen.CodeGenerator.Companion.toColumnId

class CodeGeneratorUtilTest {

    @Test fun `should return original name from sanitiseName when no changes needed`() {
        "Abc".let {
            it.sanitiseName() shouldBeSameInstance it
        }
        "data12345".let {
            it.sanitiseName() shouldBeSameInstance it
        }
    }

    @Test fun `should sanitise name correctly`() {
        "Abc-123".sanitiseName() shouldBe "Abc123"
        "Abc-123-456".sanitiseName() shouldBe "Abc123456"
    }

    @Test fun `should convert number to column id`() {
        0.toColumnId() shouldBe "A"
        1.toColumnId() shouldBe "B"
        25.toColumnId() shouldBe "Z"
        26.toColumnId() shouldBe "AA"
        27.toColumnId() shouldBe "AB"
        51.toColumnId() shouldBe "AZ"
        52.toColumnId() shouldBe "BA"
        53.toColumnId() shouldBe "BB"
        (26 * 26).toColumnId() shouldBe "ZA"
        (26 * 26 + 25).toColumnId() shouldBe "ZZ"
        (27 * 26).toColumnId() shouldBe "AAA"
        (27 * 26 + 1).toColumnId() shouldBe "AAB"
    }

}

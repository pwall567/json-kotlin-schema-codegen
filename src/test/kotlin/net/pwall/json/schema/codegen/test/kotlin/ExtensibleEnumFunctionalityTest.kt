/*
 * @(#) ExtensibleEnumFunctionalityTest.kt
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

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldNotBe

class ExtensibleEnumFunctionalityTest {

    @Test fun `should compare extensible enum`() {
        val enum1 = getAlpha()
        (enum1 == TestExtensibleEnum.ALPHA) shouldBe true
        (enum1 != getBeta()) shouldBe true
    }

    @Test fun `should compare extensible enum with constructed enum`() {
        val enum1 = getAlpha()
        TestExtensibleEnum("ALPHA") shouldBe enum1
        TestExtensibleEnum("DELTA") shouldNotBe enum1
    }

    @Test fun `should toString extensible enum`() {
        val enum1 = getAlpha()
        enum1.toString() shouldBe "ALPHA"
        val enum2 = TestExtensibleEnum("GAMMA")
        enum2.toString() shouldBe "GAMMA"
    }

    companion object {

        fun getAlpha(): TestExtensibleEnum {
            return TestExtensibleEnum.ALPHA
        }

        fun getBeta(): TestExtensibleEnum {
            return TestExtensibleEnum.BETA
        }

    }

}

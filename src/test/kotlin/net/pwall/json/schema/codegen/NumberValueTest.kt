/*
 * @(#) NumberValueTest.kt
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

import java.math.BigDecimal

import io.kstuff.test.shouldBe

class NumberValueTest {

    @Test fun `should handle int 0`() {
        val intValue = 0
        val numberValue = NumberValue(intValue)
        numberValue.isInt shouldBe true
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe false
        numberValue.isZero shouldBe true
        numberValue.isOne shouldBe false
    }

    @Test fun `should handle int 1`() {
        val intValue = 1
        val numberValue = NumberValue(intValue)
        numberValue.isInt shouldBe true
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe false
        numberValue.isZero shouldBe false
        numberValue.isOne shouldBe true
    }

    @Test fun `should handle other int`() {
        val intValue = -99
        val numberValue = NumberValue(intValue)
        numberValue.isInt shouldBe true
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe false
        numberValue.isZero shouldBe false
        numberValue.isOne shouldBe false
    }

    @Test fun `should handle long 0`() {
        val longValue = 0L
        val numberValue = NumberValue(longValue)
        numberValue.isInt shouldBe false
        numberValue.isLong shouldBe true
        numberValue.isDecimal shouldBe false
        numberValue.isZero shouldBe true
        numberValue.isOne shouldBe false
    }

    @Test fun `should handle long 1`() {
        val longValue = 1L
        val numberValue = NumberValue(longValue)
        numberValue.isInt shouldBe false
        numberValue.isLong shouldBe true
        numberValue.isDecimal shouldBe false
        numberValue.isZero shouldBe false
        numberValue.isOne shouldBe true
    }

    @Test fun `should handle decimal 0`() {
        val decimalValue = BigDecimal.ZERO
        val numberValue = NumberValue(decimalValue)
        numberValue.isInt shouldBe false
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe true
        numberValue.isZero shouldBe true
        numberValue.isOne shouldBe false
    }

    @Test fun `should handle decimal 1`() {
        val decimalValue = BigDecimal.ONE
        val numberValue = NumberValue(decimalValue)
        numberValue.isInt shouldBe false
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe true
        numberValue.isZero shouldBe false
        numberValue.isOne shouldBe true
    }

    @Test fun `should handle other decimal`() {
        val decimalValue = BigDecimal("123.45")
        val numberValue = NumberValue(decimalValue)
        numberValue.isInt shouldBe false
        numberValue.isLong shouldBe false
        numberValue.isDecimal shouldBe true
        numberValue.isZero shouldBe false
        numberValue.isOne shouldBe false
    }

}

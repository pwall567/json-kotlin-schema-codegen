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

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.expect

class NumberValueTest {

    @Test fun `should handle int 0`() {
        val intValue = 0
        val numberValue = NumberValue(intValue)
        expect(true) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(false) { numberValue.isDecimal }
        expect(true) { numberValue.isZero }
        expect(false) { numberValue.isOne }
    }

    @Test fun `should handle int 1`() {
        val intValue = 1
        val numberValue = NumberValue(intValue)
        expect(true) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(false) { numberValue.isDecimal }
        expect(false) { numberValue.isZero }
        expect(true) { numberValue.isOne }
    }

    @Test fun `should handle other int`() {
        val intValue = -99
        val numberValue = NumberValue(intValue)
        expect(true) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(false) { numberValue.isDecimal }
        expect(false) { numberValue.isZero }
        expect(false) { numberValue.isOne }
    }

    @Test fun `should handle long 0`() {
        val longValue = 0L
        val numberValue = NumberValue(longValue)
        expect(false) { numberValue.isInt }
        expect(true) { numberValue.isLong }
        expect(false) { numberValue.isDecimal }
        expect(true) { numberValue.isZero }
        expect(false) { numberValue.isOne }
    }

    @Test fun `should handle long 1`() {
        val longValue = 1L
        val numberValue = NumberValue(longValue)
        expect(false) { numberValue.isInt }
        expect(true) { numberValue.isLong }
        expect(false) { numberValue.isDecimal }
        expect(false) { numberValue.isZero }
        expect(true) { numberValue.isOne }
    }

    @Test fun `should handle decimal 0`() {
        val decimalValue = BigDecimal.ZERO
        val numberValue = NumberValue(decimalValue)
        expect(false) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(true) { numberValue.isDecimal }
        expect(true) { numberValue.isZero }
        expect(false) { numberValue.isOne }
    }

    @Test fun `should handle decimal 1`() {
        val decimalValue = BigDecimal.ONE
        val numberValue = NumberValue(decimalValue)
        expect(false) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(true) { numberValue.isDecimal }
        expect(false) { numberValue.isZero }
        expect(true) { numberValue.isOne }
    }

    @Test fun `should handle other decimal`() {
        val decimalValue = BigDecimal("123.45")
        val numberValue = NumberValue(decimalValue)
        expect(false) { numberValue.isInt }
        expect(false) { numberValue.isLong }
        expect(true) { numberValue.isDecimal }
        expect(false) { numberValue.isZero }
        expect(false) { numberValue.isOne }
    }

}

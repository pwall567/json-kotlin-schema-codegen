/*
 * @(#) NumberValue.kt
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
import java.math.RoundingMode

class NumberValue(val rawNumber: Number) : ValidationValue {

    init {
        require(rawNumber is Int || rawNumber is Long || rawNumber is BigDecimal)
    }

    val isInt: Boolean
        get() = rawNumber is Int

    val isLong: Boolean
        get() = rawNumber is Long

    val isDecimal: Boolean
        get() = rawNumber is BigDecimal

    val isZero: Boolean
        get() = when (rawNumber) {
            is Int -> rawNumber == 0
            is Long -> rawNumber == 0L
            is BigDecimal -> BigDecimal.ZERO.compareTo(rawNumber) == 0
            else -> throw IllegalStateException("Impossible type in NumberValue")
        }

    val isOne: Boolean
        get() = when (rawNumber) {
            is Int -> rawNumber == 1
            is Long -> rawNumber == 1L
            is BigDecimal -> BigDecimal.ONE.compareTo(rawNumber) == 0
            else -> throw IllegalStateException("Impossible type in NumberValue")
        }

    val asLong: Long
        get() = when (rawNumber) {
            is BigDecimal -> rawNumber.setScale(0, RoundingMode.DOWN).toLong()
            is Long -> rawNumber
            else -> rawNumber.toLong()
        }

    override fun equals(other: Any?): Boolean = this === other || other is NumberValue && rawNumber == other.rawNumber

    override fun hashCode(): Int = rawNumber.hashCode()

    override fun toString(): String = rawNumber.toString()

    companion object {

        val NumberValue?.isInt: Boolean
            get() = this?.isInt ?: false

        val NumberValue?.isLong: Boolean
            get() = this?.isLong ?: false

        val NumberValue?.isDecimal: Boolean
            get() = this?.isDecimal ?: false

        val NumberValue?.isZero: Boolean
            get() = this?.isZero ?: false

        val NumberValue?.isOne: Boolean
            get() = this?.isOne ?: false

    }

}

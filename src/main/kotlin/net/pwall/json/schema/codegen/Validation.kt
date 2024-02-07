/*
 * @(#) Validation.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2023 Peter Wall
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

import net.pwall.util.Util.eq

class Validation(
    val type: Type,
    val value: Any? = null,
    val negate: Boolean = false,
) {

    override fun equals(other: Any?): Boolean = eq(other) {
        type == it.type && value == it.value && negate == it.negate
    }

    override fun hashCode(): Int = type.hashCode() xor value.hashCode() xor negate.hashCode()

    enum class Type {
        PATTERN,
        MULTIPLE_INT, MULTIPLE_LONG, MULTIPLE_DECIMAL,
        MAXIMUM_INT, MAXIMUM_LONG, MAXIMUM_DECIMAL, EXCLUSIVE_MAXIMUM_DECIMAL,
        MAXIMUM_DECIMAL_ZERO, EXCLUSIVE_MAXIMUM_DECIMAL_ZERO,
        MINIMUM_INT, MINIMUM_LONG, MINIMUM_DECIMAL, EXCLUSIVE_MINIMUM_DECIMAL,
        MINIMUM_DECIMAL_ZERO, EXCLUSIVE_MINIMUM_DECIMAL_ZERO,
        RANGE_INT, RANGE_LONG, RANGE_DECIMAL,
        EMAIL, HOSTNAME, IPV4, IPV6, DURATION, JSON_POINTER, RELATIVE_JSON_POINTER,
        DATE_TIME, DATE, TIME, UUID, URI, URI_REFERENCE,
        MAX_ITEMS, MIN_ITEMS, CONST_ITEMS, RANGE_ITEMS,
        MAX_LENGTH, MIN_LENGTH, CONST_LENGTH, RANGE_LENGTH,
        CONST_INT, CONST_LONG, CONST_DECIMAL, CONST_DECIMAL_ZERO, CONST_STRING,
        CONST_ENUM, ENUM_STRING, ENUM_INT,
        ARRAY_ITEMS,
        ADDITIONAL_PROPERTIES, PATTERN_PROPERTIES, UNEXPECTED_PROPERTIES,
    }

}

/*
 * @(#) Validation.kt
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

class Validation(val type: Type, val value: Any? = null) {

    enum class Type {
        PATTERN,
        MULTIPLE_INT, MULTIPLE_LONG, MULTIPLE_DECIMAL,
        MAXIMUM_INT, MAXIMUM_LONG, MAXIMUM_DECIMAL, EXCLUSIVE_MAXIMUM_DECIMAL,
        MINIMUM_INT, MINIMUM_LONG, MINIMUM_DECIMAL, EXCLUSIVE_MINIMUM_DECIMAL,
        EMAIL, HOSTNAME, IPV4, IPV6,
        MAX_ITEMS, MIN_ITEMS,
        MAX_LENGTH, MIN_LENGTH,
        CONST_INT, CONST_LONG, CONST_DECIMAL, CONST_STRING
    }

}

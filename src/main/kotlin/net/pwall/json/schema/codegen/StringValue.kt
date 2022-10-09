/*
 * @(#) StringValue.kt
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

class StringValue(val rawString: String) : ValidationValue {

    val kotlinString: String by lazy {
        val sb1 = StringBuilder(rawString.length + 5)
        val sb2 = StringBuilder(rawString.length + 10)
        sb1.append('"')
        sb2.append("\"\"\"")
        for (ch in rawString) {
            when (ch) {
                '"' -> {
                    sb1.append("\\\"")
                    if (sb2.length > 4 && sb2[sb2.length - 1] == '"' && sb2[sb2.length - 2] == '"')
                        sb2.append("\${'\"'}")
                    else
                        sb2.append('"')
                }
                '$' -> {
                    sb1.append("\\\$")
                    sb2.append("\${'\$'}")
                }
                '\\' -> {
                    sb1.append("\\\\")
                    sb2.append("\\\\")
                }
                in ' '..'\u007E' -> {
                    sb1.append(ch)
                    sb2.append(ch)
                }
                '\n' -> {
                    sb1.append("\\n")
                    sb2.append("\\n")
                }
                '\r' -> {
                    sb1.append("\\r")
                    sb2.append("\\r")
                }
                '\t' -> {
                    sb1.append("\\t")
                    sb2.append("\\t")
                }
                '\b' -> {
                    sb1.append("\\b")
                    sb2.append("\\b")
                }
                else -> {
                    sb1.append("\\u")
                    sb1.appendHex(ch.code)
                    sb2.append("\\u")
                    sb2.appendHex(ch.code)
                }
            }
        }
        sb1.append('"')
        sb2.append("\"\"\"")
        if (sb1.length < sb2.length) sb1.toString() else sb2.toString()
    }

    val javaString: String by lazy {
        buildString(rawString.length + 5) {
            append('"')
            for (ch in rawString) {
                when (ch) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    in ' '..'\u007E' -> append(ch)
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    else -> {
                        append("\\u")
                        appendHex(ch.code)
                    }
                }
            }
            append('"')
        }
    }

    override fun equals(other: Any?): Boolean = this === other || other is StringValue && rawString == other.rawString

    override fun hashCode(): Int = rawString.hashCode()

    override fun toString(): String = rawString

    companion object {

        private const val hexChars = "0123456789ABCDEF"

        fun StringBuilder.appendHex(hex: Int) {
            append(hexChars[(hex shr 12) and 0xF])
            append(hexChars[(hex shr 8) and 0xF])
            append(hexChars[(hex shr 4) and 0xF])
            append(hexChars[hex and 0xF])
        }

    }

}

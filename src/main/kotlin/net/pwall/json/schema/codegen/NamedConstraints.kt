/*
 * @(#) NamedConstraints.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2024, 2025 Peter Wall
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

import io.jstuff.util.IntOutput

import net.pwall.json.schema.JSONSchema
import net.pwall.util.DefaultValue
import net.pwall.util.Deferred
import net.pwall.util.Name.Companion.capitalise

class NamedConstraints(schema: JSONSchema, val name: String) : Constraints(schema) {

    var baseProperty: Constraints? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var propertyName: String by DefaultValue {
        name
    }

    @Suppress("unused")
    override val displayName: String
        get() = name

    @Suppress("unused")
    val kotlinName: String by Deferred {
        checkKotlinName(propertyName)
    }

    @Suppress("unused")
    val javaName: String by Deferred {
        checkJavaName(propertyName)
    }

    @Suppress("unused")
    val capitalisedName: String by Deferred {
        javaName.capitalise()
    }

    companion object {

        private const val NAME_START_MASK = 1
        private const val NAME_CONTINUATION_MASK = 2
        private const val JAVA_NAME_START_MASK = 4
        private const val JAVA_NAME_CONTINUATION_MASK = 8
        private const val ALLOWED_SPECIAL_MASK = 16
        private val maskArray = ByteArray(128) {
            val alphabetic = (NAME_START_MASK or NAME_CONTINUATION_MASK or JAVA_NAME_START_MASK or
                    JAVA_NAME_CONTINUATION_MASK).toByte()
            when (it.toChar()) {
                in 'A'..'Z' -> alphabetic
                in 'a'..'z' -> alphabetic
                in '0'..'9' -> (NAME_CONTINUATION_MASK or JAVA_NAME_CONTINUATION_MASK).toByte()
                '_' -> alphabetic
                '$' -> (JAVA_NAME_START_MASK or JAVA_NAME_CONTINUATION_MASK or ALLOWED_SPECIAL_MASK).toByte()
                in " !\"#$%'()*+,-=?@^_`{|}~" -> ALLOWED_SPECIAL_MASK.toByte()
                else -> 0
            }
        }

        private fun Char.hasMaskBit(bit: Int): Boolean = this < '\u0080' && (maskArray[code].toInt() and bit) != 0

        private val kotlinReserved = setOf(
            "as",
            "as?",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "!in",
            "interface",
            "is",
            "!is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while"
        )

        private val javaReserved = setOf(
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "false",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "null",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "staticfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "true",
            "try",
            "void",
            "volatile",
            "while"
        )

        fun checkKotlinName(name: String): String {
            if (name in kotlinReserved)
                return "`$name`"
            if (name[0].hasMaskBit(NAME_START_MASK) && name.all { it.hasMaskBit(NAME_CONTINUATION_MASK) })
                return name
            return buildString {
                var needsBackticks = !name[0].hasMaskBit(NAME_START_MASK)
                for (ch in name) {
                    when {
                        ch.hasMaskBit(NAME_CONTINUATION_MASK) -> append(ch)
                        ch.hasMaskBit(ALLOWED_SPECIAL_MASK) -> {
                            needsBackticks = true
                            append(ch)
                        }
                        else -> {
                            append('_')
                            IntOutput.append2Hex(this, ch.code)
                        }
                    }
                }
                if (needsBackticks) {
                    insert(0, '`')
                    append('`')
                }
            }
        }

        fun checkJavaName(name: String): String {
            if (name in javaReserved)
                return name + '_'
            if (name[0].hasMaskBit(JAVA_NAME_START_MASK) && name.all { it.hasMaskBit(JAVA_NAME_CONTINUATION_MASK) })
                return name
            return buildString {
                if (name[0].let { it.hasMaskBit(JAVA_NAME_CONTINUATION_MASK) && !it.hasMaskBit(JAVA_NAME_START_MASK) })
                    append('_')
                for (ch in name) {
                    if (ch.hasMaskBit(JAVA_NAME_CONTINUATION_MASK))
                        append(ch)
                    else if (ch == ' ' || ch == '-')
                        append('_')
                    else {
                        append('_')
                        IntOutput.append2Hex(this, ch.code)
                    }
                }
            }
        }

    }

}

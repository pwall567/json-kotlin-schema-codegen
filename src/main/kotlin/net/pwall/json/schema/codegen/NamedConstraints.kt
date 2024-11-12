/*
 * @(#) NamedConstraints.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2024 Peter Wall
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

import net.pwall.json.schema.JSONSchema
import net.pwall.util.Name.Companion.capitalise

class NamedConstraints(schema: JSONSchema, val name: String) : Constraints(schema) {

    var baseProperty: Constraints? = null

    @Suppress("unused")
    val propertyName: String
        get() = name

    @Suppress("unused")
    override val displayName: String
        get() = name

    @Suppress("unused")
    val kotlinName: String
        get() = checkKotlinName(name)

    @Suppress("unused")
    val javaName: String
        get() = checkJavaName(name)

    @Suppress("unused")
    val capitalisedName: String
        get() = javaName.capitalise()

    companion object {

        private val kotlinNameRegex = Regex("^[A-Za-z_][A-Za-z0-9_]+$")

        private val javaNameRegex = Regex("^[A-Za-z_$][A-Za-z0-9_$]+$")

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

        fun checkKotlinName(name: String): String =
                if (kotlinNameRegex.containsMatchIn(name) && name !in kotlinReserved) name else "`$name`"

        fun checkJavaName(name: String): String = when {
            !javaNameRegex.containsMatchIn(name) ->
                    name.replace(Regex("^[^A-Za-z$]+"), "_").replace(Regex("[^A-Za-z0-9$]+"), "_")
            name in javaReserved -> "${name}_"
            else -> name
        }

    }

}

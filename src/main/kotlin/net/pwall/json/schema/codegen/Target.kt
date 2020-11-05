/*
 * @(#) Target.kt
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

import java.io.File

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException

/**
 * A code generation target.  The class contains several properties that exist just for the purposes of template
 * expansion.
 *
 * @author  Peter Wall
 */
class Target(val schema: JSONSchema, constraints: Constraints, className: String, val packageName: String?,
        val subDirectories: List<String>, val suffix: String, val file: File,
        @Suppress("unused") val generatorComment: String? = null) : ClassDescriptor(constraints, className) {

    @Suppress("unused")
    val indent = Indent()

    val qualifiedClassName: String
        get() = packageName?.let { "$it.$className" } ?: className

    var baseClass: Target? = null

    val systemClasses = mutableListOf<SystemClass>()
    val imports = mutableListOf<String>()

    @Suppress("unused")
    val statics = mutableListOf<Static>()
    @Suppress("unused")
    val nestedClasses = mutableListOf<ClassDescriptor>()

    @Suppress("unused")
    val staticsPresent: Boolean
        get() = statics.isNotEmpty()

    @Suppress("unused")
    val nestedClassesPresent: Boolean
        get() = nestedClasses.isNotEmpty()

    @Suppress("unused")
    val validationsOrNestedClassesPresent: Boolean
        get() = validationsPresent || nestedClassesPresent

    @Suppress("unused")
    val validationsOrNestedClassesOrStaticsPresent: Boolean
        get() = validationsPresent || nestedClassesPresent || staticsPresent

    fun addNestedClass(constraints: Constraints, innerClassName: String): ClassDescriptor {
        var actualInnerClassName = innerClassName
        if (nestedClasses.any { it.className == innerClassName }) {
            for (i in 1..1000) {
                if (i == 1000)
                    throw JSONSchemaException("Too many identically named inner classes - $innerClassName")
                if (!nestedClasses.any { it.className == "$innerClassName$i" }) {
                    actualInnerClassName = "$innerClassName$i"
                    break
                }
            }
        }
        val nestedClass = ClassDescriptor(constraints, actualInnerClassName)
        nestedClasses.add(nestedClass)
        return nestedClass
    }

    fun addStatic(type: StaticType, staticNamePrefix: String, value: Any): Static {
        statics.find { it.type == type && it.value == value }?.let { return it }
        return Static(type, "$staticNamePrefix${statics.size}", value).also { statics.add(it) }
    }

    enum class StaticType { DECIMAL, STRING, PATTERN, STRING_ARRAY, INT_ARRAY }

    data class Static(val type: StaticType, val staticName: String, val value: Any)

}

/*
 * @(#) ClassDescriptor.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2023, 2025 Peter Wall
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

open class ClassDescriptor(val constraints: Constraints, override val className: String) : ClassId {

    override val packageName: String? = null

    var validationsPresent: Boolean = false

    var baseClass: ClassDescriptor? = null

    val interfaces = mutableListOf<ClassId>()

    val derivedClasses = mutableListOf<ClassDescriptor>()

    @Suppress("unused")
    val hasBaseClass: Boolean
        get() = baseClass != null

    @Suppress("MemberVisibilityCanBePrivate")
    val hasBaseClassWithProperties: Boolean
        get() = baseClass.let { it != null &&  it.constraints.properties.isNotEmpty() }

    @Suppress("unused")
    val hasBaseClassWithPropertiesOrIsBaseClass: Boolean
        get() = hasBaseClassWithProperties || derivedClasses.isNotEmpty()

    @Suppress("unused")
    val validationsOrBaseClassWithPropertiesPresent: Boolean
        get() = validationsPresent || hasBaseClassWithProperties

    @Suppress("unused")
    val copySameAsBaseCopy: Boolean
        get() {
            baseClass?.let {
                val properties = constraints.properties
                val baseProperties = it.constraints.properties
                if (properties.size != baseProperties.size)
                    return false
                for (i in properties.indices)
                    if (properties[i].baseProperty != baseProperties[i])
                        return false
                return true
            }
            return false
        }

}

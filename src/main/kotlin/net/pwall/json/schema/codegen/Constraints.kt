/*
 * @(#) Constraints.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021 Peter Wall
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
import java.net.URI

import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONSequence
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.validation.FormatValidator

open class Constraints(val schema: JSONSchema) {

    @Suppress("unused")
    val openBrace = '{'

    @Suppress("unused")
    val closeBrace = '}'

    @Suppress("unused")
    val openAngleBracket = '<'

    @Suppress("unused")
    val closeAngleBracket = '>'

    @Suppress("unused")
    open val displayName: String
        get() = "value"

    @Suppress("unused")
    var uri: URI? = schema.uri

    var objectValidationsPresent: Boolean? = null

    var localTypeName: String? = null

    @Suppress("unused")
    val isLocalType: Boolean
        get() = localTypeName != null

    var isEnumClass = false

    val types = mutableListOf<JSONSchema.Type>()

    var systemClass: SystemClass? = null

    var nullable: Boolean? = null

    var isRequired = false

    var defaultValue: DefaultValue? = null

    val properties = mutableListOf<NamedConstraints>()

    @Suppress("unused")
    val numberOfProperties: Int
        get() = properties.size

    @Suppress("unused")
    val baseProperties: List<NamedConstraints>
        get() = properties.filter { it.baseProperty != null }

    @Suppress("unused")
    val nonBaseProperties: List<NamedConstraints>
        get() = properties.filter { it.baseProperty == null }

    var extendedInDerived: Boolean = false

    val required = mutableListOf<String>()

    var arrayItems: Constraints? = null
    var minItems: Int? = null
    var maxItems: Int? = null
    var uniqueItems: Boolean = false

    var oneOfSchemata: List<Constraints> = emptyList()

    var minimum: Number? = null // Number will be BigDecimal, Long or Int
    var exclusiveMinimum: Number? = null
    var maximum: Number? = null
    var exclusiveMaximum: Number? = null
    val multipleOf = mutableListOf<Number>()

    var maxLength: Int? = null
    var minLength: Int? = null
    var format: FormatValidator.FormatChecker? = null
    var regex: Regex? = null

    var enumValues: JSONSequence<*>? = null
    var constValue: JSONValue? = null

    @Suppress("unused")
    val isSystemClass: Boolean
        get() = systemClass != null

    @Suppress("unused")
    val safeDescription: String?
        get() = schema.description?.trim()?.replace("*/", "* /")

    @Suppress("unused")
    val isIdentifiableType: Boolean
        get() = types.size == 1

    @Suppress("unused")
    val isObject: Boolean
        get() = isType(JSONSchema.Type.OBJECT) || types.isEmpty() && properties.isNotEmpty() // ?

    @Suppress("unused")
    val isArray: Boolean
        get() = isType(JSONSchema.Type.ARRAY) || types.isEmpty() && arrayItems != null // ???

    @Suppress("unused")
    val isString: Boolean
        get() = isType(JSONSchema.Type.STRING) ||
                types.isEmpty() && properties.isEmpty() && arrayItems == null &&
                        (format != null || regex != null || maxLength != null || minLength != null ||
                                constValue is JSONString || enumImpliesString())

    @Suppress("unused")
    val isBoolean: Boolean
        get() = isType(JSONSchema.Type.BOOLEAN)

    @Suppress("unused")
    val isDecimal: Boolean
        get() = isType(JSONSchema.Type.NUMBER)

    @Suppress("unused")
    val isInt: Boolean
        get() = isIntOrLong && (rangeImpliesInt() || constImpliesInt() || enumImpliesInt() || formatImpliesInt())

    @Suppress("unused")
    val isLong: Boolean
        get() = isIntOrLong && !(rangeImpliesInt() || constImpliesInt() || enumImpliesInt() || formatImpliesInt())

    @Suppress("unused")
    val isIntOrLong: Boolean
        get() = isType(JSONSchema.Type.INTEGER)

    @Suppress("unused")
    val isPrimitive: Boolean
        get() = isIntOrLong || isBoolean

    val minimumLong: Long?
        get() {
            minimum?.let { min ->
                exclusiveMinimum?.let { emin ->
                    return maxOf(min.asLong(), emin.asLong() + 1)
                }
                return min.asLong()
            }
            return exclusiveMinimum?.let { it.asLong() + 1 }
        }

    val maximumLong: Long?
        get() {
            maximum?.let { max ->
                exclusiveMaximum?.let { emax ->
                    return minOf(max.asLong(), emax.asLong() - 1)
                }
                return max.asLong()
            }
            return exclusiveMaximum?.let { it.asLong() - 1 }
        }

    val validations = mutableListOf<Validation>()

    fun addValidation(type: Validation.Type, value: Any? = null) {
        validations.add(Validation(type, value))
    }

    private fun isType(type: JSONSchema.Type): Boolean = types.size == 1 && types[0] == type

    private fun constImpliesInt(): Boolean = constValue?.let {
        when (it) {
            is JSONInteger -> true
            is JSONLong -> it.value in Int.MIN_VALUE..Int.MAX_VALUE
            is JSONDecimal -> it.value.asLong() in Int.MIN_VALUE..Int.MAX_VALUE
            else -> false
        }
    } ?: false

    private fun enumImpliesInt(): Boolean {
        enumValues?.let { array ->
            return array.all {
                when (it) {
                    is JSONInteger -> true
                    is JSONLong -> it.value in Int.MIN_VALUE..Int.MAX_VALUE
                    is JSONDecimal -> it.value.asLong() in Int.MIN_VALUE..Int.MAX_VALUE
                    else -> false
                }
            }
        }
        return false
    }

    private fun enumImpliesString(): Boolean {
        enumValues?.let { array ->
            return array.all { it is JSONString }
        }
        return false
    }

    private fun formatImpliesInt(): Boolean = format == FormatValidator.Int32FormatChecker

    private fun rangeImpliesInt(): Boolean = minimumImpliesInt() && maximumImpliesInt()

    private fun minimumImpliesInt(): Boolean = minimumLong?.let { it >= Int.MIN_VALUE } ?: false

    private fun maximumImpliesInt(): Boolean = maximumLong?.let { it <= Int.MAX_VALUE } ?: false

    /**
     * Does this `Constraints` resolve to the same generated type as another?
     */
    fun sameType(other: Constraints): Boolean {
        if (nullable != other.nullable)
            return false
        if (isSystemClass)
            return other.isSystemClass && systemClass == other.systemClass
        if (isLocalType)
            return other.isLocalType && localTypeName == other.localTypeName
        if (isString)
            return other.isString
        if (isInt)
            return other.isInt
        if (isLong)
            return other.isLong
        if (isDecimal)
            return other.isDecimal
        if (isBoolean)
            return other.isBoolean
        if (isArray)
            return other.isArray && uniqueItems == other.uniqueItems && sameTypes(this.arrayItems, other.arrayItems)
        return true
    }

    fun copyFrom(other: Constraints) {
        uri = other.uri
        objectValidationsPresent = other.objectValidationsPresent
        localTypeName = other.localTypeName
        isEnumClass = other.isEnumClass
        types.addAll(other.types)
        systemClass = other.systemClass
        nullable = other.nullable
        isRequired = other.isRequired
        defaultValue = other.defaultValue
        for (property in other.properties)
            properties.add(NamedConstraints(property.schema, property.name).also { it.copyFrom(property) })
        arrayItems = other.arrayItems?.let { Constraints(it.schema).also { a -> a.copyFrom(it) } }
        minItems = other.minItems
        maxItems = other.maxItems
        uniqueItems = other.uniqueItems
        oneOfSchemata = other.oneOfSchemata // ?
        minimum = other.minimum
        exclusiveMinimum = other.exclusiveMinimum
        maximum = other.maximum
        exclusiveMaximum = other.exclusiveMaximum
        multipleOf.addAll(other.multipleOf)
        maxLength = other.maxLength
        minLength = other.minLength
        format = other.format
        regex = other.regex
        enumValues = other.enumValues
        constValue = other.constValue
    }

    data class DefaultValue(val defaultValue: Any?, val type: JSONSchema.Type)

    companion object {

        fun Number.asLong(): Long = when (this) {
            is BigDecimal -> this.setScale(0, RoundingMode.DOWN).toLong()
            is Long -> this
            else -> this.toLong()
        }

        fun sameTypes(a: Constraints?, b: Constraints?): Boolean =
                a == null && b == null || a != null && b != null && a.sameType(b)

    }

}

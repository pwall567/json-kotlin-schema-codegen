/*
 * @(#) Constraints.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2023, 2024 Peter Wall
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

import io.kjson.JSONArray
import io.kjson.JSONNumber
import io.kjson.JSONString
import io.kjson.JSONValue

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGenerator.Companion.addOnce
import net.pwall.json.schema.validation.FormatValidator

open class Constraints(val schema: JSONSchema, val negated: Boolean = false) : Annotated() {

    @Suppress("unused")
    open val displayName: String
        get() = "value"

    @Suppress("unused")
    var uri: URI? = schema.uri

    var negatedConstraints: Constraints? = null

    var objectValidationsPresent: Boolean? = null

    var localType: ClassId? = null

    val isLocalType: Boolean
        get() = localType != null

    var isEnumClass = false

    val types = mutableListOf<JSONSchema.Type>()

    var systemClass: SystemClass? = null

    var nullable: Boolean? = null

    var isRequired = false

    var defaultValue: DefaultPropertyValue? = null

    var fieldAnnotated: Annotated? = null

    val properties = mutableListOf<NamedConstraints>()

    val patternProperties = mutableListOf<Triple<Regex, Constraints, Target.Static?>>()

    var additionalProperties: Constraints? = null

    @Suppress("unused")
    val additionalPropertiesFalse: Boolean
        get() = additionalProperties?.let { it.schema is JSONSchema.False } ?: false

    @Suppress("unused")
    val additionalPropertiesTrue: Boolean
        get() = additionalProperties?.let { it.schema is JSONSchema.True } ?: true

    @Suppress("unused")
    val additionalPropertiesSchema: Boolean
        get() = additionalProperties?.let { it.schema !is JSONSchema.True && it.schema !is JSONSchema.False } ?: false

    @Suppress("unused")
    val hasProperties: Boolean
        get() = properties.isNotEmpty()

    @Suppress("unused")
    val hasPatternProperties: Boolean
        get() = patternProperties.isNotEmpty()

    @Suppress("unused")
    val additionalPropertiesNeedsInit: Boolean
        get() {
            if (properties.isNotEmpty() || patternProperties.isNotEmpty() || minProperties != null ||
                    maxProperties != null)
                return true
            additionalProperties?.let {
                if (it.validations.isNotEmpty())
                    return true
            }
            return false
        }

    @Suppress("unused")
    val mapMayBeEmpty: Boolean
        get() = properties.all { it.nullable == true || it.defaultValue != null }

    @Suppress("unused")
    val numberOfProperties: Int
        get() = properties.size

    @Suppress("unused")
    val baseProperties: List<NamedConstraints>
        get() = properties.filter { it.baseProperty != null }

    @Suppress("unused")
    val nonBaseProperties: List<NamedConstraints>
        get() = properties.filter { it.baseProperty == null }

    var minProperties: Int? = null
    var maxProperties: Int? = null

    var extendedInDerived: Boolean = false
    var extendedFromBase: Boolean = false

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
    val format = mutableListOf<FormatValidator.FormatChecker>()
    val regex = mutableListOf<Regex>()

    var enumValues: JSONArray? = null
    var constValue: JSONValue? = null
    var extensibleEnum: Boolean = false

    @Suppress("MemberVisibilityCanBePrivate")
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

    val isArray: Boolean
        get() = isType(JSONSchema.Type.ARRAY) || types.isEmpty() && arrayItems != null // ???

    val isString: Boolean
        get() = isType(JSONSchema.Type.STRING) ||
                types.isEmpty() && properties.isEmpty() && arrayItems == null &&
                        (format.isNotEmpty() || regex.isNotEmpty() || maxLength != null || minLength != null ||
                                constValue is JSONString || enumImpliesString())

    val isBoolean: Boolean
        get() = isType(JSONSchema.Type.BOOLEAN)

    val isDecimal: Boolean
        get() = isType(JSONSchema.Type.NUMBER)

    val isInt: Boolean
        get() = isIntOrLong && (rangeImpliesInt() || constImpliesInt() || enumImpliesInt() || formatImpliesInt())

    val isLong: Boolean
        get() = isIntOrLong && !(rangeImpliesInt() || constImpliesInt() || enumImpliesInt() || formatImpliesInt())

    @Suppress("MemberVisibilityCanBePrivate")
    val isIntOrLong: Boolean
        get() = isType(JSONSchema.Type.INTEGER)

    @Suppress("MemberVisibilityCanBePrivate")
    val isPrimitive: Boolean
        get() = isIntOrLong || isBoolean

    val isUntyped: Boolean
        get() = !isSystemClass && !isLocalType && !isString && !isPrimitive && !isArray

    @Suppress("unused")
    val trace: String
        get() {
            return "" // this is here solely to allow "{{trace}}' elements in templates - breakpoint this line
        }

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

    var validations = mutableListOf<Validation>()

    fun addValidation(type: Validation.Type, value: Any? = null) {
        validations.addOnce(Validation(type, value, negated))
    }

    private fun isType(type: JSONSchema.Type): Boolean = types.size == 1 && types[0] == type

    private fun constImpliesInt(): Boolean = constValue?.let {
        it is JSONNumber && it.isInt()
    } ?: false

    private fun enumImpliesInt(): Boolean {
        enumValues?.let { array ->
            return array.all { it is JSONNumber && it.isInt() }
        }
        return false
    }

    private fun enumImpliesString(): Boolean {
        enumValues?.let { array ->
            return array.all { it is JSONString }
        }
        return false
    }

    private fun formatImpliesInt(): Boolean = format.contains(FormatValidator.Int32FormatChecker)

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
            return other.isLocalType && localType?.qualifiedClassName == other.localType?.qualifiedClassName
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
        other.negatedConstraints?.let {
            if (it.negated) {
                negatedConstraints = Constraints(other.schema, true).also { nc ->
                    nc.copyFrom(it)
                    nc.negatedConstraints = this
                }
            }
        }
        objectValidationsPresent = other.objectValidationsPresent
        localType = other.localType
        isEnumClass = other.isEnumClass
        types.addAll(other.types)
        systemClass = other.systemClass
        nullable = other.nullable
        isRequired = other.isRequired
        defaultValue = other.defaultValue
        for (property in other.properties)
            properties.add(NamedConstraints(property.schema, property.name).also { it.copyFrom(property) })
        for (patternPropertyPair in other.patternProperties)
            patternProperties.add(Triple(patternPropertyPair.first,
                    Constraints(patternPropertyPair.second.schema).also { it.copyFrom(patternPropertyPair.second) },
                    patternPropertyPair.third))
        additionalProperties = other.additionalProperties
        minProperties = other.minProperties
        maxProperties = other.maxProperties
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
        format.addAll(other.format)
        regex.addAll(other.regex)
        enumValues = other.enumValues
        constValue = other.constValue
        validations = mutableListOf<Validation>().also { it.addAll(other.validations) }
    }

    data class DefaultPropertyValue(val defaultValue: Any?, val type: JSONSchema.Type)

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

/*
 * @(#) Constraints.kt
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
import java.net.URI

import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONSequence
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
        get() = properties.filter { it.baseProperty }

    @Suppress("unused")
    val nonBaseProperties: List<NamedConstraints>
        get() = properties.filter { !it.baseProperty }

    val required = mutableListOf<String>()

    var arrayItems: Constraints? = null
    var minItems: Int? = null
    var maxItems: Int? = null

    var minimum: Number? = null // Number will be BigDecimal, Long or Int
    var exclusiveMinimum: Number? = null
    var maximum: Number? = null
    var exclusiveMaximum: Number? = null
    var multipleOf = mutableListOf<Number>()

    var maxLength: Int? = null
    var minLength: Int? = null
    var format: FormatValidator.FormatType? = null
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
        get() = types.size == 1 && types[0] == JSONSchema.Type.OBJECT || types.isEmpty() && properties.isNotEmpty() // ?

    @Suppress("unused")
    val isArray: Boolean
        get() = types.size == 1 && types[0] == JSONSchema.Type.ARRAY || types.isEmpty() && arrayItems != null // ???

    @Suppress("unused")
    val isString: Boolean
        get() = types.size == 1 && types[0] == JSONSchema.Type.STRING || types.isEmpty() && properties.isEmpty() &&
                arrayItems == null && (format != null || regex != null || maxLength != null || minLength != null) // ???

    @Suppress("unused")
    val isBoolean: Boolean
        get() = types.size == 1 && types[0] == JSONSchema.Type.BOOLEAN

    @Suppress("unused")
    val isDecimal: Boolean
        get() = types.size == 1 && types[0] == JSONSchema.Type.NUMBER && !isIntOrLong

    @Suppress("unused")
    val isInt: Boolean
        get() = isIntOrLong && (rangeImpliesInt() || constImpliesInt() || enumImpliesInt())

    @Suppress("unused")
    val isLong: Boolean
        get() = isIntOrLong && !(rangeImpliesInt() || constImpliesInt() || enumImpliesInt())

    @Suppress("unused")
    val isIntOrLong: Boolean
        get() = types.size == 1 && types[0] == JSONSchema.Type.INTEGER

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

    private fun constImpliesInt(): Boolean = constValue?.let {
        when (it) {
            is JSONInteger -> true
            is JSONLong -> it.get() in Int.MIN_VALUE..Int.MAX_VALUE
            is JSONDecimal -> it.get().asLong() in Int.MIN_VALUE..Int.MAX_VALUE
            else -> false
        }
    } ?: false

    private fun enumImpliesInt(): Boolean {
        enumValues?.let { array ->
            return array.all {
                when (it) {
                    is JSONInteger -> true
                    is JSONLong -> it.get() in Int.MIN_VALUE..Int.MAX_VALUE
                    is JSONDecimal -> it.get().asLong() in Int.MIN_VALUE..Int.MAX_VALUE
                    else -> false
                }
            }
        }
        return false
    }

    private fun rangeImpliesInt(): Boolean = minimumImpliesInt() && maximumImpliesInt()

    private fun minimumImpliesInt(): Boolean = minimumLong?.let { it >= Int.MIN_VALUE } ?: false

    private fun maximumImpliesInt(): Boolean = maximumLong?.let { it <= Int.MAX_VALUE } ?: false

    data class DefaultValue(val defaultValue: Any?, val type: JSONSchema.Type)

    companion object {

        fun Number.asLong(): Long = when (this) {
            is BigDecimal -> this.setScale(0, RoundingMode.DOWN).toLong()
            is Long -> this
            else -> this.toLong()
        }

    }

}

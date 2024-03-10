/*
 * @(#) Configurator.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2022, 2023, 2024 Peter Wall
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

import java.net.URI

import net.pwall.json.JSONBoolean
import net.pwall.json.JSONFunctions.displayString
import net.pwall.json.JSONMapping
import net.pwall.json.JSONSequence
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.pointer.JSONReference
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGenerator.Companion.fatal
import net.pwall.json.schema.output.BasicErrorEntry
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.mustache.Template

object Configurator {

    val extensionKeywordPattern = Regex("^x(-[A-Za-z0-9]+)+$")

    fun configure(generator: CodeGenerator, ref: JSONReference, uri: URI? = null) {
        // TODO validate against schema?
        val extensionValidators = mutableMapOf<String, MutableMap<String, CustomValidator>>()
        val nonStandardFormats = mutableMapOf<String, CustomFormat>()
        val parser = generator.schemaParser
        if (ref.value !is JSONMapping<*>)
            fatal("Config must be object")
        ref.ifPresent<JSONString>("title") {}
        ref.ifPresent<JSONString>("version") {}
        ref.ifPresent<JSONString>("description") {}
        ref.ifPresent<JSONString?>("packageName") {
            generator.basePackageName = it?.let { nonEmptyString(it) }
        }
        ref.ifPresent<JSONString?>("markerInterface") {
            generator.markerInterface = it?.let { ClassName.of(nonEmptyString(it)) }
        }
        ref.ifPresent<JSONString?>("generatorComment") {
            generator.generatorComment = it?.let { nonEmptyString(it) }
        }
        ref.ifPresent<JSONString?>("commentTemplate") {
            generator.commentTemplate = it?.let { Template.parse(nonEmptyString(it)) }
        }
        ref.ifPresent<JSONString>("targetLanguage") {
            generator.targetLanguage = when (it.value) {
                "kotlin" -> TargetLanguage.KOTLIN
                "java" -> TargetLanguage.JAVA
                "typescript" -> TargetLanguage.TYPESCRIPT
                else -> invalid(it)
            }
        }
        ref.ifPresent<JSONString>("nestedClassNameOption") {
            generator.nestedClassNameOption = when (it.value) {
                "property" -> CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
                "refSchema" -> CodeGenerator.NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA
                else -> invalid(it)
            }
        }
        ref.ifPresent<JSONString>("additionalPropertiesOption") {
            generator.additionalPropertiesOption = when (it.value) {
                "ignore" -> CodeGenerator.AdditionalPropertiesOption.IGNORE
                "strict" -> CodeGenerator.AdditionalPropertiesOption.STRICT
                else -> invalid(it)
            }
        }
        ref.ifPresent<JSONString>("examplesValidationOption") {
            generator.examplesValidationOption = when (it.value) {
                "none" -> {
                    parser.options.validateExamples = false
                    CodeGenerator.ValidationOption.NONE
                }
                "warn" -> {
                    parser.options.validateExamples = true
                    CodeGenerator.ValidationOption.WARN
                }
                "block" -> {
                    parser.options.validateExamples = true
                    CodeGenerator.ValidationOption.BLOCK
                }
                else -> {
                    invalid(it)
                }
            }
        }
        ref.ifPresent<JSONString>("defaultValidationOption") {
            generator.defaultValidationOption = when (it.value) {
                "none" -> {
                    parser.options.validateDefault = false
                    CodeGenerator.ValidationOption.NONE
                }
                "warn" -> {
                    parser.options.validateDefault = true
                    CodeGenerator.ValidationOption.WARN
                }
                "block" -> {
                    CodeGenerator.ValidationOption.BLOCK
                }
                else -> {
                    parser.options.validateDefault = true
                    invalid(it)
                }
            }
        }
        ref.ifPresent<JSONString>("extensibleEnumKeyword") {
            it.value.let { keyword ->
                if (!extensionKeywordPattern.containsMatchIn(keyword))
                    fatal("Illegal extension keyword at $pointer")
                generator.extensibleEnumKeyword = keyword
            }
        }
        ref.ifPresent<JSONBoolean>("derivePackageFromStructure") {
            generator.derivePackageFromStructure = it.value
        }
        ref.ifPresent<JSONMapping<*>>("extensionValidations") {
            forEachKey { ext ->
                if (ext.value !is JSONMapping<*>)
                    fatal("Config entry ${ext.pointer} invalid entry")
                ext.forEachKey {
                    if (it.value !is JSONMapping<*>)
                        fatal("Config entry ${it.pointer} invalid entry")
                    extensionValidators.getOrPut(ext.current) { mutableMapOf() }[it.current] =
                            CustomValidator(uri, it.pointer, it.current, parser.parseSchema(ref.base, it, uri))
                }
            }
        }
        ref.ifPresent<JSONMapping<*>>("nonStandardFormat") {
            forEachKey {
                if (it.value !is JSONMapping<*>)
                    fatal("Config entry ${it.pointer} invalid entry")
                nonStandardFormats[it.current] =
                        CustomFormat(uri, it.pointer, it.current, parser.parseSchema(ref.base, it, uri))
            }
        }
        ref.ifPresent<JSONMapping<*>>("customClasses") {
            ifPresent<JSONMapping<*>>("format") {
                forEachKey {
                    (it.value as? JSONString)?.let { s ->
                        generator.addCustomClassByFormat(it.current, it.nonEmptyString(s))
                    } ?: it.invalid(it.value)
                }
            }
            ifPresent<JSONMapping<*>>("uri") {
                forEachKey {
                    (it.value as? JSONString)?.let { s ->
                        generator.addCustomClassByURI(URI(it.current), it.nonEmptyString(s))
                    } ?: it.invalid(it.value)
                }
            }
            ifPresent<JSONMapping<*>>("extension") {
                forEachKey { ext ->
                    if (ext.value !is JSONMapping<*>)
                        fatal("Config entry ${ext.pointer} invalid entry")
                    ext.forEachKey {
                        (it.value as? JSONString)?.let { s ->
                            generator.addCustomClassByExtension(ext.current, it.current, it.nonEmptyString(s))
                        } ?: it.invalid(it.value)
                    }
                }
            }
            forEachKey {
                if (it.current !in listOf("format", "uri", "extension"))
                    fatal("Config entry ${it.pointer} unrecognised mapping type")
            }
        }
        ref.ifPresent<JSONString>("decimalClassName") {
            generator.decimalClass = ClassName.of(it.value)
        }
        ref.ifPresent<JSONMapping<*>>("classNames") {
            forEachKey {
                (it.value as? JSONString)?.let { s ->
                    generator.addClassNameMapping(URI(it.current), it.nonEmptyString(s))
                } ?: it.invalid(it.value)
            }
        }
        ref.ifPresent<JSONMapping<*>>("annotations") {
            ifPresent<JSONMapping<*>>("classes") {
                forEachKey { entry ->
                    when (val value = entry.value) {
                        null -> generator.addClassAnnotation(entry.current)
                        is JSONString -> generator.addClassAnnotation(entry.current, Template.parse(value.value))
                        else -> fatal("Config entry ${entry.pointer} invalid entry")
                    }
                }
            }
            ifPresent<JSONMapping<*>>("fields") {
                forEachKey { entry ->
                    when (val value = entry.value) {
                        null -> generator.addFieldAnnotation(entry.current)
                        is JSONString -> generator.addFieldAnnotation(entry.current, Template.parse(value.value))
                        else -> fatal("Config entry ${entry.pointer} invalid entry")
                    }
                }
            }
            forEachKey {
                if (it.current != "classes" && it.current != "fields")
                    fatal("Config entry ${it.pointer} unrecognised annotation type")
            }
        }
        ref.ifPresent<JSONValue>("companionObject") {
            when (it) {
                is JSONBoolean -> generator.companionObjectForAll = it.value
                is JSONSequence<*> -> {
                    generator.companionObjectForClasses.addAll(it.map { v ->
                        if (v !is JSONString)
                            fatal("Config entry companionObject invalid value")
                        v.value
                    })
                }
                else -> fatal("Config entry companionObject invalid value")
            }
        }
        if (extensionValidators.isNotEmpty()) {
            parser.customValidationHandler = { key, _, _, value ->
                extensionValidators[key]?.get((value as? JSONString)?.value)
            }
        }
        if (nonStandardFormats.isNotEmpty()) {
            parser.nonstandardFormatHandler = { keyword ->
                nonStandardFormats[keyword]?.let { FormatValidator.DelegatingFormatChecker(keyword, it) }
            }
        }
    }

    private fun JSONReference.forEachKey(block: (JSONReference) -> Unit) {
        value.let {
            if (it !is JSONMapping<*>)
                fatal("Config entry ${this.pointer} must be object - ${it.displayValue()}")
            for (key in it.keys)
                block(child(key))
        }
    }

    private inline fun <reified T : JSONValue?> JSONReference.ifPresent(
        name: String,
        block: JSONReference.(T) -> Unit
    ): ConditionDSL {
        child(name).let {
            if (it.isValid) {
                it.checkType<T, Unit>(it.value) { v ->
                    it.block(v)
                    return ConditionDSL(this@ifPresent, true)
                }
            }
        }
        return ConditionDSL(this@ifPresent, false)
    }

    class ConditionDSL(val ref: JSONReference, private val condition: Boolean) {

        fun andAlso(block: JSONReference.() -> Unit): ConditionDSL {
            if (condition)
                ref.block()
            return this
        }

        fun orElse(block: JSONReference.() -> Unit): ConditionDSL {
            if (!condition)
                ref.block()
            return ConditionDSL(ref, !condition)
        }

    }

    private inline fun <reified T : JSONValue?, V> JSONReference.checkType(
        value: JSONValue?,
        block: JSONReference.(T) -> V
    ): V = when (value) {
        is T -> block(value)
        else -> fatal("Config entry $pointer incorrect type - ${value.displayValue()}")
    }

    private fun JSONReference.nonEmptyString(s: JSONString): String = s.value.takeIf { it.isNotEmpty() } ?:
            fatal("Config entry $pointer must not be empty")

    private fun JSONValue?.displayValue(): String {
        return when (this) {
            null -> "null"
            is JSONString -> displayString(value, 21)
            is JSONSequence<*> -> when (size) {
                0 -> "[]"
                1 -> "[${this[0].displayValue()}]"
                else -> "[...]"
            }
            is JSONMapping<*> -> when (size) {
                0 -> "{}"
                1 -> entries.iterator().next().let { "{${displayString(it.key, 21)}:${it.value.displayValue()}}" }
                else -> "{...}"
            }
            else -> toString()
        }
    }

    private fun JSONReference.invalid(value: JSONValue?): Nothing {
        fatal("Config entry $pointer invalid - ${value.displayValue()}")
    }

    class CustomValidator(uri: URI?, location: JSONPointer, val name: String, val schema: JSONSchema) :
            JSONSchema.Validator(uri, location) {

        override fun getErrorEntry(
            relativeLocation: JSONPointer,
            json: JSONValue?,
            instanceLocation: JSONPointer,
        ): BasicErrorEntry? {
            if (schema is Validator)
                return schema.getErrorEntry(relativeLocation.child(name), json, instanceLocation)
            val basicOutput = schema.validateBasic(relativeLocation.child(name), json, instanceLocation)
            return basicOutput.errors?.let {
                it.firstOrNull { e -> e.error != subSchemaErrorMessage }
            }
        }

        override fun validate(json: JSONValue?, instanceLocation: JSONPointer): Boolean =
            schema.validate(json, instanceLocation)

    }

    class CustomFormat(uri: URI?, location: JSONPointer, val name: String, val schema: JSONSchema) :
            JSONSchema.Validator(uri, location) {

        override fun getErrorEntry(
            relativeLocation: JSONPointer,
            json: JSONValue?,
            instanceLocation: JSONPointer,
        ): BasicErrorEntry? {
            if (schema is Validator)
                return schema.getErrorEntry(relativeLocation, json, instanceLocation)
            val basicOutput = schema.validateBasic(relativeLocation, json, instanceLocation)
            return basicOutput.errors?.let {
                it.firstOrNull { e -> e.error != subSchemaErrorMessage }
            }
        }

        override fun validate(json: JSONValue?, instanceLocation: JSONPointer): Boolean =
            schema.validate(json, instanceLocation)

    }

}

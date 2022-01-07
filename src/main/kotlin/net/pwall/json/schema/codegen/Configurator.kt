/*
 * @(#) Configurator.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2021, 2022 Peter Wall
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
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.mustache.Template

object Configurator {

    fun configure(generator: CodeGenerator, ref: JSONReference, uri: URI? = null) {
        // TODO validate against schema?
        val extensionValidators = mutableMapOf<String, MutableMap<String, CustomValidator>>()
        val nonStandardFormats = mutableMapOf<String, CustomValidator>()
        val parser = generator.schemaParser ?: Parser().also { generator.schemaParser = it }
        if (ref.value !is JSONMapping<*>)
            fatal("Config must be object")
        ref.ifPresent<JSONString>("title") {}
        ref.ifPresent<JSONString>("version") {}
        ref.ifPresent<JSONString>("description") {}
        ref.ifPresent<JSONString?>("packageName") {
            generator.basePackageName = it?.let { nonEmptyString(it) }
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
                        CustomValidator(uri, it.pointer, parser.parseSchema(ref.base, it, null))
                }
            }
        }
        ref.ifPresent<JSONMapping<*>>("nonStandardFormat") {
            forEachKey {
                if (it.value !is JSONMapping<*>)
                    fatal("Config entry ${it.pointer} invalid entry")
                nonStandardFormats[it.current] =
                        CustomValidator(uri, it.pointer, parser.parseSchema(ref.base, it, null))
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
        ref.ifPresent<JSONMapping<*>>("classNames") {
            forEachKey {
                (it.value as? JSONString)?.let { s ->
                    generator.addClassNameMapping(URI(it.current), it.nonEmptyString(s))
                } ?: it.invalid(it.value)
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

    private inline fun <reified T: JSONValue?> JSONReference.ifPresent(
        name: String,
        block: JSONReference.(T) -> Unit
    ) {
        child(name).let {
            if (it.isValid)
                it.checkType<T, Unit>(it.value) { v -> it.block(v) }
        }
    }

    private inline fun <reified T: JSONValue?, V> JSONReference.checkType(
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

    class CustomValidator(uri: URI?, location: JSONPointer, val schema: JSONSchema) :
            JSONSchema.Validator(uri, location) {

        override fun getErrorEntry(
            relativeLocation: JSONPointer,
            json: JSONValue?,
            instanceLocation: JSONPointer,
        ): BasicErrorEntry? {
            fatal("Config error - validation for code generation only")
        }

        override fun validate(json: JSONValue?, instanceLocation: JSONPointer): Boolean {
            fatal("Config error - validation for code generation only")
        }

    }

}

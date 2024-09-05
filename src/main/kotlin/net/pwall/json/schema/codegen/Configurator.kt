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

import io.kjson.JSON.displayValue
import io.kjson.JSON.typeError
import io.kjson.JSONArray
import io.kjson.JSONBoolean
import io.kjson.JSONObject
import io.kjson.JSONString
import io.kjson.JSONValue
import io.kjson.mustache.Template
import io.kjson.pointer.JSONPointer
import io.kjson.pointer.JSONRef
import io.kjson.pointer.forEachKey
import io.kjson.pointer.optionalChild
import io.kjson.pointer.withOptionalChild

import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.codegen.CodeGenerator.Companion.fatal
import net.pwall.json.schema.output.BasicErrorEntry
import net.pwall.json.schema.validation.FormatValidator

object Configurator {

    val extensionKeywordPattern = Regex("^x(-[A-Za-z0-9]+)+$")

    fun configure(generator: CodeGenerator, ref: JSONRef<JSONObject>, uri: URI? = null) {
        // TODO validate against schema?
        val extensionValidators = mutableMapOf<String, MutableMap<String, CustomValidator>>()
        val nonStandardFormats = mutableMapOf<String, CustomFormat>()
        val parser = generator.schemaParser
        ref.optionalChild<JSONString>("title")?.nonEmptyString()
        ref.optionalChild<JSONString>("version")?.nonEmptyString()
        ref.optionalChild<JSONString>("description")?.nonEmptyString()
        ref.withOptionalChild<JSONString?>("packageName") {
            generator.basePackageName = optionalNonEmptyString()
        }
        ref.withOptionalChild<JSONString?>("markerInterface") {
            generator.markerInterface = optionalNonEmptyString()?.let { ClassName.of(it) }
        }
        ref.withOptionalChild<JSONString?>("generatorComment") {
            generator.generatorComment = optionalNonEmptyString()
        }
        ref.withOptionalChild<JSONString?>("commentTemplate") {
            generator.commentTemplate = optionalNonEmptyString()?.let { Template.parse(it) }
        }
        ref.withOptionalChild<JSONString>("targetLanguage") {
            generator.targetLanguage = when (it.value) {
                "kotlin" -> TargetLanguage.KOTLIN
                "java" -> TargetLanguage.JAVA
                "typescript" -> TargetLanguage.TYPESCRIPT
                else -> invalid()
            }
        }
        ref.withOptionalChild<JSONString>("nestedClassNameOption") {
            generator.nestedClassNameOption = when (it.value) {
                "property" -> CodeGenerator.NestedClassNameOption.USE_NAME_FROM_PROPERTY
                "refSchema" -> CodeGenerator.NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA
                else -> invalid()
            }
        }
        ref.withOptionalChild<JSONString>("additionalPropertiesOption") {
            generator.additionalPropertiesOption = when (it.value) {
                "ignore" -> CodeGenerator.AdditionalPropertiesOption.IGNORE
                "strict" -> CodeGenerator.AdditionalPropertiesOption.STRICT
                else -> invalid()
            }
        }
        ref.withOptionalChild<JSONString>("examplesValidationOption") {
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
                else -> invalid()
            }
        }
        ref.withOptionalChild<JSONString>("defaultValidationOption") {
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
                    invalid()
                }
            }
        }
        ref.withOptionalChild<JSONString>("extensibleEnumKeyword") {
            it.value.let { keyword ->
                if (!extensionKeywordPattern.containsMatchIn(keyword))
                    fatal("Illegal extension keyword at $pointer")
                generator.extensibleEnumKeyword = keyword
            }
        }
        ref.withOptionalChild<JSONBoolean>("derivePackageFromStructure") {
            generator.derivePackageFromStructure = it.value
        }
        ref.withOptionalChild<JSONObject>("extensionValidations") {
            forEachKey<JSONObject> { ext ->
                forEachKey<JSONObject> {
                    extensionValidators.getOrPut(ext) { mutableMapOf() }[it] =
                            CustomValidator(uri, pointer, it, parser.parseSchema(base!!, pointer, uri))
                }
            }
        }
        ref.withOptionalChild<JSONObject>("nonStandardFormat") {
            forEachKey<JSONObject> {
                nonStandardFormats[it] = CustomFormat(uri, pointer, it, parser.parseSchema(base!!, pointer, uri))
            }
        }
        ref.withOptionalChild<JSONObject>("customClasses") {
            withOptionalChild<JSONObject>("format") {
                forEachKey<JSONString> {
                    generator.addCustomClassByFormat(it, nonEmptyString())
                }
            }
            withOptionalChild<JSONObject>("uri") {
                forEachKey<JSONString> {
                    generator.addCustomClassByURI(URI(it), nonEmptyString())
                }
            }
            withOptionalChild<JSONObject>("extension") {
                forEachKey<JSONObject> { ext ->
                    forEachKey<JSONString> {
                        generator.addCustomClassByExtension(ext, it, nonEmptyString())
                    }
                }
            }
            forEachKey<JSONValue?> {
                if (it !in listOf("format", "uri", "extension"))
                    fatal("Config item $pointer unrecognised mapping type")
            }
        }
        ref.withOptionalChild<JSONString>("decimalClassName") {
            generator.decimalClass = ClassName.of(nonEmptyString())
        }
        ref.withOptionalChild<JSONObject>("classNames") {
            forEachKey<JSONString> {
                generator.addClassNameMapping(URI(it), nonEmptyString())
            }
        }
        ref.withOptionalChild<JSONObject>("annotations") {
            withOptionalChild<JSONObject>("classes") {
                forEachKey<JSONString?> { entry ->
                    if (node != null)
                        generator.addClassAnnotation(entry, Template.parse(asRef<JSONString>().nonEmptyString()))
                    else
                        generator.addClassAnnotation(entry)
                }
            }
            withOptionalChild<JSONObject>("fields") {
                forEachKey<JSONString?> { entry ->
                    if (node != null)
                        generator.addFieldAnnotation(entry, Template.parse(asRef<JSONString>().nonEmptyString()))
                    else
                        generator.addFieldAnnotation(entry)
                }
            }
            forEachKey<JSONValue?> {
                if (it != "classes" && it != "fields")
                    fatal("Config item $pointer unrecognised annotation type")
            }
        }
        ref.withOptionalChild<JSONValue>("companionObject") {
            when (it) {
                is JSONBoolean -> generator.companionObjectForAll = it.value
                is JSONArray -> {
                    generator.companionObjectForClasses.addAll(it.map { v ->
                        if (v !is JSONString)
                            fatal("Config item companionObject invalid value")
                        v.value
                    })
                }
                else -> fatal("Config item companionObject invalid value")
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

    private fun JSONRef<JSONString?>.optionalNonEmptyString(): String? = node?.let {
        if (it.value.isEmpty())
            it.typeError("non-empty string", pointer, "Config item")
        it.value
    }

    private fun JSONRef<JSONString>.nonEmptyString(): String = node.let {
        if (it.value.isEmpty())
            it.typeError("non-empty string", pointer, "Config item")
        it.value
    }

    private fun JSONRef<JSONValue?>.invalid(): Nothing {
        fatal("Config item $pointer invalid - ${node.displayValue()}")
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

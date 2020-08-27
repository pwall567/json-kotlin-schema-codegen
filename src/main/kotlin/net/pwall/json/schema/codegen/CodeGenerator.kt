/*
 * @(#) CodeGenerator.kt
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

import net.pwall.json.JSONArray
import net.pwall.json.JSONBoolean
import net.pwall.json.JSONInteger
import net.pwall.json.JSONObject
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.subschema.CombinationSchema
import net.pwall.json.schema.subschema.ItemSchema
import net.pwall.json.schema.subschema.PropertySchema
import net.pwall.json.schema.subschema.RefSchema
import net.pwall.json.schema.subschema.RequiredSchema
import net.pwall.json.schema.validation.ArrayValidator
import net.pwall.json.schema.validation.ConstValidator
import net.pwall.json.schema.validation.DefaultValidator
import net.pwall.json.schema.validation.EnumValidator
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.json.schema.validation.NumberValidator
import net.pwall.json.schema.validation.PatternValidator
import net.pwall.json.schema.validation.StringValidator
import net.pwall.json.schema.validation.TypeValidator
import net.pwall.mustache.Template
import net.pwall.util.Java
import net.pwall.util.Strings

class CodeGenerator(
        var templates: String = "kotlin",
        var suffix: String = "kt",
        var basePackageName: String? = null,
        var baseDirectoryName: String = ".",
        var derivePackageFromStructure: Boolean = true
) {

    var generatedClassNumber = 0

    var schemaParser: Parser? = null

    private val defaultSchemaParser: Parser by lazy {
        Parser()
    }

    private val actualSchemaParser: Parser
        get() = schemaParser ?: defaultSchemaParser

    var templateParser: net.pwall.mustache.parser.Parser? = null

    private val defaultTemplateParser: net.pwall.mustache.parser.Parser by lazy {
        net.pwall.mustache.parser.Parser().also {
            it.resolvePartial = { name ->
                CodeGenerator::class.java.getResourceAsStream("/$templates/$name.mustache").reader()
            }
        }
    }

    private val actualTemplateParser: net.pwall.mustache.parser.Parser
        get() = templateParser ?: defaultTemplateParser

    var template: Template? = null

    private val defaultTemplate: Template by lazy {
        actualTemplateParser.parse(actualTemplateParser.resolvePartial("class"))
    }

    private val actualTemplate: Template
        get() = template ?: defaultTemplate

    var outputResolver: OutputResolver? = null

    private val defaultOutputResolver: OutputResolver = { baseDirectory, subDirectories, className, suffix ->
        var dir = checkDirectory(File(baseDirectory), "output directory")
        subDirectories.forEach { dir = checkDirectory(File(dir, it), "output directory") }
        File(dir, "$className.$suffix").writer()
    }

    private val actualOutputResolver: OutputResolver
        get() = outputResolver ?: defaultOutputResolver

    fun setTemplateDirectory(directory: File, suffix: String = "mustache") {
        when {
            directory.isFile -> throw JSONSchemaException("Template directory must be a directory")
            directory.isDirectory -> {}
            else -> throw JSONSchemaException("Error accessing template directory")
        }
        templateParser = net.pwall.mustache.parser.Parser().also {
            it.resolvePartial = { name ->
                File(directory, "$name.$suffix").reader()
            }
        }
    }

    fun generate(inputDir: File, subDirectories: List<String> = emptyList()) {
        val parser = actualSchemaParser
        parser.preLoad(inputDir)
        when {
            inputDir.isFile -> generate(parser.parse(inputDir), subDirectories)
            inputDir.isDirectory -> generate(parser, inputDir, subDirectories)
        }
    }

    private fun generate(parser: Parser, inputDir: File, subDirectories: List<String>) {
        inputDir.listFiles()?.forEach {
            when {
                it.isDirectory -> { generate(parser, it, subDirectories + it.name.mapDirectoryName()) }
                it.isFile -> { generate(parser.parse(it), subDirectories) }
            }
        }
    }

    private fun String.mapDirectoryName(): String = StringBuilder().also {
        for (ch in this)
            if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9')
                it.append(ch)
    }.toString()

    fun generate(schema: JSONSchema, subDirectories: List<String>) {
        process(schema)?.let { constraints ->
            var packageName = basePackageName
            if (derivePackageFromStructure)
                subDirectories.forEach { packageName = if (packageName.isNullOrEmpty()) it else "$packageName.$it" }
            constraints.packageName = packageName
            constraints.description = schema.description
            val className = constraints.nameFromURI ?: "GeneratedClass${++generatedClassNumber}"
            actualOutputResolver(baseDirectoryName, subDirectories, className, suffix).use {
                actualTemplate.processTo(it, constraints)
            }
        }
    }

    private fun process(schema: JSONSchema): Constraints? {
        val constraints = Constraints(schema)
        processSchema(schema, constraints)
        if (!constraints.isObject)
            return null
        analyseConstraints(constraints, constraints)
        constraints.systemClasses.sortBy { it.order }
        constraints.imports.sort()
        return constraints
    }

    private fun analyseConstraints(parentConstraints: Constraints, constraints: Constraints) {
        constraints.properties.forEach { property ->
            if (property.isObject) {
                // TODO - if the first item in an object is "allOf", consider using it as a base class
                // check whether it has a "$id" to use as base class name (mapper to map id to classname?)
                // TODO - how do we handle nested classes?
                // answer - we always generate as nested classes for now, look at alternatives later
                val refChild = property.schema.findRefChild()
                // if refChild is a class being generated, use it
                val innerClassName = property.capitalisedName
                property.localTypeName = parentConstraints.addNestedClass(property, innerClassName)
                analyseConstraints(parentConstraints, property)
            }
            if (property.isArray) {
                parentConstraints.systemClasses.addOnce(Constraints.SystemClass.LIST)
                property.arrayItems?.let {
                    analyseConstraints(parentConstraints, it)
                    if (it.isObject) {
                        // what do we use as inner class name?
                        val innerClassName = it.schema.findRefChild()?.let { ref ->
                            ref.fragment?.substringAfterLast('/')?.let { s -> Strings.capitalise(s) }
                        } ?: Strings.capitalise(property.name.depluralise())
                        it.localTypeName = parentConstraints.addNestedClass(it, innerClassName)
                    }
                }
                if (property.maxItems != null || property.minItems != null)
                    constraints.validationsPresent = true
            }
            if (property.isInt) {
                if (property.maximum != null || property.exclusiveMaximum != null || property.minimum != null ||
                        property.exclusiveMaximum != null || property.multipleOf != null)
                    constraints.validationsPresent = true
            }
            if (property.isString) {
                when (property.format) {
                    FormatValidator.FormatType.EMAIL,
                    FormatValidator.FormatType.HOSTNAME -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.VALIDATION)
                        constraints.validationsPresent = true
                    }
                    FormatValidator.FormatType.DATE_TIME -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.DATE_TIME)
                        property.systemClass = Constraints.SystemClass.DATE_TIME
                    }
                    FormatValidator.FormatType.DATE -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.DATE)
                        property.systemClass = Constraints.SystemClass.DATE
                    }
                    FormatValidator.FormatType.TIME -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.TIME)
                        property.systemClass = Constraints.SystemClass.TIME
                    }
                    FormatValidator.FormatType.DURATION -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.DURATION)
                        property.systemClass = Constraints.SystemClass.DURATION
                    }
                    FormatValidator.FormatType.UUID -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.UUID)
                        property.systemClass = Constraints.SystemClass.UUID
                    }
                    FormatValidator.FormatType.URI, FormatValidator.FormatType.URI_REFERENCE -> {
                        parentConstraints.systemClasses.addOnce(Constraints.SystemClass.URI)
                        property.systemClass = Constraints.SystemClass.URI
                    }
                    else -> {}
                }
                if (property.enumValues != null || property.constValue != null) {
                    // TODO ...
                    // should enums (and const) cause an enum class to be created? or a string with a value check?
                }
                if (property.regex != null) {
                    constraints.validationsPresent = true
                    parentConstraints.systemClasses.addOnce(Constraints.SystemClass.REGEX)
                    parentConstraints.statics.find {
                        it.type == Constraints.StaticType.PATTERN && it.value == property.regex.toString()
                    }?.let { entry ->
                        property.regexStaticName = entry.staticName
                    } ?: "cg_regex${parentConstraints.statics.size}".let {
                            parentConstraints.statics.add(Constraints.Static(Constraints.StaticType.PATTERN, it,
                                    Java.quote(property.regex.toString())))
                            property.regexStaticName = it
                        }
                }
            }
            if (property.isDecimal) {
                parentConstraints.systemClasses.addOnce(Constraints.SystemClass.DECIMAL)
                property.systemClass = Constraints.SystemClass.DECIMAL
            }
            // TODO - more validation types (enum, const)
            when {
                property.name in constraints.required -> property.isRequired = true
                property.nullable == true || property.defaultValue != null -> {}
                else -> property.nullable = true // should be error, but that would be unhelpful
            }
        }
    }

    private fun Constraints.addNestedClass(constraints: Constraints, innerClassName: String): String {
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
        nestedClasses.add(NestedClass(constraints, actualInnerClassName))
        return actualInnerClassName
    }

    private fun String.depluralise(): String = when {
        this.endsWith("es") -> dropLast(2)
        this.endsWith('s') -> dropLast(1)
        else -> this
    }

    private fun JSONSchema.findRefChild(): RefSchema? =
            ((this as? JSONSchema.General)?.children?.find { it is RefSchema }) as RefSchema?

    private fun <T: Any> MutableList<T>.addOnce(entry: T) {
        if (entry !in this)
            add(entry)
    }

    private fun processSchema(schema: JSONSchema, constraints: Constraints) {
        when (schema) {
            is JSONSchema.True -> throw JSONSchemaException("Can't generate code for \"true\" schema")
            is JSONSchema.False -> throw JSONSchemaException("Can't generate code for \"false\" schema")
            is JSONSchema.Not -> throw JSONSchemaException("Can't generate code for \"not\" schema")
            is JSONSchema.SubSchema -> processSubSchema(schema, constraints)
            is JSONSchema.Validator -> processValidator(schema, constraints)
            is JSONSchema.General -> processGeneral(schema, constraints)
        }
    }

    private fun processDefaultValue(value: JSONValue?): Constraints.DefaultValue =
            when (value) {
                null -> Constraints.DefaultValue(null, JSONSchema.Type.NULL)
                is JSONInteger -> Constraints.DefaultValue(value.get(), JSONSchema.Type.INTEGER)
                is JSONString -> Constraints.DefaultValue(value.toJSON(), JSONSchema.Type.STRING)
                is JSONBoolean -> Constraints.DefaultValue(value.get(), JSONSchema.Type.BOOLEAN)
                is JSONArray -> Constraints.DefaultValue(value.map { processDefaultValue(it) }, JSONSchema.Type.ARRAY)
                is JSONObject -> throw JSONSchemaException("Can't handle object as default value")
                else -> throw JSONSchemaException("Unexpected default value")
            }

    private fun processSubSchema(subSchema: JSONSchema.SubSchema, constraints: Constraints) {
        when (subSchema) {
            is CombinationSchema -> processCombinationSchema(subSchema, constraints)
            is ItemSchema -> processSchema (subSchema.itemSchema,
                    constraints.arrayItems ?: Constraints(subSchema.itemSchema).also { constraints.arrayItems = it })
            is PropertySchema -> processPropertySchema(subSchema, constraints)
            is RefSchema -> processSchema(subSchema.target, constraints)
            is RequiredSchema -> subSchema.properties.forEach {
                    if (it !in constraints.required) constraints.required.add(it) }
        }
    }

    private fun processCombinationSchema(combinationSchema: CombinationSchema, constraints: Constraints) {
        if (combinationSchema.name != "allOf") // can only handle allOf currently
            throw JSONSchemaException("Can't generate code for \"${combinationSchema.name}\" schema")
        combinationSchema.array.forEach { processSchema(it, constraints) }
    }

    private fun processValidator(validator: JSONSchema.Validator, constraints: Constraints) {
        when (validator) {
            is DefaultValidator -> constraints.defaultValue = processDefaultValue(validator.value)
            is ConstValidator -> processConstValidator(validator, constraints)
            is EnumValidator -> processEnumValidator(validator, constraints)
            is FormatValidator -> processFormatValidator(validator, constraints)
            is NumberValidator -> processNumberValidator(validator, constraints)
            is PatternValidator -> processPatternValidator(validator, constraints)
            is StringValidator -> processStringValidator(validator, constraints)
            is TypeValidator -> processTypeValidator(validator, constraints)
            is ArrayValidator -> processArrayValidator(validator, constraints)
        }
    }

    private fun processConstValidator(constValidator: ConstValidator, constraints: Constraints) {
        if (constraints.constValue != null)
            throw JSONSchemaException("Duplicate const")
        constraints.constValue = constValidator.value
    }

    private fun processEnumValidator(enumValidator: EnumValidator, constraints: Constraints) {
        if (constraints.enumValues != null)
            throw JSONSchemaException("Duplicate enum")
        constraints.enumValues = enumValidator.array
    }

    private fun processFormatValidator(formatValidator: FormatValidator, constraints: Constraints) {
        if (constraints.format != null)
            throw JSONSchemaException("Duplicate format")
        constraints.format = formatValidator.type
    }

    private fun processNumberValidator(numberValidator: NumberValidator, constraints: Constraints) {
        when (numberValidator.condition) {
            NumberValidator.ValidationType.MULTIPLE_OF -> constraints.multipleOf =
                    Constraints.lcm(constraints.multipleOf, numberValidator.value)
            NumberValidator.ValidationType.MINIMUM -> constraints.minimum =
                    Constraints.maximumOf(constraints.minimum, numberValidator.value)
            NumberValidator.ValidationType.EXCLUSIVE_MINIMUM -> constraints.exclusiveMinimum =
                    Constraints.maximumOf(constraints.exclusiveMinimum, numberValidator.value)
            NumberValidator.ValidationType.MAXIMUM -> constraints.maximum =
                    Constraints.minimumOf(constraints.maximum, numberValidator.value)
            NumberValidator.ValidationType.EXCLUSIVE_MAXIMUM -> constraints.exclusiveMaximum =
                    Constraints.minimumOf(constraints.exclusiveMaximum, numberValidator.value)
        }
    }

    private fun processArrayValidator(arrayValidator: ArrayValidator, constraints: Constraints) {
        when (arrayValidator.condition) {
            ArrayValidator.ValidationType.MAX_ITEMS -> constraints.maxItems =
                    Constraints.minimumOf(constraints.maxItems, arrayValidator.value)?.toInt()
            ArrayValidator.ValidationType.MIN_ITEMS -> constraints.minItems =
                    Constraints.maximumOf(constraints.minLength, arrayValidator.value)?.toInt()
        }
    }

    private fun processPatternValidator(patternValidator: PatternValidator, constraints: Constraints) {
        if (constraints.regex != null)
            throw JSONSchemaException("Duplicate pattern")
        constraints.regex = patternValidator.regex
    }

    private fun processStringValidator(stringValidator: StringValidator, constraints: Constraints) {
        when (stringValidator.condition) {
            StringValidator.ValidationType.MAX_LENGTH -> constraints.maxLength =
                    Constraints.minimumOf(constraints.maxLength, stringValidator.value)?.toInt()
            StringValidator.ValidationType.MIN_LENGTH -> constraints.minLength =
                    Constraints.maximumOf(constraints.minLength, stringValidator.value)?.toInt()
        }
    }

    private fun processPropertySchema(propertySchema: PropertySchema, constraints: Constraints) {
        propertySchema.properties.forEach { (name, schema) ->
            val propertyConstraints = constraints.properties.find { it.name == name } ?:
                    NamedConstraints(schema, name).also { constraints.properties.add(it) }
            processSchema(schema, propertyConstraints)
        }
    }

    private fun processTypeValidator(typeValidator: TypeValidator, constraints: Constraints) {
        typeValidator.types.forEach {
            when (it) {
                JSONSchema.Type.NULL -> constraints.nullable = true
                in constraints.types -> {}
                else -> constraints.types.add(it)
            }
        }
    }

    private fun processGeneral(general: JSONSchema.General, constraints: Constraints) {
        general.children.forEach { processSchema(it, constraints) }
    }

    companion object {

        private fun checkDirectory(directory: File, name: String): File {
            when {
                !directory.exists() -> {
                    if (!directory.mkdirs())
                        throw JSONSchemaException("Error creating $name - $directory")
                }
                directory.isDirectory -> {}
                directory.isFile -> throw JSONSchemaException("File given for $name - $directory")
                else -> throw JSONSchemaException("Error accessing $name - $directory")
            }
            return directory
        }

    }

}

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
import java.math.BigDecimal

import net.pwall.json.JSONArray
import net.pwall.json.JSONBoolean
import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONNumberValue
import net.pwall.json.JSONObject
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.codegen.Constraints.Companion.asLong
import net.pwall.json.schema.codegen.log.Log
import net.pwall.json.schema.codegen.log.NullLog
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.subschema.AllOfSchema
import net.pwall.json.schema.subschema.CombinationSchema
import net.pwall.json.schema.subschema.ItemsSchema
import net.pwall.json.schema.subschema.PropertiesSchema
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
import net.pwall.mustache.parser.Parser as MustacheParser
import net.pwall.util.Strings

class CodeGenerator(
        var templates: String = "kotlin",
        var suffix: String = "kt",
        var templateName: String = "class",
        var enumTemplateName: String = "enum",
        var basePackageName: String? = null,
        var baseDirectoryName: String = ".",
        var derivePackageFromStructure: Boolean = true,
        val log: Log = NullLog
) {

    var schemaParser: Parser? = null

    private val defaultSchemaParser: Parser by lazy {
        Parser()
    }

    private val actualSchemaParser: Parser
        get() = schemaParser ?: defaultSchemaParser

    var templateParser: MustacheParser? = null

    private val defaultTemplateParser: MustacheParser by lazy {
        MustacheParser().also {
            it.resolvePartial = { name ->
                CodeGenerator::class.java.getResourceAsStream("/$templates/$name.mustache").reader()
            }
        }
    }

    private val actualTemplateParser: MustacheParser
        get() = templateParser ?: defaultTemplateParser

    var template: Template? = null

    private val defaultTemplate: Template by lazy {
        actualTemplateParser.parse(actualTemplateParser.resolvePartial(templateName))
    }

    private val actualTemplate: Template
        get() = template ?: defaultTemplate

    var enumTemplate: Template? = null

    private val defaultEnumTemplate: Template by lazy {
        actualTemplateParser.parse(actualTemplateParser.resolvePartial(enumTemplateName))
    }

    private val actualEnumTemplate: Template
        get() = enumTemplate ?: defaultEnumTemplate

    var outputResolver: OutputResolver? = null

    private val defaultOutputResolver: OutputResolver = { baseDirectory, subDirectories, className, suffix ->
        var dir = checkDirectory(File(baseDirectory))
        subDirectories.forEach { dir = checkDirectory(File(dir, it)) }
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
        templateParser = MustacheParser().also {
            it.resolvePartial = { name ->
                File(directory, "$name.$suffix").reader()
            }
        }
    }

    fun generate(vararg inputFiles: File) {
        generate(inputFiles.asList())
    }

    fun generate(inputFiles: List<File>) {
        val targets = mutableListOf<Target>()
        val parser = actualSchemaParser
        for (inputFile in inputFiles) {
            parser.preLoad(inputFile)
            when {
                inputFile.isFile -> addTarget(targets, emptyList(), inputFile)
                inputFile.isDirectory -> addTargets(targets, emptyList(), inputFile)
            }
        }
        // generate all targets
        for (target in targets) {
            processSchema(target.schema, target.constraints)
            log.info { "Generating for target ${target.file}" }
            when {
                target.constraints.isObject -> { // does it look like an object? generate a class
                    log.info { "-- target class ${target.qualifiedClassName}" }
                    target.validationsPresent = analyseObject(target, target.constraints, targets)
                    target.systemClasses.sortBy { it.order }
                    target.imports.sort()
                    actualOutputResolver(baseDirectoryName, target.subDirectories, target.className,
                            target.suffix).use {
                        actualTemplate.processTo(it, target)
                    }
                }
                target.constraints.isString && target.constraints.enumValues != null -> {
                    actualOutputResolver(baseDirectoryName, target.subDirectories, target.className,
                            target.suffix).use {
                        actualEnumTemplate.processTo(it, target)
                    }
                }
                // TODO - generate other types of output (other than object) - enum?
                else -> log.info { "-- nothing to generate" }
            }
        }
        // TODO - generate index - for html
    }

    // TODO - how do we allow for "domain primitives" like Amount etc.?

    private fun addTarget(targets: MutableList<Target>, subDirectories: List<String>, inputFile: File) {
        var packageName = basePackageName
        if (derivePackageFromStructure)
            subDirectories.forEach { packageName = if (packageName.isNullOrEmpty()) it else "$packageName.$it" }
        val parser = actualSchemaParser
        val schema = parser.parse(inputFile)
        val className = schema.uri?.let {
            // TODO change to allow name ending with "/schema"?
            val uriName = it.toString().substringBefore('#').substringAfterLast('/')
            val uriNameWithoutSuffix = when {
                uriName.endsWith(".schema.json", ignoreCase = true) -> uriName.dropLast(12)
                uriName.endsWith("-schema.json", ignoreCase = true) -> uriName.dropLast(12)
                uriName.endsWith("_schema.json", ignoreCase = true) -> uriName.dropLast(12)
                uriName.endsWith(".schema", ignoreCase = true) -> uriName.dropLast(7)
                uriName.endsWith("-schema", ignoreCase = true) -> uriName.dropLast(7)
                uriName.endsWith("_schema", ignoreCase = true) -> uriName.dropLast(7)
                uriName.endsWith(".json", ignoreCase = true) -> uriName.dropLast(5)
                else -> uriName
            }
            uriNameWithoutSuffix.split('-', '.').joinToString(separator = "") { part -> Strings.capitalise(part) }.
                    sanitiseName()
        } ?: "GeneratedClass${targets.size}"
        targets.add(Target(schema, Constraints(schema), className, packageName, subDirectories, suffix, inputFile))
    }

    private fun addTargets(targets: MutableList<Target>, subDirectories: List<String>, inputDir: File) {
        inputDir.listFiles()?.forEach {
            when {
                it.isDirectory -> addTargets(targets, subDirectories + it.name.mapDirectoryName(), it)
                it.isFile -> addTarget(targets, subDirectories, it)
            }
        }
    }

    private fun String.mapDirectoryName(): String = StringBuilder().also {
        for (ch in this)
            if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9')
                it.append(ch)
    }.toString()

    private fun analyseObject(target: Target, constraints: Constraints, targets: List<Target>): Boolean {
        constraints.objectValidationsPresent?.let { return it }
        (constraints.schema as? JSONSchema.General)?.let {
            for (child in it.children) {
                if (child is PropertiesSchema)
                    break
                if (child is AllOfSchema && child.array.size == 1) {
                    val general = child.array[0]
                    if (general is JSONSchema.General && general.children.size == 1) {
                        val ref = general.children[0]
                        if (ref is RefSchema) {
                            val refTarget = targets.find { t -> t.schema.uri == ref.target.uri }
                            if (refTarget != null) {
                                val baseTarget = Target(refTarget.schema, Constraints(refTarget.schema),
                                        refTarget.className, refTarget.packageName, refTarget.subDirectories,
                                        refTarget.suffix, refTarget.file)
                                target.baseClass = baseTarget
                                processSchema(baseTarget.schema, baseTarget.constraints)
                                analyseObject(baseTarget, baseTarget.constraints, targets)
                                return analyseDerivedObject(target, constraints, baseTarget)
                            }
                        }
                    }
                    break
                }
            }
        }
        // now carry on and analyse properties
        return analyseProperties(target, constraints).also { constraints.objectValidationsPresent = it }
    }

    private fun analyseDerivedObject(target: Target, constraints: Constraints, refTarget: Target): Boolean {
        constraints.properties.forEach { property ->
            if (refTarget.constraints.properties.any { it.propertyName == property.propertyName })
                property.baseProperty = true
        }
        return analyseProperties(target, constraints)
    }

    private fun analyseProperties(target: Target, constraints: Constraints): Boolean {
        constraints.properties.forEach { property ->
            when {
                property.name in constraints.required -> property.isRequired = true
                property.nullable == true || property.defaultValue != null -> {}
                else -> property.nullable = true // should be error, but that would be unhelpful
            }
        }
        return constraints.properties.fold(false) { result, property -> analyseProperty(target, property) || result }
    }

    private fun analyseProperty(target: Target, property: NamedConstraints): Boolean { // true == validations present
        when {
            property.isObject -> {
                val innerClassName = property.capitalisedName
                val innerClass = target.addNestedClass(property, innerClassName)
                property.localTypeName = innerClass.className
                innerClass.validationsPresent = analyseProperties(target, property)
                return false
            }
            property.isArray -> {
                target.systemClasses.addOnce(SystemClass.LIST)
                property.arrayItems?.let {
                    if (it.isObject) {
                        val innerClassName = it.schema.findRefChild()?.let { ref ->
                            ref.fragment?.substringAfterLast('/')?.let { s -> Strings.capitalise(s) }
                        } ?: Strings.capitalise(property.name.depluralise())
                        val nestedClass = target.addNestedClass(it, innerClassName)
                        it.localTypeName = nestedClass.className
                        nestedClass.validationsPresent = analyseProperties(target, it)
                    }
                }
                var validationsPresent = false
                property.minItems?.let {
                    property.addValidation(Validation.Type.MIN_ITEMS, NumberValue(it))
                    validationsPresent = true
                }
                property.maxItems?.let {
                    property.addValidation(Validation.Type.MAX_ITEMS, NumberValue(it))
                    validationsPresent = true
                }
                return validationsPresent
            }
            property.isInt -> {
                return analyseInt(property)
            }
            property.isLong -> {
                return analyseLong(property)
            }
            property.isDecimal -> {
                target.systemClasses.addOnce(SystemClass.DECIMAL)
                property.systemClass = SystemClass.DECIMAL
                return analyseDecimal(target, property)
            }
            property.isString -> {
                property.enumValues?.let { array ->
                    if (array.all { it is JSONString && it.get().isValidIdentifier() }) {
                        val innerClassName = property.capitalisedName
                        val innerClass = target.addNestedClass(property, innerClassName)
                        property.localTypeName = innerClass.className
                        return false
                    }
                }
                var validationsPresent = false
                property.constValue?.let {
                    if (it is JSONString) {
                        val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.get()))
                        property.addValidation(Validation.Type.CONST_STRING, stringStatic)
                        validationsPresent = true
                    }
                }
                property.minLength?.let {
                    property.addValidation(Validation.Type.MIN_LENGTH, NumberValue(it))
                    validationsPresent = true
                }
                property.maxLength?.let {
                    property.addValidation(Validation.Type.MAX_LENGTH, NumberValue(it))
                    validationsPresent = true
                }
                validationsPresent = analyseFormat(target, property) || validationsPresent
                validationsPresent = analyseRegex(target, property) || validationsPresent
                return validationsPresent
            }
        }
        return false
    }

    private fun String.isValidIdentifier(): Boolean {
        if (!this[0].isJavaIdentifierStart())
            return false
        for (i in 1 until length)
            if (!this[i].isJavaIdentifierPart())
                return false
        return true
    }

    private fun analyseInt(property: NamedConstraints): Boolean {
        var result = false
        property.constValue?.let {
            when (it) {
                is JSONInteger -> {
                    property.addValidation(Validation.Type.CONST_INT, it.get())
                    result = true
                }
                is JSONLong -> {
                    it.get().let { v ->
                        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                            property.addValidation(Validation.Type.CONST_INT, v)
                            result = true
                        }
                    }
                }
                is JSONDecimal -> {
                    it.get().asLong().let { v ->
                        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                            property.addValidation(Validation.Type.CONST_INT, v)
                            result = true
                        }
                    }
                }
            }
        }
        property.minimumLong?.let {
            if (it in Int.MIN_VALUE..Int.MAX_VALUE) {
                property.addValidation(Validation.Type.MINIMUM_INT, it)
                result = true
            }
        }
        property.maximumLong?.let {
            if (it in Int.MIN_VALUE..Int.MAX_VALUE) {
                property.addValidation(Validation.Type.MAXIMUM_INT, it)
                result = true
            }
        }
        for (multiple in property.multipleOf) {
            property.addValidation(Validation.Type.MULTIPLE_INT, multiple.asLong())
            result = true
        }
        return result
    }

    private fun analyseLong(property: NamedConstraints): Boolean {
        var result = false
        property.constValue?.let {
            when (it) {
                is JSONInteger -> {
                    property.addValidation(Validation.Type.CONST_LONG, it.get())
                    result = true
                }
                is JSONLong -> {
                    property.addValidation(Validation.Type.CONST_LONG, it.get())
                    result = true
                }
                is JSONDecimal -> {
                    property.addValidation(Validation.Type.CONST_LONG, it.get().asLong())
                }
            }
        }
        property.minimumLong?.let {
            property.addValidation(Validation.Type.MINIMUM_LONG, it)
            result = true
        }
        property.maximumLong?.let {
            property.addValidation(Validation.Type.MAXIMUM_LONG, it)
            result = true
        }
        for (multiple in property.multipleOf) {
            property.addValidation(Validation.Type.MULTIPLE_LONG, multiple.asLong())
            result = true
        }
        return result
    }

    private fun analyseDecimal(target: Target, property: NamedConstraints): Boolean {
        var result = false
        property.constValue?.let {
            if (it is JSONNumberValue) {
                val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec",
                        NumberValue(it.bigDecimalValue()))
                property.addValidation(Validation.Type.CONST_DECIMAL, decimalStatic) // TODO template
                result = true
            }
        }
        property.minimum?.let {
            val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
            property.addValidation(Validation.Type.MINIMUM_DECIMAL, decimalStatic)
            result = true
        }
        property.exclusiveMinimum?.let {
            val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
            property.addValidation(Validation.Type.EXCLUSIVE_MINIMUM_DECIMAL, decimalStatic)
            result = true
        }
        property.maximum?.let {
            val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
            property.addValidation(Validation.Type.MAXIMUM_DECIMAL, decimalStatic)
            result = true
        }
        property.exclusiveMaximum?.let {
            val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
            property.addValidation(Validation.Type.EXCLUSIVE_MAXIMUM_DECIMAL, decimalStatic)
            result = true
        }
        for (multiple in property.multipleOf) {
            val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(multiple))
            property.addValidation(Validation.Type.MULTIPLE_DECIMAL, decimalStatic)
            result = true
        }
        return result
    }

    private fun analyseFormat(target: Target, property: Constraints): Boolean {
        when (property.format) {
            FormatValidator.FormatType.EMAIL -> {
                target.systemClasses.addOnce(SystemClass.VALIDATION)
                property.addValidation(Validation.Type.EMAIL)
                return true
            }
            FormatValidator.FormatType.HOSTNAME -> {
                target.systemClasses.addOnce(SystemClass.VALIDATION)
                property.addValidation(Validation.Type.HOSTNAME)
                return true
            }
            FormatValidator.FormatType.IPV4 -> {
                target.systemClasses.addOnce(SystemClass.VALIDATION)
                property.addValidation(Validation.Type.IPV4)
                return true
            }
            FormatValidator.FormatType.IPV6 -> {
                target.systemClasses.addOnce(SystemClass.VALIDATION)
                property.addValidation(Validation.Type.IPV6)
                return true
            }
            FormatValidator.FormatType.DATE_TIME -> {
                target.systemClasses.addOnce(SystemClass.DATE_TIME)
                property.systemClass = SystemClass.DATE_TIME
            }
            FormatValidator.FormatType.DATE -> {
                target.systemClasses.addOnce(SystemClass.DATE)
                property.systemClass = SystemClass.DATE
            }
            FormatValidator.FormatType.TIME -> {
                target.systemClasses.addOnce(SystemClass.TIME)
                property.systemClass = SystemClass.TIME
            }
            FormatValidator.FormatType.DURATION -> {
                target.systemClasses.addOnce(SystemClass.DURATION)
                property.systemClass = SystemClass.DURATION
            }
            FormatValidator.FormatType.UUID -> {
                target.systemClasses.addOnce(SystemClass.UUID)
                property.systemClass = SystemClass.UUID
            }
            FormatValidator.FormatType.URI, FormatValidator.FormatType.URI_REFERENCE -> {
                target.systemClasses.addOnce(SystemClass.URI)
                property.systemClass = SystemClass.URI
            }
            else -> {}
        }
        return false
    }

    private fun analyseRegex(target: Target, property: Constraints): Boolean {
        if (property.regex != null) {
            target.systemClasses.addOnce(SystemClass.REGEX)
            val regexStatic = target.addStatic(Target.StaticType.PATTERN, "cg_regex",
                    StringValue(property.regex.toString()))
            property.addValidation(Validation.Type.PATTERN, regexStatic)
            return true
        }
        return false
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
            is JSONSchema.General -> schema.children.forEach { processSchema(it, constraints) }
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
            is ItemsSchema -> processSchema (subSchema.itemSchema,
                    constraints.arrayItems ?: Constraints(subSchema.itemSchema).also { constraints.arrayItems = it })
            is PropertiesSchema -> processPropertySchema(subSchema, constraints)
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
            NumberValidator.ValidationType.MULTIPLE_OF -> constraints.multipleOf.add(numberValidator.value)
            NumberValidator.ValidationType.MINIMUM -> constraints.minimum =
                    maximumOf(constraints.minimum, numberValidator.value)
            NumberValidator.ValidationType.EXCLUSIVE_MINIMUM -> constraints.exclusiveMinimum =
                    maximumOf(constraints.exclusiveMinimum, numberValidator.value)
            NumberValidator.ValidationType.MAXIMUM -> constraints.maximum =
                    minimumOf(constraints.maximum, numberValidator.value)
            NumberValidator.ValidationType.EXCLUSIVE_MAXIMUM -> constraints.exclusiveMaximum =
                    minimumOf(constraints.exclusiveMaximum, numberValidator.value)
        }
    }

    private fun processArrayValidator(arrayValidator: ArrayValidator, constraints: Constraints) {
        when (arrayValidator.condition) {
            ArrayValidator.ValidationType.MAX_ITEMS -> constraints.maxItems =
                    minimumOf(constraints.maxItems, arrayValidator.value)?.toInt()
            ArrayValidator.ValidationType.MIN_ITEMS -> constraints.minItems =
                    maximumOf(constraints.minLength, arrayValidator.value)?.toInt()
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
                    minimumOf(constraints.maxLength, stringValidator.value)?.toInt()
            StringValidator.ValidationType.MIN_LENGTH -> constraints.minLength =
                    maximumOf(constraints.minLength, stringValidator.value)?.toInt()
        }
    }

    private fun processPropertySchema(propertySchema: PropertiesSchema, constraints: Constraints) {
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

    companion object {

        fun String.sanitiseName(): String {
            for (i in 0 until length) {
                val ch = this[i]
                if (!(ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9')) {
                    val sb = StringBuilder(substring(0, i))
                    for (j in i + 1 until length) {
                        val ch2 = this[j]
                        if (ch2 in 'A'..'Z' || ch2 in 'a'..'z' || ch2 in '0'..'9')
                            sb.append(ch)
                    }
                    return sb.toString()
                }
            }
            return this
        }

        private fun checkDirectory(directory: File): File {
            when {
                !directory.exists() -> {
                    if (!directory.mkdirs())
                        throw JSONSchemaException("Error creating output directory - $directory")
                }
                directory.isDirectory -> {}
                directory.isFile -> throw JSONSchemaException("File given for output directory - $directory")
                else -> throw JSONSchemaException("Error accessing output directory - $directory")
            }
            return directory
        }

        fun minimumOf(a: Number?, b: Number?): Number? {
            return when (a) {
                null -> b
                is BigDecimal -> when (b) {
                    null -> a
                    is BigDecimal -> if (a < b) a else b
                    else -> if (a < BigDecimal(b.toLong())) a else b
                }
                else -> when (b) {
                    null -> a
                    is BigDecimal -> if (BigDecimal(a.toLong()) < b) a else b
                    else -> if (a.toLong() < b.toLong()) a else b
                }
            }
        }

        fun maximumOf(a: Number?, b: Number?): Number? {
            return when (a) {
                null -> b
                is BigDecimal -> when (b) {
                    null -> a
                    is BigDecimal -> if (a > b) a else b
                    else -> if (a > BigDecimal(b.toLong())) a else b
                }
                else -> when (b) {
                    null -> a
                    is BigDecimal -> if (BigDecimal(a.toLong()) > b) a else b
                    else -> if (a.toLong() > b.toLong()) a else b
                }
            }
        }

    }

}

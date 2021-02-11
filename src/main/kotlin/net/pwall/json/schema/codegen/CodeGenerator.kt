/*
 * @(#) CodeGenerator.kt
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

import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

import net.pwall.json.JSONBoolean
import net.pwall.json.JSONDecimal
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONMapping
import net.pwall.json.JSONNumberValue
import net.pwall.json.JSONSequence
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.codegen.Constraints.Companion.asLong
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.subschema.AllOfSchema
import net.pwall.json.schema.subschema.CombinationSchema
import net.pwall.json.schema.subschema.ExtensionSchema
import net.pwall.json.schema.subschema.ItemsSchema
import net.pwall.json.schema.subschema.PropertiesSchema
import net.pwall.json.schema.subschema.RefSchema
import net.pwall.json.schema.subschema.RequiredSchema
import net.pwall.json.schema.validation.ArrayValidator
import net.pwall.json.schema.validation.ConstValidator
import net.pwall.json.schema.validation.DefaultValidator
import net.pwall.json.schema.validation.DelegatingValidator
import net.pwall.json.schema.validation.EnumValidator
import net.pwall.json.schema.validation.FormatValidator
import net.pwall.json.schema.validation.NumberValidator
import net.pwall.json.schema.validation.PatternValidator
import net.pwall.json.schema.validation.StringValidator
import net.pwall.json.schema.validation.TypeValidator
import net.pwall.log.Logger
import net.pwall.log.LoggerFactory
import net.pwall.mustache.Template
import net.pwall.mustache.parser.Parser as MustacheParser
import net.pwall.util.Strings

/**
 * JSON Schema Code Generator.  The class my be parameterised either by constructor parameters or by setting the
 * appropriate variables after construction.
 *
 * @author  Peter Wall
 */
class CodeGenerator(
        /** Template subdirectory name (within the assembled code generator artefact) */
        var templates: String = "kotlin",
        /** The filename suffix to be applied to generated files */
        var suffix: String = "kt",
        /** The primary template to use for the generation of a class */
        var templateName: String = "class",
        /** The primary template to use for the generation of an enum */
        var enumTemplateName: String = "enum",
        /** The base package name for the generated classes */
        var basePackageName: String? = null,
        /** The base output directory for generated files */
        var baseDirectoryName: String = ".",
        /** A boolean flag to indicate the schema files in subdirectories are to be output to sub-packages */
        var derivePackageFromStructure: Boolean = true,
        /** A comment to add to the header of generated files */
        var generatorComment: String? = null,
        /** An optional marker interface to add to each generated class */
        var markerInterface: String? = null,
        /** A [Logger] object for the output of logging messages */
        val log: Logger = LoggerFactory.getDefaultLogger(CodeGenerator::class.qualifiedName)
) {

    enum class NestedClassNameOption {
        USE_NAME_FROM_REF_SCHEMA,
        USE_NAME_FROM_PROPERTY
    }

    var nestedClassNameOption = NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA

    private val customClassesByURI = mutableListOf<CustomClassByURI>()
    private val customClassesByFormat = mutableListOf<CustomClassByFormat>()
    private val customClassesByExtension = mutableListOf<CustomClassByExtension>()

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
                val inputStream = CodeGenerator::class.java.getResourceAsStream("/$templates/$name.mustache") ?:
                        throw JSONSchemaException("Can't locate template partial /$templates/$name.mustache")
                inputStream.reader()
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

    /**
     * Generate classes for a set of schema files (specified as a `vararg` array).  Directories will be traversed
     * recursively.
     *
     * @param   inputFiles  the files
     */
    fun generate(vararg inputFiles: File) {
        generate(inputFiles.asList())
    }

    /**
     * Generate classes for a set of files (specified as a [List]).  Directories will be traversed recursively.
     *
     * @param   inputFiles  the list of files
     */
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
        generateAllTargets(targets)
    }

    /**
     * Generate classes for a set of schema files (specified as a `vararg` array of [Path]).  Directories will be
     * traversed recursively.
     *
     * @param   inputPaths  the files
     */
    fun generateFromPaths(vararg inputPaths: Path) {
        generateFromPaths(inputPaths.asList())
    }

    /**
     * Generate classes for a set of files (specified as a [List] of [Path]).  Directories will be traversed
     * recursively.
     *
     * @param   inputPaths  the list of files
     */
    fun generateFromPaths(inputPaths: List<Path>) {
        val targets = mutableListOf<Target>()
        val parser = actualSchemaParser
        for (inputPath in inputPaths) {
            parser.preLoad(inputPath)
            when {
                Files.isRegularFile(inputPath) -> addTarget(targets, emptyList(), inputPath)
                Files.isDirectory(inputPath) -> addTargets(targets, emptyList(), inputPath)
            }
        }
        generateAllTargets(targets)
    }

    private fun generateAllTargets(targets: List<Target>) {
        for (target in targets) {
            processSchema(target.schema, target.constraints)
            log.info { "Generating for target ${target.file}" }
            generateTarget(target, targets)
        }
        // TODO - generate index - for html
    }

    private fun generateTarget(target: Target, targets: List<Target>) {
        nameGenerator = NameGenerator()
        when {
            target.constraints.isObject -> { // does it look like an object? generate a class
                log.info { "-- target class ${target.qualifiedClassName}" }
                target.validationsPresent = analyseObject(target, target.constraints, targets)
                target.systemClasses.sortBy { it.order }
                target.imports.sort()
                target.baseClass?.let {
                    if (it.packageName != target.packageName && !target.imports.contains(it.qualifiedClassName))
                        target.baseImport = it.qualifiedClassName
                }
                actualOutputResolver(baseDirectoryName, target.subDirectories, target.className,
                        target.suffix).use {
                    actualTemplate.processTo(AppendableFilter(it), target)
                }
            }
            target.constraints.isString && target.constraints.enumValues != null -> {
                log.info { "-- target enum ${target.qualifiedClassName}" }
                actualOutputResolver(baseDirectoryName, target.subDirectories, target.className,
                        target.suffix).use {
                    actualEnumTemplate.processTo(it, target)
                }
            }
            else -> log.info { "-- nothing to generate" }
        }
    }

    /**
     * Generate a single class.
     *
     * @param   schema      the [JSONSchema]
     * @param   className   the class name
     * @param   subDirectories  list of subdirectory names to use for the output file
     */
    fun generateClass(schema: JSONSchema, className: String, subDirectories: List<String> = emptyList()) {
        var packageName = basePackageName
        if (derivePackageFromStructure)
            subDirectories.forEach { packageName = if (packageName.isNullOrEmpty()) it else "$packageName.$it" }
        val target = Target(schema, Constraints(schema), className, packageName, subDirectories, suffix,
                dummyFile.toString(), generatorComment, markerInterface)
        processSchema(target.schema, target.constraints)
        log.info { "Generating for internal schema" }
        generateTarget(target, listOf(target))
    }

    /**
     * Generate classes as specified by a list of pairs - Schema and class name.
     *
     * @param   schemaList  list of [Pair] of [JSONSchema] and [String] (class name)
     * @param   subDirectories  list of subdirectory names to use for the output files
     */
    fun generateClasses(schemaList: List<Pair<JSONSchema, String>>, subDirectories: List<String> = emptyList()) {
        var packageName = basePackageName
        if (derivePackageFromStructure)
            subDirectories.forEach { packageName = if (packageName.isNullOrEmpty()) it else "$packageName.$it" }
        val targets = schemaList.map { Target(it.first, Constraints(it.first), it.second, packageName, subDirectories,
                suffix, dummyFile.toString(), generatorComment, markerInterface).also {
                        t -> processSchema(t.schema, t.constraints) } }
        log.info { "Generating for internal schema" }
        for (target in targets)
            generateTarget(target, targets)
    }

    /**
     * Generate classes for all definitions in a composite file (e.g. schema definitions embedded in an OpenAPI or
     * Swagger document).
     *
     * @param   base            the base of the composite object
     * @param   pointer         pointer to the structure containing the schema definitions (e.g. /definitions)
     * @param   subDirectories  list of subdirectory names to use for the output files
     * @param   filter          optional filter to select which classes to include (by name)
     */
    fun generateAll(base: JSONValue, pointer: JSONPointer, subDirectories: List<String> = emptyList(),
            filter: (String) -> Boolean = { true }) {
        val definitions = (pointer.find(base) as? JSONMapping<*>) ?:
                throw JSONSchemaException("Can't find definitions - $pointer")
        generateClasses(definitions.keys.filter(filter).map {
            actualSchemaParser.parseSchema(base, pointer.child(it), URI("https:/pwall.net/internal")) to it
        }, subDirectories)
    }

    private fun addTarget(targets: MutableList<Target>, subDirectories: List<String>, inputFile: File) {
        val schema = actualSchemaParser.parse(inputFile)
        addTarget(targets, subDirectories, schema, inputFile.toString())
    }

    private fun addTarget(targets: MutableList<Target>, subDirectories: List<String>, inputPath: Path) {
        val schema = actualSchemaParser.parse(inputPath)
        addTarget(targets, subDirectories, schema, inputPath.toString())
    }

    private fun addTarget(targets: MutableList<Target>, subDirectories: List<String>, schema: JSONSchema,
            filename: String) {
        var packageName = basePackageName
        if (derivePackageFromStructure)
            subDirectories.forEach { packageName = if (packageName.isNullOrEmpty()) it else "$packageName.$it" }
        val className = schema.uri?.let {
            // TODO change to allow name ending with "/schema"?
            val uriName = it.toString().substringBefore('#').substringAfterLast('/')
            val uriNameWithoutExtension = when {
                uriName.endsWith(".json", ignoreCase = true) -> uriName.dropLast(5)
                uriName.endsWith(".yaml", ignoreCase = true) -> uriName.dropLast(5)
                uriName.endsWith(".yml", ignoreCase = true) -> uriName.dropLast(4)
                else -> uriName
            }
            val uriNameWithoutSuffix = when {
                uriNameWithoutExtension.endsWith(".schema", ignoreCase = true) -> uriNameWithoutExtension.dropLast(7)
                uriNameWithoutExtension.endsWith("-schema", ignoreCase = true) -> uriNameWithoutExtension.dropLast(7)
                uriNameWithoutExtension.endsWith("_schema", ignoreCase = true) -> uriNameWithoutExtension.dropLast(7)
                else -> uriNameWithoutExtension
            }
            uriNameWithoutSuffix.split('-', '.').joinToString(separator = "") { part -> Strings.capitalise(part) }.
                    sanitiseName()
        } ?: "GeneratedClass${targets.size}"
        targets.add(Target(schema, Constraints(schema), className, packageName, subDirectories, suffix, filename,
                generatorComment, markerInterface))
    }

    private fun addTargets(targets: MutableList<Target>, subDirectories: List<String>, inputDir: File) {
        inputDir.listFiles()?.forEach {
            when {
                it.isDirectory -> {
                    if (!it.name.startsWith('.'))
                        addTargets(targets, subDirectories + it.name.mapDirectoryName(), it)
                }
                it.isFile -> addTarget(targets, subDirectories, it)
            }
        }
    }

    private fun addTargets(targets: MutableList<Target>, subDirectories: List<String>, inputDir: Path) {
        Files.newDirectoryStream(inputDir).use { dir ->
            dir.forEach {
                when {
                    Files.isDirectory(it) -> {
                        if (!it.fileName.toString().startsWith('.'))
                            addTargets(targets, subDirectories + it.fileName.toString().mapDirectoryName(), it)
                    }
                    Files.isRegularFile(it) -> addTarget(targets, subDirectories, it)
                }
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
                    child.array[0].findRefChild()?.let { refChild ->
                        val refTarget = targets.find { t -> t.schema.uri == refChild.target.uri }
                        if (refTarget != null) {
                            val baseTarget = Target(refTarget.schema, Constraints(refTarget.schema),
                                    refTarget.className, refTarget.packageName, refTarget.subDirectories,
                                    refTarget.suffix, refTarget.file, generatorComment, markerInterface)
                            target.baseClass = baseTarget
                            processSchema(baseTarget.schema, baseTarget.constraints)
                            analyseObject(baseTarget, baseTarget.constraints, targets)
                            return analyseDerivedObject(target, constraints, baseTarget, targets)
                        }
                    }
                    break
                }
            }
        }
        // now carry on and analyse properties
        return analyseProperties(target, constraints, targets).also { constraints.objectValidationsPresent = it }
    }

    private fun analyseDerivedObject(target: Target, constraints: Constraints, refTarget: Target,
            targets: List<Target>): Boolean {
        constraints.properties.forEach { property ->
            if (refTarget.constraints.properties.any { it.propertyName == property.propertyName })
                property.baseProperty = true
        }
        return analyseProperties(target, constraints, targets)
    }

    private fun analyseProperties(target: Target, constraints: Constraints, targets: List<Target>): Boolean {
        constraints.properties.forEach { property ->
            when {
                property.name in constraints.required -> property.isRequired = true
                property.nullable == true || property.defaultValue != null -> {}
                else -> property.nullable = true // should be error, but that would be unhelpful
            }
        }
        return constraints.properties.fold(false) { result, property ->
            analyseProperty(target, property, targets) || result
        }
    }

    private fun useTarget(constraints: Constraints, target: Target, otherTarget: Target) {
        if (otherTarget.packageName != target.packageName)
            target.imports.addOnce(otherTarget.qualifiedClassName)
        constraints.localTypeName = otherTarget.className
    }

    private fun findTargetClass(constraints: Constraints, target: Target, targets: List<Target>,
                defaultName: () -> String) {
        targets.find { it.schema === constraints.schema }?.let {
            useTarget(constraints, target, it)
            return
        }
        val refChild = constraints.schema.findRefChild()
        refChild?.let { targets.find { t -> t.schema === it.target } }?.let {
            useTarget(constraints, target, it)
            return
        }
        val nestedClassName = when (nestedClassNameOption) {
            NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA ->
                    refChild?.fragment?.substringAfterLast('/') ?: defaultName()
            NestedClassNameOption.USE_NAME_FROM_PROPERTY -> defaultName()
        }
        val nestedClass = target.addNestedClass(constraints, Strings.capitalise(nestedClassName))
        nestedClass.validationsPresent = analyseProperties(target, constraints, targets)
        constraints.localTypeName = nestedClass.className
    }

    private fun findCustomClass(schema: JSONSchema, target: Target): String? {
        customClassesByExtension.find { it.match(schema) }?.let {
            return it.applyToTarget(target)
        }
        schema.uri?.resolve("#${schema.location}")?.let { uri ->
            customClassesByURI.find { uri.resolve(it.uri) == uri }?.let {
                return it.applyToTarget(target)
            }
        }
        return null
    }

    private fun analyseProperty(target: Target, property: NamedConstraints, targets: List<Target>): Boolean {
        // true == validations present
        findCustomClass(property.schema, target)?.let {
            property.localTypeName = it
            return false
        }
        customClassesByFormat.find { it.match(property) }?.let {
            property.localTypeName = it.applyToTarget(target)
            return false
        }
        property.schema.findRefChild()?.let { refChild ->
            findCustomClass(refChild.target, target)?.let {
                property.localTypeName = it
                return false
            }
        }
        when {
            property.isObject -> {
                findTargetClass(property, target, targets) { property.name }
                return false
            }
            property.isArray -> {
                target.systemClasses.addOnce(SystemClass.LIST)
                var validationsPresent = false
                property.arrayItems?.let {
                    if (analyseArray(it, target, targets) { property.name.depluralise() }) {
                        property.addValidation(Validation.Type.ARRAY_ITEMS)
                        validationsPresent = true
                    }
                }
                property.minItems?.let {
                    property.addValidation(Validation.Type.MIN_ITEMS, NumberValue(it))
                    validationsPresent = true
                }
                property.maxItems?.let {
                    property.addValidation(Validation.Type.MAX_ITEMS, NumberValue(it))
                    validationsPresent = true
                }
                property.defaultValue?.let {
                    if (it.type != JSONSchema.Type.ARRAY)
                        property.defaultValue = null
                }
                return validationsPresent
            }
            property.isInt -> {
                return analyseInt(property, target)
            }
            property.isLong -> {
                return analyseLong(property, target)
            }
            property.isDecimal -> {
                target.systemClasses.addOnce(SystemClass.DECIMAL)
                property.systemClass = SystemClass.DECIMAL
                return analyseDecimal(target, property)
            }
            property.isString -> {
                return analyseString(property, target, targets) { property.name }
            }
        }
        return false
    }

    private fun analyseArray(property: Constraints, target: Target, targets: List<Target>, defaultName: () -> String):
            Boolean {
        return when {
            property.isObject -> {
                findTargetClass(property, target, targets, defaultName)
                false
            }
            property.isInt -> analyseInt(property, target)
            property.isLong -> analyseLong(property, target)
            property.isDecimal -> {
                target.systemClasses.addOnce(SystemClass.DECIMAL)
                property.systemClass = SystemClass.DECIMAL
                analyseDecimal(target, property)
            }
            property.isString -> analyseString(property, target, targets, defaultName)
            property.isArray -> property.arrayItems?.let {
                analyseArray(it, target, targets, defaultName).also { validations ->
                    if (validations)
                        property.addValidation(Validation.Type.ARRAY_ITEMS)
                }
            } ?: false
            else -> false
        }
    }

    private fun analyseString(property: Constraints, target: Target, targets: List<Target>, defaultName: () -> String):
            Boolean {
        var validationsPresent = false
        property.enumValues?.let { array ->
            if (array.all { it is JSONString && it.get().isValidIdentifier() }) {
                findTargetClass(property, target, targets, defaultName)
                property.defaultValue?.let {
                    if (it.type == JSONSchema.Type.STRING &&
                            array.any { a -> a.toString() == it.defaultValue.toString() } ) {
                        val enumDefault = EnumDefault(property.localTypeName!!, it.defaultValue.toString())
                        property.defaultValue = Constraints.DefaultValue(enumDefault, JSONSchema.Type.STRING)
                    }
                    else
                        property.defaultValue = null
                }
                return false
            }
            if (array.all { it is JSONString }) {
                target.systemClasses.addOnce(SystemClass.ARRAYS)
                target.systemClasses.addOnce(SystemClass.LIST)
                val arrayStatic = target.addStatic(Target.StaticType.STRING_ARRAY, "cg_array",
                        array.map { StringValue(it.toString()) })
                property.addValidation(Validation.Type.ENUM_STRING, arrayStatic)
                validationsPresent = true
            }
        }
        property.constValue?.let {
            if (it is JSONString) {
                val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.get()))
                property.addValidation(Validation.Type.CONST_STRING, stringStatic)
                validationsPresent = true
            }
        }
        property.minLength?.let { min ->
            property.maxLength?.let { max ->
                if (min == max)
                    property.addValidation(Validation.Type.CONST_LENGTH, NumberValue(min))
                else
                    property.addValidation(Validation.Type.RANGE_LENGTH, min to max)
            } ?: property.addValidation(Validation.Type.MIN_LENGTH, NumberValue(min))
            validationsPresent = true
        } ?: property.maxLength?.let { max ->
            property.addValidation(Validation.Type.MAX_LENGTH, NumberValue(max))
            validationsPresent = true
        }
        validationsPresent = analyseFormat(target, property) || validationsPresent
        validationsPresent = analyseRegex(target, property) || validationsPresent
        return validationsPresent
    }

    private fun analyseInt(property: Constraints, target: Target): Boolean {
        var result = false
        property.constValue?.let {
            when (it) {
                is JSONInteger -> {
                    property.addValidation(Validation.Type.CONST_INT, it.get())
                    property.enumValues = null
                    result = true
                }
                is JSONLong -> {
                    it.get().let { v ->
                        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                            property.addValidation(Validation.Type.CONST_INT, v)
                            property.enumValues = null
                            result = true
                        }
                    }
                }
                is JSONDecimal -> {
                    it.get().asLong().let { v ->
                        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                            property.addValidation(Validation.Type.CONST_INT, v)
                            property.enumValues = null
                            result = true
                        }
                    }
                }
            }
        }
        property.enumValues?.let { array ->
            if (array.all { it is JSONNumberValue }) {
                target.systemClasses.addOnce(SystemClass.ARRAYS)
                target.systemClasses.addOnce(SystemClass.LIST)
                val arrayStatic = target.addStatic(Target.StaticType.INT_ARRAY, "cg_array", array.map {
                    when (it) {
                        is JSONInteger -> NumberValue(it.get())
                        is JSONLong -> NumberValue(it.get())
                        is JSONDecimal -> NumberValue(it.get())
                        else -> NumberValue(0)
                    }
                })
                property.addValidation(Validation.Type.ENUM_INT, arrayStatic)
                result = true
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
        result = analyseFormat(target, property) || result
        property.defaultValue?.let {
            if (it.type != JSONSchema.Type.INTEGER)
                property.defaultValue = null
        }
        return result
    }

    private fun analyseLong(property: Constraints, target: Target): Boolean {
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
        result = analyseFormat(target, property) || result
        property.defaultValue?.let {
            if (it.type != JSONSchema.Type.INTEGER)
                property.defaultValue = null
        }
        return result
    }

    private fun analyseDecimal(target: Target, property: Constraints): Boolean {
        var result = false
        property.constValue?.let {
            if (it is JSONNumberValue) {
                val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec",
                        NumberValue(it.bigDecimalValue()))
                property.addValidation(Validation.Type.CONST_DECIMAL, decimalStatic)
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
        property.defaultValue?.let {
            if (it.type != JSONSchema.Type.NUMBER && it.type != JSONSchema.Type.INTEGER)
                property.defaultValue = null
        }
        return result
    }

    private fun analyseFormat(target: Target, property: Constraints): Boolean {
        property.format?.let {
            when (it.name) {
                FormatValidator.EmailFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.EMAIL)
                    return true
                }
                FormatValidator.HostnameFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.HOSTNAME)
                    return true
                }
                FormatValidator.IPV4FormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.IPV4)
                    return true
                }
                FormatValidator.IPV6FormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.IPV6)
                    return true
                }
                FormatValidator.DurationFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.DURATION)
                    return true
                }
                FormatValidator.JSONPointerFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.JSON_POINTER)
                    return true
                }
                FormatValidator.RelativeJSONPointerFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.RELATIVE_JSON_POINTER)
                    return true
                }
                FormatValidator.DateTimeFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.DATE_TIME)
                    property.systemClass = SystemClass.DATE_TIME
                }
                FormatValidator.DateFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.DATE)
                    property.systemClass = SystemClass.DATE
                }
                FormatValidator.TimeFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.TIME)
                    property.systemClass = SystemClass.TIME
                }
                FormatValidator.UUIDFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.UUID)
                    property.systemClass = SystemClass.UUID
                }
                FormatValidator.URIFormatChecker.name, FormatValidator.URIReferenceFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.URI)
                    property.systemClass = SystemClass.URI
                }
            }
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
//        this.endsWith("es") -> dropLast(2) // need a more sophisticated way of handling plurals ending with -es
        this.endsWith('s') -> dropLast(1)
        else -> this
    }

    private fun JSONSchema.findRefChild(): RefSchema? =
            ((this as? JSONSchema.General)?.children?.find { it is RefSchema }) as RefSchema?

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
                is JSONString -> Constraints.DefaultValue(StringValue(value.get()), JSONSchema.Type.STRING)
                is JSONBoolean -> Constraints.DefaultValue(value.get(), JSONSchema.Type.BOOLEAN)
                is JSONSequence<*> -> Constraints.DefaultValue(value.map { processDefaultValue(it) },
                        JSONSchema.Type.ARRAY)
                is JSONMapping<*> -> throw JSONSchemaException("Can't handle object as default value")
                else -> throw JSONSchemaException("Unexpected default value")
            }

    private fun processSubSchema(subSchema: JSONSchema.SubSchema, constraints: Constraints) {
        when (subSchema) {
            is CombinationSchema -> processCombinationSchema(subSchema, constraints)
            is ItemsSchema -> processSchema(subSchema.itemSchema,
                    constraints.arrayItems ?: ItemConstraints(subSchema.itemSchema, constraints.displayName,
                            nameGenerator.generate()).also { constraints.arrayItems = it })
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
            is DelegatingValidator -> processValidator(validator.validator, constraints)
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
            throw JSONSchemaException("Duplicate format - ${formatValidator.location}")
        val newFormat = formatValidator.checker
        if (newFormat is FormatValidator.DelegatingFormatChecker)
            for (validator in newFormat.validators)
                processValidator(validator, constraints)
        if (constraints.format == null) // it may have been set by delegated validator
            constraints.format = newFormat
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

    fun addCustomClassByURI(uri: URI, qualifiedClassName: String) {
        customClassesByURI.add(CustomClassByURI(uri, qualifiedClassName))
    }

    fun addCustomClassByURI(uri: URI, className: String, packageName: String?) {
        customClassesByURI.add(CustomClassByURI(uri, className, packageName))
    }

    fun addCustomClassByFormat(name: String, qualifiedClassName: String) {
        customClassesByFormat.add(CustomClassByFormat(name, qualifiedClassName))
    }

    fun addCustomClassByFormat(name: String, className: String, packageName: String?) {
        customClassesByFormat.add(CustomClassByFormat(name, className, packageName))
    }

    fun addCustomClassByExtension(extensionId: String, extensionValue: Any?, qualifiedClassName: String) {
        customClassesByExtension.add(CustomClassByExtension(extensionId, extensionValue, qualifiedClassName))
    }

    fun addCustomClassByExtension(extensionId: String, extensionValue: Any?, className: String, packageName: String?) {
        customClassesByExtension.add(CustomClassByExtension(extensionId, extensionValue, className, packageName))
    }

    private var nameGenerator = NameGenerator()

    class NameGenerator(var suffix: Int = 0) {

        fun generate(): String = "cg_${suffix++}"

    }

    /**
     * This class is intended to look like a [StringValue], for when the default value of a string is an enum value.
     */
    class EnumDefault(private val className: String, private val defaultValue: String) {

        override fun toString(): String {
            return "$className.$defaultValue"
        }

        @Suppress("unused")
        val kotlinString: String
            get() = toString()

    }

    abstract class CustomClass(private val className: String, private val packageName: String?) {

        fun applyToTarget(target: Target): String {
            packageName?.let {
                if (it != target.packageName)
                    target.imports.addOnce("${it}.$className")
            }
            return className
        }

    }

    class CustomClassByURI(val uri: URI, className: String, packageName: String?) :
            CustomClass(className, packageName) {

        constructor(uri: URI, qualifiedClassName: String) :
                this(uri, qualifiedClassName.substringAfterLast('.'),
                        qualifiedClassName.substringBeforeLast('.').takeIf { it.isNotEmpty() })

    }

    class CustomClassByFormat(val name: String, className: String, packageName: String?) :
            CustomClass(className, packageName) {

        constructor(name: String, qualifiedClassName: String) :
                this(name, qualifiedClassName.substringAfterLast('.'),
                        qualifiedClassName.substringBeforeLast('.').takeIf { it.isNotEmpty() })

        fun match(constraints: Constraints): Boolean {
            return constraints.format?.name == name
        }

    }

    class CustomClassByExtension(private val extensionId: String, private val extensionValue: Any?, className: String,
            packageName: String?) : CustomClass(className, packageName) {

        constructor(extensionId: String, extensionValue: Any?, qualifiedClassName: String) :
                this(extensionId, extensionValue, qualifiedClassName.substringAfterLast('.'),
                        qualifiedClassName.substringBeforeLast('.').takeIf { it.isNotEmpty() })

        fun match(schema: JSONSchema): Boolean = when (schema) {
            is JSONSchema.General -> schema.children.any { match(it) }
            is ExtensionSchema -> schema.name == extensionId && schema.value == extensionValue
            else -> false
        }

    }

    class AppendableFilter(private val destination: Appendable, private val maxNewlines: Int = 2) : Appendable {

        private var newlines = 0

        override fun append(c: Char): Appendable {
            if (c == '\n') {
                if (newlines < maxNewlines) {
                    destination.append(c)
                    newlines++
                }
            }
            else {
                destination.append(c)
                newlines = 0
            }
            return this
        }

        override fun append(csq: CharSequence?): Appendable {
            val text = csq ?: "null"
            return append(text, 0, text.length)
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
            val text = csq ?: "null"
            for (i in start until end)
                append(text[i])
            return this
        }

    }

    companion object {

        val dummyFile = File("./none")

        fun <T: Any> MutableList<T>.addOnce(entry: T) {
            if (entry !in this)
                add(entry)
        }

        private fun String.isValidIdentifier(): Boolean {
            if (!this[0].isJavaIdentifierStart())
                return false
            for (i in 1 until length)
                if (!this[i].isJavaIdentifierPart())
                    return false
            return true
        }

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

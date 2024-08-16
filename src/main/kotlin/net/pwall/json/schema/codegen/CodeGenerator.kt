/*
 * @(#) CodeGenerator.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2022, 2023, 2024 Peter Wall
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

import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

import java.io.File
import java.io.Reader
import java.math.BigDecimal
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

import net.pwall.json.JSON
import net.pwall.json.JSONArray
import net.pwall.json.JSONBoolean
import net.pwall.json.JSONDecimal
import net.pwall.json.JSONDouble
import net.pwall.json.JSONFloat
import net.pwall.json.JSONInteger
import net.pwall.json.JSONLong
import net.pwall.json.JSONMapping
import net.pwall.json.JSONNumberValue
import net.pwall.json.JSONSequence
import net.pwall.json.JSONString
import net.pwall.json.JSONValue
import net.pwall.json.JSONZero
import net.pwall.json.pointer.JSONPointer
import net.pwall.json.pointer.JSONReference
import net.pwall.json.schema.JSONSchema
import net.pwall.json.schema.JSONSchemaException
import net.pwall.json.schema.codegen.Constraints.Companion.asLong
import net.pwall.json.schema.parser.Parser
import net.pwall.json.schema.parser.Parser.Companion.dropFragment
import net.pwall.json.schema.subschema.AdditionalPropertiesSchema
import net.pwall.json.schema.subschema.AllOfSchema
import net.pwall.json.schema.subschema.CombinationSchema
import net.pwall.json.schema.subschema.ExtensionSchema
import net.pwall.json.schema.subschema.ItemsSchema
import net.pwall.json.schema.subschema.PatternPropertiesSchema
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
import net.pwall.json.schema.validation.PropertiesValidator
import net.pwall.json.schema.validation.StringValidator
import net.pwall.json.schema.validation.TypeValidator
import net.pwall.json.schema.validation.UniqueItemsValidator
import net.pwall.log.Log.getLogger
import net.pwall.log.Logger
import net.pwall.mustache.Context
import net.pwall.mustache.Template
import net.pwall.mustache.parser.Parser as MustacheParser
import net.pwall.util.DefaultValue
import net.pwall.util.Strings
import net.pwall.yaml.YAMLSimple

/**
 * JSON Schema Code Generator.  The class may be parameterised either by constructor parameters or by setting the
 * appropriate variables after construction.
 *
 * @author  Peter Wall
 */
class CodeGenerator(
    /** Target language */
    var targetLanguage: TargetLanguage = TargetLanguage.KOTLIN,
    /** The primary template to use for the generation of a class */
    var templateName: String = "class",
    /** The primary template to use for the generation of an enum */
    @Suppress("MemberVisibilityCanBePrivate")
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
    var markerInterface: ClassId? = null,
    /** A [Logger] object for the output of logging messages */
    val log: Logger = getLogger(CodeGenerator::class.qualifiedName)
) {

    enum class AdditionalPropertiesOption {
        IGNORE,
        STRICT,
    }

    var additionalPropertiesOption = AdditionalPropertiesOption.IGNORE

    enum class NestedClassNameOption {
        USE_NAME_FROM_REF_SCHEMA,
        USE_NAME_FROM_PROPERTY
    }

    var nestedClassNameOption = NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA

    enum class ValidationOption {
        NONE,
        WARN,
        BLOCK,
    }

    var examplesValidationOption = ValidationOption.NONE

    var defaultValidationOption = ValidationOption.NONE

    var extensibleEnumKeyword: String? = null

    var commentTemplate: Template? = null

    private val customClassesByURI = mutableListOf<CustomClassByURI>()
    private val customClassesByFormat = mutableListOf<CustomClassByFormat>()
    private val customClassesByExtension = mutableListOf<CustomClassByExtension>()

    private val classNameMapping = mutableListOf<Pair<URI, String>>()

    fun addClassNameMapping(uri: URI, name: String) {
        classNameMapping.add(uri to name)
    }

    var schemaParser by DefaultValue {
        Parser().apply {
            options.validateExamples = examplesValidationOption != ValidationOption.NONE
            options.validateDefault = defaultValidationOption != ValidationOption.NONE
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var templateParser by DefaultValue {
        MustacheParser {name ->
            partialResolver(name)
        }
    }

    private fun partialResolver(name: String): Reader {
        for (dir in targetLanguage.directories)
            CodeGenerator::class.java.getResourceAsStream("/$dir/$name.mustache")?.let { return it.reader() }
        fatal("Can't locate template partial $name")
    }

    var template by DefaultValue {
        val parser = templateParser
        val resolver = parser.resolvePartial
        parser.parse(parser.resolver(templateName))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var interfaceTemplateName = "interface"

    @Suppress("MemberVisibilityCanBePrivate")
    var interfaceTemplate by DefaultValue {
        val parser = templateParser
        val resolver = parser.resolvePartial
        parser.parse(parser.resolver(interfaceTemplateName))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var enumTemplate by DefaultValue {
        val parser = templateParser
        val resolver = parser.resolvePartial
        parser.parse(parser.resolver(enumTemplateName))
    }

    var indexFileName: TargetFileName? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var indexTemplateName: String = "index"

    @Suppress("MemberVisibilityCanBePrivate")
    var indexTemplate by DefaultValue {
        val parser = templateParser
        val resolver = parser.resolvePartial
        parser.parse(parser.resolver(indexTemplateName))
    }

    var outputResolver by DefaultValue<OutputResolver> {
        { targetFileName ->
            targetFileName.resolve(File(baseDirectoryName)).also { checkDirectory(it.parentFile) }.writer()
        }
    }

    private val classAnnotations = mutableListOf<Pair<ClassId, Template?>>()

    fun addClassAnnotation(className: String, template: Template? = null) {
        if (!className.isValidClassName())
            fatal("Not a valid annotation class name - \"$className\"")
        classAnnotations.add(ClassName.of(className) to template)
    }

    private val fieldAnnotations = mutableListOf<Pair<ClassId, Template?>>()

    fun addFieldAnnotation(className: String, template: Template? = null) {
        if (!className.isValidClassName())
            fatal("Not a valid annotation class name - \"$className\"")
        fieldAnnotations.add(ClassName.of(className) to template)
    }

    var decimalClass = ClassName.of(BigDecimal::class)

    private val generatorContext = Context(GeneratorContext).child(object {

        @Suppress("unused")
        val additionalPropertiesOption: AdditionalPropertiesOption
            get() = this@CodeGenerator.additionalPropertiesOption

        @Suppress("unused")
        val nestedClassName: NestedClassNameOption
            get() = nestedClassNameOption

        @Suppress("unused")
        val decimalClass: ClassId
            get() = this@CodeGenerator.decimalClass

    })

    fun setTemplateDirectory(directory: File, suffix: String = "mustache") {
        when {
            directory.isFile -> fatal("Template directory must be a directory")
            directory.isDirectory -> {}
            else -> fatal("Error accessing template directory")
        }
        templateParser = MustacheParser().also {
            it.resolvePartial = { name ->
                File(directory, "$name.$suffix").reader()
            }
        }
    }

    var companionObjectForAll: Boolean? = null

    val companionObjectForClasses: MutableSet<String> = mutableSetOf()

    /**
     * Configure the `CodeGenerator` using a config file, specified by a [File].
     *
     * @param   file    a [File] pointing to the JSON or YAML config file
     * @param   uri     an optional URI (for error reporting)
     */
    fun configure(file: File, uri: URI? = null) {
        val fileName = file.name
        configure(when {
            fileName.looksLikeYAML() -> YAMLSimple.process(file).rootNode
            else -> JSON.parse(file)
        }, uri ?: file.toURI())
    }

    /**
     * Configure the `CodeGenerator` using a config file, specified by a [Path].
     *
     * @param   path    the [Path] to the JSON or YAML config file
     * @param   uri     an optional URI (for error reporting)
     */
    fun configure(path: Path, uri: URI? = null) {
        val reader = Files.newBufferedReader(path)
        val fileName = path.toFile().name
        configure(when {
            fileName.looksLikeYAML() -> YAMLSimple.process(reader).rootNode
            else -> JSON.parse(reader)
        }, uri ?: path.toUri())
    }

    /**
     * Configure the `CodeGenerator` using a config file (in the form af a parsed JSON object).
     *
     * @param   json    the JSON object
     * @param   uri     an optional URI (for error reporting)
     */
    fun configure(json: JSONValue, uri: URI? = null) {
        Configurator.configure(this, JSONReference(json), uri)
    }

    private val targets = mutableListOf<Target>()

    /**
     * Clear the target list.
     */
    fun clearTargets() {
        targets.clear()
    }

    /**
     * Add a target using the supplied details.
     *
     * @param   schema          the JSON schema
     * @param   className       the class name to use for the generated class
     * @param   subDirectories  the subdirectory names to use for package name determination
     * @param   source          an optional source comment string
     * @param   json            original JSON (for template expansion)
     */
    fun addTarget(
        schema: JSONSchema,
        className: String,
        subDirectories: List<String> = emptyList(),
        source: String = "added target",
        json: JSONValue? = null,
    ) {
        targets.add(
            Target(
                schema = schema,
                constraints = Constraints(schema),
                nameGenerator = nameGenerator,
                targetFile = TargetFileName(className, targetLanguage.ext, getOutputDirs(subDirectories)),
                source = source,
                generatorComment = generatorComment,
                commentTemplate = commentTemplate,
                json = json
            ).apply {
                markerInterface?.let { addInterface(it) }
                companionObjectNeeded = companionObjectForAll ?: (className in companionObjectForClasses)
            }
        )
    }

    private fun getOutputDirs(subDirectories: List<String>): List<String> {
        val outputDir = basePackageName?.split('.') ?: emptyList()
        return if (derivePackageFromStructure)
            outputDir + subDirectories
        else
            outputDir
    }

    /**
     * Generate classes for a set of schema files specified by URI.
     */
    fun generate(vararg inputs: URI) {
        clearTargets()
        for (uri in inputs)
            addTarget(uri)
        generateAllTargets()
    }

    /**
     * Add a target by URI.
     *
     * @param   uri             the URI
     * @param   packageNames    an optional list of subpackage names to add to base package
     */
    fun addTarget(uri: URI, packageNames: List<String> = emptyList()) {
        val json = schemaParser.jsonReader.readJSON(uri)
        val schema = schemaParser.parse(uri)
        checkValidationErrors(uri)
        addTarget(packageNames, schema, uri.toString(), json)
    }

    private fun checkValidationErrors(identifier: Any) {
        val defaultValidationErrors = schemaParser.defaultValidationErrors
        if (defaultValidationOption != ValidationOption.NONE) {
            for (validationError in defaultValidationErrors) {
                validationError.errors?.forEach {
                    if (it.error != JSONSchema.subSchemaErrorMessage)
                        log.warn { "$identifier${it.keywordLocation}: ${it.error}, at ${it.instanceLocation}" }
                }
            }
        }
        val examplesValidationErrors = schemaParser.examplesValidationErrors
        if (examplesValidationOption != ValidationOption.NONE) {
            for (validationError in examplesValidationErrors) {
                validationError.errors?.forEach {
                    if (it.error != JSONSchema.subSchemaErrorMessage)
                        log.warn { "$identifier${it.keywordLocation}: ${it.error}, at ${it.instanceLocation}" }
                }
            }
        }
        if (defaultValidationOption == ValidationOption.BLOCK && defaultValidationErrors.isNotEmpty() ||
                examplesValidationOption == ValidationOption.BLOCK && examplesValidationErrors.isNotEmpty())
            fatal("Validation errors encountered")
        defaultValidationErrors.clear()
        examplesValidationErrors.clear()
    }

    /**
     * Add a target by URI.
     *
     * @param   uri             the URI
     * @param   packageName     a subpackage name to add to base package
     */
    fun addTarget(uri: URI, packageName: String) {
        addTarget(uri, listOf(packageName))
    }

    val numTargets: Int
        get() = targets.size

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
        clearTargets()
        addTargets(inputFiles)
        generateAllTargets()
    }

    /**
     * Add targets for a set of files (specified as a [List]).  Directories will be traversed recursively.
     *
     * @param   inputFiles      the list of files
     * @param   packageNames    an optional list of subpackage names to add to base package
     */
    fun addTargets(inputFiles: List<File>, packageNames: List<String> = emptyList()) {
        val parser = schemaParser
        for (inputFile in inputFiles)
            parser.preLoad(inputFile)
        for (inputFile in inputFiles) {
            when {
                inputFile.isFile -> addTarget(inputFile, packageNames)
                inputFile.isDirectory -> addTargets(inputFile, packageNames)
            }
        }
    }

    /**
     * Add targets for a set of files (specified as a [List]).  Directories will be traversed recursively.
     *
     * @param   inputFiles      the list of files
     * @param   packageName     a subpackage name to add to base package
     */
    fun addTargets(inputFiles: List<File>, packageName: String) {
        addTargets(inputFiles, listOf(packageName))
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
        clearTargets()
        addTargetsByPath(inputPaths)
        generateAllTargets()
    }

    /**
     * Add targets for a set of files (specified as a [List] of [Path]).  Directories will be traversed recursively.
     *
     * @param   inputPaths      the list of files
     * @param   packageNames    an optional list of subpackage names to add to base package
     */
    fun addTargetsByPath(inputPaths: List<Path>, packageNames: List<String> = emptyList()) {
        val parser = schemaParser
        for (inputPath in inputPaths)
            parser.preLoad(inputPath)
        for (inputPath in inputPaths) {
            when {
                Files.isRegularFile(inputPath) -> addTarget(inputPath, packageNames)
                Files.isDirectory(inputPath) -> addTargets(inputPath, packageNames)
            }
        }
    }

    /**
     * Add targets for a set of files (specified as a [List] of [Path]).  Directories will be traversed recursively.
     *
     * @param   inputPaths      the list of files
     * @param   packageName     a subpackage name to add to base package
     */
    fun addTargetsByPath(inputPaths: List<Path>, packageName: String) {
        addTargetsByPath(inputPaths, listOf(packageName))
    }

    private fun addTargets(inputDir: File, packageNames: List<String>) {
        inputDir.listFiles()?.forEach {
            when {
                it.isDirectory -> {
                    if (!it.name.startsWith('.'))
                        addTargets(it, packageNames + it.name.mapDirectoryName())
                }
                it.isFile -> addTarget(it, packageNames)
            }
        }
    }

    private fun addTargets(inputDir: Path, packageNames: List<String>) {
        Files.newDirectoryStream(inputDir).use { dir ->
            dir.forEach {
                when {
                    Files.isDirectory(it) -> {
                        if (!it.fileName.toString().startsWith('.'))
                            addTargets(it, packageNames + it.fileName.toString().mapDirectoryName())
                    }
                    Files.isRegularFile(it) -> addTarget(it, packageNames)
                }
            }
        }
    }

    private fun String.mapDirectoryName(): String = buildString {
        for (ch in this@mapDirectoryName)
            if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9')
                append(ch)
    }

    /**
     * Add target for an individual file or directory.
     *
     * @param   inputFile       the file
     * @param   packageNames    an optional list of subpackage names to add to base package
     */
    fun addTarget(inputFile: File, packageNames: List<String> = emptyList()) {
        when {
            inputFile.isFile -> {
                val json = schemaParser.jsonReader.readJSON(inputFile)
                val schema = schemaParser.parse(inputFile)
                checkValidationErrors(schema.uri ?: inputFile)
                addTarget(packageNames, schema, inputFile.toString(), json)
            }
            inputFile.isDirectory -> {
                schemaParser.preLoad(inputFile)
                addTargets(inputFile, packageNames)
            }
        }
    }

    /**
     * Add target for an individual file.
     *
     * @param   inputFile       the file
     * @param   packageName     a subpackage name to add to base package
     */
    fun addTarget(inputFile: File, packageName: String) {
        addTarget(inputFile, listOf(packageName))
    }

    /**
     * Add target for an individual file or directory specified by [Path].
     *
     * @param   inputPath       the [Path]
     * @param   packageNames    an optional list of subpackage names to add to base package
     */
    fun addTarget(inputPath: Path, packageNames: List<String> = emptyList()) {
        when {
            Files.isRegularFile(inputPath) -> {
                val json = schemaParser.jsonReader.readJSON(inputPath)
                val schema = schemaParser.parse(inputPath)
                checkValidationErrors(schema.uri ?: inputPath)
                addTarget(packageNames, schema, inputPath.toString(), json)
            }
            Files.isDirectory(inputPath) -> {
                schemaParser.preLoad(inputPath)
                addTargets(inputPath, packageNames)
            }
        }
    }

    /**
     * Add target for an individual file specified by [Path].
     *
     * @param   inputPath       the [Path]
     * @param   packageName     a subpackage name to add to base package
     */
    fun addTarget(inputPath: Path, packageName: String) {
        addTarget(inputPath, listOf(packageName))
    }

    /**
     * Generate all targets added to the target list.
     */
    fun generateAllTargets() {
        processTargetCrossReferences()
        for (target in targets)
            generateTarget(target)
        generateIndex()
    }

    private fun generateIndex() {
        indexFileName?.let { name ->
            log.info { "-- index $name" }
            outputResolver(name).use {
                indexTemplate.processTo(AppendableFilter(it), TargetIndex(
                    targets = targets.filter { t ->
                        t.constraints.isObject ||
                                t.constraints.isEnumClass && t.constraints.enumValues.let { e ->
                                    e != null && allIdentifier(e)
                                }
                    },
                    targetFile = name,
                    generatorComment = generatorComment),
                )
            }
        }
    }

    data class TargetIndex(
        val targets: List<Target>,
        val targetFile: TargetFileName,
        val generatorComment: String?,
    ) {

        @Suppress("unused")
        val commentLines = generatorComment?.split('\n')

    }

    private fun processTargetCrossReferences() {
        for (target in targets)
            processSchema(target.schema, target.constraints)
        for (target in targets)
            if (target.constraints.isObject)
                target.validationsPresent = analyseObject(target, target, target.constraints)
        for (target in targets)
            findOneOfDerivedClasses(target.constraints, target)
    }

    private fun generateTarget(target: Target) {
        log.info { "Generating for ${target.source}" }
        nameGenerator = NameGenerator()
        val constraints = target.constraints
        when {
            constraints.isObject -> { // does it look like an object? generate a class
                log.info { "-- target class ${target.qualifiedClassName}" }
                constraints.applyAnnotations(classAnnotations, target, target)
                for (property in constraints.properties)
                    property.fieldAnnotated = Annotated().apply { applyAnnotations(fieldAnnotations, target, property) }
                for (nestedClass in target.nestedClasses) {
                    val nestedClassConstraints = nestedClass.constraints
                    nestedClassConstraints.applyAnnotations(classAnnotations, target, nestedClass)
                    for (property in nestedClassConstraints.properties) {
                        property.fieldAnnotated = Annotated().apply {
                            applyAnnotations(fieldAnnotations, target, property)
                        }
                    }
                }
                target.systemClasses.sortBy { it.order }
                target.imports.sort()
                outputResolver(target.targetFile).use {
                    template.appendTo(AppendableFilter(it), generatorContext.child(target))
                }
            }
            constraints.oneOfSchemata.any { it.isObject } -> {
                // it wasn't an object, but it had a oneOf with object children
                log.info { "-- target interface ${target.qualifiedClassName}" }
                outputResolver(target.targetFile).use {
                    interfaceTemplate.appendTo(AppendableFilter(it), generatorContext.child(target))
                }
            }
            constraints.isString && constraints.enumValues.let { it != null && allIdentifier(it) } -> {
                log.info { "-- target enum ${target.qualifiedClassName}" }
                outputResolver(target.targetFile).use {
                    enumTemplate.appendTo(AppendableFilter(it), generatorContext.child(target))
                }
            }
            else -> log.info { "-- nothing to generate for ${target.className}" }
        }
    }

    private fun findOneOfDerivedClasses(constraints: Constraints, target: Target) {
        for (i in constraints.oneOfSchemata.indices) {
            val oneOfItem = constraints.oneOfSchemata[i]
            val oneOfTarget = oneOfItem.schema.findTarget()
            if (oneOfTarget != null) {
                if (!constraints.isObject) {
                    oneOfTarget.addInterface(target)
                    target.derivedClasses.add(oneOfTarget)
                }
                else
                    createCombinedClass(i, constraints, oneOfTarget.constraints, target)
            }
            else {
                if (oneOfItem.isObject)
                    createCombinedClass(i, constraints, oneOfItem, target)
            }
        }
    }

    private fun createCombinedClass(i: Int, constraints: Constraints, additionalConstraints: Constraints,
            target: Target) {
        // create a nested class with current as a base class and oneOfTarget properties,
        // and remove (merge?) overlapping properties
        val nestedConstraints = Constraints(constraints.schema)
        for (property in constraints.properties) {
            nestedConstraints.properties.add(NamedConstraints(property.schema, property.name).also {
                it.copyFrom(property)
                it.baseProperty = property
            })
        }
        nestedConstraints.required.addAll(constraints.required)
        for (property in additionalConstraints.properties) {
            val existingProperty = nestedConstraints.properties.find { it.name == property.name }
            if (existingProperty != null)
                existingProperty.validations.addAll(property.validations)
            else {
                nestedConstraints.properties.add(NamedConstraints(property.schema, property.name).also {
                    it.copyFrom(property)
                })
            }
            if (additionalConstraints.required.contains(property.name))
                nestedConstraints.required.add(property.name)
        }
        val nestedClass = target.addNestedClass(nestedConstraints, null, Strings.toIdentifier(i))
        nestedClass.baseClass = target
        nestedClass.validationsPresent = analyseProperties(target, nestedConstraints)
        target.derivedClasses.add(nestedClass)
    }

    private fun JSONSchema.findTarget(): Target? {
        return (this as? JSONSchema.General)?.children?.singleOrNull()?.let { ref ->
            if (ref is RefSchema) targets.find { it.schema === ref.target } else null
        }
    }

    /**
     * Generate a single class.
     *
     * @param   schema          the [JSONSchema]
     * @param   className       the class name
     * @param   subDirectories  list of subdirectory names to use for the output file
     * @param   json            original JSON (for template expansion)
     */
    fun generateClass(
        schema: JSONSchema,
        className: String,
        subDirectories: List<String> = emptyList(),
        json: JSONValue? = null,
    ) {
        clearTargets()
        addTarget(
            schema = schema,
            className = className,
            subDirectories = subDirectories,
            source = schema.uri?.toString() ?: internalSchema,
            json = json,
        )
        generateAllTargets()
    }

    /**
     * Generate classes as specified by a list of pairs - Schema and class name.
     *
     * @param   schemaList          list of [Pair] of [JSONSchema] and [String] (class name)
     * @param   subDirectories      list of subdirectory names to use for the output files
     * @param   json                original JSON (for template expansion)
     * @param   logCommentFunction  optional function to create logging comment
     */
    fun generateClasses(
        schemaList: List<Pair<JSONSchema, String>>,
        subDirectories: List<String> = emptyList(),
        json: JSONValue? = null,
        logCommentFunction: ((String) -> String)? = null,
    ) {
        clearTargets()
        for (entry in schemaList) {
            addTarget(
                schema = entry.first,
                className = entry.second,
                subDirectories = subDirectories,
                source = logCommentFunction?.invoke(entry.second) ?: entry.first.uri?.toString() ?: internalSchema,
                json = json,
            )
        }
        generateAllTargets()
    }

    /**
     * Generate classes for all definitions in a composite file (e.g. schema definitions embedded in an OpenAPI or
     * Swagger document).
     *
     * @param   base            the base of the composite object
     * @param   pointer         pointer to the structure containing the schema definitions (e.g. /definitions)
     * @param   subDirectories  list of subdirectory names to use for the output files
     * @param   uri             the default URI of the document
     * @param   filter          optional filter to select which classes to include (by name)
     */
    fun generateAll(
        base: JSONValue,
        pointer: JSONPointer,
        subDirectories: List<String> = emptyList(),
        uri: URI = URI("https:/pwall.net/internal"),
        filter: (String) -> Boolean = { true }
    ) {
        clearTargets()
        addCompositeTargets(base, pointer, subDirectories, uri, filter)
        generateAllTargets()
    }

    /**
     * Add targets for all definitions in a composite file (e.g. schema definitions embedded in an OpenAPI or Swagger
     * document) for a file located by URI.
     *
     * @param   uri             the URI of the document
     * @param   pointer         pointer to the structure containing the schema definitions (e.g. /definitions)
     * @param   filter          optional filter to select which classes to include (by name)
     */
    fun addCompositeTargets(
        uri: URI,
        pointer: JSONPointer,
        filter: (String) -> Boolean = { true }
    ) = addCompositeTargets(
        base = schemaParser.jsonReader.readJSON(uri),
        pointer = pointer,
        uri = uri,
        filter = filter
    )

    /**
     * Add targets for all definitions in a composite file (e.g. schema definitions embedded in an OpenAPI or Swagger
     * document).
     *
     * @param   base            the base of the composite object
     * @param   pointer         pointer to the structure containing the schema definitions (e.g. /definitions)
     * @param   subDirectories  list of subdirectory names to use for the output files
     * @param   uri             the default URI of the document
     * @param   filter          optional filter to select which classes to include (by name)
     */
    fun addCompositeTargets(
        base: JSONValue,
        pointer: JSONPointer,
        subDirectories: List<String> = emptyList(),
        uri: URI = URI("https:/pwall.net/internal"),
        filter: (String) -> Boolean = { true }
    ) {
        val documentURI = Parser.getIdOrNull(base)?.let { URI(it) } ?: uri
        val definitions = (pointer.find(base) as? JSONMapping<*>) ?: fatal("Can't find definitions - $pointer")
        for (name in definitions.keys) {
            if (filter(name))
                addTarget(
                    schema = schemaParser.parseSchema(base, pointer.child(name), documentURI).also {
                        checkValidationErrors(uri)
                    },
                    className = name,
                    subDirectories = subDirectories,
                    source = "$documentURI#$pointer/$name",
                    json = base,
                )
        }
    }

    private fun addTarget(subDirectories: List<String>, schema: JSONSchema, source: String, json: JSONValue) {
        val className = schema.uri?.let { uri ->
            classNameMapping.find { it.first == uri }?.second ?: run {
                // TODO change to allow name ending with "/schema"?
                uri.toString().substringBefore('#').substringAfterLast(':').substringAfterLast('/')
                    .replace(
                        Regex("""(?:[.-_]json)?(?:[.-_]schema)?(?:\.(?:json|ya?ml))?$""", RegexOption.IGNORE_CASE),
                        ""
                    ).split('-', '.', '_').joinToString(separator = "") { Strings.capitalise(it) }.sanitiseName()
            }
        } ?: "GeneratedClass$numTargets"
        addTarget(
            schema = schema,
            className = className,
            subDirectories = subDirectories,
            source = source,
            json = json,
        )
    }

    private fun analyseObject(target: Target, classDescriptor: ClassDescriptor, constraints: Constraints): Boolean {
        constraints.objectValidationsPresent?.let { return it }
        (constraints.schema as? JSONSchema.General)?.let {
            for (child in it.children) {
                if (child is PropertiesSchema)
                    break
                if (child is AllOfSchema) {
                    child.array.firstOrNull()?.findRefChild()?.let { refChild ->
                        val refTarget = targets.find { t -> t.schema.locationMatches(refChild.target) }
                        if (refTarget != null) {
                            refTarget.derivedClasses.add(classDescriptor)
                            classDescriptor.baseClass = refTarget
                            target.addImport(refTarget)
                            analyseObject(refTarget, refTarget, refTarget.constraints)
                            return analyseDerivedObject(target, constraints, refTarget)
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
        analysePropertiesRequired(constraints)
        var validationsPresent = false
        constraints.properties.forEach { property ->
            val baseConstraints = refTarget.constraints.properties.find { it.propertyName == property.propertyName }
            if (baseConstraints != null) {
                baseConstraints.localType?.let {
                    if (it.packageName != null)
                        target.addImport(it)
                }
                property.baseProperty = baseConstraints
                when {
                    baseConstraints.isEnumClass -> {
                        if (property.processAdditionalConstraintsEnum(baseConstraints,
                                baseConstraints.localType, baseConstraints.enumValues))
                            validationsPresent = true
                    }
                    baseConstraints.isLocalType -> {}
                    baseConstraints.isString -> {
                        if (property.processAdditionalConstraintsString(baseConstraints, target))
                            validationsPresent = true
                    }
                    baseConstraints.isInt -> {
                        if (property.processAdditionalConstraintsInt(baseConstraints, target))
                            validationsPresent = true
                    }
                    baseConstraints.isLong -> {
                        if (property.processAdditionalConstraintsLong(baseConstraints))
                            validationsPresent = true
                    }
                    baseConstraints.isArray -> {
                        if (property.processAdditionalConstraintsArray(baseConstraints, target))
                            validationsPresent = true
                    }
                    // TODO other types with additional constraints?? (decimal, object)
                }
                if (baseConstraints.isArray)
                    property.arrayItems = baseConstraints.arrayItems
                val customClass = findCustomClass(baseConstraints.schema, target) ?:
                        baseConstraints.schema.findRefChild()?.let { findCustomClass(it.target, target) }
                if (customClass != null)
                    property.localType = customClass
                else {
                    property.localType = baseConstraints.localType?.also {
                        if (it is CustomClass)
                            target.addImport(it)
                    }
                    baseConstraints.systemClass?.let {
                        property.systemClass = it
                        target.systemClasses.addOnce(it)
                    }
                }
                if (!property.sameType(baseConstraints)) {
                    baseConstraints.extendedInDerived = true
                    property.extendedFromBase = true
                }
            }
            else {
                if (analyseProperty(target, property, property, property.name))
                    validationsPresent = true
            }
        }
        return validationsPresent
    }

    private fun NamedConstraints.processAdditionalConstraintsArray(baseConstraints: Constraints, target: Target): Boolean {
        var validationsPresent = false
        arrayItems?.let {
            baseConstraints.arrayItems.let { baseItems ->
                if (baseItems == null || it != baseItems) {
                    if (analyseProperty(target, it, this, name.depluralise())) {
                        addValidation(Validation.Type.ARRAY_ITEMS)
                        validationsPresent = true
                    }
                }
            }
        }
        if (checkMinMaxItems(minItems?.takeIf { baseConstraints.minItems.let { b -> b == null || b < it } },
                maxItems?.takeIf { baseConstraints.maxItems.let { b -> b == null || b > it } }))
            validationsPresent = true
        defaultValue?.let {
            if (it.type != JSONSchema.Type.ARRAY)
                defaultValue = null
        }
        return validationsPresent
    }

    private fun Constraints.processAdditionalConstraintsString(baseConstraints: Constraints, target: Target): Boolean {
        var validationsPresent = false
        if (constValue != null && constValue != baseConstraints.constValue) {
            (constValue as? JSONString)?.let {
                val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.value))
                addValidation(Validation.Type.CONST_STRING, stringStatic)
                validationsPresent = true
            }
        }
        enumValues?.let { values ->
            baseConstraints.enumValues.let { baseValues ->
                if ((baseValues == null || !values.containsAll(baseValues)) && values.all { it is JSONString }) {
                    target.systemClasses.addOnce(SystemClass.ARRAYS)
                    target.systemClasses.addOnce(SystemClass.LIST)
                    val arrayStatic = target.addStatic(Target.StaticType.STRING_ARRAY, "cg_array",
                        values.map { StringValue(it.toString()) })
                    addValidation(Validation.Type.ENUM_STRING, arrayStatic)
                    validationsPresent = true
                }
            }
        }
        if (checkMinMaxLength(minLength?.takeIf { baseConstraints.minLength.let { b -> b == null || b < it } },
                maxLength?.takeIf { baseConstraints.maxLength.let { b -> b == null || b > it } }))
            validationsPresent = true

        validationsPresent = checkRegex(target) || validationsPresent
        return validationsPresent
    }

    private fun Constraints.processAdditionalConstraintsInt(baseConstraints: Constraints, target: Target): Boolean {
        var validationsPresent = false
        constValue?.let {
            if (it != baseConstraints.constValue) {
                if (checkConstInt(it)) {
                    validationsPresent = true
                }
            }
        }
        enumValues?.let { values ->
            baseConstraints.enumValues.let { baseValues ->
                if ((baseValues == null || !values.containsAll(baseValues)) && values.all { it is JSONNumberValue }) {
                    target.systemClasses.addOnce(SystemClass.ARRAYS)
                    target.systemClasses.addOnce(SystemClass.LIST)
                    val arrayStatic = target.addStatic(Target.StaticType.INT_ARRAY, "cg_array", values.map {
                        when (it) {
                            is JSONInteger -> NumberValue(it.value)
                            is JSONLong -> NumberValue(it.value)
                            is JSONDecimal -> NumberValue(it.value)
                            else -> NumberValue(0)
                        }
                    })
                    addValidation(Validation.Type.ENUM_INT, arrayStatic)
                    validationsPresent = true
                }
            }
        }
        if (checkMinMaxInt(minimumLong?.takeIf { baseConstraints.minimumLong.let { b -> b == null || b < it } },
                maximumLong?.takeIf { baseConstraints.maximumLong.let { b -> b == null || b > it } }))
            validationsPresent = true
        if (multipleOf.isNotEmpty() &&
            (baseConstraints.multipleOf.isEmpty() || baseConstraints.multipleOf.containsAll(multipleOf))) {
            for (multiple in multipleOf) {
                if (!baseConstraints.multipleOf.contains(multiple)) {
                    addValidation(Validation.Type.MULTIPLE_INT, multiple.asLong())
                    validationsPresent = true
                }
            }
        }
        return validationsPresent
    }

    private fun Constraints.processAdditionalConstraintsLong(baseConstraints: Constraints): Boolean {
        var validationsPresent = false
        constValue?.let {
            if (it != baseConstraints.constValue) {
                if (checkConstLong(it)) {
                    validationsPresent = true
                }
            }
        }
        // TODO do we need enum check for long?
        if (checkMinMaxLong(minimumLong?.takeIf { baseConstraints.minimumLong.let { b -> b == null || b < it } },
                maximumLong?.takeIf { baseConstraints.maximumLong.let { b -> b == null || b > it } }))
            validationsPresent = true
        if (multipleOf.isNotEmpty() &&
                (baseConstraints.multipleOf.isEmpty() || baseConstraints.multipleOf.containsAll(multipleOf))) {
            for (multiple in multipleOf) {
                if (!baseConstraints.multipleOf.contains(multiple)) {
                    addValidation(Validation.Type.MULTIPLE_INT, multiple.asLong())
                    validationsPresent = true
                }
            }
        }
        return validationsPresent
    }

    private fun Constraints.processAdditionalConstraintsEnum(
        baseConstraints: Constraints,
        localType: ClassId?,
        enumValues: JSONSequence<*>?,
    ): Boolean {
        isEnumClass = true
        if (localType != null && enumValues != null) {
            this.localType = localType
            this.enumValues = enumValues
            (defaultValue ?: baseConstraints.defaultValue)?.let { value ->
                val valueString = value.defaultValue.toString()
                defaultValue = if (value.type == JSONSchema.Type.STRING &&
                        enumValues.any { a -> a.toString() == valueString }) {
                    Constraints.DefaultPropertyValue(
                        defaultValue = EnumValue(localType.className, valueString),
                        type = JSONSchema.Type.STRING,
                    )
                } else null
            }
            if (constValue != baseConstraints.constValue) {
                (constValue as? JSONString)?.let {
                    if (enumValues.any { a -> a.toString() == it.value }) {
                        addValidation(Validation.Type.CONST_ENUM, EnumValue(localType.className, it.value))
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun analysePropertiesRequired(constraints: Constraints) {
        constraints.properties.forEach { property ->
            when {
                property.name in constraints.required -> property.isRequired = true
                property.nullable == true || property.defaultValue != null -> {}
                else -> property.nullable = true // should be error, but that would be unhelpful
            }
        }
    }

    /**
     * Decision table
     * ```
     * additionalPropertiesOption == IGNORE                    Y N N N N N N N N
     * additionalProperties is (false/true/schema)             - F F F T S S S S
     * named properties present                                - - Y N - Y Y N N
     * pattern properties present                              - N Y Y - Y N Y N
     *
     * output normal class                                     X X - - - - - - -
     * output map-based class                                  - - X X X X X X X
     * use Map<String, Any?>                                   - - X X X X X X -
     * derive map type from additionalProperties               - - - - - - - - X
     * check type and add validations for named properties     - - - X - X X - -
     * check type and add validations for pattern properties   - - X X - X - X -
     * add validation, no excess properties allowed            - - X X - - - - -
     * add validation, excess properties must be a/p type      - - - - - X X X -
     * ```
     */
    private fun analyseProperties(target: Target, constraints: Constraints): Boolean {
        analysePropertiesRequired(constraints)
        var additionalPropertiesValidationRequired = false
        if (additionalPropertiesOption != AdditionalPropertiesOption.IGNORE) {
            for (i in constraints.patternProperties.indices) {
                val patternPropertyTriple = constraints.patternProperties[i]
                val patternPropertyConstraints = patternPropertyTriple.second
                analyseProperty(target, patternPropertyConstraints, patternPropertyConstraints, "patternProperty")
                val patternPropertyRegex = patternPropertyTriple.first
                val patternPropertyStatic = target.addStatic(Target.StaticType.PATTERN, "cg_regex",
                        StringValue(patternPropertyRegex.toString()))
                val newPatternPropertyTriple = Triple(patternPropertyRegex, patternPropertyConstraints,
                        patternPropertyStatic)
                constraints.patternProperties.removeAt(i)
                constraints.patternProperties.add(i, newPatternPropertyTriple)
                constraints.addValidation(Validation.Type.PATTERN_PROPERTIES, newPatternPropertyTriple)
            }
            constraints.additionalProperties?.let {
                when (it.schema) {
                    is JSONSchema.True -> {}
                    is JSONSchema.False -> if (constraints.patternProperties.isNotEmpty()) {
                        // if no patternProperties it will be generated as a data class, so no need to check unexpected
                        constraints.addValidation(Validation.Type.UNEXPECTED_PROPERTIES)
                        additionalPropertiesValidationRequired = true
                    }
                    else -> {
                        additionalPropertiesValidationRequired = analyseProperty(target, it, it, "additionalProperties")
                        // if no properties or patternProperties, the map will use the a/p type, so no check needed
                        // also, no point in checking if the a/p type is Any?
                        if (!(constraints.properties.isEmpty() && constraints.patternProperties.isEmpty()) &&
                                !it.isUntyped) {
                            constraints.addValidation(Validation.Type.ADDITIONAL_PROPERTIES, it)
                            additionalPropertiesValidationRequired = true
                        }
                    }
                }
            }
            constraints.minProperties?.let { minP ->
                constraints.maxProperties?.let { maxP ->
                    if (minP == maxP)
                        constraints.addValidation(Validation.Type.CONST_PROPERTIES, minP)
                    else
                        constraints.addValidation(Validation.Type.RANGE_PROPERTIES, minP to maxP)
                } ?: constraints.addValidation(Validation.Type.MINIMUM_PROPERTIES, NumberValue(minP))
            } ?: constraints.maxProperties?.let { maxP ->
                constraints.addValidation(Validation.Type.MAXIMUM_PROPERTIES, maxP)
            }
        }
        return constraints.properties.fold(additionalPropertiesValidationRequired) { result, property ->
            analyseProperty(target, property, property, property.name) || result
        }
    }

    private fun useTarget(constraints: Constraints, target: Target, otherTarget: Target) {
        target.addImport(otherTarget)
        constraints.localType = otherTarget
    }

    private fun findRefClass(constraints: Constraints, target: Target): Boolean {
        targets.find { it.schema === constraints.schema }?.let {
            useTarget(constraints, target, it)
            return true
        }
        val refChild = constraints.schema.findRefChild()
        refChild?.let { targets.find { t -> t.schema === it.target } }?.let {
            useTarget(constraints, target, it)
            return true
        }
        return false
    }

    private fun findTargetClass(
        constraints: Constraints,
        refConstraints: Constraints,
        target: Target,
        defaultName: () -> String,
    ) {
        if (!findRefClass(constraints, target)) {
            val nestedClassName = refConstraints.uri?.let { uri ->
                val location = refConstraints.schema.location.toString()
                classNameMapping.find { it.first.dropFragment() == uri && it.first.fragment == location }?.second
            } ?: when (nestedClassNameOption) {
                NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA ->
                    refConstraints.schema.findRefChild()?.fragment?.substringAfterLast('/') ?: defaultName()
                NestedClassNameOption.USE_NAME_FROM_PROPERTY -> defaultName()
            }
            val nestedClass = target.addNestedClass(constraints, constraints.schema,
                    Strings.capitalise(nestedClassName).replace(Regex("_+([a-z])")) { Strings.capitalise(it.groups[1]!!.value) })
            nestedClass.validationsPresent = analyseObject(target, nestedClass, constraints)
            constraints.localType = nestedClass
        }
    }

    private fun findCustomClass(schema: JSONSchema, target: Target): ClassId? {
        customClassesByExtension.find { it.match(schema) }?.let {
            it.applyToTarget(target)
            return it
        }
        schema.uri?.resolve(schema.location.toURIFragment())?.let { uri ->
            customClassesByURI.find { uri.resolve(it.uri) == uri }?.let {
                it.applyToTarget(target)
                return it
            }
        }
        return null
    }

    private fun analyseProperty(
        target: Target,
        property: Constraints,
        arrayProperty: Constraints,
        name: String,
    ): Boolean {
        // true == validations present
        findCustomClass(property.schema, target)?.let {
            property.localType = it
            return analyseCustomClass(property)
        }
        customClassesByFormat.find { it.match(property) }?.let {
            it.applyToTarget(target)
            property.localType = it
            return analyseCustomClass(property)
        }
        property.schema.findRefChild()?.let { refChild ->
            findCustomClass(refChild.target, target)?.let {
                property.localType = it
                return analyseCustomClass(property)
            }
        }
        return when {
            property.isObject -> {
                val referringProperty = arrayProperty.takeIf { it.schema.findRefChild() != null } ?: property
                findTargetClass(property, referringProperty, target) { name }
                false
            }
            property.isArray -> analyseArray(target, property, name)
            property.isInt -> analyseInt(property, target)
            property.isLong -> analyseLong(property, target)
            property.isDecimal -> {
                target.systemClasses.addOnce(SystemClass.DECIMAL)
                property.systemClass = SystemClass.DECIMAL
                analyseDecimal(target, property)
            }
            property.isString -> analyseString(property, target) { name }
            property.isBoolean -> false
            else -> {
                findRefClass(property, target)
                false
            }
        }
    }

    private fun analyseCustomClass(property: Constraints): Boolean {
        property.localType?.let { classId ->
            val customClass: KClass<*> = try {
                Class.forName(classId.qualifiedClassName).kotlin
            } catch (_: Exception) {
                return false
            }
            var validationsPresent = false
            // if custom class implements CharSequence, allow minLength / maxLength
            if (customClass.isSubclassOf(CharSequence::class)) {
                if (property.checkMinMaxLength(property.minLength, property.maxLength))
                    validationsPresent = true
                property.negatedConstraints?.let { nc ->
                    if (nc.checkMinMaxLength(nc.minLength, nc.maxLength))
                        validationsPresent = true
                }
            }
            // TODO if custom class implements Comparable<itself>, allow minimum / maximum? (number only?)
            // TODO if custom class is an Enum, allow default?
            return validationsPresent
        }
        return false
    }

    private fun analyseArray(target: Target, property: Constraints, name: String): Boolean {
        target.systemClasses.addOnce(if (property.uniqueItems) SystemClass.SET else SystemClass.LIST)
        var validationsPresent = false
        property.arrayItems?.let { item ->
            if (analyseProperty(target, item, property, name.depluralise())) {
                property.addValidation(Validation.Type.ARRAY_ITEMS)
                validationsPresent = true
            }
            if (item.isEnumClass) {
                (property.defaultValue?.defaultValue as? List<*>)?.let { array ->
                    property.defaultValue = Constraints.DefaultPropertyValue(
                        defaultValue = array.mapNotNull{
                            (it as? Constraints.DefaultPropertyValue)?.let { defaultItem ->
                                val enumDefault = EnumValue(item.localType!!.className, defaultItem.defaultValue.toString())
                                Constraints.DefaultPropertyValue(enumDefault, JSONSchema.Type.STRING)
                            }
                        },
                        type = JSONSchema.Type.ARRAY,
                    )
                }
            }
        }
        if (property.checkMinMaxItems(property.minItems, property.maxItems))
            validationsPresent = true
        property.negatedConstraints?.let {
            // TODO - does it make sense to process negated array item definitions?
            if (it.checkMinMaxItems(it.minItems, it.maxItems))
                validationsPresent = true
        }
        property.defaultValue?.let {
            if (it.type != JSONSchema.Type.ARRAY)
                property.defaultValue = null
        }
        return validationsPresent
    }

    private fun Constraints.checkMinMaxItems(minimumItems: Int?, maximumItems: Int?): Boolean {
        return minimumItems?.takeIf { it > Int.MIN_VALUE }?.let { minV ->
            maximumItems?.takeIf { it < Int.MAX_VALUE }?.let { maxV ->
                if (minV == maxV)
                    addValidation(Validation.Type.CONST_ITEMS, NumberValue(minV))
                else
                    addValidation(Validation.Type.RANGE_ITEMS, minV to maxV)
            } ?: addValidation(Validation.Type.MIN_ITEMS, NumberValue(minV))
            true
        } ?: maximumItems?.takeIf { it < Int.MAX_VALUE }?.let {
            addValidation(Validation.Type.MAX_ITEMS, NumberValue(it))
            true
        } ?: false
    }

    private fun analyseString(property: Constraints, target: Target, defaultName: () -> String): Boolean {
        var validationsPresent = analyseFormat(target, property)
        if (property.systemClass != null)
            return false
        property.enumValues?.let { array ->
            if (allIdentifier(array)) {
                property.isEnumClass = true
                findTargetClass(property, property, target, defaultName)
                property.defaultValue?.let {
                    if (it.type == JSONSchema.Type.STRING &&
                            array.any { a -> a.toString() == it.defaultValue.toString() } ) {
                        val enumDefault = EnumValue(property.localType!!.className, it.defaultValue.toString())
                        property.defaultValue = Constraints.DefaultPropertyValue(enumDefault, JSONSchema.Type.STRING)
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
                val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.value))
                property.addValidation(Validation.Type.CONST_STRING, stringStatic)
                validationsPresent = true
            }
        }
        if (property.checkMinMaxLength(property.minLength, property.maxLength))
            validationsPresent = true
        property.negatedConstraints?.let { nc ->
            nc.enumValues?.let { array ->
                if (array.all { it is JSONString }) {
                    target.systemClasses.addOnce(SystemClass.ARRAYS)
                    target.systemClasses.addOnce(SystemClass.LIST)
                    val arrayStatic = target.addStatic(Target.StaticType.STRING_ARRAY, "cg_array",
                            array.map { StringValue(it.toString()) })
                    nc.addValidation(Validation.Type.ENUM_STRING, arrayStatic)
                    validationsPresent = true
                }
            }
            nc.constValue?.let {
                if (it is JSONString) {
                    val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.value))
                    nc.addValidation(Validation.Type.CONST_STRING, stringStatic)
                    validationsPresent = true
                }
            }
            if (nc.checkMinMaxLength(nc.minLength, nc.maxLength))
                validationsPresent = true
        }
        validationsPresent = property.checkRegex(target) || validationsPresent
        return validationsPresent
    }

    private fun Constraints.checkMinMaxLength(minimumLength: Int?, maximumLength: Int?): Boolean {
        return minimumLength?.takeIf { it > Int.MIN_VALUE }?.let { minV ->
            maximumLength?.takeIf { it < Int.MAX_VALUE }?.let { maxV ->
                if (minV == maxV)
                    addValidation(Validation.Type.CONST_LENGTH, NumberValue(minV))
                else
                    addValidation(Validation.Type.RANGE_LENGTH, minV to maxV)
            } ?: addValidation(Validation.Type.MIN_LENGTH, NumberValue(minV))
            true
        } ?: maximumLength?.takeIf { it < Int.MAX_VALUE }?.let {
            addValidation(Validation.Type.MAX_LENGTH, NumberValue(it))
            true
        } ?: false
    }

    private fun analyseInt(property: Constraints, target: Target): Boolean {
        var result = applyIntValidations(property, target)
        property.negatedConstraints?.let { result = applyIntValidations(it, target) || result }
        return result
    }

    private fun applyIntValidations(property: Constraints, target: Target): Boolean {
        var result = false
        property.constValue?.let {
            if (property.checkConstInt(it)) {
                property.enumValues = null
                result = true
            }
        }
        property.enumValues?.let { array ->
            if (array.all { it is JSONNumberValue }) {
                target.systemClasses.addOnce(SystemClass.ARRAYS)
                target.systemClasses.addOnce(SystemClass.LIST)
                val arrayStatic = target.addStatic(Target.StaticType.INT_ARRAY, "cg_array", array.map {
                    when (it) {
                        is JSONInteger -> NumberValue(it.value)
                        is JSONLong -> NumberValue(it.value)
                        is JSONDecimal -> NumberValue(it.value)
                        else -> NumberValue(0)
                    }
                })
                property.addValidation(Validation.Type.ENUM_INT, arrayStatic)
                result = true
            }
        }
        if (property.checkMinMaxInt(property.minimumLong, property.maximumLong))
            result = true
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

    private fun Constraints.checkMinMaxInt(minimumLong: Long?, maximumLong: Long?): Boolean {
        return minimumLong?.takeIf { it in (Int.MIN_VALUE + 1)..Int.MAX_VALUE }?.let { minV ->
            maximumLong?.takeIf { it in Int.MIN_VALUE until Int.MAX_VALUE }?.let { maxV ->
                addValidation(Validation.Type.RANGE_INT, minV to maxV)
            } ?: addValidation(Validation.Type.MINIMUM_INT, minV)
            true
        } ?: maximumLong?.takeIf { it in Int.MIN_VALUE until Int.MAX_VALUE }?.let {
            addValidation(Validation.Type.MAXIMUM_INT, it)
            true
        } ?: false
    }

    private fun Constraints.checkConstInt(constValue: JSONValue): Boolean {
        when (constValue) {
            is JSONInteger -> {
                addValidation(Validation.Type.CONST_INT, constValue.value)
                return true
            }
            is JSONLong -> {
                constValue.value.let { v ->
                    if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                        addValidation(Validation.Type.CONST_INT, v)
                        return true
                    }
                }
            }
            is JSONDecimal -> {
                constValue.value.asLong().let { v ->
                    if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                        addValidation(Validation.Type.CONST_INT, v)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun analyseLong(property: Constraints, target: Target): Boolean {
        var result = applyLongValidations(property, target)
        property.negatedConstraints?.let { result = applyLongValidations(it, target) || result }
        return result
    }

    private fun applyLongValidations(property: Constraints, target: Target): Boolean {
        var result = false
        property.constValue?.let {
            if (property.checkConstLong(it))
                result = true
        }
        if (property.checkMinMaxLong(property.minimumLong, property.maximumLong))
            result = true
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

    private fun Constraints.checkMinMaxLong(minimumLong: Long?, maximumLong: Long?): Boolean {
        return minimumLong?.takeIf { it > Long.MIN_VALUE }?.let { minV ->
            maximumLong?.takeIf { it < Long.MAX_VALUE }?.let { maxV ->
                addValidation(Validation.Type.RANGE_LONG, minV to maxV)
            } ?: addValidation(Validation.Type.MINIMUM_LONG, minV)
            true
        } ?: maximumLong?.takeIf { it < Long.MAX_VALUE }?.let {
            addValidation(Validation.Type.MAXIMUM_LONG, it)
            true
        } ?: false
    }

    private fun Constraints.checkConstLong(constValue: JSONValue): Boolean {
        when (constValue) {
            is JSONInteger -> {
                addValidation(Validation.Type.CONST_LONG, constValue.value)
                return true
            }
            is JSONLong -> {
                addValidation(Validation.Type.CONST_LONG, constValue.value)
                return true
            }
            is JSONDecimal -> {
                addValidation(Validation.Type.CONST_LONG, constValue.value.asLong())
                return true
            }
        }
        return false
    }

    private fun analyseDecimal(target: Target, property: Constraints): Boolean {
        var result = applyDecimalValidations(target, property)
        property.negatedConstraints?.let { result = applyDecimalValidations(target, it) || result }
        return result
    }

    private fun applyDecimalValidations(target: Target, property: Constraints): Boolean {
        var result = false
        property.constValue?.let {
            if (it is JSONNumberValue) {
                if (it.isZero())
                    property.addValidation(Validation.Type.CONST_DECIMAL_ZERO)
                else {
                    val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec",
                            NumberValue(it.bigDecimalValue()))
                    property.addValidation(Validation.Type.CONST_DECIMAL, decimalStatic)
                }
                result = true
            }
        }
        property.minimum?.let { minV ->
            property.maximum?.let { maxV ->
                val decimalStatic1 = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(minV))
                val decimalStatic2 = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(maxV))
                property.addValidation(Validation.Type.RANGE_DECIMAL, decimalStatic1 to decimalStatic2)
            } ?: run {
                if (minV.isZero())
                    property.addValidation(Validation.Type.MINIMUM_DECIMAL_ZERO)
                else {
                    val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(minV))
                    property.addValidation(Validation.Type.MINIMUM_DECIMAL, decimalStatic)
                }
            }
            result = true
        } ?: property.maximum?.let {
            if (it.isZero())
                property.addValidation(Validation.Type.MAXIMUM_DECIMAL_ZERO)
            else {
                val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
                property.addValidation(Validation.Type.MAXIMUM_DECIMAL, decimalStatic)
            }
            result = true
        }
        property.exclusiveMinimum?.let {
            if (it.isZero())
                property.addValidation(Validation.Type.EXCLUSIVE_MINIMUM_DECIMAL_ZERO)
            else {
                val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
                property.addValidation(Validation.Type.EXCLUSIVE_MINIMUM_DECIMAL, decimalStatic)
            }
            result = true
        }
        property.exclusiveMaximum?.let {
            if (it.isZero())
                property.addValidation(Validation.Type.EXCLUSIVE_MAXIMUM_DECIMAL_ZERO)
            else {
                val decimalStatic = target.addStatic(Target.StaticType.DECIMAL, "cg_dec", NumberValue(it))
                property.addValidation(Validation.Type.EXCLUSIVE_MAXIMUM_DECIMAL, decimalStatic)
            }
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

    private fun Number.isZero(): Boolean = when (this) {
        is BigDecimal -> this.compareTo(BigDecimal.ZERO) == 0
        is Long -> this == 0L
        is Int -> this == 0
        is Double -> this == 0.0
        is Float -> this == 0.0F
        else -> false
    }

    private fun analyseFormat(target: Target, property: Constraints): Boolean {
        var result = applyFormat(target, property)
        property.negatedConstraints?.let {
            result = applyFormat(target, it) || result
        }
        return result
    }

    private fun applyFormat(target: Target, property: Constraints): Boolean {
        var result = false
        property.format.forEach {
            when (it.name) {
                FormatValidator.EmailFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.EMAIL)
                    result = true
                }
                FormatValidator.HostnameFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.HOSTNAME)
                    result = true
                }
                FormatValidator.IPV4FormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.IPV4)
                    result = true
                }
                FormatValidator.IPV6FormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.IPV6)
                    result = true
                }
                FormatValidator.DurationFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.DURATION)
                    result = true
                }
                FormatValidator.JSONPointerFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.JSON_POINTER)
                    result = true
                }
                FormatValidator.RelativeJSONPointerFormatChecker.name -> {
                    target.systemClasses.addOnce(SystemClass.VALIDATION)
                    property.addValidation(Validation.Type.RELATIVE_JSON_POINTER)
                    result = true
                }
                FormatValidator.DateTimeFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.DATE_TIME)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.DATE_TIME)
                        property.systemClass = SystemClass.DATE_TIME
                    }
                }
                FormatValidator.DateFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.DATE)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.DATE)
                        property.systemClass = SystemClass.DATE
                    }
                }
                FormatValidator.TimeFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.TIME)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.TIME)
                        property.systemClass = SystemClass.TIME
                    }
                }
                FormatValidator.UUIDFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.UUID)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.UUID)
                        property.systemClass = SystemClass.UUID
                    }
                }
                FormatValidator.URIFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.URI)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.URI)
                        property.systemClass = SystemClass.URI
                    }
                }
                FormatValidator.URIReferenceFormatChecker.name -> {
                    if (property.negated) {
                        target.systemClasses.addOnce(SystemClass.VALIDATION)
                        property.addValidation(Validation.Type.URI_REFERENCE)
                        result = true
                    }
                    else {
                        target.systemClasses.addOnce(SystemClass.URI)
                        property.systemClass = SystemClass.URI
                    }
                }
            }
        }
        return result
    }

    private fun Constraints.checkRegex(target: Target): Boolean {
        val validationsSize = validations.size
        applyRegex(target)
        negatedConstraints?.applyRegex(target)
        return validationsSize < validations.size
    }

    private fun Constraints.applyRegex(target: Target) {
        regex.forEach {
            target.systemClasses.addOnce(SystemClass.REGEX)
            val regexStatic = target.addStatic(Target.StaticType.PATTERN, "cg_regex", StringValue(it.toString()))
            addValidation(Validation.Type.PATTERN, regexStatic)
        }
    }

    private fun String.depluralise(): String = when {
//        this.endsWith("es") -> dropLast(2) // need a more sophisticated way of handling plurals ending with -es
        this.endsWith('s') -> dropLast(1)
        else -> this
    }

    private fun JSONSchema.findRefChild(): RefSchema? {
        if (this !is JSONSchema.General)
            return null
        if (children.size == 1 && children[0] is CombinationSchema) {
            val combinationSchema = children[0] as CombinationSchema
            if (combinationSchema.name == "anyOf" || combinationSchema.name == "oneOf") {
                return when (val i = combinationSchema.findNullableSpecialCase()) {
                    0, 1 -> (combinationSchema.array[i] as? JSONSchema.General)?.locateRefSchema()
                    else -> null
                }
            }
        }
        return locateRefSchema()
    }

    private fun JSONSchema.General.locateRefSchema(): RefSchema? {
        return children.filterIsInstance<RefSchema>().firstOrNull()
    }

    private fun processSchema(schema: JSONSchema, constraints: Constraints) {
        when (schema) {
            is JSONSchema.SubSchema -> processSubSchema(schema, constraints)
            is JSONSchema.Validator -> processValidator(schema, constraints)
            is JSONSchema.General -> schema.children.forEach { processSchema(it, constraints) }
            is JSONSchema.Not -> processNotSchema(schema.nested, constraints)
            is JSONSchema.False -> constraints.nullable = true
            is JSONSchema.True -> constraints.nullable = true
            else -> {} // is there anything else?
        }
    }

    private fun processNotSchema(schema: JSONSchema, constraints: Constraints) {
        val constraintsNot = constraints.negatedConstraints ?: Constraints(constraints.schema, true).also {
            it.negatedConstraints = constraints
            constraints.negatedConstraints = it
            it.validations = constraints.validations
        }
        processSchema(schema, constraintsNot)
    }

    private fun processDefaultValue(value: JSONValue?): Constraints.DefaultPropertyValue =
            when (value) {
                null -> Constraints.DefaultPropertyValue(null, JSONSchema.Type.NULL)
                is JSONInteger -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.INTEGER)
                is JSONZero -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.INTEGER)
                is JSONLong -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.INTEGER)
                is JSONDecimal -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.NUMBER)
                is JSONDouble -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.NUMBER)
                is JSONFloat -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.NUMBER)
                is JSONString -> Constraints.DefaultPropertyValue(StringValue(value.value), JSONSchema.Type.STRING)
                is JSONBoolean -> Constraints.DefaultPropertyValue(value.value, JSONSchema.Type.BOOLEAN)
                is JSONSequence<*> -> Constraints.DefaultPropertyValue(value.map { processDefaultValue(it) },
                        JSONSchema.Type.ARRAY)
                is JSONMapping<*> -> fatal("Can't handle object as default value")
                else -> fatal("Unexpected default value")
            }

    private fun processSubSchema(subSchema: JSONSchema.SubSchema, constraints: Constraints) {
        when (subSchema) {
            is CombinationSchema -> processCombinationSchema(subSchema, constraints)
            is ItemsSchema -> processSchema(subSchema.itemSchema,
                    constraints.arrayItems ?: ItemConstraints(subSchema.itemSchema, constraints.displayName,
                            nameGenerator.generate()).also { constraints.arrayItems = it })
            is PropertiesSchema -> processPropertySchema(subSchema, constraints)
            is PatternPropertiesSchema -> processPatternPropertiesSchema(subSchema, constraints)
            is AdditionalPropertiesSchema -> processAdditionalPropertiesSchema(subSchema, constraints)
            is RefSchema -> processSchema(subSchema.target, constraints)
            is RequiredSchema -> subSchema.properties.forEach {
                    if (it !in constraints.required) constraints.required.add(it) }
            is ExtensionSchema -> processExtensionSchema(subSchema, constraints)
        }
    }

    private fun processExtensionSchema(extensionSchema: ExtensionSchema, constraints: Constraints) {
        if (extensionSchema.name == extensibleEnumKeyword) {
            val enumValues = extensionSchema.value
            if (enumValues !is List<*>)
                fatal("Extensible enum content is not array - ${extensionSchema.location}")
            val enumJSONArray = JSONArray(enumValues.map { JSONString(it.toString()) })
            if (constraints.enumValues != null && constraints.enumValues != enumJSONArray)
                fatal("Duplicate enum - ${extensionSchema.location}")
            constraints.enumValues = enumJSONArray
            constraints.extensibleEnum = true
        }
    }

    private fun processCombinationSchema(combinationSchema: CombinationSchema, constraints: Constraints) {
        when (combinationSchema.name) {
            "allOf" -> combinationSchema.array.forEach { processSchema(it, constraints) }
            "oneOf" -> {
                when (val i = combinationSchema.findNullableSpecialCase()) {
                    0, 1 -> {
                        processSchema(combinationSchema.array[i], constraints)
                        constraints.nullable = true
                    }
                    else -> {
                        constraints.oneOfSchemata = combinationSchema.array.map { schema ->
                            Constraints(schema).also { processSchema(schema, it) }
                        }
                    }
                }
            }
            "anyOf" -> { // special case involving anyOf and type null (otherwise ignore for now)
                when (val i = combinationSchema.findNullableSpecialCase()) {
                    0, 1 -> {
                        processSchema(combinationSchema.array[i], constraints)
                        constraints.nullable = true
                    }
                    else -> {}
                }
            }
        }
    }

    private fun processValidator(validator: JSONSchema.Validator, constraints: Constraints) {
        when (validator) {
            is DefaultValidator -> constraints.defaultValue = processDefaultValue(validator.value)
            is ConstValidator -> processConstValidator(validator, constraints)
            is EnumValidator -> processEnumValidator(validator, constraints)
            is FormatValidator -> processFormatValidator(validator, constraints)
            is NumberValidator -> processNumberValidator(validator, constraints)
            is PropertiesValidator -> processPropertiesValidator(validator, constraints)
            is PatternValidator -> processPatternValidator(validator, constraints)
            is StringValidator -> processStringValidator(validator, constraints)
            is TypeValidator -> processTypeValidator(validator, constraints)
            is ArrayValidator -> processArrayValidator(validator, constraints)
            is UniqueItemsValidator -> processUniqueItemsValidator(constraints)
            is DelegatingValidator -> processValidator(validator.validator, constraints)
            is Configurator.CustomValidator -> processSchema(validator.schema, constraints)
            is Configurator.CustomFormat -> processSchema(validator.schema, constraints)
        }
    }

    private fun processConstValidator(constValidator: ConstValidator, constraints: Constraints) {
        if (constraints.constValue != null && constraints.constValue != constValidator.value)
            fatal("Duplicate const - ${constValidator.location}")
        constraints.constValue = constValidator.value
    }

    private fun processEnumValidator(enumValidator: EnumValidator, constraints: Constraints) {
        if (constraints.enumValues != null && constraints.enumValues != enumValidator.array)
            fatal("Duplicate enum - ${enumValidator.location}")
        constraints.enumValues = enumValidator.array
    }

    private fun processFormatValidator(formatValidator: FormatValidator, constraints: Constraints) {
        val newFormat = formatValidator.checker
        constraints.format.add(newFormat)
        if (newFormat is FormatValidator.DelegatingFormatChecker) {
            for (validator in newFormat.validators)
                processValidator(validator, constraints)
        }
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

    private fun processPropertiesValidator(propertiesValidator: PropertiesValidator, constraints: Constraints) {
        when (propertiesValidator.condition) {
            PropertiesValidator.ValidationType.MIN_PROPERTIES -> constraints.minProperties =
                    constraints.minProperties?.let { min(it, propertiesValidator.value) } ?: propertiesValidator.value
            PropertiesValidator.ValidationType.MAX_PROPERTIES -> constraints.maxProperties =
                    constraints.maxProperties?.let { max(it, propertiesValidator.value) } ?: propertiesValidator.value
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

    private fun processUniqueItemsValidator(constraints: Constraints) {
        constraints.uniqueItems = true
    }

    private fun processPatternValidator(patternValidator: PatternValidator, constraints: Constraints) {
        constraints.regex.addOnce(patternValidator.regex)
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

    private fun processPatternPropertiesSchema(patternPropertiesSchema: PatternPropertiesSchema,
            constraints: Constraints) {
        patternPropertiesSchema.properties.forEach { (regex, schema) ->
            val patternPropertyPair = constraints.patternProperties.find { it.first == regex } ?:
                    Triple(regex, Constraints(schema), null).also { constraints.patternProperties.add(it) }
            processSchema(schema, patternPropertyPair.second)
        }
    }

    private fun processAdditionalPropertiesSchema(additionalPropertiesSchema: AdditionalPropertiesSchema,
            constraints: Constraints) {
        val additionalPropertiesConstraints = constraints.additionalProperties ?:
                Constraints(additionalPropertiesSchema.schema).also { constraints.additionalProperties = it }
        processSchema(additionalPropertiesSchema.schema, additionalPropertiesConstraints)
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

    fun addCustomClassByURI(uri: URI, className: String, packageName: String? = null) {
        customClassesByURI.add(CustomClassByURI(uri, className, packageName))
    }

    fun addCustomClassByURI(uri: URI, classId: ClassId) {
        customClassesByURI.add(CustomClassByURI(uri, classId.className, classId.packageName))
    }

    fun addCustomClassByFormat(name: String, qualifiedClassName: String) {
        customClassesByFormat.add(CustomClassByFormat(name, qualifiedClassName))
    }

    fun addCustomClassByFormat(name: String, className: String, packageName: String?) {
        customClassesByFormat.add(CustomClassByFormat(name, className, packageName))
    }

    fun addCustomClassByFormat(name: String, classId: ClassId) {
        customClassesByFormat.add(CustomClassByFormat(name, classId.className, classId.packageName))
    }

    fun addCustomClassByExtension(extensionId: String, extensionValue: Any?, qualifiedClassName: String) {
        customClassesByExtension.add(CustomClassByExtension(extensionId, extensionValue, qualifiedClassName))
    }

    fun addCustomClassByExtension(extensionId: String, extensionValue: Any?, className: String, packageName: String?) {
        customClassesByExtension.add(CustomClassByExtension(extensionId, extensionValue, className, packageName))
    }

    fun addCustomClassByExtension(extensionId: String, extensionValue: Any?, classId: ClassId) {
        customClassesByExtension.add(CustomClassByExtension(extensionId, extensionValue, classId.className,
                classId.packageName))
    }

    private var nameGenerator = NameGenerator()

    class NameGenerator(private var suffix: Int = 0) {

        fun generate(): String = "cg_${suffix++}"

    }

    /**
     * This class is intended to look like a [StringValue], for when the default value of a string is an enum value.
     */
    class EnumValue(val className: String, val value: String) : ValidationValue {

        override fun toString(): String {
            return "$className.$value"
        }

        @Suppress("unused")
        val kotlinString: String
            get() = toString()

        @Suppress("unused")
        val javaString: String
            get() = toString()

    }

    abstract class CustomClass(override val className: String, override val packageName: String?) : ClassId {

        fun applyToTarget(target: Target) {
            target.addImport(this)
        }

        override fun toString(): String = qualifiedClassName

    }

    class CustomClassByURI(val uri: URI, className: String, packageName: String?) :
            CustomClass(className, packageName) {

        constructor(uri: URI, qualifiedClassName: String) :
                this(uri, qualifiedClassName.substringAfterLast('.'),
                        qualifiedClassName.substringBeforeLast('.', "").takeIf { it.isNotEmpty() })

    }

    class CustomClassByFormat(val name: String, className: String, packageName: String?) :
            CustomClass(className, packageName) {

        constructor(name: String, qualifiedClassName: String) :
                this(name, qualifiedClassName.substringAfterLast('.'),
                        qualifiedClassName.substringBeforeLast('.', "").takeIf { it.isNotEmpty() })

        fun match(constraints: Constraints): Boolean {
            return constraints.format.any { it.name == name }
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
                if (newlines >= maxNewlines)
                    return this
                newlines++
            }
            else
                newlines = 0
            destination.append(c)
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

        const val internalSchema = "internal schema"

        fun String.looksLikeYAML() = endsWith(".yaml", ignoreCase = true) || endsWith(".yml", ignoreCase = true)

        fun <T: Any> MutableList<T>.addOnce(entry: T) {
            if (entry !in this)
                add(entry)
        }

        fun allIdentifier(array: JSONSequence<*>): Boolean {
            return array.all { it is JSONString && it.value.isValidIdentifier() }
        }

        private fun String.isValidIdentifier(): Boolean {
            if (isEmpty() || !this[0].isJavaIdentifierStart())
                return false
            for (i in 1 until length)
                if (!this[i].isJavaIdentifierPart())
                    return false
            return true
        }

        fun String.isValidClassName(): Boolean = split('.').all { it.isValidIdentifier() }

        fun String.sanitiseName(): String {
            for (i in indices) {
                val ch = this[i]
                if (!ch.isJavaIdentifierPart()) {
                    return buildString {
                        append(this@sanitiseName, 0, i)
                        for (j in i + 1 until this@sanitiseName.length) {
                            val ch2 = this@sanitiseName[j]
                            if (ch2.isJavaIdentifierPart())
                                append(ch2)
                        }
                    }
                }
            }
            return this
        }

        private fun checkDirectory(directory: File): File {
            when {
                !directory.exists() -> {
                    directory.parentFile?.let { checkDirectory(it) }
                    if (!directory.mkdir())
                        fatal("Error creating output directory - $directory")
                }
                directory.isDirectory -> {}
                directory.isFile -> fatal("File given for output directory - $directory")
                else -> fatal("Error accessing output directory - $directory")
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

        fun JSONSchema.locationMatches(other: JSONSchema): Boolean {
            return uri == other.uri && location == other.location
        }

        fun fatal(message: String): Nothing {
            throw JSONSchemaException(message)
        }

        fun CombinationSchema.findNullableSpecialCase(): Int {
            if (array.size == 2) {
                if (array[0].isSingleTypeNull())
                    return 1
                if (array[1].isSingleTypeNull())
                    return 0
            }
            return -1
        }

        private fun JSONSchema.isSingleTypeNull(): Boolean {
            return this is JSONSchema.General && children.size == 1 && children[0].let {
                it is TypeValidator && it.types == listOf(JSONSchema.Type.NULL)
            }
        }

    }

}

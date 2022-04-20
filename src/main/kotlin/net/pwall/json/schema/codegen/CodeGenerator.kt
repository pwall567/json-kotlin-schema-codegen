/*
 * @(#) CodeGenerator.kt
 *
 * json-kotlin-schema-codegen  JSON Schema Code Generation
 * Copyright (c) 2020, 2021, 2022 Peter Wall
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
import java.io.IOException
import java.io.Reader
import java.math.BigDecimal
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

import net.pwall.json.JSON
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
import net.pwall.json.pointer.JSONReference
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
import net.pwall.json.schema.validation.UniqueItemsValidator
import net.pwall.log.Logger
import net.pwall.log.LoggerFactory
import net.pwall.mustache.Template
import net.pwall.mustache.parser.Parser as MustacheParser
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
    val log: Logger = LoggerFactory.getDefaultLogger(CodeGenerator::class.qualifiedName)
) {

    enum class NestedClassNameOption {
        USE_NAME_FROM_REF_SCHEMA,
        USE_NAME_FROM_PROPERTY
    }

    var nestedClassNameOption = NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA

    var commentTemplate: Template? = null

    private val customClassesByURI = mutableListOf<CustomClassByURI>()
    private val customClassesByFormat = mutableListOf<CustomClassByFormat>()
    private val customClassesByExtension = mutableListOf<CustomClassByExtension>()

    private val classNameMapping = mutableListOf<Pair<URI, String>>()

    fun addClassNameMapping(uri: URI, name: String) {
        classNameMapping.add(uri to name)
    }

    private var schemaParserField: Parser? = null

    var schemaParser: Parser
        get() = schemaParserField ?: defaultSchemaParser.also { schemaParserField = it }
        set(sp) { schemaParserField = sp }

    private val defaultSchemaParser: Parser by lazy {
        Parser()
    }

    private var templateParserField: MustacheParser? = null

    var templateParser: MustacheParser
        get() = templateParserField ?: defaultTemplateParser.also { templateParserField = it }
        set(tp) { templateParserField = tp }

    private val defaultTemplateParser: MustacheParser by lazy {
        MustacheParser { name ->
            partialResolver(name)
        }
    }

    private fun partialResolver(name: String): Reader {
        for (dir in targetLanguage.directories)
            CodeGenerator::class.java.getResourceAsStream("/$dir/$name.mustache")?.let { return it.reader() }
        fatal("Can't locate template partial $name")
    }

    private var templateField: Template? = null

    var template: Template
        get() = templateField ?: defaultTemplate.also { templateField = it }
        set(t) { templateField = t }

    private val defaultTemplate: Template by lazy {
        val resolver = templateParser.resolvePartial
        templateParser.parse(templateParser.resolver(templateName))
    }

    var interfaceTemplateName = "interface"

    private var interfaceTemplateField: Template? = null

    var interfaceTemplate: Template
        get() = interfaceTemplateField ?: defaultInterfaceTemplate.also { interfaceTemplateField = it }
        set(it) { interfaceTemplateField = it }

    private val defaultInterfaceTemplate: Template by lazy {
        val resolver = templateParser.resolvePartial
        templateParser.parse(templateParser.resolver(interfaceTemplateName))
    }

    var enumTemplateField: Template? = null

    var enumTemplate: Template
        get() = enumTemplateField ?: defaultEnumTemplate.also { enumTemplateField = it }
        set(et) { enumTemplateField = et }

    private val defaultEnumTemplate: Template by lazy {
        val resolver = templateParser.resolvePartial
        templateParser.parse(templateParser.resolver(enumTemplateName))
    }

    var indexFileName: TargetFileName? = null

    var indexTemplateName: String = "index"

    var indexTemplateField: Template? = null

    var indexTemplate: Template
        get() = interfaceTemplateField ?: defaultIndexTemplate.also { indexTemplateField = it }
        set(it) { indexTemplateField = it }

    private val defaultIndexTemplate: Template by lazy {
        val resolver = templateParser.resolvePartial
        templateParser.parse(templateParser.resolver(indexTemplateName))
    }

    var outputResolverField: OutputResolver? = null

    var outputResolver: OutputResolver
        get() = outputResolverField ?: defaultOutputResolver.also { outputResolverField = it }
        set(or) { outputResolverField = or }

    private val defaultOutputResolver: OutputResolver = { targetFileName ->
        targetFileName.resolve(File(baseDirectoryName)).also { checkDirectory(it.parentFile) }.writer()
    }

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
                targetFile = TargetFileName(className, targetLanguage.ext, getOutputDirs(subDirectories)),
                source = source,
                generatorComment = generatorComment,
                commentTemplate = commentTemplate,
                json = json
            ).apply { markerInterface?.let { addInterface(it) } }
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
     * @param   uri     the URI
     */
    fun addTarget(uri: URI) {
        val json = schemaParser.jsonReader.readJSON(uri)
        val schema = schemaParser.parse(uri)
        addTarget(emptyList(), schema, uri.toString(), json)
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
                inputFile.isFile -> addTarget(packageNames, inputFile)
                inputFile.isDirectory -> addTargets(packageNames, inputFile)
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
                Files.isRegularFile(inputPath) -> addTarget(packageNames, inputPath)
                Files.isDirectory(inputPath) -> addTargets(packageNames, inputPath)
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
                indexTemplate.processTo(AppendableFilter(it), TargetIndex(targets, name, generatorComment))
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
        when {
            target.constraints.isObject -> { // does it look like an object? generate a class
                log.info { "-- target class ${target.qualifiedClassName}" }
                target.systemClasses.sortBy { it.order }
                target.imports.sort()
                outputResolver(target.targetFile).use {
                    template.processTo(AppendableFilter(it), target)
                }
            }
            target.constraints.oneOfSchemata.any { it.isObject } -> {
                // it wasn't an object, but it had a oneOf with object children
                log.info { "-- target interface ${target.qualifiedClassName}" }
                outputResolver(target.targetFile).use {
                    interfaceTemplate.processTo(AppendableFilter(it), target)
                }
            }
            target.constraints.isString && target.constraints.enumValues.let { it != null && allIdentifier(it) } -> {
                log.info { "-- target enum ${target.qualifiedClassName}" }
                outputResolver(target.targetFile).use {
                    enumTemplate.processTo(it, target)
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
        // create a nested class with current as a base class and oneOfTarget properties, and remove (merge?) overlapping properties
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
                    schema = schemaParser.parseSchema(base, pointer.child(name), documentURI),
                    className = name,
                    subDirectories = subDirectories,
                    source = "$documentURI#$pointer/$name",
                    json = base,
                )
        }
    }

    private fun addTarget(subDirectories: List<String>, inputFile: File) {
        val json = schemaParser.jsonReader.readJSON(inputFile)
        val schema = schemaParser.parse(inputFile)
        addTarget(subDirectories, schema, inputFile.toString(), json)
    }

    private fun addTarget(subDirectories: List<String>, inputPath: Path) {
        val json = schemaParser.jsonReader.readJSON(inputPath)
        val schema = schemaParser.parse(inputPath)
        addTarget(subDirectories, schema, inputPath.toString(), json)
    }

    private fun addTarget(subDirectories: List<String>, schema: JSONSchema, source: String, json: JSONValue) {
        val className = schema.uri?.let { uri ->
            classNameMapping.find { it.first == uri }?.second ?: run {
                // TODO change to allow name ending with "/schema"?
                val uriName = uri.toString().substringBefore('#').substringAfterLast(':').substringAfterLast('/')
                val uriNameNoExtension = when {
                    uriName.endsWith(".json", ignoreCase = true) -> uriName.dropLast(5)
                    uriName.endsWith(".yaml", ignoreCase = true) -> uriName.dropLast(5)
                    uriName.endsWith(".yml", ignoreCase = true) -> uriName.dropLast(4)
                    else -> uriName
                }
                when {
                    uriNameNoExtension.endsWith(".schema", ignoreCase = true) -> uriNameNoExtension.dropLast(7)
                    uriNameNoExtension.endsWith("-schema", ignoreCase = true) -> uriNameNoExtension.dropLast(7)
                    uriNameNoExtension.endsWith("_schema", ignoreCase = true) -> uriNameNoExtension.dropLast(7)
                    else -> uriNameNoExtension
                }.split('-', '.').joinToString(separator = "") { part -> Strings.capitalise(part) }.sanitiseName()
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

    private fun addTargets(subDirectories: List<String>, inputDir: File) {
        inputDir.listFiles()?.forEach {
            when {
                it.isDirectory -> {
                    if (!it.name.startsWith('.'))
                        addTargets(subDirectories + it.name.mapDirectoryName(), it)
                }
                it.isFile -> addTarget(subDirectories, it)
            }
        }
    }

    private fun addTargets(subDirectories: List<String>, inputDir: Path) {
        Files.newDirectoryStream(inputDir).use { dir ->
            dir.forEach {
                when {
                    Files.isDirectory(it) -> {
                        if (!it.fileName.toString().startsWith('.'))
                            addTargets(subDirectories + it.fileName.toString().mapDirectoryName(), it)
                    }
                    Files.isRegularFile(it) -> addTarget(subDirectories, it)
                }
            }
        }
    }

    private fun String.mapDirectoryName(): String = StringBuilder().also {
        for (ch in this)
            if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9')
                it.append(ch)
    }.toString()

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
                property.baseProperty = baseConstraints
                baseConstraints.defaultValue?.let { property.defaultValue = it }
                if (baseConstraints.isArray)
                    property.arrayItems = baseConstraints.arrayItems
                val customClass = findCustomClass(baseConstraints.schema, target) ?:
                        baseConstraints.schema.findRefChild()?.let { findCustomClass(it.target, target) }
                if (customClass != null)
                    property.localTypeName = customClass
                else {
                    property.localTypeName = baseConstraints.localTypeName
                    baseConstraints.systemClass?.let {
                        property.systemClass = it
                        target.systemClasses.addOnce(it)
                    }
                }
                if (!property.sameType(baseConstraints))
                    baseConstraints.extendedInDerived = true
            }
            else {
                if (analyseProperty(target, property, property, property.name))
                    validationsPresent = true
            }
        }
        return validationsPresent
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

    private fun analyseProperties(target: Target, constraints: Constraints): Boolean {
        analysePropertiesRequired(constraints)
        return constraints.properties.fold(false) { result, property ->
            analyseProperty(target, property, property, property.name) || result
        }
    }

    private fun useTarget(constraints: Constraints, target: Target, otherTarget: Target) {
        target.addImport(otherTarget)
        constraints.localTypeName = otherTarget.className
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
            val nestedClassName = when (nestedClassNameOption) {
                NestedClassNameOption.USE_NAME_FROM_REF_SCHEMA ->
                    refConstraints.schema.findRefChild()?.fragment?.substringAfterLast('/') ?: defaultName()
                NestedClassNameOption.USE_NAME_FROM_PROPERTY -> defaultName()
            }
            val nestedClass = target.addNestedClass(constraints, constraints.schema,
                    Strings.capitalise(nestedClassName))
            nestedClass.validationsPresent = analyseObject(target, nestedClass, constraints)
            constraints.localTypeName = nestedClass.className
        }
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

    private fun analyseProperty(
        target: Target,
        property: Constraints,
        arrayProperty: Constraints,
        name: String,
    ): Boolean {
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

    private fun analyseArray(target: Target, property: Constraints, name: String): Boolean {
        target.systemClasses.addOnce(if (property.uniqueItems) SystemClass.SET else SystemClass.LIST)
        var validationsPresent = false
        property.arrayItems?.let {
            if (analyseProperty(target, it, property, name.depluralise())) {
                property.addValidation(Validation.Type.ARRAY_ITEMS)
                validationsPresent = true
            }
        }
        property.minItems?.let { minI ->
            property.maxItems?.let { maxI ->
                if (minI == maxI)
                    property.addValidation(Validation.Type.CONST_ITEMS, NumberValue(minI))
                else
                    property.addValidation(Validation.Type.RANGE_ITEMS, minI to maxI)
            } ?: property.addValidation(Validation.Type.MIN_ITEMS, NumberValue(minI))
            validationsPresent = true
        } ?: property.maxItems?.let { maxI ->
            property.addValidation(Validation.Type.MAX_ITEMS, NumberValue(maxI))
            validationsPresent = true
        }
        property.defaultValue?.let {
            if (it.type != JSONSchema.Type.ARRAY)
                property.defaultValue = null
        }
        return validationsPresent
    }

    private fun analyseString(property: Constraints, target: Target, defaultName: () -> String):
            Boolean {
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
                val stringStatic = target.addStatic(Target.StaticType.STRING, "cg_str", StringValue(it.value))
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
        validationsPresent = analyseRegex(target, property) || validationsPresent
        return validationsPresent
    }

    private fun analyseInt(property: Constraints, target: Target): Boolean {
        var result = false
        property.constValue?.let {
            when (it) {
                is JSONInteger -> {
                    property.addValidation(Validation.Type.CONST_INT, it.value)
                    property.enumValues = null
                    result = true
                }
                is JSONLong -> {
                    it.value.let { v ->
                        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
                            property.addValidation(Validation.Type.CONST_INT, v)
                            property.enumValues = null
                            result = true
                        }
                    }
                }
                is JSONDecimal -> {
                    it.value.asLong().let { v ->
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
        property.minimumLong?.takeIf { minV -> minV in (Int.MIN_VALUE + 1)..Int.MAX_VALUE }?.let { minV ->
            property.maximumLong?.takeIf { maxV -> maxV in Int.MIN_VALUE until Int.MAX_VALUE }?.let { maxV ->
                property.addValidation(Validation.Type.RANGE_INT, minV to maxV)
            } ?: property.addValidation(Validation.Type.MINIMUM_INT, minV)
            result = true
        } ?: property.maximumLong?.takeIf { it in Int.MIN_VALUE until Int.MAX_VALUE }?.let {
            property.addValidation(Validation.Type.MAXIMUM_INT, it)
            result = true
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
                    property.addValidation(Validation.Type.CONST_LONG, it.value)
                    result = true
                }
                is JSONLong -> {
                    property.addValidation(Validation.Type.CONST_LONG, it.value)
                    result = true
                }
                is JSONDecimal -> {
                    property.addValidation(Validation.Type.CONST_LONG, it.value.asLong())
                }
            }
        }
        property.minimumLong?.takeIf { it > Long.MIN_VALUE }?.let { minV ->
            property.maximumLong?.takeIf { it < Long.MAX_VALUE }?.let { maxV ->
                property.addValidation(Validation.Type.RANGE_LONG, minV to maxV)
            } ?: property.addValidation(Validation.Type.MINIMUM_LONG, minV)
            result = true
        } ?: property.maximumLong?.takeIf { it < Long.MAX_VALUE }?.let {
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
            (this as? JSONSchema.General)?.children?.filterIsInstance<RefSchema>()?.firstOrNull()

    private fun processSchema(schema: JSONSchema, constraints: Constraints) {
        when (schema) {
            is JSONSchema.SubSchema -> processSubSchema(schema, constraints)
            is JSONSchema.Validator -> processValidator(schema, constraints)
            is JSONSchema.General -> schema.children.forEach { processSchema(it, constraints) }
            else -> {} // for now, just ignore boolean and "not" schema
        }
    }

    private fun processDefaultValue(value: JSONValue?): Constraints.DefaultValue =
            when (value) {
                null -> Constraints.DefaultValue(null, JSONSchema.Type.NULL)
                is JSONInteger -> Constraints.DefaultValue(value.value, JSONSchema.Type.INTEGER)
                is JSONString -> Constraints.DefaultValue(StringValue(value.value), JSONSchema.Type.STRING)
                is JSONBoolean -> Constraints.DefaultValue(value.value, JSONSchema.Type.BOOLEAN)
                is JSONSequence<*> -> Constraints.DefaultValue(value.map { processDefaultValue(it) },
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
            is RefSchema -> processSchema(subSchema.target, constraints)
            is RequiredSchema -> subSchema.properties.forEach {
                    if (it !in constraints.required) constraints.required.add(it) }
        }
    }

    private fun processCombinationSchema(combinationSchema: CombinationSchema, constraints: Constraints) {
        when (combinationSchema.name) {
            "allOf" -> combinationSchema.array.forEach { processSchema(it, constraints) }
            "oneOf" -> {
                constraints.oneOfSchemata = combinationSchema.array.map { schema ->
                    Constraints(schema).also { processSchema(schema, it) }
                }
            }
            "anyOf" -> {} // ignore for now - not sure what we can usefully do
        }
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
            is UniqueItemsValidator -> processUniqueItemsValidator(constraints)
            is DelegatingValidator -> processValidator(validator.validator, constraints)
            is Configurator.CustomValidator -> processSchema(validator.schema, constraints)
        }
    }

    private fun processConstValidator(constValidator: ConstValidator, constraints: Constraints) {
        if (constraints.constValue != null)
            fatal("Duplicate const")
        constraints.constValue = constValidator.value
    }

    private fun processEnumValidator(enumValidator: EnumValidator, constraints: Constraints) {
        if (constraints.enumValues != null)
            fatal("Duplicate enum")
        constraints.enumValues = enumValidator.array
    }

    private fun processFormatValidator(formatValidator: FormatValidator, constraints: Constraints) {
        if (constraints.format != null)
            fatal("Duplicate format - ${formatValidator.location}")
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

    private fun processUniqueItemsValidator(constraints: Constraints) {
        constraints.uniqueItems = true
    }

    private fun processPatternValidator(patternValidator: PatternValidator, constraints: Constraints) {
        if (constraints.regex != null)
            fatal("Duplicate pattern")
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
    class EnumDefault(private val className: String, private val defaultValue: String) {

        override fun toString(): String {
            return "$className.$defaultValue"
        }

        @Suppress("unused")
        val kotlinString: String
            get() = toString()

    }

    abstract class CustomClass(override val className: String, override val packageName: String?) : ClassId {

        fun applyToTarget(target: Target): String {
            target.addImport(this)
            return className
        }

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
                    try {
                        Files.createDirectories(directory.toPath())
                    }
                    catch (e: IOException) {
                        fatal("Error creating output directory - $directory", e)
                    }
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

        fun fatal(message: String, t: Throwable): Nothing {
            throw JSONSchemaException(message, t)
        }

    }

}

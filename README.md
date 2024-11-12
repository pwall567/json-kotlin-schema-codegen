![Stop the war in Ukraine](https://pwall.net/ukraine1.png)

# json-kotlin-schema-codegen

[![Build Status](https://github.com/pwall567/json-kotlin-schema-codegen/actions/workflows/build.yml/badge.svg)](https://github.com/pwall567/json-kotlin-schema-codegen/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.9.24&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.9.24)
[![Maven Central](https://img.shields.io/maven-central/v/net.pwall.json/json-kotlin-schema-codegen?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.pwall.json%22%20AND%20a:%22json-kotlin-schema-codegen%22)

Code generation for JSON Schema (Draft 07).

## NEW

**NOTE:** &ndash; from version 0.110, the underlying JSON and YAML libraries have been switched from
[`jsonutil`](https://github.com/pwall567/jsonutil) and [`yaml-simple`](https://github.com/pwall567/yaml-simple) to
[`kjson-core`](https://github.com/pwall567/kjson-core) and [`kjson-yaml`](https://github.com/pwall567/kjson-yaml).
The change should be transparent to most users.

Also, the Mustache processor has been switched from `kotlin-mustache` to `mustache-k`, but this change will almost
certainly not affect any users.

New in version 0.106 &ndash; the `classNames` configuration option has been extended to allow the configuration of
generated nested class names.
See [`classNames`](CONFIG.md#classnames) in the [Configuration Guide](CONFIG.md) for more details.

New in version 0.105 &ndash; the generator will optionally validate `default` and `examples` entries against the schema
in which they appear.
See the
[`examplesValidationOption` and `defaultValidationOption`](CONFIG.md#examplesvalidationoption-and-defaultvalidationoption)
section in the [Configuration Guide](CONFIG.md).

New in version 0.100 &ndash; the generator will produce classes that handle `additionalProperties` and
`patternProperties`.
See the [`additionalProperties` and `patternProperties`](PROPERTIES.md) guide for more details.

New in version 0.87 &ndash; the generator now recognises a special case of `anyOf` or `oneOf` to specify nullability.
See [below](#nullability).

New in version 0.84 &ndash; the generator will now recognise the `not` schema for most validations, and will output
reversed validation checks.
For example, `"not": { "const": "error" }` in a property sub-schema will test that a string is **not** equal to "error".

Added to the code generator &ndash; the ability to configure generation options using a JSON or YAML file.
See the documentation at [CONFIG.md](CONFIG.md).

Also build tool support &ndash; see [below](#build-tool-support).

Also, the ability to add annotations to generated classes &ndash; see [`annotations`](CONFIG.md#annotations).

And from version 0.86 onward, the ability to force the output of a companion object for all or selected classes &ndash;
see [`companionObject`](CONFIG.md#companionobject).

## Background

[JSON Schema](https://json-schema.org/) provides a means of describing JSON values &ndash; the properties of an object,
constraints on values etc. &ndash; in considerable detail.
Many APIs now use JSON Schema to specify the content of JSON parameters and response objects, either directly or as part
of an [OpenAPI](https://www.openapis.org/) specification, and one way of ensuring conformity to the specified schema is
to generate code directly from the schema itself.

This is not always possible &ndash; some characteristics described by a schema may not be representable in the
implementation language.
But for a large subset of schema definitions a viable code representation is achievable, and this library attempts to
provide conversions for the broadest possible range of JSON Schema definitions.

The library uses a template mechanism (employing [Mustache](https://github.com/pwall567/mustache-k) templates), and
templates are provided to generate classes in Kotlin and Java, or interfaces in TypeScript.

## Quick Start

Simply create a `CodeGenerator`, supply it with details like destination directory and package name, and invoke the
generation process:
```kotlin
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "output/directory"
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(File("/path/to/example.schema.json"))
```
The resulting code will be something like this (assuming the schema is the one referred to in the introduction to
[json-kotlin-schema](https://github.com/pwall567/json-kotlin-schema)):
```kotlin
package com.example

import java.math.BigDecimal

data class Test(
    /** Product identifier */
    val id: BigDecimal,
    /** Name of the product */
    val name: String,
    val price: BigDecimal,
    val tags: List<String>? = null,
    val stock: Stock? = null
) {

    init {
        require(price >= cg_dec0) { "price < minimum 0 - $price" }
    }

    data class Stock(
        val warehouse: BigDecimal? = null,
        val retail: BigDecimal? = null
    )

    companion object {
        private val cg_dec0 = BigDecimal.ZERO
    }

}
```
Some points to note:
- the generated class is an immutable value object (in Java, getters are generated but not setters)
- validations in the JSON Schema become initialisation checks in Kotlin
- nested objects are converted to Kotlin nested classes (or Java static nested classes)
- fields of type `number` are implemented as `BigDecimal` (there is insufficient information in the schema in this case
to allow the field to be considered an `Int` or `Long`)
- non-required fields may be nullable and may be omitted from the constructor (the inclusion of `null` in the `type`
array will allow a field to be nullable, but not to be omitted from the constructor)
- a `description` will be converted to a KDoc comment in the generated code if available

## Multiple Files

The Code Generator can process a single file or multiple files in one invocation.
The `generate()` function takes either a `List` or a `vararg` parameter array, and each item may be a file or a
directory.
In the latter case all files in the directory with filenames ending `.json` or `.yaml` (or `.yml`) will be processed.

It is preferable to process multiple files in this way because the code generator can create references to other classes
that it knows about &ndash; that is, classes generated in the same run.
For example, if a `properties` entry consists of a  `$ref` pointing to a schema that is in the list of files to be
generated, then a reference to an object of that type will be generated (instead of a nested class).

## Clean Code

It is important to note that the output code will be &ldquo;clean&rdquo; &ndash; that is, it will not contain
annotations or other references to external libraries.
I recommend the use of the [`kjson`](https://github.com/pwall567/kjson) library for JSON serialisation and
deserialisation, but the classes generated by this library should be capable of being processed by the library of your
choice.

There is one exception to this &ndash; classes containing properties subject to &ldquo;`format`&rdquo; validations will
in some cases cause references to the external library [`json-validation`](https://github.com/pwall567/json-validation)
to be generated, and this library must be included in the build of the generated code.

The format keywords that will lead to the requirement for this library are:

`email`                 
`hostname`              
`ipv4`                  
`ipv6`                  
`json-pointer`          
`relative-json-pointer` 
`uri-template`          

The `idn-email`, `idn-hostname`, `iri` and `iri-reference` format keywords are not currently implemented, and the use of
these keywords will have no effect.

## `additionalProperties`

The default in the JSON Schema specification for `additionalProperties` is `true`, meaning that any additional
properties in an object will be accepted without validation.
Many schema designers will be happy with this default, or will even explicitly specify `true`, so that future extensions
to the schema will not cause existing uses to have problems.

Unfortunately, for a class to accept properties with names not known in advance requires very much more complex code
than for a simple class, so in normal usage the code generator takes the `additionalProperties` setting to be `false`,
even if it is specified otherwise.

To specify that the code generator is to interpret the `additionalProperties` keyword strictly as specified (including
defaulting to `true`), the configuration option [`additionalPropertiesOption`](CONFIG.md#additionalpropertiesoption)
must be set to `strict` (see the [`additionalProperties` and `patternProperties`](PROPERTIES.md) guide for more
information).

Most JSON deserialisation libraries have a means of specifying that additional properties are to be ignored; for the
[`kjson`](https://github.com/pwall567/kjson) library, the `allowExtra` variable (`Boolean`) in `JSONConfig` must be set
to `true`.

## `data class`

The code generator will create a `data class` whenever possible.
This has a number of advantages, including the automatic provision of `equals` and `hashCode` functions, keeping the
generated code as concise and readable as possible.

Unfortunately, it is not always possible to use a `data class`.
When the generated code involves inheritance, with one class extending another, the base class will be generated as an
`open class` and the derived class as a plain `class`.

In these cases the code generator will supply the missing functions &ndash; `equals`, `hashCode`, `toString`, `copy` and
the `component[n]` functions that would otherwise be created automatically for a `data class`. 

## Numbers

To ensure that the full range of values can be accommodated, any property or array item of type `number` or `integer`
will be generated as a `java.math.BigDecimal`, unless it includes validations that restrict the range of values to those
that will fit in an `Int` or a `Long` (see [Kotlin Multi-Platform (KMP)](#kotlin-multi-platform-kmp) for non-JVM uses), 

For example:
```yaml
    month:
      type: integer
      minimum: 1
      maximum: 12
```
This will cause the property `month` to be generated as an `Int`, because the possible range of values will always fit
in 32 bits.

It is always good practice to specify `minimum` and `maximum` constraints on `integer` properties to ensure that the
appropriate form of storage is used.

## Nullability

The standard way of specifying a value as nullable in JSON Schema is to use the `type` keyword:
```json
    { "type": [ "object", "null" ] }
```
When the code generator encounters a property or array item defined in this way, it will make the generated type
nullable.

A problem arises when we consider the interaction of this declaration of nullability with the `required` keyword.
What should the generator produce for an object property that does not include `null` as a possible type, but is not in
the `required` list?
The solution adopted by the code generator is to treat the property as if it had been defined to allow `null`, and this
seems to work well for the majority of cases, although strictly speaking, it is not an accurate reflection of the
schema.

In particular, it helps with the case of utility sub-schema which is included by means of `$ref` in multiple places, in
some cases nullable and in some cases not.
For example, an invoice may have a billing address and an optional delivery address, both of which follow a common
pattern defined in its own schema.
The shared definition will have `"type": "object"`, but the delivery address will need to be nullable, so generating a
nullable type for a reference omitted from the `required` list will have the desired effect.

But this solution does not work for all circumstances.
For example, it does not cover the case of an included sub-schema as an array item &ndash; there is no `required` for
array items.

One way of specifying such a schema using the full capabilities of JSON Schema is as follows:
```json
{
  "type": "object",
  "properties": {
    "billingAddress": {
      "$ref": "http://example.com/schema/address"
    },
    "deliveryAddress": {
      "anyOf": [
        { "$ref": "http://example.com/schema/address" },
        { "type": "null" }
      ]
    }
  },
  "required": [ "billingAddress", "deliveryAddress" ]
}
```
It is not easy to generate code for the general case of `oneOf` or `anyOf`, but the code generator will detect this
specific case to output the `deliveryAddress` as nullable:

1. The `anyOf` or `oneOf` array must have exactly two sub-schema items
2. One of the items must be just `{ "type": "null" }`

In this case, the code generator will generate code for the other sub-schema item (the one that is not
`{ "type": "null" }`, often a `$ref`), and treat the result as nullable.

## Custom Classes

(**NOTE** &ndash; the configuration file may be a simpler way to specify custom classes, particularly when combined with
other configuration options.  See the [Configuration Guide](CONFIG.md).)

The code generator can use custom types for properties and array items.
This can be valuable when, for example, an organisation has its own custom classes for what are sometimes called
"domain primitives" &ndash; value objects representing a fundamental concept for the functional area.

A common example of a domain primitive is a class to hold a money value, taking a `String` in its constructor and
storing the value as either a `Long` of cents or a `BigDecimal`.

There are three ways of specifying a custom class to the code generator:

1. URI
1. Custom `format` types
1. Custom keywords

### URI

An individual item in a schema may be nominated by the URI of the element itself.
For example, in the schema mentioned in the [Quick Start](#quick-start) section there is a field named `price`.
To specify that the code generator is to use a `Money` class for this field, use:
```kotlin
        codeGenerator.addCustomClassByURI(URI("http://pwall.net/test#/properties/price"), "com.example.Money")
```
The base URI can be **either** the URL used to locate the schema file **or** the URI in the `$id` of the schema.

A distinct advantage of this technique is that when a `$ref` is used to share a common definition of a field type, the
destination of the `$ref` can be specified to the code generator function shown above, and all references to it will use
the nominated class.
It is also the least obtrusive approach &ndash; it does not require modification to the schema or non-standard syntax.

### Format

The JSON Schema specification allows for non-standard `format` types.
For example, if the specification of the property in the schema contained `"format": "x-local-money"`, then the
following will cause the property to use a custom class:
```kotlin
        codeGenerator.addCustomClassByFormat("x-local-money", "com.example.Money")
```

### Custom Keyword

The JSON Schema specification also allows for completely non-standard keywords.
For example, the schema could contain `"x-local-type": "money"`, in which case the following would invoke the use of the
custom class:
```kotlin
        codeGenerator.addCustomClassByExtension("x-local-type", "money", "com.example.Money")
```

## Kotlin Multi-Platform (KMP)

Most uses of the code generator target the JVM versions of Kotlin (and the code generator itself runs only on the JVM),
but with the right configuration it can be used to generate code for any environment.
The main areas that require configuration are:
1. The default classes for strings with `format: date`, `format: time` and `format:date-time` are classes from the JVM
   `java.time` package (`java.time.LocalDate`, `java.time.OffsetTime` and `java.time.OffsetDateTime` respectively).
   Schema files using these formats must include [`customClasses`](CONFIG.md#customclasses) configuration, specifying
   alternative classes (the [`kotlinx-datetime`](https://kotlinlang.org/api/kotlinx-datetime/) library my be suitable,
   although it does not have exact matches for the JVM classes).
2. Decimal values use `java.math.BigDecimal` on the JVM, and this can **not** be configured using `customClasses`.
   Starting from version 0.107, the [`decimalClassName`](CONFIG.md#decimalclassname) configuration setting allows the
   specification of an alternative class for decimal values.

## JSON Schema Version

This code generator targets the Draft-07 of the JSON Schema specification, and it includes some features from Draft
2019-09.

It also includes support for the `int32` and `int64` format types from the
[OpenAPI 3.0 Specification](https://swagger.io/specification/).

## API Reference

A `CodeGenerator` object is used to perform the generation.
It takes a number of parameters, many of which can be specified either as constructor parameters or by modifying
variables in the constructed instance.

### Parameters

- `targetLanguage` &ndash; a `TargetLanguage` `enum` specifying the target language for code generation (the options are
`KOTLIN`, `JAVA` or `TYPESCRIPT` &ndash; TypeScript coverage is not as advanced as that of the others at this time)
- `templateName` &ndash; the primary template to use for the generation of a class
- `enumTemplateName` &ndash; the primary template to use for the generation of an enum
- `basePackageName` &ndash; the base package name for the generated classes (if directories are supplied to the
`generate()` function, the subdirectory names are used as sub-package names)
- `baseDirectoryName` &ndash; the base directory to use for generated output (in line with the Java convention, output
directory structure will follow the package structure)
- `derivePackageFromStructure` &ndash; a boolean flag (default `true`) to indicate that generated code for schema files
in subdirectories are to be output to sub-packages following the same structure
- `generatorComment` &ndash; a comment to add to the header of generated files
- `markerInterface` &ndash; a &ldquo;marker&rdquo; interface to be added to every class

### Functions

#### `configure()`

The `configure()` function takes a `File` or `Path` specifying a configuration file.
See [CONFIG.md](CONFIG.md) for details of the configuration options.

#### `generate()`

There are two `generate()` functions, one taking a `List` of `File`s, the other taking a `vararg` list of `File`
arguments.
As described above, it is helpful to supply all the schema objects to be generated in a single operation.

#### `generateClass()`, `generateClasses()`

While the `generate()` functions take a file or files and convert them to an internal form before generating code, the
`generateClass()` and `generateClasses()` functions take pre-parsed schema objects.
This can be valuable in cases like an OpenAPI file which contains a set of schema definitions embedded in another file.

#### `generateAll()`

The `generateAll()` function allows the use of a composite file such as an OpenAPI file containing several schema
definitions.
For example, an OpenAPI file will typically have a `components` section which contains definitions of the objects input
to or output from the API.
Using the `generateAll()` function, the set of definitions can be selected (and optionally filtered) and the classes
generated for each of them.

## Build Tool Support

To simplify the use of the code generator in conjunction with the common build tools the following plugins will perform
code generation as a pre-pass to the build of a project, allowing classes to be generated and compiled in a single
operation:

- [`json-kotlin-gradle`](https://github.com/pwall567/json-kotlin-gradle)
- [`json-kotlin-maven`](https://github.com/pwall567/json-kotlin-maven)

## Dependency Specification

The latest version of the library is 0.112, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin-schema-codegen</artifactId>
      <version>0.112</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-kotlin-schema-codegen:0.112'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin-schema-codegen:0.112")
```

Peter Wall

2024-11-13

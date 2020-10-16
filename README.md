# json-kotlin-schema-codegen

Code generation for JSON Schema.

## Background

[JSON Schema](https://json-schema.org/) provides a means of describing JSON values - the properties of an object,
constraints on values etc. - in considerable detail.
Many APIs now use JSON Schema to specify the content of JSON parameters and response objects, and one way of ensuring
conformity to the specified schema is to generate code directly from the schema itself.

This is not always possible - some characteristics described by a schema may not be representable in the implementation
language.
But for a large subset of schema definitions a viable code representation is possible, and this library attempts to
provide conversions for the broadest possible range of JSON Schema definitions.

The library uses a template mechanism (using [Mustache](https://github.com/pwall567/kotlin-mustache) templates), and
templates are provided to generate classes in Kotlin and Java.

## Quick Start

Simply create a `CodeGenerator`, supply it with details like destination directory and package name, and invoke the
generation process:
```kotlin
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "output/directory"
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(File("/path/to/example.schema.json"))
```

## Further Explanation

The Code Generator can process a single file or multiple files in one invocation.
The `generate()` function takes a `vararg` parameter list, and each item may be a file or a directory; in the latter
all the files in the directory with filenames ending `.json` or `.yaml` will be processed.

It is preferable to process multiple files in this way because the code generator can create references to other classes
that it knows about - that is, classes generated in the same run.

## Reference

A `CodeGenerator` object is used to perform the generation.
It takes a number of parameters, many of which can be specified either as constructor parameters or by modifying
variables in the constructed instance.

### Parameters

- `templates` - the set of templates to use (the default is "kotlin" and the options are "kotlin", "java" or
"typescript" - typescript coverage is extremely rudimentary at this time)
- `suffix` - filename suffix to use on generated files (default ".kt")
- `basePackageName` - the base package name (if directories are supplied to the `generate()` function, the subdirectory
names are used as sub-package names)
- `baseDirectoryName` - the base directory to use for generated output

## Dependency Specification

The latest version of the library is 0.11.1, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin-schema-codegen</artifactId>
      <version>0.11.1</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-kotlin-schema-codegen:0.11.1'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin-schema-codegen:0.11.1")
```

Peter Wall

2020-10-16

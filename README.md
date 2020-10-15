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
templates are provided for Kotlin and Java.

## Quick Start

Simply create a `CodeGenerator`, supply it with details like destination directory and package name, and invoke the
generation process:
```kotlin
        val codeGenerator = CodeGenerator()
        codeGenerator.baseDirectoryName = "output/directory"
        codeGenerator.basePackageName = "com.example"
        codeGenerator.generate(File("/path/to/example.schema.json"))
```

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

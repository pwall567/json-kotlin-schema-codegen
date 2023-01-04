# Configuration Guide &ndash; `json-kotlin-schema-codegen`

## Introduction

The code generator will make intelligent choices for most aspects of the generation process, and in many cases the
defaults chosen will be ideal, or at least acceptable.
But for many situations, additional configuration is required &ndash; for example, to nominate the name to be used for a
generated class, or to specify how a non-standard extension to the JSON Schema syntax is to be handled.

All of this functionality is accessible through the `CodeGenerator` object, but the configuration file provides a
concise and convenient means of customising the code generation for a project.

The config file may take the form of a JSON or YAML file, or may be supplied as a parsed JSON object.


## To Use

The use of the configuration file is simple &ndash; just provide the file to the `configure()` function of the
`CodeGenerator`:
```kotlin
        val codeGenerator = CodeGenerator()
        codeGenerator.configure(File("/path/to/config.json"))
        codeGenerator.baseDirectoryName = "output/directory"
        codeGenerator.generate(File("/path/to/example.schema.json"))
```


## Configuration Settings

The configuration file includes the following:
- [`title`](#title)
- [`version`](#version)
- [`description`](#description)
- [`targetLanguage`](#targetlanguage)
- [`packageName`](#packagename)
- [`markerInterface`](#markerinterface)
- [`generatorComment`](#generatorcomment)
- [`nestedClassNameOption`](#nestedclassnameoption)
- [`derivePackageFromStructure`](#derivepackagefromstructure)
- [`extensionValidations`](#extensionvalidations)
- [`nonStandardFormat`](#nonstandardformat)
- [`customClasses`](#customclasses)
- [`classNames`](#classnames)
- [`annotations`](#annotations)
- [`companionObject`](#companionobject)


## `title`

Adds a title to the configuration (optional &ndash; for documentation purposes only).
If present, the value must be a string.


## `version`

Adds a version id to the configuration (optional &ndash; for documentation purposes only).
If present, the value must be a string.


## `description`

Adds a description to the configuration (optional &ndash; for documentation purposes only).
If present, the value must be a string.


## `targetLanguage`

The default target language is Kotlin; to change this, the `targetLanguage` property may be used:
```json
{
  "targetLanguage": "java"
}
```
The values allowed are `kotlin`, `java` or `typescript`.

**New in version 0.80:** Java output now includes `Builder` classes, to aid with creation of classes with long
constructor parameter lists.


## `packageName`

The package name may be specified as a configuration option:
```json
{
  "packageName": "com.example.data"
}
```
The value must be a non-empty string, or `null` to specify that no package name is to be used (this is the default).


## `markerInterface`

The code generator allows a &ldquo;marker&rdquo; interface to be added to each generated class.
This may be specified as a configuration option:
```json
{
  "markerInterface": "com.example.Model"
}
```
The value must be a non-empty string, or `null` to specify that no marker interface is to be used (this is the default).


## `generatorComment`

The generator comment (added to the comment block at the start of each generated file) may be specified as a
configuration option:
```json
{
  "generatorComment": "Generated from v1.1 of the schema"
}
```
The value must be a non-empty string, or `null` to specify that no generator comment is to be used (this is the
default).


## `nestedClassNameOption`

Consider the following schema:
```json
{
  "$schema": "http://json-schema.org/draft/2019-09/schema",
  "$id": "http://example.com/test-1",
  "type": "object",
  "properties": {
    "name": {
      "$ref": "#/$defs/NameType"
    }
  },
  "$defs": {
    "NameType": {
      "type": "object",
      "properties": {
        "givenName": {
          "type": "string"
        },
        "surname": {
          "type": "string"
        }
      }
    }
  }
}
```
When the code generator needs to create a nested class for the `name` property, there are two options for choosing the
name of that class:
- when the property references another schema by means of a `$ref` as in this example, use a name derived from the
reference (in this case that will be `NameType`)
- derive the name from the property name (in this case, `Name` &ndash; the capitalised form of `name`)

The naming option may be specified by:
```json
{
  "nestedClassNameOption": "refSchema"
}
```
The values allowed are `refSchema` (this is the default) or `property`.


## `derivePackageFromStructure`

When generating a classes from a set of schema files in a directory structure, the code generator will optionally use
the directory structure of the schema files to determine the package to be used for the generated classes.
This option (default `true`) may be configured by the use of the `derivePackageFromStructure` setting:
```json
{
  "derivePackageFromStructure": false
}
```


## `extensionValidations`

The JSON Schema specification allows for extensions &ndash; additional keywords denoting aspects of the schema
description not covered by the general specification.
For example, an organisation may have a defined set of "domain primitives" &ndash; small immutable objects that are
used frequently throughout the organisation's IT systems &ndash; and may wish to use JSON Schema extensions to simplify
the use of these types:
```json
{
  "$schema": "http://json-schema.org/draft/2019-09/schema",
  "$id": "http://example.com/test-2",
  "type": "object",
  "properties": {
    "amount": {
      "type": "string",
      "x-local-type": "money"
    },
    "currency": {
      "type": "string",
      "x-local-type": "currency"
    }
  }
}
```

The code generator provides two separate mechanisms for dealing with JSON Schema extensions:
- extension validations, where an extension is declared to be the equivalent of other schema constructs, or
- custom classes, making use of pre-existing classes that provide the required functionality.

The second option is described below (see [`customClasses`](#customclasses)); the first describes the type in terms of
regular JSON Schema syntax:
```json
{
  "extensionValidations": {
    "x-local-type": {
      "money": {
        "type": "string",
        "pattern": "^[0-9]{1,9}\\.[0-9]{2}$"
      },
      "currency": {
        "type": "string",
        "pattern": "^[A-Z]{3}$"
      }
    }
  }
}
```
This specifies an extension `x-local-type` with two possible values:
- `money`, which causes a pattern validation for decimal strings values to be added to the property
- `currency`, which does the same with a 3-character alphabetic pattern validation.

The schema object (the JSON object following `money` or `currency` in the above example) may contain any form of
validation, and as well as the pattern validations as shown here, typical uses might include a `minimum` of zero (to
disallow negative values), a `minLength` of 1 (to enforce the use of non-empty strings) or a `format` of `uuid` (to
require that an application-specific id is always a UUID).
The validations provided in this way are added to any existing schema definitions at the point where the extension
appears.
This may have the effect of causing the generator to output initialisation validations, or in some cases may affect the
choice of generated class for a property &ndash; for example, adding a format validation may cause the generator to use
a standard class like `UUID` or `LocalDate`.

There is no limit to the number of extension keywords or the values allowable for each keyword, but only string values
may be specified using this mechanism.
More complex extensions will still require configuration code to configure the `CodeGenerator` object.


## `nonStandardFormat`

The `format` construct in JSON Schema may be used with non-standard format keywords, and many users will prefer this
over defining an extension keyword and value.

Using this approach, the schema definition from the earlier example becomes:
```json
{
  "$schema": "http://json-schema.org/draft/2019-09/schema",
  "$id": "http://example.com/test-3",
  "type": "object",
  "properties": {
    "amount": {
      "type": "string",
      "format": "money"
    },
    "currency": {
      "type": "string",
      "format": "currency"
    }
  }
}
```

Again, there are two alternative mechanisms for configuring the code generator to work with non-standard format
keywords.
And again, the second option is described under [`customClasses`](#customclasses); the first uses regular JSON Schema
syntax, this time on an individual format:
```json
{
  "nonStandardFormat": {
    "money": {
      "type": "string",
      "pattern": "^[0-9]{1,9}\\.[0-9]{2}$"
    },
    "currency": {
      "type": "string",
      "pattern": "^[A-Z]{3}$"
    }
  }
}
```
This specifies two new `format` keywords, `money` and `currency` which act like the extension definitions above.

And as with extensions, there are no limits on the number of additional keywords defined, or on the types of schema
definitions used.
It is even possible to define a format as mapping to another format &ndash; for example, specifying a new keyword
`x-iso8601-date` as a schema containing `"format": "date"` will cause the code generator to use `LocalDate` for any
properties using that format (assuming the format for `date` has not itself been overridden).


## `customClasses`

The code generator will decide on the data type for the properties of an object or the members of an array based on a
set of built-in rules.
For example, a Kotlin `String` will be generated for a property of type `string`, unless the property also has a
`format`, in which case the type chosen may be `LocalDate` or `UUID` or one of a number of other known types.

In most cases, the choices made by the generator will be exactly what the user wants, but in some cases there will be a
need to specify the use of nominated types for certain properties or array items.

To revisit the example of `money` and `currency` data types &ndash; many organisations will have their own classes to
hold values of these types, and they will require the generated code to make use of these classes.

### `extension`

The JSON Schema extensions described above can be used to indicate that a custom class is to be used, for example:
```json
{
  "customClasses": {
    "extension": {
      "x-local-type": {
        "money": "com.example.util.Money",
        "currency": "com.example.util.Currency"
      }
    }
  }
}
```
This will cause all properties that contain these extension constructs to be generated as references to the specified
classes.
If the generated classes are to be used in conjunction with automated JSON serialisation and deserialisation, it is the
responsibility of the user to provide custom functions to handle this.

### `format`

The use of non-standard `format` keywords can also be used to specify the generation of custom class references:
```json
{
  "customClasses": {
    "format": {
      "money": "com.example.util.Money",
      "currency": "com.example.util.Currency"
    }
  }
}
```
This mechanism can also be used to specify alternative classes for formats that would normally cause the generator to
use one of its inbuilt mappings.
For example, even though the Joda Time library has long been deprecated, many organisations still have a considerable
investment in software that uses that library, so an implementation may wish to direct the code generation to use Joda
Time classes for `date` or `date-time` formats.

### `uri`

The most fine-grained control of custom class selection can be achieved by specifying the location of the property in
the schema by means of its URI (including fragment locator):
```json
{
  "customClasses": {
    "uri": {
      "https://example.com/demonstration/account.schema.json#/properties/balance": "com.example.util.Money",
      "https://example.com/demonstration/account.schema.json#/properties/currency": "com.example.util.Currency"
    }
  }
}
```
It could be very tedious to specify every occurrence of a particular type individually, but the technique also applies
to definitions included by means of a `$ref`.
Specifying the URI of the referenced definition will cause the nominated class to be used for all references to the
definition.

This use of URI to specify custom class selection has the distinct advantage that it may be used in conjunction with
schema files that are not open to modification, for example schema files that are read directly from public websites.

**NOTE:** If a default value is given for a property that maps to a custom class, the code generator will output a
constructor for the custom class, with the default value as a single parameter.
If no such constructor exists, default values should be avoided.

## `classNames`

The code generator will attempt to choose names for generated classes based on the `$id` of the schema.
On most occasions this will lead to satisfactory results, but in many cases, users will wish to specify the class name
to be used for the generated code.
```json
{
  "classNames": {
    "urn:jsonschema:com:example:Person": "Person"
  }
}
```
This will cause the schema definition with the specified `$id` to be generated as a class with the name given.
Note that in this case, the class name is **not** a fully-qualified class name &ndash; the package name used will be the
one specified with the [`packageName`](#packagename) configuration option (possibly extended by the directory structure
&ndash; see the [`derivePackageFromStructure`](#derivepackagefromstructure) option).


## `annotations`

The code generator can be configured to add annotations to the generated classes and fields (properties).
The annotation may be a simple marker annotation with no parameters, or may have parameters formed from a
[Mustache](https://github.com/pwall567/kotlin-mustache) template.

For example, to add a `@Generated` annotation to each class:
```json
{
  "annotations": {
    "classes": {
      "javax.annotation.Generated": "\"{{&generator}}\", date=\"{{&dateTime}}\""
    }
  }
}
```

This will result in an annotation similar to the following being added to each class:
```kotlin
import javax.annotation.Generated

@Generated("net.pwall.json.schema.codegen.CodeGenerator", date="2022-07-18T10:36:18.626+10:00")
```

The example illustrates several important points:
1. Class annotations are defined in a section headed `classes` (field annotations are in a section headed `fields`).
2. The annotation name must be supplied as a fully-qualified class name.
   The fully-qualified name will be used on an `import`, and just the class name will be used on the annotation itself.
3. The parameters for the annotation must be supplied together as a single string, and any special characters like
   quotes must be escaped with backslashes as is normal for JSON (when using YAML, single-quoted strings allow any
   special characters other than single quote itself).
   No syntax checking is performed on the parameters, and any errors may result in code that will not compile.
   The template must **not** include the parentheses surrounding the parameters;
   these will be added by the generator when required.
4. The context object used during Mustache template processing contains several variables that may be useful.
   Some of these are listed [below](#template-context-object).

A second example shows a field annotation with no parameters:
```json
{
  "annotations": {
    "fields": {
      "com.example.anno.Demo": null
    }
  }
}
```

This will result in the simple annotation `@Demo` being added to each field with no parameters (and no parentheses),
with an `import com.example.anno.Demo` added to the start of the file.

This example shows:
1. Field annotations are defined in a section headed `fields`.
2. The absence of parameters is indicated by a `null` template string.

### Template Context Object

The Mustache template expansion can take values from a "context object".
For class annotations, the context object contains the following:

| Name          | Type         | Description                                |
|---------------|--------------|--------------------------------------------|
| `className`   | `String`     | the class name                             |
| `packageName` | `String?`    | the package name (or `null`)               |
| `source`      | `String`     | the schema source file name (if available) |
| `schema`      | `JSONSchema` | the schema as a `JSONSchema`               |

For field annotations, the context object contains:

| Name          | Type      | Description                                                                |
|---------------|-----------|----------------------------------------------------------------------------|
| `name`        | `String`  | the field name                                                             |
| `kotlinName`  | `String`  | the name, usable as a Kotlin variable name (see below)                     |
| `javaName`    | `String`  | the name, usable as a Java variable name (see below)                       |
| `isObject`    | `Boolean` | `true` if the field type is `object`                                       |
| `isArray`     | `Boolean` | `true` if the field type is `array`                                        |
| `isString`    | `Boolean` | `true` if the field type is `string`                                       |
| `isBoolean`   | `Boolean` | `true` if the field type is `boolean`                                      |
| `isDecimal`   | `Boolean` | `true` if the field type is `decimal`                                      |
| `isIntOrLong` | `Boolean` | `true` if the field type is `integer`                                      |
| `isInt`       | `Boolean` | `true` if the field type is `integer` and will fit in an `Int` (see below) |
| `isLong`      | `Boolean` | `true` if the field type is `integer` and will not fit in an `Int`         |
| `isRequired`  | `Boolean` | `true` if the field appears in a `required` constraint                     |

The `kotlinName` will be the same as the `name`, except when it contains characters unacceptable in a Kotlin variable
name (such as spaces) or when it is a reserved word (like `val`).
In these cases, the `kotlinName` is the original name enclosed in backticks.

Java does not have the backtick mechanism, so `javaName` is the original name with unacceptable characters replaced by
underscore, and names clashing with reserved words being suffixed with underscore.

A field that is declared to be an `integer` will be generated as a `Long`, unless there are `minimum` and `maximum`
constraints limiting it to 32 bits (signed).

The `Boolean` values may be used in a Mustache conditional "section", as follows:
```json
{
  "annotations": {
    "fields": {
      "com.example.anno.Demo": "\"Field {{name}} is {{^isString}}not {{/isString}}a String\""
    }
  }
}
```

### Parent Context Object

Mustache name resolution uses a chain of context objects; if a name is not found in the current context object,
resolution switches to the parent context object, then to the parent's parent, and so on.
For both class annotations and field annotations, the parent context object provides information on the current code
generation run:

| Name        | Type             | Description                                               |
|-------------|------------------|-----------------------------------------------------------|
| `dateTime`  | `OffsetDateTime` | the date/time of the current generation run               |
| `date`      | `LocalDate`      | the date portion of the above date/time                   |
| `time`      | `LocalTime`      | the time portion of the above date/time                   |
| `generator` | `String`         | the code generator name, suitable for use in `@Generated` |
| `uuid`      | `UUID`           | a random UUID, for tagging a specific build               |

The use of this information is shown in the first example in the [`annotations`](#annotations) section.

## `companionObject`

It can be useful to create "factory" functions to instantiate the generated classes, using extension functions on the
companion object of the class.
For example, to create an instance of a generated class `Person`:
```kotlin
    fun Person.Companion.create(name: String): Person {
        // create a Person with the supplied name
    }
```

This requires that the generated class have a companion object, and that is not always the case.
To force the output of a companion object for one or more specified classes, or for all classes, the `companionObject`
configuration setting may be used.
This setting takes either a boolean, to enable or disable the output of companion objects for all classes, or an array
of strings nominting the individual classes.

To select this option for all classes:
```json
{
   "companionObject": true
}
```

Or for a selected set of classes:
```json
{
   "companionObject": [ "Class1", "Class2" ]
}
```
(just the class name, not the fully-qualified name including package, must be specified)

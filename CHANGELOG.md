# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [0.111] - 2024-09-11
### Changed
- templates: relocated under base template directory
- `CodeGenerator`: changed to load templates from new location

## [0.110] - 2024-09-05
### Changed
- `CodeGenerator`, `Configurator`, `Constraints`, `Target`: Switched from `jsonutil` library to `kjson-core`, and from
  `yaml-simple` to `kjson-yaml`
- `CodeGenerator`, `Configurer`, `Target`, `Annotated`: Switched from `kotlin-mustache` to `mustache-k`
- `CodeGenerator`: added explicit types to public APIs

## [0.109] - 2024-09-05
### Changed
- templates: fixed bug in templates for map-based classes
- `CodeGenerator`: added explicit types to public APIs

## [0.108] - 2024-08-07
### Added
- `build.yml`, `deploy.yml`: converted project to GitHub Actions
### Changed
- `pom.xml`: updated Kotlin version to 1.9.24, updated dependency versions
### Removed
- `.travis.yml`

## [0.107] - 2024-03-11
### Changed
- `CodeGenerator`, `Configurator`, `ClassName`, templates: added ability to configure class name for decimal values
- templates: fixed bug in output of comments for nested enum classes
- `pom.xml`: updated dependency version

## [0.106] - 2024-03-04
### Changed
- `CodeGenerator`: extended `classNames` configuration option to cover nested classes
- `CodeGenerator`, `Configurator`, `Constraints`, templates: added extensible enum (experimental)

## [0.105] - 2024-03-01
### Changed
- templates: switch from `matches` to `containsMatchIn` for `Regex` pattern matching in `patternProperties`
- `CodeGenerator`: updated for changes to default and examples validation
- `pom.xml`: updated dependency versions

## [0.104] - 2024-02-25
### Changed
- `CodeGenerator`, `Configurator`: added ability to validate examples and default values
- `pom.xml`: updated dependency versions

## [0.103] - 2024-02-22
### Changed
- `CodeGenerator`: fixed a bug in `patternProperties`
- templates: fixed bug in arrays in map classes
- `pom.xml`: updated dependency version

## [0.102] - 2024-02-18
### Changed
- templates: fixed another bug in nested map classes

## [0.101] - 2024-02-18
### Changed
- `CodeGenerator`, `Validation`, templates: improved validations, improved `toString` in delegating classes
- templates: fixed bug in enums nested in map classes
- `pom.xml`: updated dependency version

## [0.100] - 2024-02-08
### Changed
- `CodeGenerator`, `Configurator`, `Constraints`, `Target`, `Validation`, templates: added code generation for
  `additionalProperties`, `patternProperties` `minProperties` and `maxProperties`
- `pom.xml`: updated dependency version

## [0.99] - 2023-12-12
### Changed
- `pom.xml`: updated dependency versions

## [0.98] - 2023-11-20
### Changed
- `CodeGenerator`: reverted change from version 0.97
- `Target`: fixed bug in base / derived classes with array of object in base

## [0.97] - 2023-11-20
### Changed
- `CodeGenerator`: fixed bug in base / derived classes with array of object in base

## [0.96] - 2023-11-06
### Changed
- `CodeGenerator`: fixed bug in default values for array of enum
- `CodeGenerator`: allow `minLength` / `maxLength` on custom classes that implement `CharSequence`

## [0.95] - 2023-09-27
### Changed
- templates: fixed minor bug in Java output
- templates: fixed minor bug in output of companion object
- `pom.xml`: updated dependency version

## [0.94] - 2023-09-17
### Changed
- `CodeGenerator`: fixed bug in `sanitiseName()`
- `CodeGenerator`, `Constraints`, `Target`, `Annotated`: apply annotations to nested classes

## [0.93] - 2023-09-06
### Changed
- `pom.xml`: updated dependency version
- `pom.xml`: updated Kotlin version to 1.8.22

## [0.92] - 2023-09-04
### Changed
- templates: modified Java code to avoid potential name clash
- `CodeGenerator`: bug fix (import for class used in base class)

## [0.91] - 2023-07-10
### Changed
- `pom.xml`: updated dependency versions

## [0.90] - 2023-04-19
### Changed
- `CodeGenerator`, templates: fixed bug in Typescript output (index)

## [0.89] - 2023-04-19
### Changed
- `StringValue`: switch hex conversion to use `IntOutput`
- `pom.xml`: update Kotlin version to 1.7.21
- templates: fixed bug in Typescript output (secondary interfaces and enums)
- `Constraints`, `Validation`: bug fix (duplicate validation)

## [0.88] - 2023-02-14
### Changed
- `CodeGenerator`, `Constraints`, `ClassDescriptor`, templates: bug fix (custom class import for derived class)

## [0.87] - 2023-02-12
### Changed
- `CodeGenerator`: added `anyOf` or `oneOf` nullability special case

## [0.86] - 2023-01-04
### Changed
- `CodeGenerator`, `Configurator`, `Target`, templates: added ability to force output of companion object
- `pom.xml`: bumped dependency version

## [0.85] - 2022-12-08
### Changed
- `CodeGenerator`, `DefaultValue`: fixed bug in handling of `default`

## [0.84] - 2022-11-08
### Changed
- `CodeGenerator`, `Constraints`, `Validation`, templates: added limited handling of `not` schema
- `pom.xml`: bumped dependency versions

## [0.83] - 2022-10-16
### Changed
- `CodeGenerator`: bug fix - issue #14
- `CodeGenerator`: improved handling of default values (`DefaultValue` class)
- `CodeGenerator`, `Constraints`, templates: improved Java handling of nullability in derived class
- `pom.xml`: bumped dependency versions

## [0.82] - 2022-07-20
### Added
- `Annotated`, `GeneratorContext`, `annotations.mustache`: annotation handling
### Changed
- `CodeGenerator`, `Configurator`, `Constraints`, `Target`: added annotation handling
- templates, config schema: annotation handling
- `CodeGenerator`: improved error messages
- templates: added default values for custom classes
- `CodeGenerator`: allow duplicates of some validations if they are identical

## [0.81] - 2022-06-30
### Changed
- `pom.xml`: bumped dependency version

## [0.80] - 2022-06-30
### Changed
- `pom.xml`: switched to `log-front` 5.0, dropped `logback`
- `pom.xml`: bumped dependency version
- templates: added `Builder` classes to Java output

## [0.79] - 2022-06-16
### Changed
- `CodeGenerator`: fixed additional bug in validation in derived types

## [0.78] - 2022-06-13
### Added
- `ValidationValue`: marker interface for values used in validations
### Changed
- `CodeGenerator`, `Validation`, templates: fixed bug in validation in derived types
- `NumberValue`, `StringValue`, `Target`: make use of `ValidationValue`
- `pom.xml`: bumped dependency version

## [0.77] - 2022-04-22
### Changed
- templates: bug fix in `multipleOf`

## [0.76] - 2022-04-22
### Changed
- `CodeGenerator`: added more flexible ways to specify targets
- `pom.xml`: bumped dependency version

## [0.75] - 2022-04-21
### Changed
- `CodeGenerator`: another attempt to fix the directory creation problem

## [0.74] - 2022-04-20
### Changed
- `CodeGenerator`: added functions to build target list
- `CodeGenerator`: bug fix - unnecessary output of nested classes for oneOf
- `pom.xml`: bumped dependency versions

## [0.73] - 2022-03-27
### Changed
- `CodeGenerator`, `TargetFileName`: changed creation of directories for generated code
- `pom.xml`: bumped test dependency

## [0.72] - 2022-03-02
### Changed
- `CodeGenerator`, `Constraints`, templates: improved handling of derived classes

## [0.71] - 2022-02-20
### Changed
- `CodeGenerator`: added ability to specify inputs by URI
- `ClassName`: added `of` function
- `Configurator`: added `markerInterface` option

## [0.70] - 2022-02-15
### Changed
- `CodeGenerator`: improved handling of settable parameters

## [0.69] - 2022-02-13
### Changed
- `CodeGenerator`: fixed bug - preload not working correctly
- `CodeGenerator`: changed handling of target list
- `pom.xml`: bumped dependency versions

## [0.68] - 2022-01-20
### Changed
- `CodeGenerator`: fixed bug - derived class losing type details for array in base class

## [0.67] - 2022-01-07
### Changed
- `CodeGenerator`, `Configurator`, `Target`, templates: added comment template functionality
- `CodeGenerator`, `Target`: fixed bug in nested class naming

## [0.66] - 2021-12-11
### Changed
- `pom.xml`: bumped dependency version

## [0.65] - 2021-12-09
### Added
- tests using json-schema.org examples
- `Configurator`: initial implementation of `configure`
- `codegen-config.schema.json`: JSON Schema for config file
- `CONFIG.md`: notes on configuration
### Changed
- `CodeGenerator`: initial implementation of `configure`

## [0.64] - 2021-11-18
### Changed
- `CodeGenerator`: fixed regression bug in `oneOf`

## [0.63] - 2021-11-18
### Changed
- `CodeGenerator`: changed class name selection to accommodate URNs

## [0.62] - 2021-11-15
### Changed
- `CodeGenerator`, templates: fixed bug in base/derived nested classes

## [0.61] - 2021-11-14
### Changed
- templates: fixed bug in constructors (base/derived classes)
- `CodeGenerator`, `Constraints`, `NamedConstraints`, templates: fixed bug in base/derived classes

## [0.60] - 2021-11-10
### Changed
- `CodeGenerator`: improved usage of base and derived class pattern

## [0.59] - 2021-11-09
### Changed
- templates: add toString when generated class not a data class
- `README.md`: added notes on custom classes and the use of data class

## [0.58] - 2021-11-09
### Changed
- `CodeGenerator`, templates: fix bugs in copy and component functions

## [0.57] - 2021-11-08
### Changed
- `CodeGenerator`, templates: fix bugs in copy and component functions

## [0.56] - 2021-11-08
### Changed
- `CodeGenerator`, templates: fix bugs in copy and component functions

## [0.55] - 2021-11-07
### Changed
- `CodeGenerator`, templates: added copy and component function when generated class not a data class

## [0.54] - 2021-11-07
### Changed
- `CodeGenerator`, templates: more bug fixes

## [0.53] - 2021-11-07
### Changed
- `CodeGenerator`, templates: more bug fixes

## [0.52] - 2021-11-07
### Changed
- `pom.xml`: updated to Kotlin 1.5.20

## [0.51] - 2021-11-07
### Changed
- `CodeGenerator`, templates: fixed more bugs in handing of bases classes with `anyOf`

## [0.50] - 2021-11-05
### Changed
- `CodeGenerator`, `ClassDescriptor`, `Target`, templates: fixed bug in handing of bases classes with `anyOf`

## [0.49] - 2021-11-04
### Changed
- `CodeGenerator`: fixed bug - use of `Any` type when property has no attributes

## [0.48] - 2021-10-07
### Changed
- `pom.xml`: bumped dependency version

## [0.47] - 2021-09-28
### Changed
- `CodeGenerator`: fixed another problem with `oneOf`

## [0.46] - 2021-09-28
### Changed
- `Target`, `ClassDescriptor`: fixed problem with `oneOf`

## [0.45] - 2021-09-27
### Changed
- `CodeGenerator`, `Constraints`, templates: restructure handling of `oneOf`

## [0.44] - 2021-09-27
### Changed
- `CodeGenerator`, templates: another attempt to fix bug in handling of `oneOf`

## [0.43] - 2021-09-26
### Changed
- templates: fixed bug in handling of `oneOf` constructs

## [0.42] - 2021-09-26
### Changed
- `CodeGenerator`, `Validation`, templates: improved decimal range checks
- `CodeGenerator`, `Constraints`, `Target`, `ClassDescriptor`, templates: added handling of `oneOf` constructs

## [0.41] - 2021-09-22
### Changed
- templates: improved default value handling

## [0.40] - 2021-09-21
### Changed
- `pom.xml`: bumped dependency versions

## [0.39] - 2021-09-21
### Changed
- `pom.xml`: bumped dependency versions

## [0.38] - 2021-09-21
### Changed
- `pom.xml`: bumped dependency versions

## [0.37] - 2021-09-21
### Changed
- `pom.xml`: bumped dependency versions

## [0.36] - 2021-09-17
### Changed
- `CodeGenerator`: removed generation of unnecessary min-max checks
- `pom.xml`: bumped dependency versions

## [0.35] - 2021-08-13
### Changed
- `Target`: check for repeated use of same reference and generate nested class only once
- `CodeGenerator`: simplify specification of custom classes

## [0.34] - 2021-06-20
### Changed
- `CodeGenerator`: changed code generation to ignore "oneOf", "anyOf" and "not"
- `CodeGenerator`: fixed bug - output of enums with invalid names
- `ItemConstraints`, `NamedConstraints`, templates: allow for property names that are not valid Kotlin
- `pom.xml`: updated dependency versions

## [0.33] - 2021-05-20
### Changed
- `pom.xml`: updated dependency version

## [0.32] - 2021-05-17
### Changed
- templates: modified index template for TypeScript 

## [0.31] - 2021-04-20
### Changed
- `pom.xml`: bumped dependency versions (with consequent changes)
- `CodeGenerator`: minor optimisations
- `ClassId`, `ClassName`: clarified use of package and directory structure

## [0.30] - 2021-04-05
### Changed
- `CodeGenerator`: reorganised array validations (particularly nested arrays)
- templates: improve pattern validation
- `CodeGenerator`, `Validation`, templates: optimised array length validations
- templates: optimised generation for multiple validations on same property
- `CodeGenerator`: fixed bug in mapping custom classes
### Added
- integration test (initial simple test of generated code)
- integration test (tests of complex generated code)

## [0.29] - 2021-03-31
### Changed
- `CodeGenerator`, `Validation`, templates: optimised decimal comparisons
- `CodeGenerator`, `Validation`, templates: optimised integer and long range checks
- `CodeGenerator`: bug fix - no longer outputs checks for strings with format causing them to be non-string class
- templates: use `open class` for empty inner class

## [0.28] - 2021-03-23
### Changed
- `CodeGenerator`, templates: changed handling of target language
- `CodeGenerator`, `Constraints`, templates: improved handling of enums
### Added
- `TargetLanguage`: new

## [0.27] - 2021-03-14
### Changed
- `CodeGenerator`, `OutputResolver`, `Target`, `TargetClass`: major changes to handling of target file name
- `CodeGenerator`, templates: added ability to output "index" files
### Added
- `TargetFileName`: new

## [0.26.1] - 2021-03-04
### Changed
- templates: fix bug in enum in TypeScript

## [0.26] - 2021-03-04
### Changed
- templates: added support for enum in TypeScript
- `CodeGenerator`, `Target`, templates: improved TypeScript output
### Added
- `TargetClass`: abstracts functionality for imports etc.

## [0.25.1] - 2021-03-01
### Changed
- templates: fix bug in support for `uniqueItems`

## [0.25] - 2021-02-28
### Changed
- `CodeGenerator`, `Validation`, templates: added optimisation of string length checks
- `CodeGenerator`, `ItemConstraints`, templates: added checks on nested arrays
- `CodeGenerator`: added output filter to remove excess newlines
- `CodeGenerator`, `Constraints`, `SystemClass`, templates: added support for `uniqueItems`

## [0.24.1] - 2021-02-03
### Changed
- `CodeGenerator`: bug fix in default values

## [0.24] - 2021-01-27
### Changed
- templates: output `open class` for object with no specified properties

## [0.23] - 2021-01-26
### Changed
- templates: minor formatting changes, improved tests
- templates: reduced indentation of Kotlin constructor parameters (from 8 to 4) in line with new Kotlin standard
- `CodeGenerator`: allow multiple delegating validators
- `Constraints`: added support for `int32`

## [0.22] - 2021-01-17
### Changed
- `CodeGenerator`, `Validation`, templates: changed handling of format duration, added json-pointer and
relative-json-pointer
- templates: fixed bug in validation of Java nullable fields

## [0.21.1] - 2021-01-06
### Changed
- `CodeGenerator`: fixed bug in ability to specify custom type by format

## [0.21] - 2021-01-05
### Changed
- `CodeGenerator`: added ability to specify custom type by format

## [0.20] - 2021-01-04
### Changed
- `CodeGenerator`, templates: added ability to add marker interface
- `pom.xml`: bumped dependency version

## [0.19.1] - 2020-12-02
### Changed
- `CodeGenerator`: bug fix - name of generated class

## [0.19] - 2020-12-02
### Changed
- templates: added base/derived classes in Java
- templates: improved generated hashCode in Java
- `CodeGenerator`: allow ".yml" as alternative to ".yaml" file extension
- `CodeGenerator`: allow use of Path to specify files

## [0.18.1] - 2020-11-13
### Changed
- templates: bug fix - derived classes with no additional members

## [0.18] - 2020-11-12
### Changed
- `CodeGenerator`: improved handling of derived classes

## [0.17] - 2020-11-10
### Changed
- `CodeGenerator`: added `NestedClassNameOption`

## [0.16] - 2020-11-05
### Changed
- `CodeGenerator`: added `generatorComment`

## [0.15] - 2020-10-31
### Changed
- `CodeGenerator`: added `generateAll()` to simplify output of multiple classes, e.g. form a Swagger API definition
- `CodeGenerator`: added KDoc

## [0.14.3] - 2020-10-29
### Changed
- `CodeGenerator`: further improvements to custom class URI matching

## [0.14.2] - 2020-10-29
### Changed
- `CodeGenerator`: another minor improvement to custom class URI matching

## [0.14.1] - 2020-10-28
### Changed
- `CodeGenerator`: small improvement to custom class URI matching

## [0.14] - 2020-10-27
### Changed
- `CodeGenerator`: improved handling of custom types

## [0.13] - 2020-10-25
### Changed
- templates: simplified indentation of generated code
- templates: added null check on Java array validation
- `CodeGenerator`: added ability to specify custom generated type for nominated nodes

## [0.12] - 2020-10-21
### Changed
- templates: improved output of comments
- templates: added header
- templates: added validation of array items

## [0.11.1] - 2020-10-14
### Changed
- `Constraints`: added `numberOfProperties` to assist templates
- templates: improve handling of classes with no properties
- templates: allow standalone enum in Java

## [0.11] - 2020-10-13
### Changed
- `CodeGenerator`: tidied code from previous changes
- `CodeGenerator`: improved handling of default values
- `CodeGenerator`: improved validation of enums
- `CodeGenerator`: added ability to handle custom validations

## [0.10] - 2020-10-13
### Changed
- `CodeGenerator`: improved output of references to other classes in same generation run

## [0.9.2] - 2020-10-12
### Changed
- `CodeGenerator`: fix bug in `generateClasses()`

## [0.9.1] - 2020-10-12
### Changed
- `CodeGenerator`: added `generateClasses()` (multiple `generateClass()`)

## [0.9] - 2020-10-12
### Changed
- `CodeGenerator`: small improvement to logging
- `CodeGenerator`: added `generateClass()`
- `pom.xml`: added logback for test only
- `pom.xml`: updated `yaml-simple` version

## [0.8] - 2020-10-09
### Changed
- `pom.xml`: updated Mustache version
- `pom.xml`: updated `json-kotlin-schema` version
- `pom.xml`: changed to use `log-front` library
- templates: make use of advanced Mustache features
- several classes: changed to accommodate use of YAML for schema

## [0.7] - 2020-09-17
### Changed
- templates: improved generated Java hashCode
- `pom.xml`: updated Kotlin version to 1.4.0

## [0.6.1] - 2020-09-15
### Changed
- `CodeGenerator`: Fixed bug in validation of nested classes

## [0.6] - 2020-09-15
### Changed
- templates: Improved validation of nullable properties
- templates: output enums

## [0.5] - 2020-09-13
### Changed
- templates: Improved output of Java
- `CodeGenerator`: Added logging
- `CodeGenerator`: Major re-organisation

## [0.4.1] - 2020-08-26
### Changed
- `CodeGenerator`: improved sharing of constant values

## [0.4] - 2020-08-26
### Changed
- `CodeGenerator`: improved handling of pattern validation
- templates: improved output of validation error messages

## [0.3.1] - 2020-08-25
### Changed
- templates: bug fix - validation of nullable types

## [0.3] - 2020-08-25
### Changed
- `CodeGenerator`: Added generation of pattern test

## [0.2.1] - 2020-08-24
### Changed
- `CodeGenerator`: bug fix - repeated nested classes

## [0.2] - 2020-08-23
### Changed
- `validation_string.mustache`: Added validations `uri`, `uri-reference`, `ipv4`, `ipv6`
- `CodeGenerator`: improved output of array of object

## [0.1] - 2020-08-20
### Added
- all files: initial version (copied from `json-kotlin-schema`)

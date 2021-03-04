# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

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

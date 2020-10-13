# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
### Changed
- `CodeGenerator`: tidied code from previous changes
- `CodeGenerator`: improved handling of default values
- `CodeGenerator`: improved validation of enums

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

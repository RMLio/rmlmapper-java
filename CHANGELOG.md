# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- output format: hdt

### Fixed

- local build on Windows 7 works
- object with template with an array as input ? return multiple objects

## [4.1.0] - 2018-10-15

### Added

- output formats: turtle, trig, trix, jsonld
- functions with list input
- metadata generation

### Changed

- use environment variable for RDB testing
- all (including templating) works with function generators

### Fixed

- double join condition
- support quote in literal
- function file path not passed on to the function loader when using the CLI
- when parsing a string into an RDF term, take into account @ is not always a language tag
- parenttriplesmap handling null values
- config file

## [4.0.0] - 2018-09-03

### Added
- support for config file
- no difference between triples and quads when writing output to file

## [0.2.1] - 2018-08-14

### Fixed

- Create valid temp file for prepackaged functions jars
- Use GrelProcessor.class by default instead of GrelFunctions.jar

## [0.2.0] - 2018-08-09

### Added

- support for functions on Predicate Maps
- support for functions on Graph Maps
- support for relational databases (MySQL, PostgreSQL, and SQLServer)

## [0.1.6] - 2018-08-07

### Fixed

- If no valid graph IRI could be generate, add triple to default graph instead of crashing

## [0.1.5] - 2018-08-06

### Fixed

- Correct printing of predicates of quads

## [0.1.4] - 2018-07-16

### Fixed

- Content negotiation done right

## [0.1.3] - 2018-07-16

### Fixed

- If JSONPath gives a null, just don't create a triple instead of crashing

## [0.1.2] - 2018-07-02

### Fixed

- getting a resource file doesn't give null pointer exception when using the packaged jar

## [0.1.1] - 2018-07-02

### Fixed

- outputting quads in stdout correctly

## 0.1.0 - 2018-07-02

### Added

- support for accessing CSV data sources
- support for accessing JSON data sources with JSONPath
- support for accessing XML data sources with XPath
- support for accessing local files
- support for accessing remote files (via HTTP GET)
- basic support for functions

[4.1.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.0.0...v4.1.0
[4.0.0]: https://github.com/RMLio/rmlmapper-java/compare/v0.2.1...v4.0.0
[0.2.1]: https://github.com/RMLio/rmlmapper-java/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.6...v0.2.0
[0.1.6]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.5...v0.1.6
[0.1.5]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.4...v0.1.5
[0.1.4]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.0...v0.1.1

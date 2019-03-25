# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

## [4.3.3] - 2019-03-15

### Added

- Non-equals function and getMimeType function
- LICENSE file (MIT) (fixes [#25](https://github.com/RMLio/rmlmapper-java/issues/25))

### Fixed

- rdf prefix in some test cases
- specified turtle output in cli help and readme (fixes [#23](https://github.com/RMLio/rmlmapper-java/issues/23))

## [4.3.2] - 2019-02-27

### Added

- DBpedia spotlight can be used as a default NER function within a mapping document
- functions added, to be used for conditions

### Fixed

- updated UML diagrams
- fixed [#21](https://github.com/RMLio/rmlmapper-java/issues/21) (thanks @mariapoveda)
- support for -- and updates of -- a whole lot more test cases
- Transform data from databases according to their datatypes, where applicable
- Works with @base in mapping
- Use Exception instead of Error (almost everywhere)
- Validate URI
- `rml:query` instead of `rr:query`
- maven dependency before jitpack dependency to prevent json-ld conflict ([#19](https://github.com/RMLio/rmlmapper-java/pull/19) thanks @duschu)

## [4.3.1] - 2019-01-15

### Fixed

- filtering of quads in SimpleQuadStore

## [4.3.0] - 2018-12-17

### Added

- SPARQL endpoint support

### Changed

- rdf4j version bump to 2.4.1

## [4.2.0] - 2018-11-14

### Added

- support for SPARQL
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

[4.3.3]: https://github.com/RMLio/rmlmapper-java/compare/v4.3.2...v4.3.3
[4.3.2]: https://github.com/RMLio/rmlmapper-java/compare/v4.3.1...v4.3.2
[4.3.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.3.0...v4.3.1
[4.3.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.2.0...v4.3.0
[4.2.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.1.0...v4.2.0
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

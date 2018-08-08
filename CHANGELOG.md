# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

- support for relational databases
- support for NoSQL databases
- support for Web APIs
- support for SPARQL endpoints
- support for TPF servers

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

[0.1.6]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.5...v0.1.6
[0.1.5]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.4...v0.1.5
[0.1.4]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/RMLio/rmlmapper-java/compare/v0.1.0...v0.1.1

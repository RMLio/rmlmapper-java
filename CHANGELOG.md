# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

## [4.10.1] - 2021-06-15

### Fixed
- README Markdown rendering and links
- Follow HTTP redirects for Web APIs (see [issue 196](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/196))

## [4.10.0] - 2021-05-05

### Added
- Added support for named graphs in RDFJStore.
- Support for Logical Target and exporting to a local file with various 
serializations and compression, or a SPARQL endpoint using 
SPARQL UPDATE queries.
- W3C Web of Things Web API access support to retrieve data from Web API 
with token authentication.

### Changed
- Use RDF4JStore by default (see [issue 108](https://github.com/RMLio/rmlmapper-java/issues/108)).
- Public API of the Executor has been updated and is available under `<method>V5`,
the old API is still available but deprecated. In a future release, the old API
will be removed and the new one will drop the `V5` suffix.

### Fixed
- Add remark about duplicate removal and serialization format performance (see [issue 108](https://github.com/RMLio/rmlmapper-java/issues/108)).
- Treat empty values in CSV columns as NULL values in RDBs (see [issue 188](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/188)).
- Fixed reloading the function every iteration, hopefully this gives a speed boost
- Fix R2RML conversion of multiple Triples Maps (see [issue 186](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/186)).
- Fix datatype retrieval when RDB colum names are quoted (see [issue 185](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/185)).

## [4.9.4] - 2021-03-19

### Changed
- Link to our Docker images on Dockerhub in README (see [issue 109](https://github.com/RMLio/rmlmapper-java/issues/109))

### Fixed
- Support @ in JSONPath (see [issue 178](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/178))
- JSONRecord: ignore 'null' values in JSONArray as well (see [issue 103](https://github.com/RMLio/rmlmapper-java/issues/103))

## [4.9.3] - 2021-03-05

### Fixed

- Docker build (see [issue 106](https://github.com/RMLio/rmlmapper-java/issues/106))
- Consistent builds with Maven

## [4.9.2] - 2021-03-04

### Changed

- Endpoints of Oracle and DBpedia Spotlight for tests
- getColumnLabel instead of getColumnName (see [issue 92](https://github.com/RMLio/rmlmapper-java/issues/92))
- Updated dependencies
  - added FnO Grel v0.6.1 which adds toTitlecase, lastIndexOfMapping, sha1, md5 

### Fixed

- support SQL queries that have unnamed columns

### Added
- Add Logical Target support
- Add Web of Things support
- Add compression support

## [4.9.1] - 2020-12-04

### Added
- Support commas in JSONPath 

### Fixed
- Handling of boolean/any FnO parameters (see [MR 116](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/merge_requests/116))
- Print descriptive error when mapping file does not exist or is invalid Turtle (see Github [issue 54](https://github.com/RMLio/rmlmapper-java/issues/54))
- Print mapping file path as debug instead of a warning (see [issue 172](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/172))

## [4.9.0] - 2020-09-14

### Added
- Add to Maven Central (see [issue 94](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/94))

### Fixed
- Fix CSVW with tab (see issues [168](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/168)
and [169](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/169))

## [4.8.2] - 2020-08-17

### Added
- Support function on SubjectMap that generates blank nodes (see [issue 167](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/167))

### Changed
- Updated licenses on the README

### Fixed
- Documentation updated to reflect metadata generation
- Function objects with types other than String/Boolean don't get discarded (see [issue 165](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/165))
- Generating metadata in different RDF format (see [issue 68](https://github.com/RMLio/rmlmapper-java/issues/68))

## [4.8.1] - 2020-07-03

### Changed
- updated grel-functions-java to 0.5.2

### Fixed
- ObjectMap with type Blank Node is ignored (see [issue 164](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/164))
- Support double quotes in references of RDBs (see [issue 163](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/163))

## [4.8.0] - 2020-05-25

### Added
- Oracle driver information in README (see [issue 142](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/142))
- Support Oracle databases (see [issue 160](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/160))

### Changed
- Functions support more datatypes: `be/ugent/rml/functions/FunctionUtils.java`

### Removed
- Oracle setup file (see [issue 161](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/161))

## [4.7.0] - 2020-03-09

### Added

- support for list-style parameter arguments for functions (see test `rml-fno-test-cases/RMLFNOTC0023-CSV`)

### Changed

- conform with latest <https://fno.io> spec
  - the old way of describing a link to a JAVA library is currently still supported
- usage of external GREL functions library
  - by default, these _classes_ are loaded, _even when the function file parameter has another file_
  - moved some functions to IDLabFunctions
- FunctionLoader takes a `store` as constructor, not a file
- for now, allow fallback on old FnO IRIs
- dynamic function libraries (i.e., jars) are found relative to the cwd
- renamed Utils::getInputStreamFromMOptionValue to Utils::getInputStreamFromFileOrContentString
- changed URL of remote data file src/test/resources/test-cases/RMLTC1003-CSV/mapping.ttl
- moved IDLabFunction tests to its canonical place
- added idlab-fn:inRange function
- in `TestFunctionCore`: `doPreloadMapping` just adds up to the existing `functions_idlab.ttl` descriptions to avoid duplication
  - random generator defaults to "random_string_here" to avoid space characters.

## [4.6.0] - 2019-11-19

### Added
- R2RML support

### Changed
- QuadFactory (removes need for explicit RDF4J store)
- SQLTestCore created, used between RML SQL tests and R2RML tests

### Fixed
- Username without password is possible
- Completed prefixes in some test mappings
- Generation of blank nodes via Object Maps
- FnO IRI

## [4.5.1] - 2019-09-04

### Fixed
- Absolute paths for data source files

## [4.5.0] - 2019-08-06

### Added
- Support for `rml:languageMap` (see [issue 9](https://github.com/RMLio/rml-test-cases/issues/9) of the RML Test cases)

## Fixed
- Validation of IRIs (see [issue 37](https://github.com/RMLio/rmlmapper-java/issues/37))
- Example code in README (see [issue 39](https://github.com/RMLio/rmlmapper-java/issues/39))

## [4.4.2] - 2019-07-15

### Added

- Configure [no-response bot](https://github.com/probot/no-response) for Github ([issue 35](https://github.com/RMLio/rmlmapper-java/issues/35))
- Dockerfile
- Add table of contents to README
- Refactor data access and records
- Test case for CSV with special characters

### Fixed

- Base IRI is read from the mapping doc when using CLI

## [4.4.1] - 2019-06-17

### Added

- Javadoc generation

### Fixed

- A file that is mentioned multiple times in the mapping doc is not re-read multiple times
- Turtle output contains datatypes and languages

## [4.4.0] - 2019-05-16

### Added

- support for CSVW
- Support for combining multiple mappings files

### Changed

- Print function-file path when doing verbose debugging

### Fixed

- FileNotFoundException: actually show which file wasn't found
- Non-conforming language tags are detected

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

[4.10.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.10.0...v4.10.1
[4.10.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.9.3...v4.10.0
[4.9.4]: https://github.com/RMLio/rmlmapper-java/compare/v4.9.3...v4.9.4
[4.9.3]: https://github.com/RMLio/rmlmapper-java/compare/v4.9.2...v4.9.3
[4.9.2]: https://github.com/RMLio/rmlmapper-java/compare/v4.9.1...v4.9.2
[4.9.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.9.0...v4.9.1
[4.9.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.8.2...v4.9.0
[4.8.2]: https://github.com/RMLio/rmlmapper-java/compare/v4.8.1...v4.8.2
[4.8.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.8.0...v4.8.1
[4.8.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.7.0...v4.8.0
[4.7.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.6.0...v4.7.0
[4.6.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.5.1...v4.6.0
[4.5.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.5.0...v4.5.1
[4.5.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.4.2...v4.5.0
[4.4.2]: https://github.com/RMLio/rmlmapper-java/compare/v4.4.1...v4.4.2
[4.4.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.4.0...v4.4.1
[4.4.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.3.3...v4.4.0
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

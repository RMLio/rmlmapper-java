# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

## [7.1.2] - 2024-09-19

### Fixed
- `release.sh` now also updates `pom.xml` with the given tag.

## [7.1.1] - 2024-09-19

### Fixed
- Updated idlab-functions-java to 1.3.3 to use correct state path in stateful functions when `ifState` environment variable is set.

### Changed
- CI: use local script for release

## [7.1.0] - 2024-08-13

### Added
- Serialization format TriG is now supported for Logical Targets (fixes GitHub [issue 225](https://github.com/RMLio/rmlmapper-java/issues/225)).

### Fixed
- Crash when null is returned in an XPath function for subject generation (fixes GitHub [issue 236](https://github.com/RMLio/rmlmapper-java/issues/236))
- Update to DataIO 1.3.1: `DATAIO_NULL` value occurring in RDF output when CSV quoted string has newline character in it (fixes GitHub [issue 238](https://github.com/RMLio/rmlmapper-java/issues/238))
- RML Test report script: remove newlines from R2RML targ name

## [7.0.0] - 2024-06-07

### Fixed
- Gitlab CI R2RML test cases compliance
- Gitlab CI Maven Central retrying on failure due to Maven Central server problems
- Gitlab CI Docker Hub deployment fixed
- Update DataIO dependency to 1.1.0 (fixes GitLab [issue 274](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/274))
- R2RML Test Report CI script failed in development due to missing pip parameter.
- Optimization for self-joins without join conditions (fixes Gitlab [issue 275](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/275))
- Optimization for self joins with join condition.
- Updated junit to 5.10.0
- Updated jena to 4.9.0
- Updated testcontainers to 1.18.3
- Added explicit dependency on json-smart, since it was an indirect dependency which changed scope
- Updated idlab-functions-java to 1.3.2, fixing an issue with IncRML
- Updated dataio to 1.2.0
- Changed scope of `json-path` dependency to `runtime`

### Changed
- Changed basepath from null to http://example.com/ (see [issue 263](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/263))
- gitlab CI pull policy

### Added
- Translation to new RML. Only for Core.

## [6.5.1] - 2023-12-06

## Fixed
- Upgrade logback-classic to 1.4.14.
- Upgrade wiremock-jre8 to 2.35.1.

## [6.5.0] - 2023-12-06

### Changed
- Update dependency on `idlab-functions-java` to 1.3.1. This implies three things:
  - The namespace for IDLab functions FnO descriptions changed, so RMLMapper loads the old and the new one to remain compatible;
  - All tests in RMLMapper use the new namespace;
  - Stateful functions use a more compact and efficient state.
- Update dependency on `function-agent-java` to 1.2.0, the first version released on Maven Central.
- Upgraded rdf4j-client dependency to version 4.3.8
- Compile regex patterns for RDF4J store only once for performance.

### Fixed
- Setting option `disable-automatic-eof-marker` in a configuration file has no effect (see GitLab [issue #269](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/269)).
- Start adhering to [Keep a Changelog](http://keepachangelog.com/en/1.1.0/) from now on (`Improvements` -> `Changed`).
- Compatibility issue with old IDLab functions FnO descriptions (see GitLab [issue #270](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/270))

### Added
- Test case for working with CSV logical sources without header. 

## [6.4.0] - 2023-11-14

### Added
- Test handling Windows CRLF line ending for CSV files ([issue #201](https://github.com/RMLio/rmlmapper-java/issues/201))
- Added test for empty column handling in csv logical source files ([issue #159](https://github.com/RMLio/rmlmapper-java/issues/159))

### Improvements
- Tests: let Java handle temporary files.
- pom: upgrade maven-surefire-plugin to 3.2.2
- pom: upgrade nexus-staging-maven-plugin to 1.6.13
- pom: upgrade json-path to 2.8.0
- pom: upgrade logback-classic to 1.4.11
- resources: rml-ldes: avoid symlinks 
- pom: upgrade RDB connectors (postgresql 42.6.0, mssql-jdbc 12.4.2.jre11, ojdbc11 23.3.0.23.09, mysql-connector-j 8.2.0)

### Changed
- Use the DataIO library to handle access to files.
- Requires Java 17 language level.
- Refactoring to use interfaces where possible (e.g. `ArrayList` -> `List`).

### Fixed
- Test cases: add array initializers to avoid bugs.
- Use correct prefix for `EventStreamTarget`s in tests.
- Refactoring of Executor: code deduplication
- Document `--disable-automatic-eof-marker` option.

## [6.3.0] - 2023-11-14

### Improvements
- Add support for detecting additions, modifications, and deletions in a Knowledge Graph with FnO functions.
- Verify LDES EventStreamTarget output for additions, modifications, and deletions.
- Added RML-LDES test-cases.
- Added test for mapping with CSV file where column does not match header

### Changed
- Build Docker image in two stages, reducing the final image size.
- LDES EventStreamTarget properties are now all optional.
- Removed usage of legacy RDF model, using RDF4J instead.

### Fixed
- Don't close output streams that don't need closing.
- Add url decoders to file paths so special characters and spaces work.
- Test cases: add array initializers to avoid bugs.

## [6.2.2] - 2023-10-05

### Fixed
- Fixed self-joins with join conditions ([internal issue #199](https://github.com/RMLio/rmlmapper-java/issues/199))
- Upgrade to rdfhdt to 3.0.10 ([issue #215](https://github.com/RMLio/rmlmapper-java/issues/215))
- Improve docs on deduplication ([issue #214](https://github.com/RMLio/rmlmapper-java/issues/214))
- Handle plain old Java Arrays as well for FnO return values
- Update dependency to `idlab-functions-java`, fixing [issue #218](https://github.com/RMLio/rmlmapper-java/issues/218) 

### Changed
- Dockerfile: switched to Eclipse OpenJDK Docker base image because OpenJDK is deprecated
- CI: dropped GitHub release automation
- CI: verify if token is valid

### Improvements
- Heavily increased performance of RMLMapper through several optimizations

## [6.2.1] - 2023-07-06

### Fixed
- Updated idlab-functions-java to v1.0.0
- CSVW 1025 testcase now reads correct logical source

## [6.2.0] - 2023-06-22

### Fixed
- Updated function-agent-java to v1.1.0
- Updated grel-functions-java to v0.9.1
- Updated idlab-functions-java to v0.3.1
- Updated junit to 5.9.1
- Updated testcontainers to 1.17.6
- Updated logback to 1.4.5
- Updated Saxon-HE to 11.4
- Updated postgresql to 42.5.1
- Detect non existing files early ([issue 223](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/233))
- Subject of LDES event stream has wrong IRI (internal GitLab issue [#253](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/253))
- MS SQLServer JDBC driver was only for tests on classpath (internal GitLab issue [#255](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/255))
- Normalising Double numbers from RDBs went wrong (internal GitLab issue [#256](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/256), GitHub issue [#206](https://github.com/RMLio/rmlmapper-java/issues/206))

### Changed
- `tree:view` and `ldes:retentionPolicy` are omitted when generating LDES metadata.

### Added
- A new Maven profile `no-buildnumber` disables using and updating `buildNumber.properties`.
- Mocked DBpedia Spotlight service ([issue #250](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/250))
- Update [dynamic-loading](https://github.com/RMLio/rmlmapper-java#dynamic-loading) in README.md

## [6.1.3] - 2022-12-20

### Fixed
- Reraise execeptions from executor, so that CLI exits with non-zero exit code in the event of errors in execution ([issue 194](https://github.com/RMLio/rmlmapper-java/issues/194))

## [6.1.2] - 2022-11-22

## [6.1.1] - 2022-11-21

## [6.1.0] - 2022-11-21

### Changed
- Require Java 11+
- Port all tests to Junit 5
- Database tests are executed with a fresh instance of the required database in a Docker container
- Update of function libraries IDLab functions, GREL functions, Function Agent
- Removed need for (deprecated) SecurityManager

### Fixed
- Dropped dependency on Guava
- Dropped explicit dependency on Jetty
- Dropped dependency on mariaDB4j
- `RDF4JStore` had wrong regex to check for Literals when they have a language tag or data type.
- Updated RDF4J to 4.2.1
- Updated Jena to 4.6.1
- Updated indirect dependencies to Xerces 2.12.2
- Updated mysql-connector-java to 8.0.31
- Updated postgresql to 42.5.0
- Updated mssql-jdbc to 11.2.1.jre11
- Updated com.fasterxml.jackson.core.* dependencies to 2.14.0
- Updated jsoup to 1.15.3
- Updated opencsv to 5.7.1
- Updated poi-ooxml to 5.2.3
- Updated testcontainers to 1.17.5
- Updated mybatis to 3.5.11
- Updated ojdbc8 21.6.0.0.1 to ojdbc11 21.7.0.0
- Force Wiremock to use commons-fileupload 1.4
- Main: Output paths can again be fully relative
- Dropped dependency on com.spotify.docker-client ([issue 231](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/231))
- Running multiple pipelines should no longer interfere with each other ([issue 245](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/245))
- Fixed TriplesMaps detection of MappingConformer [issue 251](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/251)
- `NamedNodeGenerator` now checks if the given IRI is valid ([issue 249](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/249))

### Added
- pom.xml: Added Testcontainers library dependencies for databases we test on
- pom.xml: Added JUnit5 dependencies
- Check for changelog changes in a separate lint stage during CI. 

## [6.0.0] - 2022-07-04

### Changed
- Run all tests in parallel on CI infrastructure
- Upgrade Function Agent to v0.1.0

### Fixed
- Upgrade postgresql JDBC driver to 42.3.3
- Rename `logback.xml` to `logback-test.xml` in test resources (see [issue 240](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/240))
- If exception occurs while mapping data, already processed data gets written to output file
- Properly close resources such as input streams
- Github release notes are now properly extracted
- Discard UTF Byte-Order-Marks (BOM) (see [issue 171](https://github.com/RMLio/rmlmapper-java/issues/171))
- Dropped lib directory for functions as we now use a separate FnO Function Agent

### Added
- IDLabFunctions: added functions lookup and lookupWithDelimiter
- IDLabFunctionsTest: added unit tests for lookup functions
- IDLabFunctionsTest: relocate csv files used by tests for lookup function
- IDLabFunctions: silence stack trace
- CSVW: filter out rows with a comment prefix
- Output file path now gets checked before the mapping
- .gitignore: ignore vscode files
- pom.xml: add Saxon-HE v11.3 dependency
- XMLRecord: adjusted to use Saxon
- XMLRecordFactory: adjusted to use Saxon
- SaxNamespaceResolver: added class for resolving namespaces in Saxon. This fixes (#154)[https://github.com/RMLio/rmlmapper-java/issues/154].
- Added unit tests for XPath 2/3 expressions and functions
- Loggers across the codebase have been updated to use parameterized strings
- Add support for WoT OAuth2 Security Scheme (see [issue 212](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/212))
- Integration of independent function handler

## [5.0.0] - 2022-03-11
- TestCore: enable debug logs when VERBOSE env variable is set (see [issue 230](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/230))
- Switched to fork of ODFtoolkit (see [issue 237](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/237))

### Added

- Add support for LDES Logical Target
- Add support for generating unique reproducible IRIs for LDES

### Changed

- Write LDES state to disk when mapping execution is complete.
- Removed deprecated execute() and executeWithFunction() methods, and refactored the executeV5() executeWithFunctionV5() method to execute() and executeWithFunction().

### Fixed

- Clarified Readme for quick start
- No cartesian product when referring to the same logical source (see [issue 28](https://github.com/RMLio/rmlmapper-java/issues/28)])
  - If you still want the cartesian product, update your mappings to refer to different logical sources with the same contents
- Upgraded jetty-server and jetty-security to 9.4.44.v20210927
- Upgraded wiremock-jre8 to 2.32.0
- Upgraded ch.qos.logback to 1.2.10
- Upgraded commons-cli to 1.5.0
- Upgraded com.jayway.jsonpath to 2.7.0
- Upgraded ch.vorburger.mariaDB4j to 2.5.3
- Upgraded com.microsoft.sqlserver to 10.2.0.jre8
- Upgraded com.fasterxml.jackson.core to 2.13.1
- Upgraded org.jsoup to 1.14.3
- Upgraded org.apache.poi to 5.0.0
- Resources: functions_grel: use xsd:integer (see [issue 234](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/234))
- Print error if referenceformulation is unsupported

### Changed

- Upgrade postgresql JDBC driver to 42.3.2 (see [issue 146](https://github.com/RMLio/rmlmapper-java/issues/146))

## [4.15.0] - 2022-02-01

### Fixed
- Bump buildnumber during release (see [issue 227](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/227))
- Always include xml: prefix (see [issue 144](https://github.com/RMLio/rmlmapper-java/issues/144))
- `normalizeDate` function uses wrong pattern (see [issue 228](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/228))
- CI: only publish `-all.jar` releases on Github (see [issue 145](https://github.com/RMLio/rmlmapper-java/issues/145))
- CI: make use of templates

### Changed
- CSVRecordFactory: Replace Apache CSV library with OpenCSV to be able to differentiate between empty string and null (see [issue 140](https://github.com/RMLio/rmlmapper-java/issues/140))
- CI: use Gitlab's own mirror feature instead of mirroring in a CI job.
- CI: enforce CHANGELOG updates.
- Added functions to normalize dates and datetimes.

### Added
- JSONRecordFactory: support JSONL files (see [issue 221](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/221)).

## [4.14.3] - 2022-01-13

### Fixed
- Github CLI needs to specify a repo to create a release.

## [4.14.2] - 2022-01-13

### Fixed
- Maven Central automatic builds fixed.
- Upstream R2RML test cases.
- Correctly install Github CLI in CI.

## [4.14.1] - 2022-01-13

### Fixed
- Mirror development branch to Github when releasing new versions.
- Github Release stage works now properly.
- Build newer version during deployment.

## [4.14.0] - 2022-01-13

### Added
- Mapper_Postgres_CSV_Test: Add test RMLTC1027 to check whether NULLs are ignored in PostgreSQL (see [issue 159](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/159))
- IDLabFunctions: add isNull function (see [issue 219](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/219)).
- Replaced PowerMock with WireMock (see [issue 45](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/45))
- FunctionModel: Change the way Lists are processed to take into account generic types
- CSVW: Add support for csvw:null (see [issue 217](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/217))
- FnO: improve error messages for incomplete descriptions (see [issue 222](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/222))
- CSVW: Ignore nulls instead of removing (see [issue 224](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/224))

### Fixed
- Update RELEASE.md with up-to-date notes for releasing new versions (see [issue 218](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/218)).
- RELEASE: Fixed Maven Central deployment instructions (see [issue 223](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/223))
- AccessFactory: Do not clean SPARQL queries by default (see [issue 214](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/214))

## [4.13.0] - 2021-11-22

### Fixed

- update dependencies
- cleaned up code
- fixed undefined prefixes
- CI: fix MS SQL Server docker image name
- pom: mark Jena, Docker, Jetty and Fuseki as test dependencies (see [issue 127](https://github.com/RMLio/rmlmapper-java/issues/127))
- XMLRecord: fallback to STRING if NODESET fails (see [issue 128](https://github.com/RMLio/rmlmapper-java/issues/128))
- Tests: Updated RML test-cases 0002i and 0003a to no longer expect an error. (see [issue 130](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/130))
- Tests: Added test case 0002k where no sqlVersion is present, as its presence is not mandatory.
- README: mention [RML questions repository](https://github.com/kg-construct/rml-questions) (see [issue 216](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/216))
- XML parsing: allow parsing of fully namespaced xml by injecting xml source's namespaces in the XPath compiler (see [issue 134](https://github.com/RMLio/rmlmapper-java/pull/134))
- R2RML: No longer add sqlVersion 2008, as its presence is not mandatory. 
- CSVRecordFactory: allow empty column names (see [issue 124](https://github.com/RMLio/rmlmapper-java/issues/124))

### Added

- Main: add piping support of mapping rules (see [issue 124](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/124))
- Logging: Log loaded functions when in verbose mode (see [issue 136](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/136))
- Mapper_MySQL_R2RML_Test: Add information regarding non-compliance of MySQL for certain tests cases (see [issue 189](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/189))
- Tests: Add test RMLTC1027 to check whether NULLs are ignored properly for various data sources (see [issue 159](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/159))

## [4.12.0] - 2021-08-02

### Fixed
- R2RML: floating point differences between RDBs (see [issue 203](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/203))
- R2RML: do not percent-encode binary data (see [issue 184](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/184))
- R2RML: handle binary data differences between RDBs (see [issue 200](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/200))
- Create an empty output file for empty JSON arrays as well (see [issue 98](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/98))
- Always output UTF-8 encoded RDF (see [issue 119](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/119))
- Refactor RDB records for better NULL and empty string handling (see [issue 131](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/131))
- Align R2RML test cases with [kg-construct/r2rml-test-cases](https://github.com/kg-construct/r2rml-test-cases-support/pull/16) (see [issue 211](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/211))

### Added
- FunctionLoader: throw error on missing function parameters (see [issue 125](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/125))
- HTMLRecordFactory: add CSS3 selector support (see [issue 52](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/52))
- CSVRecordFactory: add spreadsheet support (see [issue 42](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/42))
- IDLabFunctions: add slugify to default idlab-fn functions (see [issue 209](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/209))
- RDBAccess: add XML support for PostgreSQL RDB (see [issue 51](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/51))
- AccessFactory: add WoT BearerSecurity scheme (see [issue 195](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/195))
- README: add clarification on where to find standalone release jars (see [issue 174](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/174))

## [4.11.0] - 2021-07-05

### Added
- R2RML test cases (see [issue 192](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/192))
- DCAT support

### Fixed
- R2RML support: handle booleans correctly (see [issue 187](https://gitlab.ilabt.imec.be/rml/proc/rmlmapper-java/-/issues/187))
- W3C Formats Namespace: use http instead of https

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

[7.1.2]: https://github.com/RMLio/rmlmapper-java/compare/v7.1.1...v7.1.2
[7.1.1]: https://github.com/RMLio/rmlmapper-java/compare/v7.1.0...v7.1.1
[7.1.0]: https://github.com/RMLio/rmlmapper-java/compare/v7.0.0...v7.1.0
[7.0.0]: https://github.com/RMLio/rmlmapper-java/compare/v6.5.1...v7.0.0
[6.5.1]: https://github.com/RMLio/rmlmapper-java/compare/v6.5.0...v6.5.1
[6.5.0]: https://github.com/RMLio/rmlmapper-java/compare/v6.3.0...v6.5.0
[6.3.0]: https://github.com/RMLio/rmlmapper-java/compare/v6.2.2...v6.3.0
[6.2.2]: https://github.com/RMLio/rmlmapper-java/compare/v6.2.1...v6.2.2
[6.2.1]: https://github.com/RMLio/rmlmapper-java/compare/v6.2.0...v6.2.1
[6.2.0]: https://github.com/RMLio/rmlmapper-java/compare/v6.1.3...v6.2.0
[6.1.3]: https://github.com/RMLio/rmlmapper-java/compare/v6.1.2...v6.1.3
[6.1.2]: https://github.com/RMLio/rmlmapper-java/compare/v6.1.1...v6.1.2
[6.1.1]: https://github.com/RMLio/rmlmapper-java/compare/v6.1.0...v6.1.1
[6.1.0]: https://github.com/RMLio/rmlmapper-java/compare/v6.0.0...v6.1.0
[6.0.0]: https://github.com/RMLio/rmlmapper-java/compare/v5.0.0...v6.0.0
[5.0.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.15.0...v5.0.0
[4.15.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.14.3...v4.15.0
[4.14.3]: https://github.com/RMLio/rmlmapper-java/compare/v4.14.2...v4.14.3
[4.14.2]: https://github.com/RMLio/rmlmapper-java/compare/v4.14.1...v4.14.2
[4.14.1]: https://github.com/RMLio/rmlmapper-java/compare/v4.14.0...v4.14.1
[4.14.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.13.0...v4.14.0
[4.13.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.12.0...v4.13.0
[4.12.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.11.0...v4.12.0
[4.11.0]: https://github.com/RMLio/rmlmapper-java/compare/v4.10.1...v4.11.0
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

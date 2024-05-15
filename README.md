# RMLMapper <!-- omit in toc -->

[![Maven Central](https://img.shields.io/maven-central/v/be.ugent.rml/rmlmapper.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22be.ugent.rml%22%20AND%20a:%22rmlmapper%22)

The RMLMapper executes RML rules to generate Linked Data.
It is a Java library, which is available via the command line ([API docs online](https://javadoc.io/doc/be.ugent.rml/rmlmapper)).
The RMLMapper loads all data in memory, so be aware when working with big datasets.

Want to get started quickly? Check out [Releases](#releases) on where to find the latest CLI build as a jar,
and see [Usage](#cli) on how to use the commandline interface!

## Table of contents <!-- omit in toc -->

- [Features](#features)
  - [Supported](#supported)
  - [Future](#future)
- [Releases](#releases)
- [Build](#build)
- [Usage](#usage)
  - [CLI](#cli)
  - [Library](#library)
  - [Docker](#docker)
  - [Including functions](#including-functions)
  - [Generating metadata](#generating-metadata)
- [Testing](#testing)
  - [RDBs](#rdbs)
- [Dependencies](#dependencies)
- [Commercial Support](#commercial-support)
- [Remarks](#remarks)
  - [Typed spreadsheet files](#typed-spreadsheet-files)
  - [XML file parsing performance](#xml-file-parsing-performance)
  - [Language tag support](#language-tag-support)
  - [Duplicate removal and serialization format](#duplicate-removal-and-serialization-format)
  - [I have a question! Where can I get help?](#i-have-a-question-where-can-i-get-help)
- [Documentation](#documentation)
  - [UML Diagrams](#uml-diagrams)

## Features

### Supported
- local data sources:
  - Excel (.xlsx)
  - LibreOffice (.ods)
  - CSV files (including CSVW)
  - JSON files (JSONPath)
  - XML files (XPath)
- remote data sources:
  - relational databases (MySQL, PostgreSQL, Oracle, and SQLServer)
  - Web APIs with W3C Web of Things
  - SPARQL endpoints
  - files via HTTP urls (via GET)
    - CSV files
    - JSON files (JSONPath (`@` can be used to select the current object.))
    - XML files (XPath)
- functions (most cases)
  - For examples on how to use functions within RML mapping documents, you can have a look at the [RML+FnO test cases](https://github.com/RMLio/rml-fno-test-cases)
- configuration file
- metadata generation
- output formats: nquads (default), turtle, trig, trix, jsonld, hdt
- join conditions
- targets:
  - local file
  - VoID dataset
  - SPARQL endpoint with SPARQL UPDATE

### Future
- functions (all cases)
- conditions (all cases)
- data sources:
  - NoSQL databases
  - TPF servers

## Releases

The standalone jar file (that has a [commandline interface](#cli)) for every release can be found on the release's page on GitHub.
You can find the latest release [here](https://github.com/RMLio/rmlmapper-java/releases/latest).
This is the recommended way to get started with RMLMapper.
Do you want to build from source yourself? Check [Build](#build).

## Build
The RMLMapper is built using Maven.
As it is also tested against Oracle (check [here](#accessing-oracle-database) for details),
it needs a specific set-up to run all tests.
That's why we recommend to build without testing: `mvn install -DskipTests=true`.
If you want, you can install with tests, and just skip the Oracle tests: `mvn test -Dtest=!Mapper_OracleDB_Test`.

A standalone jar can be found in `/target`.

Two jars are found in `/target`: a slim jar without bundled dependencies, and a standalone jar (suffixed with `-all.jar`) with all dependencies bundled.

Building with profile `no-buildnumber` disables using and updating `buildNumber.properties` (and uses `0` as build number), e.g.:
```
mvn clean package -P no-buildnumber
```
outputs for example `target/rmlmapper-<version>-r0.jar`

## Usage

### CLI
The following options are most common.

- `-m, --mapping <arg>`: one or more mapping file paths and/or strings (multiple values are concatenated).
- `-o, --output <arg>`: path to output file
- `-s,--serialization <arg>`: serialization format (nquads (default), trig, trix, jsonld, hdt)

All options can be found when executing `java -jar rmlmapper.jar --help`,
that output is found below.

```
usage: java -jar mapper.jar <options>
options:
 -b,--base-iri <arg>                 Base IRI used to expand relative IRIs
                                     in generated terms in the output.
 -c,--configfile <arg>               path to configuration file
 -d,--duplicates                     remove duplicates in the HDT,
                                     N-Triples, or N-Quads output
    --disable-automatic-eof-marker   Setting this option assumes input
                                     data has a kind of End-of-File
                                     marker. Don't use unless you're
                                     absolutely sure what you're doing!
 -dsn,--r2rml-jdbcDSN <arg>          DSN of the database when using R2RML
                                     rules
 -e,--metadatafile <arg>             path to output metadata file
 -f,--functionfile <arg>             one or more function file paths
                                     (dynamic functions with relative
                                     paths are found relative to the cwd)
 -h,--help                           show help info
 -l,--metadataDetailLevel <arg>      generate metadata on given detail
                                     level (dataset - triple - term)
 -m,--mappingfile <arg>              one or more mapping file paths and/or
                                     strings (multiple values are
                                     concatenated). r2rml is converted to
                                     rml if needed using the r2rml
                                     arguments.RDF Format is determined
                                     based on extension.
 -o,--outputfile <arg>               path to output file (default: stdout)
 -p,--r2rml-password <arg>           password of the database when using
                                     R2RML rules
 -psd,--privatesecuritydata <arg>    one or more private security files
                                     containing all private security
                                     information such as usernames,
                                     passwords, certificates, etc.
 -s,--serialization <arg>            serialization format (nquads
                                     (default), turtle, trig, trix,
                                     jsonld, hdt)
    --strict                         Enable strict mode. In strict mode,
                                     the mapper will fail on invalid IRIs
                                     instead of skipping them.
 -t,--triplesmaps <arg>              IRIs of the triplesmaps that should
                                     be executed in order, split by ','
                                     (default is all triplesmaps)
 -u,--r2rml-username <arg>           username of the database when using
                                     R2RML rules
 -v,--verbose                        show more details in debugging output                                                      
```

#### Accessing Web APIs with authentication

The [W3C Web of Things Security Ontology](https://www.w3.org/2019/wot/security)
is used to describe how Web APIs authentication should be performed 
but does not include the necessary credentials to access the Web API.
These credentials can be supplied using the `-psd <PATH>` CLI argument.
The `PATH` argument must point to one or more private security files
which contain the necessary credentials to access the Web API.

An example can be found in the test cases 
[src/test/resources/web-of-things](src/test/resources/web-of-things).

### Library

An example of how you can use the RMLMapper as an external library can be found
at [./src/test/java/be/ugent/rml/readme/ReadmeTest.java](https://github.com/RMLio/rmlmapper-java/blob/master/src/test/java/be/ugent/rml/readme/ReadmeTest.java)

### Docker

#### Dockerhub

We publish our Docker images automatically on Dockerhub for every release.
You can find our images here: [rmlio/rmlmapper-java](https://hub.docker.com/r/rmlio/rmlmapper-java).

#### Build image

You can use Docker to run the RMLMapper by following these steps:

- Build the Docker image: `docker build -t rmlmapper .`.
- Run a Docker container: `docker run --rm -v $(pwd):/data rmlmapper -m mapping.ttl`.

The same parameters are available as via the CLI.
The RMLMapper is executed in the `/data` folder in the Docker container.

### Including functions

There are three ways to include (new) functions within the RML Mapper
  * dynamic loading: you add links to java files or jar files, and those files are loaded dynamically at runtime
  * preloading: you register functionality via code, and you need to rebuild the mapper to use that functionality
  * add as dependency

Registration of functions is done using a Turtle file, which you can find in `src/main/resources/functions.ttl`



#### Dynamic loading

Create a Turtle file that describe the functions that need to be included and add the jar which contains those functions.

> Note: the java or jar-files are found relative to the cwd.
You can change the functions.ttl path (or use multiple functions.ttl paths) using a commandline-option (`-f`).

For example the snippets below dynamically link an fno:Function to a library, provided by a jar-file (`CustomFunctions.jar`). The example links a function that parses the latitude (`50.2`) out of the following string `"POINT (50.2 5.3)"`.

 `functions.ttl` contains the description of the function in Turtle:
```turtle
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix doap:    <http://usefulinc.com/ns/doap#> .
@prefix fno:     <https://w3id.org/function/ontology#> .
@prefix fnoi:    <https://w3id.org/function/vocabulary/implementation#> .
@prefix fnom:    <https://w3id.org/function/vocabulary/mapping#> .
@prefix grel:    <http://users.ugent.be/~bjdmeest/function/grel.ttl#> .
@prefix grelm:   <http://fno.io/grel/rmlmapping#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .

grel:parsePointLat a fno:Function ;
  fno:name "parsePointLat" ;
  rdfs:label "parsePointLat" ;
  dcterms:description "Parse the latitude from a point." ;
  fno:expects ( grel:valueParam ) ;
  fno:returns ( grel:stringOut ) .

grelm:javaString
    a                  fnoi:JavaClass ;
    doap:download-page "CustomFunctions.jar" ;
    fnoi:class-name    "CustomFunctions" .

grelm:parsePointLat
    a                    fno:Mapping ;
    fno:function         grel:parsePointLat ;
    fno:implementation   grelm:javaString ;
    fno:methodMapping    [ a                fnom:Function ;
                           fnom:method-name "parsePointLat" ] .
```
The accompanying java file `CustomFunctions.java`:
```java
public class CustomFunctions {
  public static String parsePointLat(String s) {
    return s.replace("POINT ", "").replace('(', ' ').replace(')', ' ').trim().split("\\s+")[0];
  }
}
```
To dynamically include the custom function, compile the java-file and include `functions.ttl` with the `-f` option:
```bash
javac CustomFunctions.java && jar cvf CustomFunctions.jar CustomFunctions.class
java -jar mapper.jar -f functions.ttl <other options>
```

#### Preloading

This overrides the dynamic loading.
An example of how you can use Preload a custom function can be found
at [./src/test/java/be/ugent/rml/readme/ReadmeFunctionTest.java](https://github.com/RMLio/rmlmapper-java/blob/master/src/test/java/be/ugent/rml/readme/ReadmeFunctionTest.java)

#### Adding as dependency

This is most interesting if you use RMLMapper as a library in your own project.
Just add the dependency to the function library you want to use in your project.

You can also add a function library as a Maven dependency in `pom.xml` of RMLMapper.
You'll have to rebuild RMLMapper to use it.

### Generating metadata

Conform to how it is described in the scientific paper [1],
the RMLMapper allows to automatically generate [PROV-O](https://www.w3.org/TR/prov-o/) metadata.
Specifically, you need the CLI arguments below.
You can specify in which output file the metadata should be stored,
and up to which level metadata should be stored (dataset, triple, or term level metadata).

```
 -e,--metadatafile <arg>          path to output metadata file
 -l,--metadataDetailLevel <arg>   generate metadata on given detail level
                                  (dataset - triple - term)
```

## Testing

### Command line
Run the tests via `test.sh`. 

### IntelliJ
Right-click `src/test/java` directory and select "Run 'All tests'". 

#### Derived tests
Some tests (Excel, ODS) are derived from other tests (CSV) using a script (`./generate_spreadsheet_test_cases.sh`)

### RDBs
Make sure you have [Docker](https://www.docker.com) running. On Unix, others read-write permission (006) is required on `/var/run/docker.sock` in order to run the tests.
The tests will fail otherwise, as Testcontainers can't spin up the container. 

#### Problems
* A problem with Docker (can't start the container) causes the SQLServer tests to fail locally. These tests will always succeed locally.
* A problem with Docker (can't start the container) causes the PostgreSQL tests to fail locally on Windows 7 machines.

## Dependencies

|                   Dependency                   | License                                                            |
|:----------------------------------------------:|--------------------------------------------------------------------|
|         ch.qos.logback logback-classic         | Eclipse Public License 1.0 & GNU Lesser General Public License 2.1 |
|      com.github.fnoio function-agent-java      | MIT                                                                |
|      com.github.fnoio grel-functions-java      | MIT                                                                |
|     com.github.fnoio idlab-functions-java      | MIT                                                                |
|           com.github.rdfhdt hdt-java           | GNU Lesser General Public License v3.0                             |
|      com.github.tomakehurst:wiremock-jre8      | Apache License 2.0                                                 |
|       com.microsoft.sqlserver mssql-jdbc       | MIT                                                                |
|         com.mysql mysql-connector-java         | GNU General Public License v2.0                                    |
|        com.oracle.database.jdbc:ojdbc11        | Oracle Free Use Terms and Conditions                               |
|             net.minidev json-smart             | Apache License 2.0                                                 |
|          org.apache.jena fuseki-main           | Apache License 2.0                                                 |
|         org.eclipse.rdf4j rdf4j-client         | Eclipse Distribution License v1.0                                  |
|      org.junit.jupiter junit-jupiter-api       | Eclipse Public License v2.0                                        |
|     org.junit.jupiter junit-jupiter-engine     | Eclipse Public License v2.0                                        |
|     org.junit.jupiter junit-jupiter-params     | Eclipse Public License v2.0                                        |
|     org.junit.vintage junit-vintage-engine     | Eclipse Public License v2.0                                        |
|           org.postgresql postgresql            | BSD                                                                |
|            org.testcontainers jdbc             | MIT                                                                |
|        org.testcontainers junit-jupiter        | MIT                                                                |
|         org.testcontainers mssqlserver         | MIT                                                                |
|            org.testcontainers mysql            | MIT                                                                |
|          org.testcontainers oracle-xe          | MIT                                                                |
|         org.testcontainers postgresql          | MIT                                                                |

## Commercial Support

Do you need...

-   training?
-   specific features?
-   different integrations?
-   bugfixes, on _your_ timeline?
-   custom code, built by experts?
-   commercial support and licensing?

You're welcome to [contact us](mailto:info@rml.io) regarding
on-premise, enterprise, and internal installations, integrations, and deployments.

We have commercial support available.

We also offer consulting for all-things-RML.

## Remarks

### Typed spreadsheet files
All spreadsheet files are as of yet regarded as plain CSV files. No type information like Currency, Date... is used.

### XML file parsing performance

The RMLMapper's XML parsing implementation (`javax.xml.parsers`) has been chosen to support full XPath.
This implementation causes a large memory consumption (up to ten times larger than the original XML file size).
However, the RMLMapper can be easily adapted to use a different XML parsing implementation that might be better suited for a specific use case.

### Language tag support

The processor checks whether correct language tags are not, using a regular expression.
The regex has no support for languages of length 5-8, but this currently only applies to 'qaa..qtz'.

### Duplicate removal and serialization format

Performance depends on the serialization format (`--serialization <format>`)
and if duplicate removal is enabled (`--duplicates`).
Experimenting with various configurations may lead to better performance for 
your use case.

### I have a question! Where can I get help?

Do you have any question related to writing RML mapping rules, 
the RML specification, etc., feel free to ask them 
here: https://github.com/kg-construct/rml-questions !
If you have found a bug or need a feature for the RMLMapper itself, 
you can make an issue in this repository.

## Documentation
Generate static files at /docs/apidocs with:
```
mvn javadoc:javadoc
```

### UML Diagrams

#### Architecture UML Diagram
##### How to generate with IntelliJ IDEA
(Requires Ultimate edition)

* Right click on package: "be.ugent.rml"
* Diagrams > Show Diagram > Java Class Diagrams
* Choose what properties of the classes you want to show in the upper left corner
* Export to file > .png  | Save diagram > .uml

#### Sequence Diagram
##### Edit on [draw.io](https://www.draw.io)
* Go to [draw.io](https://www.draw.io)
* Click on 'Open Existing Diagram' and choose the .html file

[1]: A. Dimou, T. De Nies, R. Verborgh, E. Mannens, P. Mechant, and R. Van de Walle, “Automated metadata generation for linked data generation and publishing workflows,” in Proceedings of the 9th Workshop on Linked Data on the Web, Montreal, Canada, 2016, pp. 1–10.
[PDF](http://events.linkeddata.org/ldow2016/papers/LDOW2016_paper_04.pdf)



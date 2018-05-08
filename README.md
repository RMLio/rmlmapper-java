# RMLMapper

The RMLMapper execute RML rules to generate Linked Data.
It is a Java library, which is available via the command line. 
The RMLMapper loads all data in memory, so be aware when working with big datasets.

## Features

### Supported
- local data sources:
 - CSV
 - JSON (JSONPath)
 - XML (XPath)
- functions (most cases)
- output formats: ntriples and nquads

### Future
- functions (all cases)
- conditions
- data sources:
 - databases
 - web APIs
 - remote data sources: CSV, JSON, XML
- output formats: turtle, trig

## Build
The RMLMaper is build using Maven: `mvn install`.
A standalone jar can be found in `/target`.

## Usage

### CLI
The following options are available.

- `-m, --mapping <arg>`: path to mapping document
- `-o, --output <arg>`: path to output file
- `-d, --duplicates`: remove duplicates in the output
- `-v, --verbose`: show more details
- `-h, --help`: show help
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

### Library

An example of how you can use the RMLMapper as an external library can be found below.

```
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.QuadStore;

boolean removeDuplicates = false; //set to true if you want to remove duplicates triples/quads from the output
String cwd = "/home/rml"; //path to default directory for local files
String mappingFile = "/home/rml/mapping.rml.ttl" //path to the mapping file that needs to be executed
List<String> triplesMaps = new ArrayList<>(); //list of triplesmaps to execute. When this list is empty all triplesmaps in the mapping file are executed

InputStream mappingStream = new FileInputStream(mappingFile);
Model model = Rio.parse(mappingStream, "", RDFFormat.TURTLE);
RDF4JStore rmlStore = new RDF4JStore(model);

Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(cwd, rmlStore)));
QuadStore result = executor.execute(triplesMaps, removeDuplicates);
```

### Including functions

There are two ways to include (new) functions within the RML Mapper
  * dynamic loading: you add links to java files or jar files, and those files are loaded dynamically at runtime
  * preloading: you register functionality via code, and you need to rebuild the mapper to use that functionality
  
Registration of functions is done using a Turtle file, which you can find in `src/main/resources/functions.ttl`

The snippet below for example links an fno:function to a library, provided by a jar-file (`GrelFunctions.jar`).

```
grel:toUpperCase a fno:Function ;
  fno:name "to Uppercase" ;
  rdfs:label "to Uppercase" ;
  dcterms:description "Returns the input with all letters in upper case." ;
  fno:expects ( grel:valueParam ) ;
  fno:returns ( grel:stringOut ) ;
  lib:providedBy [
    lib:localLibrary "GrelFunctions.jar";
    lib:class "GrelFunctions";
    lib:method "toUppercase"
  ].
```

#### Dynamic loading

Just put the java or jar-file in the resources folder,
at the root folder of the jar-location,
or the parent folder of the jar-location,
it will be found dynamically.

> Note: the java or jar-files are found relative to the loaded functions.ttl.
You can change the functions.ttl path using a commandline-option (`-f`).

#### Preloading

This overrides the dynamic loading.
See the snippet below for an example of how to do it.

```
import be.ugent.rml.functions.lib.GrelProcessor;

String mapPath = "path/to/mapping/file";
String outPath = "path/to/where/the/output/triples/should/be/written";

Map<String, Class> libraryMap = new HashMap<>();
libraryMap.put("GrelFunctions.jar", GrelProcessor.class);
FunctionLoader functionLoader = new FunctionLoader(libraryMap);
try {
    Executor executor = this.createExecutor(mapPath, functionLoader);
    doMapping(executor, outPath);
} catch (IOException e) {
    logger.error(e.getMessage(), e);
}
```

### Testing
#### RDBs
Make sure you have [Docker](https://www.docker.com) running.

Set the boolean constant ```LOCAL_TESTING``` in the file 'Mapper_RDBs_Test' to ```true``` for testing locally. 
This causes the creation of the required Docker containers and adds the right connection string to the mapping files.

Set the boolean constant ```LOCAL_TESTING``` in the file 'Mapper_RDBs_Test' to ```false``` for testing on / pushing to GitLab. 
This makes sure that the containers running on GitLab are used and adds the right connection strings to the mapping files.

##### Problems
* A problem with Docker (can't start the container) causes the SQLServer tests to fail locally.
* A problem with Docker (can't start the container) causes the PostgreSQL tests to fail locally on windows 7 machines.

# Dependencies

|             Dependency             | License                                                            |
|:----------------------------------:|--------------------------------------------------------------------|
| com.spotify docker client          | Apache License 2.0                                                 |
| com.h2database h2                  | Eclipse Public License 1.0 & Mozilla Public License 2.0            |
| com.googlecode.zohhak              | GNU Lesser General Public License v3.0                             |
| com.microsoft.sqlserver mssql-jdbc | MIT                                                                |
| ch.vorbuger.mariaDB4j              | Apache License 2.0                                                 |
| mysql-connector-java               | GNU General Public License v2.0                                    |
| com.google.guava                   | Apache License 2.0                                                 |
| javax.xml.parsers jaxp-api         | Apache License 2.0                                                 |
| com.jayway.jsonpath                | Apache License 2.0                                                 |
| junit                              | Eclipse Public License 1.0                                         |
| org.eclipse.rdf4j rdf4j-runtime    | Eclipse Public License 1.0                                         |
| commons-cli                        | Apache License 2.0                                                 |
| com.opencsv opencsv                | Apache License 2.0                                                 |
| commons-lang                       | Apache License 2.0                                                 |
| ch.qos.logback                     | Eclipse Public License 1.0 & GNU Lesser General Public License 2.1 |

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

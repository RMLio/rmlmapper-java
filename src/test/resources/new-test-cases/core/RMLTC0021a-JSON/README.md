## RMLTC0021a-JSON

**Title**: "Generation of triples referencing object map"

**Description**: "Tests the mapping specification referencing object map with same logical source and join condition"

**Error expected?** No

**Input**
```
{
  "students": [{
    "ID": 10,
    "Name":"Venus Williams",
    "Sport": "Tennis"
  }, {
    "ID": 20,
    "Name":"Serena Williams",
    "Sport": "Tennis"
  }, {
    "ID": 30,
    "Name":"Loena Hendrickx",
    "Sport": "Figure skating"
  }]
}

```

**Mapping**
```
@prefix activity: <http://example.com/activity/> .
@prefix ex: <http://example.com/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/RefObjectMap1> a rml:RefObjectMap;
  rml:joinCondition [
      rml:child "$.Sport";
      rml:parent "$.Sport"
    ];
  rml:parentTriplesMap <http://example.com/base/TriplesMap1> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.students[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "student.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap <http://example.com/base/RefObjectMap1>;
      rml:predicate ex:sameSportAs
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.ID}/{$.Name}"
    ] .

```

**Output**
```
<http://example.com/Student/10/Venus%20Williams> <http://example.com/sameSportAs> <http://example.com/Student/10/Venus%20Williams> . 
<http://example.com/Student/10/Venus%20Williams> <http://example.com/sameSportAs> <http://example.com/Student/20/Serena%20Williams> . 
<http://example.com/Student/20/Serena%20Williams> <http://example.com/sameSportAs> <http://example.com/Student/20/Serena%20Williams> . 
<http://example.com/Student/20/Serena%20Williams> <http://example.com/sameSportAs> <http://example.com/Student/10/Venus%20Williams> . 
<http://example.com/Student/30/Loena%20Hendrickx> <http://example.com/sameSportAs> <http://example.com/Student/30/Loena%20Hendrickx> . 



```


## RMLTC0008b-JSON

**Title**: "Generation of triples referencing object map"

**Description**: "Tests the mapping specification referencing object map without join"

**Error expected?** No

**Input**
```
{
  "students": [{
    "ID": 10,
    "Name":"Venus Williams",
    "Sport": "Tennis"
  }]
}

```

**Mapping**
```
@prefix activity: <http://example.com/activity/> .
@prefix ex: <http://example.com/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rml: <http://w3id.org/rml/> .

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
      rml:predicate ex:Sport
    ], [
      rml:object foaf:Person;
      rml:predicate rdf:type
    ], [
      rml:objectMap [
          rml:reference "$.ID"
        ];
      rml:predicate ex:id
    ], [
      rml:objectMap [
          rml:reference "$.Name"
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.ID}/{$.Name}"
    ] .

<http://example.com/base/RefObjectMap1> a rml:RefObjectMap;
  rml:parentTriplesMap <http://example.com/base/TriplesMap2> .

<http://example.com/base/TriplesMap2> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.students[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "student.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:object activity:Sport;
      rml:predicate rdf:type
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.Sport}"
    ] .

```

**Output**
```
<http://example.com/Student/10/Venus%20Williams> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>  .
<http://example.com/Student/10/Venus%20Williams> <http://xmlns.com/foaf/0.1/name> "Venus Williams" .
<http://example.com/Student/10/Venus%20Williams> <http://example.com/id> "10" . 
<http://example.com/Student/10/Venus%20Williams> <http://example.com/Sport> <http://example.com/Tennis> . 
<http://example.com/Tennis> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/activity/Sport> .



```


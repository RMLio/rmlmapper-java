## RMLTC0008c-JSON

**Title**: "Generation of triples by using multiple predicateMaps within a rml:predicateObjectMap"

**Description**: "Tests the generation of triples by using multiple predicateMaps within a rml:predicateObjectMap"

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
@prefix ex: <http://example.com/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
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
      rml:objectMap [
          rml:reference "$.Name"
        ];
      rml:predicate ex:name, foaf:name
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.ID}/{$.Name}"
    ] .

```

**Output**
```
<http://example.com/Student/10/Venus%20Williams> <http://xmlns.com/foaf/0.1/name> "Venus Williams"  .
<http://example.com/Student/10/Venus%20Williams> <http://example.com/name> "Venus Williams"  .


```


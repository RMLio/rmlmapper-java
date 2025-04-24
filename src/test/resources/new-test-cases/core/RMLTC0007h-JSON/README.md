## RMLTC0007h-JSON

**Title**: "Assigning triples to a non-IRI named graph"

**Description**: "Tests the generation of triples to a non-IRI named graph, which is an error"

**Error expected?** Yes

**Input**
```
{
  "students": [{
    "ID": 10,
    "FirstName":"Venus",
    "LastName":"Williams"
  }]
}

```

**Mapping**
```
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
          rml:reference "$.FirstName"
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:graphMap [
          rml:reference "$.ID";
          rml:termType rml:Literal
        ];
      rml:template "http://example.com/Student/{$.ID}/{$.FirstName}"
    ] .

```


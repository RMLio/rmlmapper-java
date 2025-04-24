## RMLTC0026b-JSON

**Title**: "Generation of triples from arrays with wrong reference"

**Description**: "Tests the generation of triples from array input data structures. Test should fail as reference points to the array and not the values of the array"

**Error expected?** Yes

**Input**
```
{
  "persons": [
    {"fname":"Bob","lname":"Smith","amounts":[30, 40, 50]}
  ]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.persons[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "persons.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap [
          rml:reference "$.amounts"
        ];
      rml:predicate ex:amount
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.fname}/{$.lname}"
    ] .

```


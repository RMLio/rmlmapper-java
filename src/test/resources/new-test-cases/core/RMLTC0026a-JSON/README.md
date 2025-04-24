## RMLTC0026a-JSON

**Title**: "Generation of triples from arrays"

**Description**: "Tests the generation of triples from array input data structures"

**Error expected?** No

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
          rml:reference "$.amounts[*]"
        ];
      rml:predicate ex:amount
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.fname}/{$.lname}"
    ] .

```

**Output**
```
<http://example.com/Student/Bob/Smith> <http://example.com/amount> "30" .
<http://example.com/Student/Bob/Smith> <http://example.com/amount> "40" .
<http://example.com/Student/Bob/Smith> <http://example.com/amount> "50" .

```


## RMLTC0004b-JSON

**Title**: "One column mapping, presence of rml:termType rml:Literal on rml:subjectMap"

**Description**: "Tests: (1) one column mapping (2) the presence of rml:termType rml:Literal on rml:subjectMap, which is invalid"

**Error expected?** Yes

**Input**
```
{
  "students": [{
    "Name":"Venus"
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
          rml:reference "$.Name"
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.Name}";
      rml:termType rml:Literal
    ] .

```


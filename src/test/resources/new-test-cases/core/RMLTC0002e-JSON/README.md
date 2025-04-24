## RMLTC0002e-JSON

**Title**: "Two columns mapping, an undefined rml:path"

**Description**: "Tests the presence of an undefined rml:path"

**Error expected?** Yes

**Input**
```
{
  "students": [{
    "ID": 10,
    "Name":"Venus"
  }]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.students[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "student2.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap [
          rml:reference "$.IDs"
        ];
      rml:predicate ex:id
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.ID}/{$.Name}"
    ] .

```


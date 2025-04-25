## RMLTC0025a-JSON

**Title**: "Generation of triples with constant blank node "

**Description**: "Tests the generation of triples with a constant blank node"

**Error expected?** No

**Input**
```
{
  "students": [{
    "Name":"Venus"
  },
  {
    "Name":"Julio"
  }]
}

```

**Mapping**
```
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rml: <http://w3id.org/rml/> .
@prefix ex: <http://example.com/>.

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
      rml:predicate ex:student
    ];
  rml:subject _:School .

```

**Output**
```
_:School <http://example.com/student> "Julio" .
_:School <http://example.com/student> "Venus" .

```


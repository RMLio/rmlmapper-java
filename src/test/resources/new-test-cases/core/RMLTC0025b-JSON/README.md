## RMLTC0025b-JSON

**Title**: "Usage of constant term maps in combination with explicitly defined term types"

**Description**: "Tests the usage of constant term maps in combination with explicitly defined term types"

**Error expected?** Yes

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
  rml:subjectMap [
      rml:constant "School";
      rml:termType rml:BlankNode
  ] .

```


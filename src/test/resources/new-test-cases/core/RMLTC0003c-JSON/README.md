## RMLTC0003c-JSON

**Title**: "Three columns mapping, by using a rml:template to produce literal"

**Description**: "Tests: (1) three column mapping; and (2) the use of rml:template to produce literal"

**Error expected?** No

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
          rml:template "{$.FirstName} {$.LastName}";
          rml:termType rml:Literal
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student{$.ID}"
    ] .

```

**Output**
```
<http://example.com/Student10> <http://xmlns.com/foaf/0.1/name> "Venus Williams" .

```


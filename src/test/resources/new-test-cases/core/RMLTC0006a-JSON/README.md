## RMLTC0006a-JSON

**Title**: "Use of rml:constant in rml:subjectMap, rml:predicateMap, rml:objectMap and rml:graphMap"

**Description**: "Tests the use of rml:constant in rml:subjectMap, rml:predicateMap, rml:objectMap and rml:graphMap"

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
@prefix ex: <http://example.com/> .
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
          rml:constant "Bad Student"
        ];
      rml:predicateMap [
          rml:constant ex:description
        ]
    ];
  rml:subjectMap [
      rml:constant ex:BadStudent;
      rml:graphMap [
          rml:constant <http://example.com/graph/student>
        ]
    ] .

```

**Output**
```
<http://example.com/BadStudent> <http://example.com/description> "Bad Student" <http://example.com/graph/student> .
```


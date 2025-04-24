## RMLTC0022a-JSON

**Title**: "Generating of triples with constant datatype"

**Description**: "Test triples with a fixed constant datatype"

**Error expected?** No

**Input**
```
{
  "students": [{
    "ID": 10,
    "Name":"Venus",
    "Age": 21
  }]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rml: <http://w3id.org/rml/> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

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
          rml:datatype xsd:string;
          rml:reference "$.Name"
        ];
      rml:predicate foaf:name
    ], [
      rml:objectMap [
          rml:datatype xsd:int;
          rml:reference "$.Age"
        ];
      rml:predicate ex:age
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.Name}"
    ] .

```

**Output**
```
<http://example.com/Venus> <http://xmlns.com/foaf/0.1/name> "Venus"^^<http://www.w3.org/2001/XMLSchema#string> .
<http://example.com/Venus> <http://example.com/age> "21"^^<http://www.w3.org/2001/XMLSchema#int> .

```


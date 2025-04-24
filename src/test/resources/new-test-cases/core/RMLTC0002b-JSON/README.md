## RMLTC0002b-JSON

**Title**: "Two columns mapping, generation of a BlankNode subject by using rml:template and rml:termType"

**Description**: "Tests: (1) two column mapping, no primary key; (2) generation of a BlankNode subject by using rml:template; (3) one column to one property"

**Error expected?** No

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
      rml:template "students{$.ID}";
      rml:termType rml:BlankNode
    ] .

```

**Output**
```
_:students10 <http://xmlns.com/foaf/0.1/name> "Venus" .


```


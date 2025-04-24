## RMLTC0012c-JSON

**Title**: "TriplesMap without subjectMap"

**Description**: "Tests a RML with missing information, TriplesMap without subjectMap."

**Error expected?** Yes

**Input**
```
{
  "persons": [
    {"fname":"Bob","lname":"Smith","amount":30},
    {"fname":"Sue","lname":"Jones","amount":20},
    {"fname":"Bob","lname":"Smith","amount":30}
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
          rml:template "{$.fname} {$.lname}";
          rml:termType rml:Literal
        ];
      rml:predicate foaf:name
    ], [
      rml:objectMap [
          rml:reference "$.amount"
        ];
      rml:predicate ex:amount
    ] .

```


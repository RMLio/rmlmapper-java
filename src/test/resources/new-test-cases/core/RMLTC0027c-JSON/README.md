## RMLTC0027c-JSON

**Title**: "Two triples maps, both with rml:baseIRI defined"

**Description**: "Tests the generation of triples using relative IRIs with base IRIs defined only in triples maps, without base IRI parameter"

**Error expected?** No

**Input**
```
{
  "persons": [
    {"fname":"Bob","lname":"Smith","amount":30},
    {"fname":"Sue","lname":"Jones","amount":20}
  ]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:baseIRI <http://example.com/>;
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
          rml:reference "$.amount"
        ];
      rml:predicate ex:amount
    ];
  rml:subjectMap [
      rml:template "{$.fname}"
    ] .

<http://example.com/base/TriplesMap2> a rml:TriplesMap;
  rml:baseIRI <http://example.com/>;
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
          rml:reference "$.amount"
        ];
      rml:predicate ex:amount
    ];
  rml:subjectMap [
      rml:template "{$.lname}"
    ] .

```

**Output**
```
<http://example.com/Bob> <http://example.com/amount> "30" .
<http://example.com/Jones> <http://example.com/amount> "20" .
<http://example.com/Smith> <http://example.com/amount> "30" .
<http://example.com/Sue> <http://example.com/amount> "20" .

```


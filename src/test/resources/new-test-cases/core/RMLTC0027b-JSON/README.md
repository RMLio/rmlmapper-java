## RMLTC0027b-JSON

**Title**: "Two triples maps, one with rml:baseIRI one one without and generating relative IRIs using baseIRI parameter"

**Description**: "Tests the generation of triples from relative IRI using base IRI parameter"

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
@base <http://example.com/> .

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


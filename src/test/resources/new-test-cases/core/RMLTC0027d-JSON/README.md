## RMLTC0027d-JSON

**Title**: "Two triples maps,both with rml:bseIRI defined and baseIRI parameter defined"

**Description**: "Tests the generation of triples by with base IRIs different than the base IRI parameter"

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
@base <http://example.com/>.

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:baseIRI <http://example2.com/>;
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
  rml:baseIRI <http://example2.com/>;
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
<http://example2.com/Bob> <http://example.com/amount> "30" .
<http://example2.com/Jones> <http://example.com/amount> "20" .
<http://example2.com/Smith> <http://example.com/amount> "30" .
<http://example2.com/Sue> <http://example.com/amount> "20" .

```


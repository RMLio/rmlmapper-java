## RMLTC0001a-JSON

**Title**: "One column mapping, subject URI generation by using rml:template"

**Description**: "Tests: (1) one column mapping; (2) subject URI generation by using rml:template; (3) one column to one property"

**Error expected?** No

**Input**
```
{
  "students": [{
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
  rml:subjectMap <http://example.com/base/#NameSubjectMap> .

<http://example.com/base/#NameSubjectMap> rml:template "http://example.com/{$.Name}" .

```

**Output**
```
<http://example.com/Venus> <http://xmlns.com/foaf/0.1/name> "Venus" .


```


## RMLTC0023e-JSON

**Title**: "Invalid IRI template 5"

**Description**: "Test handling of invalid IRI template"

**Error expected?** Yes

**Input**
```
{
  "students": [{
    "ID": 10,
    "{Name}":"Venus"
  }]
}

```

**Mapping**
```
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
     rml:referenceFormulation rml:JSONPath;
     rml:iterator "$.students[*]";
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "student.json"
        ]
    ];
  rml:subjectMap [
      rml:template "http://example.com/{\\{Name\\}}";
      rml:class foaf:Person;
    ] .


```


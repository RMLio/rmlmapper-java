## RMLTC0007d-JSON

**Title**: "One column mapping, specifying an rml:predicateObjectMap with rdf:type"

**Description**: "Tests subjectmap with an alternative of having rml:class, i.e., by specifying an rml:predicateObjectMap with predicate rdf:type"

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
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
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
      rml:object foaf:Person;
      rml:predicate rdf:type
    ], [
      rml:object ex:Student;
      rml:predicate rdf:type
    ], [
      rml:objectMap [
          rml:reference "$.ID"
        ];
      rml:predicate ex:id
    ], [
      rml:objectMap [
          rml:reference "$.FirstName"
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:template "http://example.com/Student/{$.ID}/{$.FirstName}"
    ] .

```

**Output**
```
<http://example.com/Student/10/Venus> <http://xmlns.com/foaf/0.1/name> "Venus" . 
<http://example.com/Student/10/Venus> <http://example.com/id> "10" .
<http://example.com/Student/10/Venus> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .
<http://example.com/Student/10/Venus> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Student> .


```


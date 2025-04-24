## RMLTC0020a-JSON

**Title**: "Generation of triples by using IRI value in columns"

**Description**: "Test the generation of triples by using IRI value in attributes"

**Error expected?** No

**Input**
```
{
  "students": [
    {"Name": "http://example.com/company/Alice"},
    {"Name": "Bob"},
    {"Name": "Bob/Charles"},
    {"Name": "path/../Danny"},
    {"Name": "Emily Smith"}
  ]
}

```

**Mapping**
```
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
    ];
  rml:subjectMap [
      rml:template "{$.Name}";
      rml:termType rml:IRI
    ] .

```

**Output**
```
<http://example.com/base/http%3A%2F%2Fexample.com%2Fcompany%2FAlice> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .
<http://example.com/base/Bob> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .
<http://example.com/base/Bob%2FCharles> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .
<http://example.com/base/path%2F..%2FDanny> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .
<http://example.com/base/Emily%20Smith> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> .

```


## RMLTC0019b-JSON

**Title**: "Generation of triples by using IRI value in columns, with data error"

**Description**: "Test the generation of triples by using IRI value in attributes, conforming RML mapping with data error (and limited results)"

**Error expected?** No

**Input**
```
{
  "persons": [
    {
      "ID": 10,
      "FirstName": "http://example.com/ns#Jhon",
      "LastName": "Smith"
    },
    {
      "ID": 20,
      "FirstName": "Carlos",
      "LastName": "Mendoza"
    },
    {
      "ID": 30,
      "FirstName": "Juan Daniel",
      "LastName": "Crespo"
    }
  ]
}

```

**Mapping**
```
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
          rml:reference "$.FirstName"
        ];
      rml:predicate foaf:name
    ];
  rml:subjectMap [
      rml:reference "$.FirstName"
    ] .

```

**Output**
```

```


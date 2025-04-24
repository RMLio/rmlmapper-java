## RMLTC0013a-JSON

**Title**: "Null value in JSON file"

**Description**: "Tests if null values in JSON files are handled correctly."

**Error expected?** No

**Input**
```
{
  "persons": [
    {"ID":"1","Name":"Alice","DateOfBirth":null},
    {"ID":"2","Name":"Bob","DateOfBirth":"September, 2010"}
  ]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
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
          rml:reference "$.DateOfBirth"
        ];
      rml:predicate ex:BirthDay
    ];
  rml:subjectMap [
      rml:template "http://example.com/Person/{$.ID}/{$.Name}/{$.DateOfBirth}"
    ] .

```

**Output**
```
<http://example.com/Person/2/Bob/September%2C%202010> <http://example.com/BirthDay> "September, 2010" .

```


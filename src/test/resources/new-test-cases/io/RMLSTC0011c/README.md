## RMLSTC0011c

**Title**: Complex JSON source

**Description**: Tests the generation of triples from complex JSON sources

**Error expected?** No

**Input**
 [http://w3id.org/rml/resources/rml-io/RMLSTC0011c/Friends.json](http://w3id.org/rml/resources/rml-io/RMLSTC0011c/Friends.json)

**Mapping**
```
@prefix rml: <http://w3id.org/rml/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix ex: <http://example.com/> .
@base <http://example.com/rules/> .

<#TriplesMap2> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
    rml:source [ a rml:FilePath;
      rml:root rml:MappingDirectory;
      rml:path "companies.json";
    ];
    rml:referenceFormulation rml:JSONPath;
    rml:iterator "$.companies[*]";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{name}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant ex:employee;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.departments[*].employees[*].name";
    ];
  ].

```

**Output**
```
<http://example.org/InnovateX> <http://example.com/employee> "Liam Brown" .
<http://example.org/InnovateX> <http://example.com/employee> "Olivia Martinez" .
<http://example.org/InnovateX> <http://example.com/employee> "Sophia White" .
<http://example.org/TechCorp> <http://example.com/employee> "Bob Smith" .
<http://example.org/TechCorp> <http://example.com/employee> "Eve Davis" .
<http://example.org/TechCorp> <http://example.com/employee> "Sarah Lee" .

```


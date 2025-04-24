## RMLSTC0011d

**Title**: Complex JSON source

**Description**: Tests the generation of triples from complex JSON sources

**Error expected?** No

**Input**
 [http://w3id.org/rml/resources/rml-io/RMLSTC0011d/Friends.json](http://w3id.org/rml/resources/rml-io/RMLSTC0011d/Friends.json)

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
    rml:iterator "$.companies[*].departments[*]";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{name}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:name;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.manager.name";
    ];
  ].

```

**Output**
```
<http://example.org/Engineering> <http://xmlns.com/foaf/0.1/name> "Alice Johnson" .
<http://example.org/Marketing> <http://xmlns.com/foaf/0.1/name> "John Doe" .
<http://example.org/Research%20%26%20Development> <http://xmlns.com/foaf/0.1/name> "Emma Wilson" .
<http://example.org/Sales> <http://xmlns.com/foaf/0.1/name> "Michael Green" .

```


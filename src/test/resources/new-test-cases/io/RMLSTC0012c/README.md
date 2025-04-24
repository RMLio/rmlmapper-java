## RMLSTC0012c

**Title**: Complex XML source

**Description**: Tests the generation of triples from complex XML sources

**Error expected?** No

**Input**
 [http://w3id.org/rml/resources/rml-io/RMLSTC0012c/Friends.json](http://w3id.org/rml/resources/rml-io/RMLSTC0012c/Friends.json)

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
      rml:path "companies.xml";
    ];
    rml:referenceFormulation rml:XPath;
    rml:iterator "/companies/company/departments/department/employees";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{../../../@id}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant ex:employee;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "employee/name";
    ];
  ].

```

**Output**
```
<http://example.org/25> <http://example.com/employee> "Bob Smith" .
<http://example.org/25> <http://example.com/employee> "Eve Davis" .
<http://example.org/25> <http://example.com/employee> "Sarah Lee" .
<http://example.org/35> <http://example.com/employee> "Liam Brown" .
<http://example.org/35> <http://example.com/employee> "Olivia Martinez" .
<http://example.org/35> <http://example.com/employee> "Sophia White" .

```


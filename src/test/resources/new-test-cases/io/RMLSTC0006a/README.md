## RMLSTC0006a

**Title**: Source with D2RQ access description

**Description**: Test source with D2RQ access description for SQL databases

**Error expected?** No

**Input**
```
id,name,age
0,Monica Geller,33
1,Rachel Green,34
2,Joey Tribbiani,35
3,Chandler Bing,36
4,Ross Geller,37

```

**Mapping**
```
@prefix rml: <http://w3id.org/rml/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@base <http://example.com/rules/> .

<#D2RQSourceAccess> a rml:Source, d2rq:Database;
  d2rq:jdbcDSN "$CONNECTIONDSN";
  d2rq:username "$USERNAME";
  d2rq:password "$PASSWORD"
.

<#TriplesMap> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
    rml:source <#D2RQSourceAccess>;
    rml:referenceFormulation rml:SQL2008Table;
    rml:iterator "Friends";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{id}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:name;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "name";
    ];
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:age;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "age";
    ];
  ];
.

```

**Output**
```
<http://example.org/0> <http://xmlns.com/foaf/0.1/age> "33" .
<http://example.org/0> <http://xmlns.com/foaf/0.1/name> "Monica Geller" .
<http://example.org/1> <http://xmlns.com/foaf/0.1/age> "34" .
<http://example.org/1> <http://xmlns.com/foaf/0.1/name> "Rachel Green" .
<http://example.org/2> <http://xmlns.com/foaf/0.1/age> "35" .
<http://example.org/2> <http://xmlns.com/foaf/0.1/name> "Joey Tribbiani" .
<http://example.org/3> <http://xmlns.com/foaf/0.1/age> "36" .
<http://example.org/3> <http://xmlns.com/foaf/0.1/name> "Chandler Bing" .
<http://example.org/4> <http://xmlns.com/foaf/0.1/age> "37" .
<http://example.org/4> <http://xmlns.com/foaf/0.1/name> "Ross Geller" .

```


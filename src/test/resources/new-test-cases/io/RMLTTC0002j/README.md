## RMLTTC0002j

**Title**: Multiple Targets: Language Map and Object Map

**Description**: Test exporting all triples to a Target in a Language Map and a Target in a Object Map.

**Error expected?** No

**Input**
```
[
  { 
    "id": 0,
    "name": "Monica Geller",
    "age": 33
  },
  { 
    "id": 1,
    "name": "Rachel Green",
    "age": 34
  },
  { 
    "id": 2,
    "name": "Joey Tribbiani",
    "age": 35
  },
  { 
    "id": 3,
    "name": "Chandler Bing",
    "age": 36
  },
  { 
    "id": 4,
    "name": "Ross Geller",
    "age": 37
  }
]

```

**Mapping**
```
@prefix rml: <http://w3id.org/rml/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix formats: <http://www.w3.org/ns/formats/> .
@prefix ex: <http://example.org> .
@base <http://example.com/rules/> .

<#TriplesMap> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
    rml:source [ a rml:FilePath;
      rml:root rml:MappingDirectory;
      rml:path "Friends.json";
    ];
    rml:referenceFormulation rml:JSONPath;
    rml:iterator "$[*]";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{$.id}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:name;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.name";
      rml:languageMap [ a rml:LanguageMap;
        rml:constant "en";
        rml:logicalTarget <#TargetDump2>;
      ];
    ];
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:age;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.age";
      rml:logicalTarget <#TargetDump1>;
    ];
  ];
.

<#TargetDump1> a rml:LogicalTarget;
  rml:target [ a rml:Target, rml:FilePath;
    rml:root rml:CurrentWorkingDirectory;
    rml:path "./dump1.nq";
  ];
  rml:serialization formats:N-Quads;
.

<#TargetDump2> a rml:LogicalTarget;
  rml:target [ a rml:Target, rml:FilePath;
    rml:root rml:CurrentWorkingDirectory;
    rml:path "./dump2.nq";
  ];
  rml:serialization formats:N-Quads;
.

```

**Output 1**
```

```

**Output 2**
```
<http://example.org/0> <http://xmlns.com/foaf/0.1/age> "33" .
<http://example.org/1> <http://xmlns.com/foaf/0.1/age> "34" .
<http://example.org/2> <http://xmlns.com/foaf/0.1/age> "35" .
<http://example.org/3> <http://xmlns.com/foaf/0.1/age> "36" .
<http://example.org/4> <http://xmlns.com/foaf/0.1/age> "37" .

```

**Output 3**
```
<http://example.org/0> <http://xmlns.com/foaf/0.1/name> "Monica Geller"@en .
<http://example.org/1> <http://xmlns.com/foaf/0.1/name> "Rachel Green"@en .
<http://example.org/2> <http://xmlns.com/foaf/0.1/name> "Joey Tribbiani"@en .
<http://example.org/3> <http://xmlns.com/foaf/0.1/name> "Chandler Bing"@en .
<http://example.org/4> <http://xmlns.com/foaf/0.1/name> "Ross Geller"@en .

```


## RMLTC0015a-JSON

**Title**: "Generation of language tags from a table with language information"

**Description**: "Generation of language tags from a table with language information"

**Error expected?** No

**Input**
```
{
  "countries": [
    {"Code": "BO", "Name": "Estado Plurinacional de Bolivia"},
    {"Code": "IE", "Name": "Irlanda"}
  ]
}

```

**Mapping**
```
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.countries[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "country_en.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap [
          rml:language "en";
          rml:reference "$.Name"
        ];
      rml:predicate rdfs:label
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.Code}"
    ] .

<http://example.com/base/TriplesMap2> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.countries[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "country_es.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap [
          rml:language "es";
          rml:reference "$.Name"
        ];
      rml:predicate rdfs:label
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.Code}"
    ] .

```

**Output**
```
<http://example.com/BO> <http://www.w3.org/2000/01/rdf-schema#label> "Bolivia, Plurinational State of"@en .
<http://example.com/BO> <http://www.w3.org/2000/01/rdf-schema#label> "Estado Plurinacional de Bolivia"@es .
<http://example.com/IE> <http://www.w3.org/2000/01/rdf-schema#label> "Ireland"@en .
<http://example.com/IE> <http://www.w3.org/2000/01/rdf-schema#label> "Irlanda"@es .



```


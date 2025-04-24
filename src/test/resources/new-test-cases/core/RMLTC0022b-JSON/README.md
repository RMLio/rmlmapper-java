## RMLTC0022b-JSON

**Title**: "Generating of triples with datatypeMap"

**Description**: "Test triples with a XSD datatype generated from the data"

**Error expected?** No

**Input**
```
[
	{ "FOO": 1, "BAR": "string"},
	{ "FOO": 2, "BAR": "int"}
]

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:referenceFormulation rml:JSONPath;
      rml:iterator "$[*]";
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "data.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:objectMap [
          rml:datatypeMap [
              rml:template "http://www.w3.org/2001/XMLSchema#{$.BAR}"
            ];
          rml:reference "$.FOO"
        ];
      rml:predicate ex:x
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.FOO}"
    ] .


```

**Output**
```
<http://example.com/1> <http://example.com/x> "1"^^<http://www.w3.org/2001/XMLSchema#string> .
<http://example.com/2> <http://example.com/x> "2"^^<http://www.w3.org/2001/XMLSchema#int> .

```


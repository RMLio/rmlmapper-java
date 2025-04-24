## RMLTC0029a-JSON

**Title**: "Generation of the right datatype for a constant in the mapping"

**Description**: "Test the honoring of the datatype specified by the constant term in the mapping"

**Error expected?** No

**Input**
```
[ { "id": "0", "foo": "bar"  } ] 

```

**Mapping**
```
@prefix rml: <http://semweb.mmlab.be/ns/rml#> .
@prefix rr: <http://www.w3.org/ns/r2rml#> .
@prefix ql: <http://semweb.mmlab.be/ns/ql#> .

 [
    a rr:TriplesMap;
    rml:logicalSource [
      rml:source "data.json" ;
      rml:referenceFormulation ql:JSONPath ;
      rml:iterator "$[*]";
    ];
    rr:subjectMap [
      rr:template "https://example.org/instances/{id}";
    ];
    rr:predicateObjectMap [
      rr:predicate <http://example.org/ns/p> ;
      rr:object true ; # datatype is boolean
    ];
  ] .

```

**Output**
```
<https://example.org/instances/0> <http://example.org/ns/p> "true"^^<http://www.w3.org/2001/XMLSchema#boolean> .

```


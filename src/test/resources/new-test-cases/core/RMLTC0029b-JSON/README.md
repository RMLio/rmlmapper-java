## RMLTC0029b-JSON

**Title**: "Generation of all named graphs when rml:defaultGraph is involved"

**Description**: "Test if the default graph is also generated correctly."

**Error expected?** No

**Input**
```
[ { "id": "0", "name": "Alice"  } ] 

```

**Mapping**
```
@prefix rml: <http://semweb.mmlab.be/ns/rml#> .
@prefix rr: <http://www.w3.org/ns/r2rml#> .
@prefix ql: <http://semweb.mmlab.be/ns/ql#> .
@prefix s: <http://schema.org/> .

 [
    a rr:TriplesMap;
    rml:logicalSource [
      rml:source "data.json" ;
      rml:referenceFormulation ql:JSONPath ;
      rml:iterator "$[*]";
    ];
    rr:subjectMap [
      rr:template "https://example.org/instances/{id}";
      rr:class s:Person ;
      rr:graph <graph:1> ;
    ];
    rr:predicateObjectMap [
      rr:predicate s:givenName ;
      rr:objectMap [ rml:reference "name" ] ;
      rr:graph rr:defaultGraph ;
    ];
  ] .

```

**Output**
```
<https://example.org/instances/0> <http://schema.org/givenName> "Alice".
<https://example.org/instances/0> <http://schema.org/givenName> "Alice" <graph:1> .
<https://example.org/instances/0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Person> <graph:1> .

```


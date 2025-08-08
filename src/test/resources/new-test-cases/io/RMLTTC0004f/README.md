## RMLTTC0004f

**Title**: Target RDF/XML

**Description**: Test export all triples as RDF/XML

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
    rml:logicalTarget <#TargetDump1>;
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:name;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.name";
    ];
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:age;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "$.age";
    ];
  ];
.

<#TargetDump1> a rml:LogicalTarget;
  rml:target [ a rml:Target, rml:FilePath;
    rml:root rml:CurrentWorkingDirectory;
    rml:path "./dump1.rdfxml";
  ];
  rml:serialization formats:RDF_XML;
.

```

**Output 1**
```

```

**Output 2**
```
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:foaf="http://xmlns.com/foaf/0.1/">
  <rdf:Description rdf:about="http://example.org/0">
    <foaf:age>33</foaf:age>
    <foaf:name>Monica Geller</foaf:name>
  </rdf:Description>
  <rdf:Description rdf:about="http://example.org/1">
    <foaf:age>34</foaf:age>
    <foaf:name>Rachel Green</foaf:name>
  </rdf:Description>
  <rdf:Description rdf:about="http://example.org/2">
    <foaf:age>35</foaf:age>
    <foaf:name>Joey Tribbiani</foaf:name>
  </rdf:Description>
  <rdf:Description rdf:about="http://example.org/3">
    <foaf:age>36</foaf:age>
    <foaf:name>Chandler Bing</foaf:name>
  </rdf:Description>
  <rdf:Description rdf:about="http://example.org/4">
    <foaf:age>37</foaf:age>
    <foaf:name>Ross Geller</foaf:name>
  </rdf:Description>
</rdf:RDF>

```


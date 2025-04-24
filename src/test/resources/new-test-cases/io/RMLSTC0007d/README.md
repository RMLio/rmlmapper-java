## RMLSTC0007d

**Title**: Source with XPath reference formulation with namespaces

**Description**: Test source with XPath reference formulation with namespaces

**Error expected?** No

**Input**
```
<Friends xmlns:ex="http://example.org">
  <ex:Character id="0">
    <ex:name>Monica Geller</ex:name>
    <ex:age>33</ex:age>
  </ex:Character>
  <ex:Character id="1">
    <ex:name>Rachel Green</ex:name>
    <ex:age>34</ex:age>
  </ex:Character>
  <ex:Character id="2">
    <ex:name>Joey Tribbiani</ex:name>
    <ex:age>35</ex:age>
  </ex:Character>
  <ex:Character id="3">
    <ex:name>Chandler Bing</ex:name>
    <ex:age>36</ex:age>
  </ex:Character>
  <ex:Character id="4">
    <ex:name>Ross Geller</ex:name>
    <ex:age>37</ex:age>
  </ex:Character>
</Friends>

```

**Mapping**
```
@prefix rml: <http://w3id.org/rml/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@base <http://example.com/rules/> .

<#TriplesMap> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
    rml:source [ a rml:FilePath;
      rml:root rml:MappingDirectory;
      rml:path "Friends.xml"
    ];
    rml:referenceFormulation [ a rml:XPathReferenceFormulation;
      rml:namespace [ a rml:Namespace;
        rml:namespacePrefix "ex";
        rml:namespaceURL "http://example.org";
      ];
    ];
    rml:iterator "//Friends/ex:Character";
  ];
  rml:subjectMap [ a rml:SubjectMap;
    rml:template "http://example.org/{@id}";
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:name;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "ex:name/text()";
    ];
  ];
  rml:predicateObjectMap [ a rml:PredicateObjectMap;
    rml:predicateMap [ a rml:PredicateMap;
      rml:constant foaf:age;
    ];
    rml:objectMap [ a rml:ObjectMap;
      rml:reference "ex:age/text()";
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


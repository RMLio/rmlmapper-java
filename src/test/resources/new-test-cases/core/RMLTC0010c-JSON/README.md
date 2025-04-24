## RMLTC0010c-JSON

**Title**: "Template with table columns with special chars and backslashes"

**Description**: "Tests a template with special chars in reference value and backslash escapes in string templates"

**Error expected?** No

**Input**
```
{
  "countries": [{
    "Country Code": 1,
    "Name":"Bolivia, Plurinational State of",
    "ISO 3166": "BO"
  }, {
    "Country Code": 2,
    "Name":"Ireland",
    "ISO 3166": "IE"
  }, {
    "Country Code": 3,
    "Name":"Saint Martin (French part)",
    "ISO 3166": "MF"
  }]
}

```

**Mapping**
```
@prefix ex: <http://example.com/> .
@prefix rml: <http://w3id.org/rml/> .

<http://example.com/base/TriplesMap1> a rml:TriplesMap;
  rml:logicalSource [ a rml:LogicalSource;
      rml:iterator "$.countries[*]";
      rml:referenceFormulation rml:JSONPath;
      rml:source [ a rml:RelativePathSource;
          rml:root rml:MappingDirectory;
          rml:path "country_info.json"
        ]
    ];
  rml:predicateObjectMap [
      rml:predicate ex:code;
      rml:objectMap [
          rml:template "\\{\\{\\{ {$.['ISO 3166']} \\}\\}\\}";
          rml:termType rml:Literal
        ]
    ];
  rml:subjectMap [
      rml:template "http://example.com/{$.['Country Code']}/{$.Name}"
    ] .

```

**Output**
```
<http://example.com/1/Bolivia%2C%20Plurinational%20State%20of> <http://example.com/code> "{{{ BO }}}" .
<http://example.com/2/Ireland> <http://example.com/code> "{{{ IE }}}" .
<http://example.com/3/Saint%20Martin%20%28French%20part%29> <http://example.com/code> "{{{ MF }}}" .


```


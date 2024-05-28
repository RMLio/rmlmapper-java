package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;

import static be.ugent.rml.NAMESPACES.RML2;

public class ReferenceFormulation {
    public static String XPath = RML2 + "XPath";
    public static String SPARQLResultsXML = NAMESPACES.FORMATS + "SPARQL_Results_XML";
    public static String CSV = RML2 + "CSV";
    public static String SPARQLResultsCSV = NAMESPACES.FORMATS + "SPARQL_Results_CSV";
    public static String RDBTable = RML2 + "SQL2008Table";
    public static String RDBQuery = RML2 + "SQL2008Query";
    public static String JSONPath = RML2 + "JSONPath";
    public static String SPARQLResultsJSON = NAMESPACES.FORMATS + "SPARQL_Results_JSON";
    public static String CSS3 = NAMESPACES.QL + "CSS3";
}

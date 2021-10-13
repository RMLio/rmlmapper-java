package be.ugent.rml.records.xpath;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

// source: https://howtodoinjava.com/java/xml/xpath-namespace-resolution-example/

public class NamespaceResolver implements NamespaceContext {
    
    public NamespaceResolver(Document document) {
        sourceDocument = document;
    }

    //Store the source document to retrive the existing namespaces
    private Document sourceDocument;

    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return sourceDocument.lookupNamespaceURI(null);
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }
    }
 
    public String getPrefix(String namespaceURI) {
        return sourceDocument.lookupPrefix(namespaceURI);
    }
 
    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}

package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.ugent.rml.records.xpath.NamespaceResolver;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This class is a specific implementation of a record for XML.
 * Every record corresponds with an XML element in a data source.
 */
public class XMLRecord extends Record {

    private Node node;

    public XMLRecord(Node node) {
        this.node = node;
    }

    /**
     * This method returns the objects for a reference (XPath) in the record.
     *
     * @param value the reference for which objects need to be returned.
     * @return a list of objects for the reference.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> results = new ArrayList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceResolver(this.node.getOwnerDocument()));
        try {
            NodeList result = (NodeList) xPath.compile(value).evaluate(node, XPathConstants.NODESET);

            for (int i = 0; i < result.getLength(); i++) {
                results.add(result.item(i).getTextContent());
            }
        } catch (XPathExpressionException e1) {
            /* Fallback to string representation if not a nodeset. */
            try {
                results.add((String) xPath.compile(value).evaluate(node));
            } catch (XPathExpressionException e2) {
                e2.printStackTrace();
            }
        }


        return results;
    }
}

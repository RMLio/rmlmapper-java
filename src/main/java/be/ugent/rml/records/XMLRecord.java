package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XMLRecord implements Record {

    private static final String[] SPARQL_NODE_TYPES = new String[]{"literal", "uri", "bnode"};

    private Node node;

    public XMLRecord(Node node) {
        this.node = node;
    }

    @Override
    public List<String> get(String value) {
        List<String> results = new ArrayList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            NodeList result = (NodeList) xPath.compile(value).evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i ++) {
                results.add(result.item(i).getTextContent());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return results;
    }
}

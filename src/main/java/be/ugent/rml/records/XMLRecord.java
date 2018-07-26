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

            if (result.getLength() == 0) {  // try with attributes (e.g. SPARQL works like this)
                NodeList childNodes = node.getChildNodes();
                Node bindingNode = null;
                int i = 0;
                while (bindingNode == null && i < childNodes.getLength()) {
                    if (childNodes.item(i).getNodeName().equals("binding")) {

                        // Check if "name" attribute matches with required value
                        NamedNodeMap attributes = childNodes.item(i).getAttributes();
                        Node nameAttribute = attributes.getNamedItem("name");
                        if (nameAttribute.getNodeValue().equals(value)) {
                            bindingNode = childNodes.item(i);
                        }
                    }
                    i++;
                }

                // Get bindingNode's child with the requested value
                i = 0;
                while (result.getLength() == 0 && i < SPARQL_NODE_TYPES.length) {
                    result = (NodeList) xPath.compile(SPARQL_NODE_TYPES[i]).evaluate(bindingNode, XPathConstants.NODESET);
                }
            }

            for (int i = 0; i < result.getLength(); i ++) {
                results.add(result.item(i).getTextContent());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return results;
    }
}

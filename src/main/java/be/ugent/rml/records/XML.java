package be.ugent.rml.records;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XML extends IteratorFormat {

    // Avoid downloading external DTDs (and entities) found in XMLs,
    // if those files can be found in the classpath.
    public class LocalEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
            String fileName = systemId.substring(systemId.lastIndexOf("/") + 1);
            if (getClass().getClassLoader().getResource(fileName) != null) {
                return new InputSource(getClass().getClassLoader().getResourceAsStream(fileName));
            } else {
                // if a local file is not found, use the default behaviour
                return null;
            }
        }
    }

    protected String getContentType() {
        return "application/xml";
    }

    @Override
    List<Record> _get(InputStream stream, String iterator) throws IOException {
        List<Record> records = new ArrayList<>();

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setEntityResolver(new LocalEntityResolver());
            Document xmlDocument = builder.parse(stream);

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList result = (NodeList) xPath.compile(iterator).evaluate(xmlDocument, XPathConstants.NODESET);

            for (int i = 0; i < result.getLength(); i ++) {
                records.add(new XMLRecord(result.item(i)));
            }
        } catch (XPathExpressionException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return records;
    }
}

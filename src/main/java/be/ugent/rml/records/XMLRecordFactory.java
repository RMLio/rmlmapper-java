package be.ugent.rml.records;

import org.apache.xmlbeans.impl.xb.xsdschema.FieldDocument.Field.Xpath;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.ugent.rml.records.xpath.NamespaceResolver;

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

/**
 * This class is a record factory that creates XML records.
 */
public class XMLRecordFactory extends IteratorFormat<Document> {

    /**
     * This method returns the records from an XML document based on an iterator.
     *
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     * @throws IOException
     */
    @Override
    List<Record> getRecordsFromDocument(Document document, String iterator) throws IOException {
        List<Record> records = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new NamespaceResolver(document));
            NodeList result = (NodeList) xPath.compile(iterator).evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                records.add(new XMLRecord(result.item(i)));
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return records;
    }

    /**
     * This method returns an XML document from an InputStream.
     *
     * @param stream the used InputStream.
     * @return an XML document.
     * @throws IOException
     */
    @Override
    Document getDocumentFromStream(InputStream stream) throws IOException {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            return builder.parse(stream);
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }
}

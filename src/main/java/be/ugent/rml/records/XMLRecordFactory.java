package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.source.Source;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import javax.xml.transform.stream.StreamSource;
import be.ugent.rml.records.xpath.SaxNamespaceResolver;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a record factory that creates XML records.
 */
public class XMLRecordFactory extends IteratorFormat<XdmNode> {

    // Saxon processor to be reused across XPath query evaluations
    private Processor saxProcessor;

    public XMLRecordFactory() {
        saxProcessor = new Processor(false);
    }

    /**
     * This method returns the records from an XML document based on an iterator.
     *
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     */
    @Override
    List<Source> getRecordsFromDocument(XdmNode document, String iterator) {
        List<Source> records = new ArrayList<>();

        try {
            XPathCompiler compiler = saxProcessor.newXPathCompiler();
            // Enable expression caching
            compiler.setCaching(true);
            // Extract and register existing source namespaces into the XPath compiler
            SaxNamespaceResolver.registerNamespaces(compiler, document);
            // Execute iterator XPath query
            XdmValue result = compiler.evaluate(iterator, document);
            // Extract set of records
            result.forEach((item) -> {
                records.add(new XMLRecord(item, compiler));
            });
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }

        return records;
    }

    /**
     * This method returns an XML document from an InputStream.
     *
     * @param stream the used InputStream.
     * @return an XML document.
     */
    @Override
    XdmNode getDocumentFromStream(InputStream stream) {
        try {
            DocumentBuilder docBuilder = saxProcessor.newDocumentBuilder();
            return docBuilder.build(new StreamSource(stream));
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }

        return null;
    }
}

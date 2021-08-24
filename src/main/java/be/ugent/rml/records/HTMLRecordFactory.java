package be.ugent.rml.records;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a record factory that creates HTML records.
 */
public class HTMLRecordFactory extends IteratorFormat<Document> {

    /**
     * This method returns the records from an HTML document based on an iterator.
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     */
    @Override
    List<Record> getRecordsFromDocument(Document document, String iterator) {
        Elements data = document.select(iterator);
        // Get the headers
        List<String> headers = data.get(0).select("th").stream().map(Element::text).collect(Collectors.toList());
        data.remove(0);
        return data
                .stream()
                .map(row -> new HTMLRecord(row, headers))
                .collect(Collectors.toList());
    }

    /**
     * This method returns an HTML document from an InputStream.
     * @param stream the used InputStream.
     * @return an HTML document.
     * @throws IOException
     */
    @Override
    Document getDocumentFromStream(InputStream stream) throws IOException {
        return Jsoup.parse(stream, "UTF-8", "http://example.com/");
    }
}

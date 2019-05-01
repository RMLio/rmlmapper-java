package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVBuilder {

    private String getContentType() {
        return "text/csv";
    }

    private CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
    private Charset csvCharset = StandardCharsets.UTF_8;
    private InputStream is;

    CSVBuilder(String path, String cwd) throws IOException {
        this.is = Utils.getInputStreamFromLocation(path, new File(cwd), getContentType());
    }

    void setOptions(QuadStore rmlStore, Term source, List<Term> iterators, Term triplesMap) {
        // CSVW Dialect options
        List<Term> dialectTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "dialect"), null));
        if (!dialectTerms.isEmpty()) {
            Term dialect = dialectTerms.get(0);
            // TODO refactor getCSVRecords to set parser and reader from dialect terms
            // TODO implement rest of https://www.w3.org/TR/tabular-metadata/#dialect-descriptions
            // TODO refactor if statements

            // commentPrefix
            // Delimiter
            // TODO must be a string
            List<Term> delimiterTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(dialect, new NamedNode(NAMESPACES.CSVW + "delimiter"), null));
            if (!delimiterTerms.isEmpty()) {
                String delimiter = delimiterTerms.get(0).getValue();
                this.csvFormat = this.csvFormat.withDelimiter(delimiter.toCharArray()[0]);
            }
            // doubleQuote
            List<Term> doubleQuoteTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(dialect, new NamedNode(NAMESPACES.CSVW + "doubleQuote"), null));
            if (!doubleQuoteTerms.isEmpty()) {
                String doubleQuoteString = doubleQuoteTerms.get(0).getValue();
                boolean doubleQuote = doubleQuoteString.equals("true");
                this.csvFormat = this.csvFormat.withEscape(doubleQuote ? '\\' : '"');
            }
            // Encoding
            // TODO refactor file Utils to set encoding
            // TODO Validate encoding with http://www.w3.org/TR/encoding/

            // header
            // headerRowCount
            // lineTerminators
            // withQuoteChar
            List<Term> quoteTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(dialect, new NamedNode(NAMESPACES.CSVW + "quoteChar"), null));
            if (!quoteTerms.isEmpty()) {
                String quote = quoteTerms.get(0).getValue();
                this.csvFormat = this.csvFormat.withQuote(quote.toCharArray()[0]);
            }
            // skipBlankRows
            // skipColumns
            // skipInitialSpace
            // skipRows
            // trim TODO not supported by opencsv, look at commons CSV or super CSV
            // @id
            // @type
        }
    }

    List<Record> getRecords() throws IOException {
        return CSV.get(is, csvCharset, csvFormat);
    }

}

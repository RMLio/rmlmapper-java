package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

class CSVW {

    private String getContentType() {
        return "text/csv";
    }

    private CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
    private Charset csvCharset = StandardCharsets.UTF_8;
    private InputStream is;

    private QuadStore rmlStore;
    private List<Term> iterators;
    private Term triplesMap;
    private Term dialect;

    CSVW(String path, String cwd) throws IOException {
        this.is = Utils.getInputStreamFromLocation(path, new File(cwd), getContentType());
    }
    
    private String helper(String term) {
        List<Term> terms = Utils.getObjectsFromQuads(this.rmlStore.getQuads(this.dialect, new NamedNode(NAMESPACES.CSVW + term), null));
        if (!terms.isEmpty()) {
            return  terms.get(0).getValue();
        }
        return null;
    }

    private Character computeCommentPrefix() {
        String output = helper("commentPrefix");
        if (output == null) {
            return this.csvFormat.getCommentMarker();
        } else {
            return output.toCharArray()[0];
        }
    }

    private boolean computeSkipHeader() {
        String output = helper("header");
        if (output == null) {
            return this.csvFormat.getSkipHeaderRecord();
        } else {
            return output.equals("true");
        }
    }

    private boolean computeTrim() {
        String output = helper("trim");
        if (output == null) {
            return this.csvFormat.getTrim();
        } else {
            return output.equals("true");
        }
    }

    private Character computeDelimiter() {
        String output = helper("delimiter");
        if (output == null) {
            return this.csvFormat.getDelimiter();
        } else {
            return output.toCharArray()[0];
        }
    }

    private Character computeDoubleQuote() {
        String output = helper("doubleQuote");
        if (output == null) {
            return this.csvFormat.getEscapeCharacter();
        } else {
            return output.equals("true") ? '\\' : '"';
        }
    }

    private Character computeQuote() {
        String output = helper("quoteChar");
        if (output == null) {
            return this.csvFormat.getQuoteCharacter();
        } else {
            return output.toCharArray()[0];
        }
    }

    void setOptions(QuadStore rmlStore, Term source, List<Term> iterators, Term triplesMap) {

        this.rmlStore = rmlStore;
        this.iterators = iterators;
        this.triplesMap = triplesMap;

        // CSVW Dialect options
        List<Term> dialectTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "dialect"), null));
        if (!dialectTerms.isEmpty()) {

            this.dialect = dialectTerms.get(0);

            // TODO implement rest of https://www.w3.org/TR/tabular-metadata/#dialect-descriptions
            // TODO implement CSVW Schema class to add header types
            this.csvFormat = this.csvFormat
                    // commentPrefix
                    .withCommentMarker(computeCommentPrefix())
                    // delimiter
                    .withDelimiter(computeDelimiter())
                    // doubleQuote
                    .withEscape(computeDoubleQuote())
                    // header
                    .withSkipHeaderRecord(computeSkipHeader())
                    // headerRowCount
                    // lineTerminators
                    // trim
                    // TODO Commons CSV doesn't support start or end trimming
                    .withTrim(computeTrim())
                    // skipBlankRows
                    // skipColumns
                    // skipInitialSpace
                    // skipRows
                    // @id
                    // @type
                    // withQuoteChar
                    .withQuote(computeQuote())
            ;

            // Encoding
            String encoding = helper("encoding");
            if (encoding != null) {
                this.csvCharset = Charset.forName(encoding);
            }
        }
    }

    List<Record> getRecords() throws IOException {
        CSVParser parser = CSVParser.parse(is, csvCharset, csvFormat);
        List<org.apache.commons.csv.CSVRecord> myEntries = parser.getRecords();
        return myEntries.stream()
                .map(CSVRecordAdapter::new)
                .collect(Collectors.toList());
    }

}

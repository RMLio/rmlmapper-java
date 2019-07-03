package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

class CSVW {

    private CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
    private Charset csvCharset = StandardCharsets.UTF_8;
    private InputStream inputStream;

    private QuadStore rmlStore;
    private Term dialect;
    private Term logicalSource;

    CSVW(InputStream inputStream, QuadStore rmlStore, Term logicalSource) {
        this.rmlStore = rmlStore;
        this.inputStream = inputStream;
        this.logicalSource = logicalSource;

        setOptions();
    }

    CSVParser getCSVParser() throws IOException {
        return CSVParser.parse(inputStream, csvCharset, csvFormat);
    }

    private void setOptions() {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Term source = sources.get(0);

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
}

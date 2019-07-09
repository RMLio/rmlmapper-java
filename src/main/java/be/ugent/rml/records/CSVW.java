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

/**
 * This class has as main goal to create a CSVParser for a Logical Source with CSVW.
 */
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

    /**
     * This method returns a CSVParser.
     * @return a CSVParser.
     * @throws IOException
     */
    CSVParser getCSVParser() throws IOException {
        return CSVParser.parse(inputStream, csvCharset, csvFormat);
    }

    /**
     * Based on the CSVW details the options for the CSVParser are set.
     */
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
                    .withCommentMarker(getCommentPrefix())
                    // delimiter
                    .withDelimiter(getDelimiter())
                    // doubleQuote
                    .withEscape(getEscapeCharacter())
                    // header
                    .withSkipHeaderRecord(getSkipHeaderRecord())
                    // headerRowCount
                    // lineTerminators
                    // trim
                    // TODO Commons CSV doesn't support start or end trimming
                    .withTrim(getTrim())
                    // skipBlankRows
                    // skipColumns
                    // skipInitialSpace
                    // skipRows
                    // @id
                    // @type
                    // withQuoteChar
                    .withQuote(getQuoteCharacter())
            ;

            // Encoding
            String encoding = getValueFromTerm("encoding");

            if (encoding != null) {
                this.csvCharset = Charset.forName(encoding);
            }
        }
    }

    /**
     * This method returns a single value (or null) for a CSVW term.
     * @param term the CSVW term, without CSVW namespace.
     * @return the value of the term, if one is found, else null.
     */
    private String getValueFromTerm(String term) {
        List<Term> terms = Utils.getObjectsFromQuads(this.rmlStore.getQuads(this.dialect, new NamedNode(NAMESPACES.CSVW + term), null));

        if (!terms.isEmpty()) {
            return  terms.get(0).getValue();
        }

        return null;
    }

    /**
     * This method determines the comment prefix.
     * @return the comment prefix.
     */
    private Character getCommentPrefix() {
        String output = getValueFromTerm("commentPrefix");

        if (output == null) {
            return this.csvFormat.getCommentMarker();
        } else {
            return output.toCharArray()[0];
        }
    }

    /**
     * This method returns whether to skip the header record.
     * @return true or false.
     */
    private boolean getSkipHeaderRecord() {
        String output = getValueFromTerm("header");

        if (output == null) {
            return this.csvFormat.getSkipHeaderRecord();
        } else {
            return output.equals("true");
        }
    }

    /**
     * This method returns whether to trim leading and trailing blanks.
     * @return true or false.
     */
    private boolean getTrim() {
        String output = getValueFromTerm("trim");

        if (output == null) {
            return this.csvFormat.getTrim();
        } else {
            return output.equals("true");
        }
    }

    /**
     * This method returns the character delimiting the values (typically ';', ',' or '\t').
     * @return the delimiter.
     */
    private Character getDelimiter() {
        String output = getValueFromTerm("delimiter");

        if (output == null) {
            return this.csvFormat.getDelimiter();
        } else {
            return output.toCharArray()[0];
        }
    }

    /**
     * This method returns the escape character.
     * @return the escape character.
     */
    private Character getEscapeCharacter() {
        String output = getValueFromTerm("doubleQuote");

        if (output == null) {
            return this.csvFormat.getEscapeCharacter();
        } else {
            return output.equals("true") ? '\\' : '"';
        }
    }

    /**
     * This method returns the character used to encapsulate values containing special characters.
     * @return the quote character.
     */
    private Character getQuoteCharacter() {
        String output = getValueFromTerm("quoteChar");

        if (output == null) {
            return this.csvFormat.getQuoteCharacter();
        } else {
            return output.toCharArray()[0];
        }
    }
}

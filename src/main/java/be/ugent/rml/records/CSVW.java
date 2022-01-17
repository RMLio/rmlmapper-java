package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.Access;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class has as main goal to create a CSVParser for a Logical Source with CSVW.
 */
class CSVW {

    private com.opencsv.CSVParserBuilder csvParser = new CSVParserBuilder().withIgnoreLeadingWhiteSpace(true);
    private Charset csvCharset = StandardCharsets.UTF_8;
    private InputStream inputStream;

    private QuadStore rmlStore;
    private Term dialect;
    private Term logicalSource;
    private List<String> nulls;
    private boolean skipHeader = false;
    private String commentPrefix = "#";

    CSVW(InputStream inputStream, QuadStore rmlStore, Term logicalSource) {
        this.rmlStore = rmlStore;
        this.inputStream = inputStream;
        this.logicalSource = logicalSource;
        this.nulls = new ArrayList<>();
        setOptions();
    }



    /**
     * Read the records from the given Access
     * @param access The access containing the records
     * @return The list of records in the Access
     */
    List<Record> getRecords(Access access) throws IOException, CsvException {
        int skipLines = this.skipHeader ? 1 : 0;
        List<String[]> records =  new CSVReaderBuilder(new InputStreamReader(inputStream, csvCharset))
                .withCSVParser(this.csvParser.build())
                .withSkipLines(skipLines)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()
                .readAll();
        String[] header = records.get(0);
        Stream<String[]> readRecords = records.subList(1, records.size())
                .stream()
                // throw away empty records
                .filter(r -> r.length != 0 && !(r.length == 1 && r[0] == null));
        if(this.getTrim()){ // trim each record value
            readRecords = readRecords.map(r -> Arrays.stream(r).map(String::trim).toArray(String[]::new));
        }
        return readRecords
                .map(record -> new CSVRecord(header, record, access.getDataTypes()))
                .map(this::replaceNulls)
                .collect(Collectors.toList());
    }

    /**
     * Based on the CSVW details the options for the CSVParser are set.
     */
    private void setOptions() {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Term source = sources.get(0);

        List<Term> nullTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "null"), null));
        if(!nullTerms.isEmpty()){
            this.nulls.addAll(nullTerms.stream().map(Term::getValue).collect(Collectors.toList()));
        }

        // CSVW Dialect options
        List<Term> dialectTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "dialect"), null));

        if (!dialectTerms.isEmpty()) {

            this.dialect = dialectTerms.get(0);
            // TODO implement rest of https://www.w3.org/TR/tabular-metadata/#dialect-descriptions
            // TODO implement CSVW Schema class to add header types
            this.csvParser = this.csvParser
                    // commentPrefix TODO
                   // .withComment(getCommentPrefix())
                    // delimiter
                    .withSeparator(getDelimiter())
                    // doubleQuote
                    .withEscapeChar(getEscapeCharacter())
                    // header TODO
                    //.(getSkipHeaderRecord())
                    // headerRowCount
                    // lineTerminators
                    // trim
                    // TODO Commons CSV doesn't support start or end trimming
                    .withIgnoreLeadingWhiteSpace(getTrim())
                    // skipBlankRows
                    // skipColumns
                    // skipInitialSpace
                    // skipRows
                    // @id
                    // @type
                    // withQuoteChar
                    .withQuoteChar(getQuoteCharacter())
            ;
            this.skipHeader = getSkipHeaderRecord();
            this.commentPrefix = getCommentPrefix();
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
    private String getCommentPrefix() {
        String output = getValueFromTerm("commentPrefix");

        if (output == null) {
            return this.commentPrefix;
        } else {
            return output;
        }
    }

    /**
     * This method returns whether to skip the header record.
     * @return true or false.
     */
    private boolean getSkipHeaderRecord() {
        String output = getValueFromTerm("header");

        if (output == null) {
            return this.skipHeader;
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
            return this.csvParser.isIgnoreLeadingWhiteSpace();
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
            return this.csvParser.getSeparator();
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
            return this.csvParser.getEscapeChar();
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
            return this.csvParser.getQuoteChar();
        } else {
            return output.toCharArray()[0];
        }
    }

    public CSVRecord replaceNulls(CSVRecord record){
        Map<String, String> data = record.getData();
        data.forEach((key, value) -> {
            if (this.nulls.contains(value)) {
                data.put(key, null);
            }
        });
        return record;
    }
}

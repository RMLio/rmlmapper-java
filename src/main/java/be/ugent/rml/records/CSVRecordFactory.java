package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.Access;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a record factory that creates CSV records.
 */
public class CSVRecordFactory implements ReferenceFormulationRecordFactory {

    /**
     * This method returns a list of CSV records for a data source.
     * @param access the access from which records need to be fetched.
     * @param logicalSource the used Logical Source.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws IOException, SQLException, ClassNotFoundException {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Term source = sources.get(0);
        CSVParser parser;

        if (source instanceof Literal) {
            // We are not dealing with something like CSVW.
            parser = getParserForNormalCSV(access);
        } else {
            List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

            // Check if we are dealing with CSVW.
            if (sourceType.get(0).getValue().equals(NAMESPACES.CSVW + "Table")) {
                CSVW csvw = new CSVW(access.getInputStream(), rmlStore, logicalSource);
                parser = csvw.getCSVParser();
            } else {
                // RDBs fall under this.
                parser = getParserForNormalCSV(access);
            }
        }

        if (parser != null) {
            List<org.apache.commons.csv.CSVRecord> myEntries = parser.getRecords();

            return myEntries.stream()
                    .map(record -> new CSVRecord(record, access.getDataTypes()))
                    .collect(Collectors.toList());
        } else {
            // We still return an empty list of records when a parser is not found.
            // This is to support certain use cases with RDBs where queries might not be valid,
            // but you don't want the RMLMapper to crash.
            return new ArrayList<>();
        }
    }

    /**
     * This method returns a CSVParser from a simple access (local/remote CSV file; no CSVW).
     * @param access the used access.
     * @return a CSVParser.
     * @throws IOException
     */
    private CSVParser getParserForNormalCSV(Access access) throws IOException, SQLException, ClassNotFoundException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(false);
        InputStream inputStream = access.getInputStream();

        if (inputStream != null) {
            try {
                return CSVParser.parse(inputStream, StandardCharsets.UTF_8, csvFormat);
            } catch (IllegalArgumentException e) {
                // TODO give warning to user
                return null;
            }
        } else {
            return null;
        }
    }
}

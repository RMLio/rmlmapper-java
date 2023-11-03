package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.CSVSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.ExcelSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.ODSSourceIterator;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a record factory that creates CSV records.
 */
public class TabularSourceFactory implements ReferenceFormulationRecordFactory {
    private static final Logger logger = LoggerFactory.getLogger(TabularSourceFactory.class);

    /**
     * This method returns a list of CSV records for a data source.
     *
     * @param access        the access from which records need to be fetched.
     * @param logicalSource the used Logical Source.
     * @param rmlStore      the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws Exception {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Term source = sources.get(0);

        if (source instanceof Literal) {
            // We are not dealing with something like CSVW.
            // Check for different spreadsheet formats
            String filePath = source.getValue();
            String extension = FilenameUtils.getExtension(filePath);
            switch (extension) {
                case "xlsx":
                    return getRecordsForExcel(access);
                case "ods":
                    return getRecordsForODT(access);
                default:
                    return getRecordsForCSV(access, null);
            }

        } else {
            List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

            // Check if we are dealing with CSVW.
            if (sourceType.get(0).getValue().equals(NAMESPACES.CSVW + "Table")) {
                CSVW csvw = new CSVW(rmlStore, logicalSource);
                return getRecordsForCSV(access, csvw);
            } else {
                // RDBs fall under this.
                return getRecordsForCSV(access, null);
            }
        }
    }

    /**
     * Get Sources for Excel file format.
     *
     * @param access Access to consume sources from
     * @return a list of sources
     */
    private List<Record> getRecordsForExcel(Access access) throws Exception {
        List<Record> output = new ArrayList<>();
        try (ExcelSourceIterator iterator = new ExcelSourceIterator(access)) {
            iterator.forEachRemaining(output::add);
        }

        return output;
    }

    /**
     * Get Sources for ODT file format.
     *
     * @param access Access to consume sources from
     * @return a list of ODT sources
     */
    private List<Record> getRecordsForODT(Access access) throws Exception {
        List<Record> output = new ArrayList<>();
        try (ODSSourceIterator iterator = new ODSSourceIterator(access)) {
            iterator.forEachRemaining(output::add);
        }
        return output;
    }

    /**
     * This method returns a CSVParser from a simple access (local/remote CSV file; no CSVW).
     *
     * @param access the used access.
     * @return a CSVParser.
     * @throws IOException
     */
    private List<Record> getRecordsForCSV(Access access, CSVW csvw) throws Exception {
        try {
            // Check if we are dealing with CSVW.
            if (csvw == null) {
                // RDBs fall under this
                try (CSVSourceIterator iterator = new CSVSourceIterator(access)) {
                    List<Record> results = new ArrayList<>();
                    iterator.forEachRemaining(results::add);

                    return results;
                }
            } else {
                return csvw.getRecords(access);
            }

        } catch (IllegalArgumentException e) {
            // We still return an empty list of records when a parser is not found.
            // This is to support certain use cases with RDBs where queries might not be valid,
            // but you don't want the RMLMapper to crash.
            logger.debug("Could not parse CSV inputstream", e);
            return new ArrayList<>();
        }
    }
}

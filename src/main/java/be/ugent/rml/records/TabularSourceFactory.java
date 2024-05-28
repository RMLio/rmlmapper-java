package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.CSVSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.ExcelSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.ODSSourceIterator;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a record factory that creates CSV records.
 */
public class TabularSourceFactory implements ReferenceFormulationRecordFactory {

    /**
     * This method returns a list of CSV records for a data source.
     *
     * @param access        the access from which records need to be fetched.
     * @param logicalSource the used Logical Source.
     * @param rmlStore      the QuadStore with the RML rules.
     * @return a list of records.
     */
    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws Exception {
        // We are not dealing with something like CSVW.
        // Check for different spreadsheet formats
        return switch (access.getContentType().toLowerCase()) {
            case "text/csv" -> getRecordsForCSV(access);
            case "text/csvw" -> getRecordsForCSVW(access, rmlStore, logicalSource);
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> getRecordsForExcel(access);
            case "application/vnd.oasis.opendocument.spreadsheet" -> getRecordsForODT(access);
            default ->
                    throw new IllegalArgumentException(String.format("Unrecognised content type: %s", access.getContentType()));
        };
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
     * @return a List of Records.
     * @throws IOException
     */
    private List<Record> getRecordsForCSV(Access access) throws Exception {
        try (CSVSourceIterator iterator = new CSVSourceIterator(access)) {
            List<Record> results = new ArrayList<>();
            iterator.forEachRemaining(results::add);

            return results;
        }
    }

    private List<Record> getRecordsForCSVW(Access access, QuadStore rmlStore, Term logicalSource) throws Exception {
        CSVW csvw = new CSVW(rmlStore, logicalSource);
        return csvw.getRecords(access);
    }
}

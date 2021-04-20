package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is a specific implementation of a record for CSV.
 * Every record corresponds with a row of the CSV data source.
 */
public class CSVRecord extends Record {

    // The CSV record that is provided by the Apache CSVParser.
    private org.apache.commons.csv.CSVRecord record;
    private Map<String, String> datatypes;

    CSVRecord(org.apache.commons.csv.CSVRecord record, Map<String, String> datatypes) {
        this.record = record;
        this.datatypes = datatypes;
    }

    /**
     * This method returns the datatype of a reference in the record.
     * @param value the reference for which the datatype needs to be returned.
     * @return the IRI of the datatype.
     */
    public String getDataType(String value) {
        String datatype = null;

        if (datatypes != null) {
            datatype = datatypes.get(value);
            /*
             * Some RDBs require quotes for capitalization, but after executing the query,
             * the quotes are dropped in the results as "ID" != ID, neither is 'ID' != ID.
             *
             * If the lookup fail, remove these quotes and try again.
             */
            if (datatype == null) {
                value = value.replaceFirst("^\"", "").replaceFirst("\"$", "");
                value = value.replaceFirst("^\'", "").replaceFirst("\'$", "");
                datatype = datatypes.get(value);
            }
        }

        return datatype;
    }

    /**
     * This method returns the objects for a column in the CSV record (= CSV row).
     * @param value the column for which objects need to be returned.
     * @return a list of objects for the column.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> result = new ArrayList<>();
        Object obj;
        obj = this.record.get(value);
        // Empty column values in CSV are the same as NULL values in RDBs and should be skipped
        if (!String.valueOf(obj).equals("")) {
            result.add(obj);
        }
        return result;
    }
}

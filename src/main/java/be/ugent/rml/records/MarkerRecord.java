package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import java.util.List;

/**
 * This class is a specific implementation of a record for the magic marker.
 * Every record always returns the magic marker, no matter th input.
 */
public class MarkerRecord extends Record {
    /**
     * This method returns the objects for a column in the CSV record (= CSV row).
     * @param value the column for which objects need to be returned.
     * @return a list of objects for the column.
     */
    @Override
    public List<Object> get(String value) {
        return List.of(IDLabFunctions.MAGIC_MARKER);
    }
}

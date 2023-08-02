package be.ugent.rml.records;

import be.ugent.knows.idlabFunctions.IDLabFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a specific implementation of a record for the magic marker.
 * Every record always returns the magic marker, no matter th input.
 */
public class MarkerRecord extends Record {

    private String MAGIC_MARKER = IDLabFunctions.MAGIC_MARKER;

    /**
     * This method returns the datatype of a reference in the record.
     * @param value the reference for which the datatype needs to be returned.
     * @return the IRI of the datatype.
     */
    public String getDataType(String value) {
        return null;
    }

    /**
     * This method returns the objects for a column in the CSV record (= CSV row).
     * @param value the column for which objects need to be returned.
     * @return a list of objects for the column.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> result = new ArrayList<>();
        result.add(MAGIC_MARKER);

        return result;
    }
}

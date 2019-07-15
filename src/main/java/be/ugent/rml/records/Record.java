package be.ugent.rml.records;

import java.util.List;

/**
 * This class represents a generic record in a data source.
 */
public abstract class Record {

    /**
     * This method returns the objects for a reference in the record.
     * @param value the reference for which objects need to be returned.
     * @return a list of objects for the reference.
     */
    public abstract List<Object> get(String value);

    /**
     * This method returns the datatype of a reference in the record.
     * @param value the reference for which the datatype needs to be returned.
     * @return the IRI of the datatype.
     */
    public String getDataType(String value) {
        return null;
    }
}

package be.ugent.rml.records;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a specific implementation of a record for ODS.
 * Every record corresponds with a row of the ODS data source.
 */
public class ODSRecord extends Record {
    private Row row;
    private Map<String, Cell> header = new HashMap<>();

    public ODSRecord(Row header, Row row) {
        // get name from first row and types from second row
        Row nextRow = header.getNextRow();
        for (int i = 0; i < header.getCellCount(); i++) {
            Cell cell = header.getCellByIndex(i);
            this.header.put(cell.getStringValue(), nextRow.getCellByIndex(i));
        }
        this.row = row;
    }

    /**
     * This method returns the datatype of a reference in the record.
     * @param value the reference for which the datatype needs to be returned.
     * @return the IRI of the datatype.
     */
    public String getDataType(String value) {
        String cellType = null;

        if (header != null && header.get(value) != null) {
            cellType = header.get(value).getValueType();
        }
        return getIRI(cellType);
    }

    /**
     * This method returns the objects for a column in the ODS record (= ODS row).
     * @param value the column for which objects need to be returned.
     * @return a list of objects for the column.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> result = new ArrayList<>();
        Object obj;
        try {
            int index = header.get(value).getColumnIndex();
            Cell cell = row.getCellByIndex(index);
            switch (cell.getValueType()) {
                case "boolean":
                    obj = cell.getBooleanValue();
                    break;
                case "float":
                    double d = cell.getDoubleValue();
                    // Cast to int if needed
                    if (d % 1 == 0) {
                        obj = (int) d;
                    } else {
                        obj = d;
                    }
                    break;
                case "string":
                default:
                    obj = cell.getStringValue();
                    break;
            }
            // TODO don't stringify all types, but retain them
            // needs object comparison in join function
            // FunctionModel
            // java.lang.IllegalArgumentException: argument type mismatch
            obj = String.valueOf(obj);
            result.add(obj);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    /**
     * Convert a cell type to a XSD datatype URI
     * @param cellType
     * @return
     */
    public static String getIRI(String cellType) {
//        https://odftoolkit.org/api/simple/org/odftoolkit/simple/table/Cell.html#getValueType--
        if (cellType == null) {
            return "";
        }
        switch (cellType) {
//            case "boolean":
//                return XSDDatatype.XSDboolean.getURI();
//            case "float":
//                return XSDDatatype.XSDdouble.getURI();
//            case "string":
//                return XSDDatatype.XSDstring.getURI();
            default:
                return XSDDatatype.XSDstring.getURI();
        }
    }
}

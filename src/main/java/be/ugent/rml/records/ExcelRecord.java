package be.ugent.rml.records;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a specific implementation of a Record for Excel.
 * Every record corresponds with a row of the Excel file.
 */
public class ExcelRecord extends Record {

    private Row row;
    private Map<String, Cell> header = new HashMap<>();

    ExcelRecord(Row header, Row row) {
        for (Cell cell : header) {
            this.header.put(cell.getStringCellValue(), cell);
        }
        this.row = row;
    }

    /**
     * This method returns the datatype of a reference in the record.
     * @param value the reference for which the datatype needs to be returned.
     * @return the IRI of the datatype.
     */
    public String getDataType(String value) {
        Cell cell = null;
        if (header != null && header.get(value) != null) {
            int index = header.get(value).getColumnIndex();
            cell = row.getCell(index);
        }
        return getIRI(cell);
    }


    /**
     * This method returns the objects for a column in the Excel record (= Excel row).
     * @param value the column for which objects need to be returned.
     * @return a list of objects for the column.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> result = new ArrayList<>();
        Object obj;
        try {
            int index = header.get(value).getColumnIndex();
            Cell cell = row.getCell(index);
            switch (cell.getCellType()) {
                case NUMERIC:
                    double d = cell.getNumericCellValue();
                    // Cast to int if needed
                    if (d % 1 == 0) {
                        obj = (int) d;
                    } else {
                        obj = d;
                    }
                    break;
                case BOOLEAN:
                    obj = cell.getBooleanCellValue();
                    break;
                default:
                    obj = cell.getStringCellValue();
                    break;
            }

            result.add(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }

    /**
     * Convert a CellType to a XSD datatype URI
     * @param cell
     * @return
     */
    public static String getIRI(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case NUMERIC:
                return cell.getNumericCellValue() % 1 == 0 ? XSDDatatype.XSDinteger.getURI() : XSDDatatype.XSDdouble.getURI();
            case BOOLEAN:
                return XSDDatatype.XSDboolean.getURI();
            default:
                return XSDDatatype.XSDstring.getURI();
        }
    }
}

package be.ugent.rml.access;

import be.ugent.rml.NAMESPACES;
import com.opencsv.CSVWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static be.ugent.rml.Utils.getHashOfString;

/**
 * This class represents the access to a relational database.
 */
public class RDBAccess implements Access {

    private String dsn;
    private DatabaseType databaseType;
    private String username;
    private String password;
    private String query;
    private String contentType;
    private Map<String, String> datatypes = new HashMap<>();

    // Datatype definitions
    private final static String DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    private final static String VARBINARY = "http://www.w3.org/2001/XMLSchema#hexBinary";
    private final static String DECIMAL = "http://www.w3.org/2001/XMLSchema#decimal";
    private final static String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    private final static String BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
    private final static String DATE = "http://www.w3.org/2001/XMLSchema#date";
    private final static String TIME = "http://www.w3.org/2001/XMLSchema#time";
    private final static String DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";


    /**
     * This constructor takes as arguments the dsn, database, username, password, query, and content type.
     *
     * @param dsn          the data source name.
     * @param databaseType the database type.
     * @param username     the username of the user that executes the query.
     * @param password     the password of the above user.
     * @param query        the SQL query to use.
     * @param contentType  the content type of the results.
     */
    public RDBAccess(String dsn, DatabaseType databaseType, String username, String password, String query, String contentType) {
        this.dsn = dsn;
        this.databaseType = databaseType;
        this.username = username;
        this.password = password;
        this.query = query;
        this.contentType = contentType;
    }

    /**
     * This method returns an InputStream of the results of the SQL query.
     *
     * @return an InputStream with the results.
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException, SQLException, ClassNotFoundException {
        // JDBC objects
        Connection connection = null;
        Statement statement = null;
        String jdbcDriver = databaseType.getDriver();
        String jdbcDSN = "jdbc:" + databaseType.getJDBCPrefix() + "//" + dsn;
        InputStream inputStream = null;

        try {
            // Register JDBC driver
            Class.forName(jdbcDriver);

            // Open connection
            String connectionString = jdbcDSN;
            boolean alreadySomeQueryParametersPresent = false;

            if (username != null && !username.equals("") && password != null && !password.equals("")) {
                if (databaseType == DatabaseType.ORACLE) {
                    connectionString = connectionString.replace(":@", ":" + username + "/" + password + "@");
                } else if (!connectionString.contains("user=")) {
                    connectionString += "?user=" + username + "&password=" + password;
                    alreadySomeQueryParametersPresent = true;
                }
            }

            if (databaseType == DatabaseType.MYSQL) {
                if (alreadySomeQueryParametersPresent) {
                    connectionString += "&";
                } else {
                    connectionString += "?";
                }

                connectionString += "serverTimezone=UTC&useSSL=false";
            }

            if (databaseType == DatabaseType.SQL_SERVER) {
                connectionString = connectionString.replaceAll("\\?|&", ";");

                if (!connectionString.endsWith(";")) {
                    connectionString += ";";
                }
            }
            connection = DriverManager.getConnection(connectionString);

            // Execute query
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            switch (contentType) {
                case NAMESPACES.QL + "XPath" :
                    inputStream = getXMLInputStream(rs);
                    break;
                default:
                    inputStream = getCSVInputStream(rs);
            }


            // Clean-up environment
            rs.close();
            statement.close();
            connection.close();

        } catch (Exception sqlE) {
            throw sqlE;
        } finally {

            // finally block used to close resources
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return inputStream;
    }

    /**
     * This method returns the datatypes used for the columns in the accessed database.
     *
     * @return a map of column names and their datatypes.
     */
    @Override
    public Map<String, String> getDataTypes() {
        return datatypes;
    }

    /**
     * This method creates an CSV-formatted InputStream from a Result Set.
     *
     * @param rs the Result Set that is used.
     * @return a CSV-formatted InputStream.
     * @throws SQLException
     */
    private InputStream getCSVInputStream(ResultSet rs) throws SQLException {
        // Get number of requested columns
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        boolean filledInDataTypes = false;
        StringWriter writer = new StringWriter();

        try {
            // Differentiate null and ""
            CSVWriter csvWriter = new CSVWriter(writer);
            csvWriter.writeNext(getCSVHeader(rsmd, columnCount));

            // Extract data from result set
            while (rs.next()) {
                String[] csvRow = new String[columnCount];

                // Iterate over column names
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnLabel(i);
                    String dataType = getColumnDataType(rsmd.getColumnTypeName(i));

                    // Register datatype during first encounter
                    if (!filledInDataTypes) {
                        if (dataType != null) {
                            datatypes.put(columnName, dataType);
                        }
                    }

                    // Normalize value and add value to CSV row.
                    if (VARBINARY.equals(dataType)) {
                        byte[] data = rs.getBytes(columnName);
                        csvRow[i - 1] = bytesToHexString(data);
                    } else {
                        String data = rs.getString(columnName);
                        csvRow[i - 1] = normalizeData(data, dataType);
                    }
                }

                // Add CSV row to CSVPrinter.
                // non-varargs call
                csvWriter.writeNext(csvRow);
                filledInDataTypes = true;
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get InputStream from StringWriter.
        return new ByteArrayInputStream(writer.toString().getBytes());
    }

    private InputStream getXMLInputStream(ResultSet rs) throws SQLException {
        // Get number of requested columns
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        StringWriter writer = new StringWriter();

        // Create document
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("Results");
            doc.appendChild(rootElement);
            // Extract data from result set
            while (rs.next()) {
                Element row = doc.createElement("row");
                rootElement.appendChild(row);

                // Iterate over column names
                for (int i = 1; i <= columnCount; i++) {
                    Element el = doc.createElement(rsmd.getColumnName(i));
                    el.appendChild(doc.createTextNode(rs.getObject(i).toString()));
                    row.appendChild(el);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get InputStream from StringWriter.
        return new ByteArrayInputStream(writer.toString().getBytes());

    }


    /**
     * This method returns the corresponding datatype for a SQL datatype.
     *
     * @param type the SQL datatype.
     * @return the url of the corresponding datatype.
     */
    private String getColumnDataType(String type) {
        switch (type.toUpperCase()) {
            case "BYTEA":
            case "BINARY":
            case "BINARY VARYING":
            case "BINARY LARGE OBJECT":
            case "VARBINARY":
                return VARBINARY;
            case "NUMERIC":
            case "DECIMAL":
                return DECIMAL;
            case "SMALLINT":
            case "INT":
            case "INT4":
            case "INT8":
            case "INTEGER":
            case "BIGINT":
                return INTEGER;
            case "FLOAT":
            case "FLOAT4":
            case "FLOAT8":
            case "REAL":
            case "DOUBLE":
            case "DOUBLE PRECISION":
                return DOUBLE;
            case "BIT":
            case "BOOL":
            case "BOOLEAN":
                return BOOLEAN;
            case "DATE":
                return DATE;
            case "TIME":
                return TIME;
            case "TIMESTAMP":
            case "DATETIME":
                return DATETIME;
        }
        return null;
    }

    /**
     * This method returns the header of the CSV.
     *
     * @param rsmd        metdata of the Result Set
     * @param columnCount the number of columns.
     * @return a String array with the headers.
     * @throws SQLException
     */
    private String[] getCSVHeader(final ResultSetMetaData rsmd, final int columnCount) throws SQLException {
        String[] headers = new String[columnCount];

        for (int i = 1; i <= columnCount; i++) {
            headers[i - 1] = rsmd.getColumnLabel(i);
            // Setting the empty header label at be.ugent.rml.access.RDBAccess.nullheader (as otherwise CSV parsers might fail),
            // (this header cannot be used by actual mapping files so this should actually not give any issues)
            //  and hope that this header will NEVER be encountered in real-world tables
            if (headers[i - 1] == null || headers[i - 1].equals("")) {
                headers[i - 1] = "be.ugent.rml.access.RDBAccess.nullheader";
            }
        }

        return headers;
    }

    /**
     * Convert a sequence of bytes to a string representation using uppercase hex symbols
     * @param bytes the bytes to convert
     * @return a string containing the hexadecimal representation of the byte array
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            // format: 0 flag for zero-padding, 2 character width, uppercase hexadecimal symbols
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    /**
     * Normalize the string representation of a data value given by the RDB.
     * @param data the string representation retrieved from the RDB of the data to be normalized.
     * @param dataType the intended datatype of the data parameter.
     * @return Normalized string representation of the data parameter, given the datatype.
     */
    private static String normalizeData(String data, String dataType) {
        if (DOUBLE.equals(dataType)) {
            // remove trailing decimal points (Quirk from MySQL, see issue 203)
            return data.replace(".0", "");
        }
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDBAccess) {
            RDBAccess access = (RDBAccess) o;

            return dsn.equals(access.getDSN())
                    && databaseType.equals(access.getDatabaseType())
                    && username.equals(access.getUsername())
                    && password.equals(access.getPassword())
                    && query.equals(access.getQuery())
                    && contentType.equals(access.getContentType());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getHashOfString(getDSN() + getDatabaseType() + getUsername() + getPassword() + getQuery() + getContentType());
    }

    /**
     * This method returns the DNS.
     *
     * @return the DNS.
     */
    public String getDSN() {
        return dsn;
    }

    /**
     * This method returns the database type.
     *
     * @return the database type.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * This method returns the username.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method returns the password.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method returns the SQL query.
     *
     * @return the SQL query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * This method returns the content type.
     *
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }
}

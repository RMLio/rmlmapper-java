package be.ugent.rml.access;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

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
    private String oracleJarPath;

    /**
     * This constructor takes as arguments the dsn, database, username, password, query, and content type.
     * @param dsn the data source name.
     * @param databaseType the database type.
     * @param username the username of the user that executes the query.
     * @param password the password of the above user.
     * @param query the SQL query to use.
     * @param contentType the content type of the results.
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

            // Turn the Results Set into a CSV stream.
            inputStream = getCSVInputStream(rs);

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
     * @return a map of column names and their datatypes.
     */
    @Override
    public Map<String, String> getDataTypes() {
        return datatypes;
    }

    /**
     * This method creates an CSV-formatted InputStream from a Result Set.
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
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(getCSVHeader(rsmd, columnCount)));
            printer.printRecords();

            // Extract data from result set
            while (rs.next()) {
                String[] csvRow = new String[columnCount];

                // Iterate over column names
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);

                    if (!filledInDataTypes) {
                        String dataType = getColumnDataType(rsmd.getColumnTypeName(i));

                        if (dataType != null) {
                            datatypes.put(columnName, dataType);
                        }
                    }

                    // Add value to CSV row.
                    csvRow[i - 1] = rs.getString(columnName);
                }

                // Add CSV row to CSVPrinter.
                // non-varargs call
                printer.printRecord((Object[]) csvRow);
                filledInDataTypes = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get InputStream from StringWriter.
        return new ByteArrayInputStream(writer.toString().getBytes());
    }

    /**
     * This method returns the corresponding datatype for a SQL datatype.
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
                return "http://www.w3.org/2001/XMLSchema#hexBinary";
            case "NUMERIC":
            case "DECIMAL":
                return "http://www.w3.org/2001/XMLSchema#decimal";
            case "SMALLINT":
            case "INT":
            case "INT4":
            case "INT8":
            case "INTEGER":
            case "BIGINT":
                return "http://www.w3.org/2001/XMLSchema#integer";
            case "FLOAT":
            case "FLOAT4":
            case "FLOAT8":
            case "REAL":
            case "DOUBLE":
            case "DOUBLE PRECISION":
                return "http://www.w3.org/2001/XMLSchema#double";
            case "BIT":
            case "BOOL":
            case "BOOLEAN":
                return "http://www.w3.org/2001/XMLSchema#boolean";
            case "DATE":
                return "http://www.w3.org/2001/XMLSchema#date";
            case "TIME":
                return "http://www.w3.org/2001/XMLSchema#time";
            case "TIMESTAMP":
            case "DATETIME":
                return "http://www.w3.org/2001/XMLSchema#dateTime";
        }
        return null;
    }

    /**
     * This method returns the header of the CSV.
     * @param rsmd metdata of the Result Set
     * @param columnCount the number of columns.
     * @return a String array with the headers.
     * @throws SQLException
     */
    private String[] getCSVHeader(final ResultSetMetaData rsmd, final int columnCount) throws SQLException {
        String[] headers = new String[columnCount];

        for (int i = 1; i <= columnCount; i++) {
            headers[i - 1] = rsmd.getColumnName(i);
        }

        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDBAccess) {
            RDBAccess access  = (RDBAccess) o;

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
     * @return the DNS.
     */
    public String getDSN() {
        return dsn;
    }

    /**
     * This method returns the database type.
     * @return the database type.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * This method returns the username.
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method returns the password.
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method returns the SQL query.
     * @return the SQL query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * This method returns the content type.
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }
}

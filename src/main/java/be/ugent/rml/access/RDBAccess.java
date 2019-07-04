package be.ugent.rml.access;

import be.ugent.rml.DatabaseType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the access to a relational database.
 */
public class RDBAccess implements Access {

    private String dsn;
    private DatabaseType.Database database;
    private String username;
    private String password;
    private String query;
    private String contentType;
    private Map<String, String> datatypes = new HashMap<>();

    /**
     * This constructor takes as arguments the dsn, database, username, password, query, and content type.
     * @param dsn: the data source name.
     * @param database: the database type.
     * @param username: the username of the user that executes the query.
     * @param password: the password of the above user.
     * @param query: the SQL query to use.
     * @param contentType: the content type of the results.
     */
    public RDBAccess(String dsn, DatabaseType.Database database, String username, String password, String query, String contentType) {
        this.dsn = dsn;
        this.database = database;
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
    public InputStream getInputStream() throws IOException {
        // JDBC objects
        Connection connection = null;
        Statement statement = null;
        String jdbcDriver = DatabaseType.getDriver(database);
        String jdbcDSN = "jdbc:" + database.toString() + "://" + dsn;
        InputStream inputStream = null;

        try {
            // Register JDBC driver
            Class.forName(jdbcDriver);

            // Open connection
            String connectionString = jdbcDSN;
            if (!connectionString.contains("user=")) {
                connectionString += "?user=" + username + "&password=" + password;
            }

            if (database == DatabaseType.Database.MYSQL) {
                connectionString += "&serverTimezone=UTC&useSSL=false";
            }

            if (database == DatabaseType.Database.SQL_SERVER) {
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
            sqlE.printStackTrace();
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
     * @param rs: the Result Set that is used.
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
                printer.printRecord(csvRow);
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
     * @param type: the SQL datatype.
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
     * @param rsmd: metdata of the Result Set
     * @param columnCount: the number of columns.
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
                    && database.equals(access.getDatabase())
                    && username.equals(access.getUsername())
                    && password.equals(access.getPassword())
                    && query.equals(access.getQuery())
                    && contentType.equals(access.getContentType());
        } else {
            return false;
        }
    }

    /**
     * This method returns the DNS.
     * @return
     */
    public String getDSN() {
        return dsn;
    }

    /**
     * This method returns the database type.
     * @return
     */
    public DatabaseType.Database getDatabase() {
        return database;
    }

    /**
     * This method returns the username.
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method returns the password.
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * This method returns the SQL query.
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * This method returns the content type.
     * @return
     */
    public String getContentType() {
        return contentType;
    }
}

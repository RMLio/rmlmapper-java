package be.ugent.rml.access;

import be.ugent.rml.DatabaseType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RDBAccess implements Access {

    private String dsn;
    private DatabaseType.Database database;
    private String username;
    private String password;
    private String query;
    private String contentType;
    private Map<String, String> datatypes = new HashMap<>();

    public RDBAccess(String dsn, DatabaseType.Database database, String username, String password, String query, String contentType) {
        this.dsn = dsn;
        this.database = database;
        this.username = username;
        this.password = password;
        this.query = query;
        this.contentType = contentType;
    }

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

    @Override
    public Map<String, String> getDataTypes() {
        return datatypes;
    }

    private InputStream getCSVInputStream(ResultSet rs) throws SQLException {
        // Get number of requested columns
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        boolean filledInDataTypes = false;
        StringBuilder csv = new StringBuilder();
        csv.append(getCSVHeader(rsmd, columnCount));

        // Extract data from result set
        while (rs.next()) {
            // Iterate over column names
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsmd.getColumnName(i);

                if (!filledInDataTypes) {
                    String dataType = getColumnDataType(rsmd.getColumnTypeName(i));

                    if (dataType != null) {
                        datatypes.put(columnName, dataType);
                    }
                }

                csv.append('"').append(rs.getString(columnName)).append('"');

                if (i != columnCount) {
                    csv.append(",");
                }
            }

            csv.append("\n");

            filledInDataTypes = true;
        }

        return new ByteArrayInputStream(csv.toString().getBytes());
    }

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

    private String getCSVHeader(final ResultSetMetaData rsmd, final int columnCount) throws SQLException {
        StringBuilder header = new StringBuilder();

        for (int i = 1; i <= columnCount; i++) {
            header.append(rsmd.getColumnName(i));

            if (i != columnCount) {
                header.append(",");
            }
        }

        header.append("\n");

        return header.toString();
    }

    // TODO implement
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}

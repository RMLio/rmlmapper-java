package be.ugent.rml.records;

import be.ugent.rml.DatabaseType;
import be.ugent.rml.NAMESPACES;

import javax.activation.UnsupportedDataTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.*;

public class RDBs  {

    /*
        This method adds the "jdbc:XXX://" prefix to the given dsn. This way the caller of this function doesn't need
        to take JDBC specific details into account.
     */
    public List<Record> get(String dsn, DatabaseType.Database database, String username, String password, String query,
                            String referenceFormulation) {
        // List containing generated records
        List<Record> records = new ArrayList<>();

        // JDBC objects
        Connection connection = null;
        Statement statement = null;
        String jdbcDriver = DatabaseType.getDriver(database);
        String jdbcDSN = "jdbc:" + database.toString() + "://" + dsn;

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

            switch(referenceFormulation) {
                case NAMESPACES.QL + "CSV":
                    records = getCSVRecords(rs);
                    break;
                case NAMESPACES.QL + "XPath":
                    records = getXMLRecords(rs);
                    break;
                default:
                    throw new Error("Unsupported rml:referenceFormulation for RDB source: " + referenceFormulation);
            }

            // Clean-up environment
            rs.close();
            statement.close();
            connection.close();

        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // finally block used to close resources
            try{
                if(statement != null) {
                    statement.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do

            try{
                if(connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return records;
    }


    private List<Record> getCSVRecords(ResultSet rs) throws SQLException {
        List<Record> records = new ArrayList<>();
        // Get number of requested columns
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        // Extract data from result set
        while(rs.next()){
            HashMap<String, List<String>> values = new HashMap<>();

            // Iterate over column names
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsmd.getColumnName(i);

                List<String> temp = new ArrayList<String>();
                temp.add(rs.getString(columnName));
                values.put(columnName, temp);
            }

            records.add(new CSVRecord(values));
        }
        return records;
    }

    private List<Record> getXMLRecords(ResultSet rs) throws SQLException {
        throw new Error("Unsupported rml:referenceFormulation for RDB source: XPath");
    }
}

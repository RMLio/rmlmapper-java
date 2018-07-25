package be.ugent.rml.records;

import be.ugent.rml.DatabaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.*;

public class RDBs  {

    /*
        This method adds the "jdbc:XXX://" prefix to the given dsn. This way the caller of this function doesn't need
        to take JDBC specific details into account.
     */
    public List<Record> get(String dsn, DatabaseType.Database database, String username, String password, String query) {
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
            System.out.println("CONNECTIONSTRINGS OF DATABASE TYPE " + database.toString() + " --> " + connectionString);
            connection = DriverManager.getConnection(connectionString);

            // Execute query
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

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

                records.add(new RDBsRecord(values));
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
}

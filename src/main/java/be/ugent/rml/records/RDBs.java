package be.ugent.rml.records;

import java.io.IOException;
import java.util.List;
import java.sql.*;

public class RDBs  {


    public List<Record> _get(String jdbcDSN, String jdbcDriver, String username, String password, String query) throws IOException {

        Connection connection = null;
        Statement statement = null;

        try {
            // Register JDBC driver
            Class.forName(jdbcDriver);

            // Open connection
            connection = DriverManager.getConnection(jdbcDSN, username, password);

            // Execute query
            System.out.println("Creating statement...");
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            // Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int id  = rs.getInt("id");
                int age = rs.getInt("age");
                String first = rs.getString("first");
                String last = rs.getString("last");

                //Display values
                System.out.print("ID: " + id);
                System.out.print(", Age: " + age);
                System.out.print(", First: " + first);
                System.out.println(", Last: " + last);
            }
            

            // Clean-up environment
            rs.close();
            statement.close();
            connection.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try{
                if(statement!=null)
                    statement.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(connection !=null)
                    connection.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }


        return null;
    }
}

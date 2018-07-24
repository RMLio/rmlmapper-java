package be.ugent.rml;

/*
    NOTE: Oracle is disabled because there are multiple drivers possible: http://www.orafaq.com/wiki/JDBC

 */

public class DatabaseType {

    public static final String MYSQL = "com.mysql.cj.jdbc.Driver";
    public static final String POSTGRES = "org.postgresql.Driver";
    public static final String SQL_SERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    //public static final String ORACLE = "oracle.jdbc.driver.OracleDrive";
    public static final String DB2 = "com.ibm.as400.access.AS400JDBCDriver";
    /*
    public static final String JAVA_DB = ;
    public static final String SYBASE = ;
    */

    /*
        Used to abstract as much as possible
        Name fields are used to create JDBC connection strings in @RDBs
     */
    public enum Database {
        MYSQL ("mysql"),
        POSTGRES ("postgresql"),
        SQL_SERVER ("sqlserver"),
        //ORACLE (""),
        DB2 ("as400");

        private final String name;

        private Database(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    /*
        Retrieves the Database enum type from a given (driver) string
     */
    public static Database getDBtype(String db) {
        String db_lower = db.toLowerCase();
        if (db_lower.contains("mysql")) {
            return Database.MYSQL;
        } else if (db_lower.contains("postgres")) {
            return Database.POSTGRES;
        } else if (db_lower.contains("sqlserver")) {
            return Database.SQL_SERVER;
        // } else if (db_lower.contains("oracle")) {
        //    return Database.ORACLE;
        } else if (db_lower.contains("ibm")) {
            return Database.DB2;
        } else {
            throw new Error("Couldn't find a driver for the given DB: " + db);
        }
    }

    /*
        Retrieves the JDBC driver URL from a given Database enum type
     */
    public static String getDriver(Database db) {
        switch(db) {
            case MYSQL:
                return MYSQL;

            case POSTGRES:
                return POSTGRES;

            case SQL_SERVER:
                return SQL_SERVER;

         //   case ORACLE:
         //       return ORACLE;

            case DB2:
                return DB2;
            default:
                throw new Error("Couldn't find a driver for the given DB: " + db);
        }
    }
}

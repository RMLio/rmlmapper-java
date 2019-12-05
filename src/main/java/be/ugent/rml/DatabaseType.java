package be.ugent.rml;

import java.util.Arrays;
import java.util.List;

/*
    NOTE: The Oracle driver has to be installed manually, because it's not on Maven due to licensing.
 */
public class DatabaseType {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    private static final String DB2_DRIVER = "com.ibm.as400.access.AS400JDBCDriver";

    /*
        jdbcPrefix fields are used to create JDBC connection strings in @RDBAccess
     */
    public enum Database {
        MYSQL ("MySQL",  "mysql", "mysql"),
        POSTGRES ("PostgreSQL", "postgresql", "postgres"),
        SQL_SERVER ("Microsoft SQL Server", "sqlserver", "sqlserver"),
        ORACLE ("Oracle", "oracle:thin", "oracle"),
        DB2 ("IBM DB2", "as400", "ibm");

        private final String name;
        private final String jdbcPrefix;
        private final String driverSubstring;

        private Database(String name, String jdbcPrefix, String driverSubstring) {
            this.name = name;
            this.jdbcPrefix = jdbcPrefix;
            this.driverSubstring = driverSubstring;
        }

        public String toString() {
            return this.name;
        }

        public String getJDBCPrefix() {
            return this.jdbcPrefix;
        }

        public String getDriverSubstring() {
            return this.driverSubstring;
        }
    }

    /*
        Retrieves the Database enum type from a given (driver) string
     */
    public static Database getDBtype(String db) {
        String dbLower = db.toLowerCase();
        List<Database> dbs = Arrays.asList(Database.values());

        int i = 0;

        while (i < dbs.size() && !dbLower.contains(dbs.get(i).getDriverSubstring())) {
            i ++;
        }

        if (i < dbs.size()) {
            return dbs.get(i);
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
                return MYSQL_DRIVER;

            case POSTGRES:
                return POSTGRESQL_DRIVER;

            case SQL_SERVER:
                return SQLSERVER_DRIVER;

            case ORACLE:
                return ORACLE_DRIVER;

            case DB2:
                return DB2_DRIVER;

            default:
                throw new Error("Couldn't find a driver for the given DB: " + db);
        }
    }
}

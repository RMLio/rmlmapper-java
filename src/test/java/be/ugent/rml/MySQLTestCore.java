package be.ugent.rml;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;


public abstract class MySQLTestCore extends DBTestCore {

    protected static String CONNECTIONSTRING = "jdbc:mysql://localhost:%d/test";

    protected static DB mysqlDB;

    @BeforeClass
    public static void startDBs() throws Exception {
        int PORTNUMBER_MYSQL;
        try {
            PORTNUMBER_MYSQL = Utils.getFreePortNumber();
        } catch (Exception ex) {
            throw new Error("Could not find a free port number for RDBs testing.");
        }

        CONNECTIONSTRING = String.format(CONNECTIONSTRING, PORTNUMBER_MYSQL);

        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(PORTNUMBER_MYSQL);
        configBuilder.addArg("--user=root");
        configBuilder.addArg("--sql-mode=STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION,ANSI_QUOTES");
        mysqlDB = DB.newEmbeddedDB(configBuilder.build());
        mysqlDB.start();
    }

    @AfterClass
    public static void stopDBs() throws ManagedProcessException {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }

        // Make sure all tempFiles are removed
        int counter = 0;
        while (!tempFiles.isEmpty()) {
            for (Iterator<String> i = tempFiles.iterator(); i.hasNext(); ) {
                try {
                    if (new File(i.next()).delete()) {
                        i.remove();
                    }
                } catch (Exception ex) {
                    counter++;
                    ex.printStackTrace();
                    // Prevent infinity loops
                    if (counter > 100) {
                        throw new Error("Could not remove all temp mapping files.");
                    }
                }
            }
        }
    }
}

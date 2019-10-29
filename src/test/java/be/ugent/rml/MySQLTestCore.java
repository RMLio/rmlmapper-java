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


public abstract class MySQLTestCore extends TestCore {

    protected static String CONNECTIONSTRING = "jdbc:mysql://localhost:%d/test";

    protected static HashSet<String> tempFiles = new HashSet<>();

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

    // Utils -----------------------------------------------------------------------------------------------------------

    protected static String replaceDSNInMappingFile(String path, String connectionString) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", connectionString);

            // Write to temp mapping file

            String fileName = Integer.toString(Math.abs(path.hashCode())) + "tempMapping.ttl";
            Path file = Paths.get(fileName);
            Files.write(file, Arrays.asList(mapping.split("\n")));

            String absolutePath = Paths.get(Utils.getFile(fileName, null).getAbsolutePath()).toString();
            tempFiles.add(absolutePath);

            return absolutePath;

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    protected static void deleteTempMappingFile(String absolutePath) {
        File file = new File(absolutePath);
        if (file.delete()) {
            tempFiles.remove(absolutePath);
        }
    }

}

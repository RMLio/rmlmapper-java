package be.ugent.rml;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Testcontainers
public abstract class DBTestCore extends TestCore {
    protected static Logger logger;
    protected static Set<String> tempFiles = new HashSet<>();

    // Testcontainers library uses SELF-typing, which will be removed in later versions. That's why <?>.
    // omitting <?> causes compiler to complain

    // This class has no information or way of knowing which specific JDBC container is required.
    // It is the child's responsibility to initialize this field in its constructor,
    // as only the child knows what container is required
    //protected JdbcDatabaseContainer<?> container;
    //protected String dbURL;

    protected final String USERNAME;
    protected final String PASSWORD;
    protected final String DOCKER_TAG;

    protected DBTestCore(String username, String password, String dockerTag) {
        this.USERNAME = username;
        this.PASSWORD = password;
        this.DOCKER_TAG = dockerTag;
    }

    @AfterAll
    public static void afterClass() {
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
                    logger.warn(ex.getMessage(), ex);
                    // Prevent infinity loops
                    if (counter > 100) {
                        throw new Error("Could not remove all temp mapping files.");
                    }
                }
            }
        }
    }

    protected String replaceDSNInMappingFile(String path) {
        try {
            // Read mapping file
            String mapping = Files.readString(Paths.get(Utils.getFile(path).getAbsolutePath()), StandardCharsets.UTF_8);

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", getDbURL());

            // Write to temp mapping file
            return writeMappingFile(mapping);
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    protected static String createTempMappingFile(String path) {
        try {
            // Read mapping file
            String mapping = Files.readString(Paths.get(Utils.getFile(path).getAbsolutePath()), StandardCharsets.UTF_8);

            // Write to temp mapping file
            return writeMappingFile(mapping);

        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    protected String CreateTempMappingFileAndReplaceDSN(String path) {
        try {
            // Read mapping file
            String mapping = Files.readString(Paths.get(Utils.getFile(path, null).getAbsolutePath()), StandardCharsets.UTF_8);

            // Replace "CONNECTIONDSN" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", getDbURL());

            // Write to temp mapping file
            return writeMappingFile(mapping);

        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    protected static void deleteTempMappingFile(String absolutePath) {
        File file = new File(absolutePath);

        if (file.delete()) {
            tempFiles.remove(absolutePath);
        }
    }

    private static String writeMappingFile(String mapping) {
        try {
            File tempFile = File.createTempFile("DBTest", ".ttl");
            tempFile.deleteOnExit();
            String tempFilePath = tempFile.getCanonicalPath();
            Files.writeString(tempFile.toPath(), mapping, StandardCharsets.UTF_8);
            tempFiles.add(tempFilePath);
            return tempFilePath;
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    protected void prepareDatabase(String path, String username, String password) {
        try (Connection conn = DriverManager.getConnection(getDbURL(), username, password)) {
            ScriptRunner runner = new ScriptRunner(conn);
            Reader reader = new BufferedReader(new FileReader(path));
            runner.setLogWriter(null); // ScriptRunner will output the contents of the SQL file to System.out by default

            runner.runScript(reader);
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String getDbURL();
}

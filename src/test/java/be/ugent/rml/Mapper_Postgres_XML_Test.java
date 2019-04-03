package be.ugent.rml;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;


@RunWith(Parameterized.class)
public class Mapper_Postgres_XML_Test extends TestCore {

    private static final Boolean LOCAL_TESTING = !Boolean.valueOf(System.getenv("CI"));

    private static Logger logger = LoggerFactory.getLogger(Mapper_Postgres_XML_Test.class);

    private static String CONNECTIONSTRING = LOCAL_TESTING ?
            "jdbc:postgresql://dia.test.iminds.be:8970/postgres?user=postgres&password=YourSTRONG!Passw0rd" :
            "jdbc:postgresql://postgres/postgres?user=postgres&password=YourSTRONG!Passw0rd";

    private static HashSet<String> tempFiles = new HashSet<>();

    private static DockerDBInfo remoteDB;

    private static class DockerDBInfo {
        String connectionString;
        String containerID;
        DockerClient docker;

        public DockerDBInfo(DockerClient docker, String connectionString, String containerID) {
            this.docker = docker;
            this.connectionString = connectionString;
            this.containerID = containerID;
        }

        DockerDBInfo(String connectionString) {
            this.connectionString = connectionString;
        }
    }

    @BeforeClass
    public static void startDBs() {
        remoteDB = new DockerDBInfo(CONNECTIONSTRING); // see .gitlab-ci.yml file
    }

    @AfterClass
    public static void stopDBs() {
        if (!LOCAL_TESTING) {
            closeDocker(remoteDB);
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

    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameter(1)
    public Class<? extends Exception> expectedException;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: Postgres_XML_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // scenarios:
//                {"RMLTC0000", null},
//                {"RMLTC0001a", null},
                {"RMLTC0001b", null},
//                {"RMLTC0002a", null},
//                {"RMLTC0002b", null},
//                {"RMLTC0002c", Error.class},
                {"RMLTC0002d", null},
//                {"RMLTC0002e", Error.class},
////                {"RMLTC0002f", null},
                {"RMLTC0002g", Error.class},
//                {"RMLTC0002h", null},
//                {"RMLTC0002i", Error.class},
//                {"RMLTC0002j", null},
//                {"RMLTC0003a", Error.class},
//                {"RMLTC0003b", null},
//                {"RMLTC0003c", null},
//                {"RMLTC0004a", null},
//                {"RMLTC0004b", null},
//                {"RMLTC0005a", null},
//                {"RMLTC0005b", null},
//                {"RMLTC0006a", null},
//                {"RMLTC0007a", null},
//                {"RMLTC0007b", null},
//                {"RMLTC0007c", null},
//                {"RMLTC0007d", null},
//                {"RMLTC0007e", null},
//                {"RMLTC0007f", null},
//                {"RMLTC0007g", null},
//                {"RMLTC0007h", null},
//                {"RMLTC0008a", null},
//                {"RMLTC0008b", null},
//                {"RMLTC0008c", null},
//                {"RMLTC0009a", null},
//                {"RMLTC0009b", null},
                {"RMLTC0009c", null},
//                See issue 102
//                {"RMLTC0009d", null},
//                {"RMLTC0010a", null},
//                {"RMLTC0010b", null},
//                {"RMLTC0010c", null},
////                {"RMLTC0011a", null},
//                {"RMLTC0011b", null},
//                {"RMLTC0012a", null},
//                {"RMLTC0012b", null},
//                {"RMLTC0012c", Error.class},
//                {"RMLTC0012d", Error.class},
//                {"RMLTC0012e", null},
////                {"RMLTC0013a", null},
                {"RMLTC0014d", null},
////                {"RMLTC0015a", null},
//                {"RMLTC0015b", Error.class},
//                {"RMLTC0016a", null},
////                {"RMLTC0016b", null},
//                {"RMLTC0016c", null},
//                {"RMLTC0016d", null},
////                {"RMLTC0016e", null},
//                {"RMLTC0018a", null},
                {"RMLTC0019a", null},
//                {"RMLTC0019b", null},
//                {"RMLTC0020a", null},
//                {"RMLTC0020b", null},
        });

    }

    @Test
    public void doMapping() throws Exception {

        //setup expected exception
        if (expectedException != null) {
            thrown.expect(expectedException);
        }

        mappingTest(testCaseName);
    }

    private void mappingTest(String testCaseName) throws Exception {

        String resourcePath = "test-cases/" + testCaseName + "-PostgreSQL-XML/resource.sql";
        String mappingPath = "./test-cases/" + testCaseName + "-PostgreSQL-XML/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-PostgreSQL-XML/output.nq";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING);

        // Execute SQL
        executeSQL(remoteDB.connectionString, resourcePath);

        // mapping
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    // Utils -----------------------------------------------------------------------------------------------------------

    private static String replaceDSNInMappingFile(String path, String connectionString) {
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

    private static void deleteTempMappingFile(String absolutePath) {
        File file = new File(absolutePath);
        if (file.delete()) {
            tempFiles.remove(absolutePath);
        }
    }

    private static void closeDocker(DockerDBInfo dockerDBInfo) {
        if (dockerDBInfo != null && dockerDBInfo.docker != null) {
            try {
                // Kill container
                dockerDBInfo.docker.killContainer(dockerDBInfo.containerID);

                // Remove container
                dockerDBInfo.docker.removeContainer(dockerDBInfo.containerID);

                // Close the docker client
                dockerDBInfo.docker.close();
            } catch (DockerException | InterruptedException ex) {
                logger.warn("Could not kill the database container with connection string: " + dockerDBInfo.connectionString + "!", ex);
            }
        }
    }

    private static void executeSQL(String connectionString, String sqlFile) throws Exception {
        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(sqlFile, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(connectionString);
        conn.createStatement().execute(sql);
        conn.close();
    }
}

package be.ugent.rml;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

import com.spotify.docker.client.messages.*;
import org.junit.*;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;
import org.junit.runner.RunWith;
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
import java.sql.SQLException;
import java.util.*;


@RunWith(ZohhakRunner.class)
public class Mapper_RDBs_Test extends TestCore {

    // Change this if needed
    private static final Boolean LOCAL_TESTING = false;

    private static Logger logger = LoggerFactory.getLogger(Mapper_RDBs_Test.class);


    private static final String DOCKER_HOST = "unix:///var/run/docker.sock";

    private static final int PORTNUMBER_MYSQL = 50789;
    private static final int PORTNUMBER_POSTGRESQL = 5432;
    private static final int PORTNUMBER_SQLSERVER = 1433;

    private static final String CONNECTIONSTRING_POSTGRESQL_LOCAL = String.format("jdbc:postgresql://0.0.0.0:%d/postgres?user=postgres", PORTNUMBER_POSTGRESQL);
    private static final String CONNECTIONSTRING_SQLSERVER_LOCAL = "jdbc:sqlserver://localhost;user=sa;password=YourSTRONG!Passw0rd;databaseName=TestDB;";

    private static final String CONNECTIONSTRING_MYSQL = String.format("jdbc:mysql://localhost:%d/test", PORTNUMBER_MYSQL);
    private static final String CONNECTIONSTRING_POSTGRESQL = "jdbc:postgresql://postgres/postgres?user=postgres";
    private static final String CONNECTIONSTRING_SQLSERVER = "jdbc:sqlserver://sqlserver;user=sa;password=YourSTRONG!Passw0rd;databaseName=TestDB;";

    private static HashSet<String> tempFiles = new HashSet<>();

    private static DB mysqlDB;
    private static DockerDBInfo postgreSQLDB;
    private static DockerDBInfo sqlServerDB;

    private static class DockerDBInfo {
        protected String connectionString;
        protected String containerID;
        protected DockerClient docker;

        public DockerDBInfo(DockerClient docker, String connectionString, String containerID) {
            this.docker = docker;
            this.connectionString = connectionString;
            this.containerID = containerID;
        }

        public DockerDBInfo(String connectionString) {
            this.connectionString = connectionString;
        }
    }

    @BeforeClass
    public static void startDBs() throws Exception {
        startMySQLDB();

        if (LOCAL_TESTING) {
            startPostgreSQLLocal();
            //startSQLServerLocal();
        } else {
            startPostgreSQL();
            startSQLServer();
        }
    }

    @AfterClass
    public static void stopDBs() throws ManagedProcessException {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }
        closeDocker(postgreSQLDB);
        closeDocker(sqlServerDB);


        // Make sure all tempFiles are removed
        int counter = 0;
        while (!tempFiles.isEmpty()) {
            for (Iterator<String> i = tempFiles.iterator(); i.hasNext();) {
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
                logger.warn("Could not kill the database container with connection string: " + dockerDBInfo.connectionString + "!");
                ex.printStackTrace();
            }
        }
    }

    // MySQL -----------------------------------------------------------------------------------------------------------

    private static void startMySQLDB() throws Exception {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(PORTNUMBER_MYSQL);
        configBuilder.addArg("--user=root");
        mysqlDB = DB.newEmbeddedDB(configBuilder.build());
        mysqlDB.start();
    }

    @Test
    public void evaluate_0000_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0000-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0000-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0000-MySQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }


    @Test
    public void evaluate_0001a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001a-MySQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0001b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0002a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0002b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0002g_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002g-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0002h_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002h-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002i-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0002j_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002j-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002j-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002j-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0003b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0003c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003c-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0004a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0004b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0006a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0006a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0006a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0006a-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007b-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007c-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007d_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007d-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007d-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007d-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007e_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007e-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007f_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007f-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007f-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007f-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007g_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007g-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0007h_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007h-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0008a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008a-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0008b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0008c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008c-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0009a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0009a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0009a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0009b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0009b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0009b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009b-MySQL/output.nq";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);
    }

    @Test
    public void evaluate_0010a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0010b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0010c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010c-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0011b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0011b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0011b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0011b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0012a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012a-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    @Test
    public void evaluate_0012b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012b-MySQL/output.ttl";
        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_MYSQL, CONNECTIONSTRING_MYSQL);
        mysqlDB.source(resourcePath);
        doMapping(tempMappingPath, outputPath);
        deleteTempMappingFile(tempMappingPath);

    }

    // PostgreSQL ------------------------------------------------------------------------------------------------------

    private static void startPostgreSQL() {
        postgreSQLDB = new DockerDBInfo(CONNECTIONSTRING_POSTGRESQL); // see .gitlab-ci.yml file
    }

    private static void startPostgreSQLLocal() {
        postgreSQLDB = new DockerDBInfo(CONNECTIONSTRING_POSTGRESQL_LOCAL);

        final String address = "0.0.0.0";
        final String exportedPort = "5432";
        final String image = "postgres:latest";

        // Map exported port to our static PORTNUMBER_POSTGRESQL
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> staticPorts = new ArrayList<>();
        staticPorts.add(PortBinding.create(address, Integer.toString(PORTNUMBER_POSTGRESQL)));
        portBindings.put(exportedPort, staticPorts);

        final HostConfig hostConfig = HostConfig.builder()
                .portBindings(portBindings)
                .build();

        final ContainerConfig config = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(image).exposedPorts(exportedPort)
                .build();

        startDockerContainer(image, config, postgreSQLDB);
    }

    @TestWith({
            "RMLTC0000-PostgreSQL, ttl",
            "RMLTC0001a-PostgreSQL, ttl",
            "RMLTC0001b-PostgreSQL, ttl",
            "RMLTC0002a-PostgreSQL, ttl",
            "RMLTC0002b-PostgreSQL, ttl",
            "RMLTC0002g-PostgreSQL, ttl",
            "RMLTC0002h-PostgreSQL, ttl",
            "RMLTC0002j-PostgreSQL, ttl",
            "RMLTC0003b-PostgreSQL, ttl",
            "RMLTC0003c-PostgreSQL, ttl",
            "RMLTC0004a-PostgreSQL, ttl",
            "RMLTC0004b-PostgreSQL, ttl",
            "RMLTC0006a-PostgreSQL, nq",
            "RMLTC0007a-PostgreSQL, ttl",
            "RMLTC0007b-PostgreSQL, nq",
            "RMLTC0007c-PostgreSQL, ttl",
            "RMLTC0007d-PostgreSQL, ttl",
            "RMLTC0007e-PostgreSQL, nq",
            "RMLTC0007f-PostgreSQL, nq",
            "RMLTC0007g-PostgreSQL, ttl",
            "RMLTC0007h-PostgreSQL, nq",
            "RMLTC0008a-PostgreSQL, nq",
            "RMLTC0008b-PostgreSQL, ttl",
            "RMLTC0008c-PostgreSQL, ttl",
            "RMLTC0009a-PostgreSQL, ttl",
            "RMLTC0009b-PostgreSQL, nq",
            "RMLTC0010a-PostgreSQL, ttl",
            "RMLTC0010b-PostgreSQL, ttl",
            "RMLTC0010c-PostgreSQL, ttl",
            "RMLTC0011b-PostgreSQL, ttl",
            "RMLTC0012a-PostgreSQL, ttl",
            "RMLTC0012b-PostgreSQL, ttl"})
    public void evaluate_XXXX_RDBs_PostgreSQL(String resourceDir, String outputExtension) throws Exception {
        String resourcePath = "test-cases/" + resourceDir + "/resource.sql";
        String mappingPath = "./test-cases/" + resourceDir + "/mapping.ttl";
        String outputPath = "test-cases/" + resourceDir + "/output." + outputExtension;

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_POSTGRESQL, CONNECTIONSTRING_POSTGRESQL_LOCAL);

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(tempMappingPath, outputPath);

        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002c-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-PostgreSQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_POSTGRESQL, CONNECTIONSTRING_POSTGRESQL_LOCAL);

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(tempMappingPath, outputPath);

        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002e-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-PostgreSQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_POSTGRESQL, CONNECTIONSTRING_POSTGRESQL_LOCAL);

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(tempMappingPath, outputPath);

        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002i-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-PostgreSQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_POSTGRESQL, CONNECTIONSTRING_POSTGRESQL_LOCAL);

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(tempMappingPath, outputPath);

        deleteTempMappingFile(tempMappingPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0003a-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-PostgreSQL/output.ttl";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_POSTGRESQL, CONNECTIONSTRING_POSTGRESQL_LOCAL);

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(tempMappingPath, outputPath);

        deleteTempMappingFile(tempMappingPath);
    }


    // SQL Server ------------------------------------------------------------------------------------------------------

    private static void startSQLServer() {
        sqlServerDB = new DockerDBInfo(CONNECTIONSTRING_SQLSERVER);
        createSQLServerTestDB();
    }

    private static void createSQLServerTestDB() {
        // Creates testing db
        try {
            // Can't set DB yet in connection string --> remove here
            final Connection conn = DriverManager.getConnection(sqlServerDB.connectionString.substring(0, sqlServerDB.connectionString.lastIndexOf("databaseName=")));
            conn.createStatement().execute("CREATE DATABASE TestDB");
            conn.close();
        } catch (SQLException ex) {
            // Doesn't matter
        }
    }
    // TODO: fix this
    private static void startSQLServerLocal()  {
        sqlServerDB = new DockerDBInfo(CONNECTIONSTRING_SQLSERVER_LOCAL);

        final String address = "0.0.0.0";
        final String exportedPort = "1433";
        final String image = "microsoft/mssql-server-linux:latest";

        // Map exported port to our static PORTNUMBER_SQLSERVER
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        List<PortBinding> staticPorts = new ArrayList<>();
        staticPorts.add(PortBinding.create(address, Integer.toString(PORTNUMBER_SQLSERVER)));
        portBindings.put(exportedPort, staticPorts);

        final HostConfig hostConfig = HostConfig.builder()
                .portBindings(portBindings)
                .build();

        final ContainerConfig config = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(image).exposedPorts(exportedPort)
                .env("ACCEPT_EULA=Y")
                .env("SA_PASSWORD=YourStrong!Passw0rd")
                .build();

        startDockerContainer(image, config, sqlServerDB);
        createSQLServerTestDB();
    }

    @TestWith({
            "RMLTC0000-SQLServer, ttl",
            "RMLTC0001a-SQLServer, ttl",
            "RMLTC0001b-SQLServer, ttl",
            "RMLTC0002a-SQLServer, ttl",
            "RMLTC0002b-SQLServer, ttl",
            "RMLTC0002g-SQLServer, ttl",
            "RMLTC0002h-SQLServer, ttl",
            "RMLTC0002j-SQLServer, ttl",
            "RMLTC0003b-SQLServer, ttl",
            "RMLTC0003c-SQLServer, ttl",
            "RMLTC0004a-SQLServer, ttl",
            "RMLTC0004b-SQLServer, ttl",
            "RMLTC0006a-SQLServer, nq",
            "RMLTC0007a-SQLServer, ttl",
            "RMLTC0007b-SQLServer, nq",
            "RMLTC0007c-SQLServer, ttl",
            "RMLTC0007d-SQLServer, ttl",
            "RMLTC0007e-SQLServer, nq",
            "RMLTC0007f-SQLServer, nq",
            "RMLTC0007g-SQLServer, ttl",
            "RMLTC0007h-SQLServer, nq",
            "RMLTC0008a-SQLServer, nq",
            "RMLTC0008b-SQLServer, ttl",
            "RMLTC0008c-SQLServer, ttl",
            "RMLTC0009a-SQLServer, ttl",
            "RMLTC0009b-SQLServer, nq",
            "RMLTC0010a-SQLServer, ttl",
            "RMLTC0010b-SQLServer, ttl",
            "RMLTC0010c-SQLServer, ttl",
            "RMLTC0011b-SQLServer, ttl",
            "RMLTC0012a-SQLServer, ttl",
            "RMLTC0012b-SQLServer, ttl"
    })
    public void evaluate_XXXX_RDBs_SQLServer(String resourceDir, String outputExtension) throws Exception {
        if (!LOCAL_TESTING) {
            String resourcePath = "test-cases/" + resourceDir + "/resource.sql";
            String mappingPath = "./test-cases/" + resourceDir + "/mapping.ttl";
            String outputPath = "test-cases/" + resourceDir + "/output." + outputExtension;

            executeSQL(sqlServerDB.connectionString, resourcePath);

            String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_SQLSERVER, CONNECTIONSTRING_SQLSERVER_LOCAL);

            doMapping(tempMappingPath, outputPath);

            deleteTempMappingFile(tempMappingPath);
        }
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_SQLServer() throws Exception {
        if (!LOCAL_TESTING) {
            String resourcePath = "test-cases/RMLTC0002c-SQLServer/resource.sql";
            String mappingPath = "./test-cases/RMLTC0002c-SQLServer/mapping.ttl";
            String outputPath = "test-cases/RMLTC0002c-SQLServer/output.ttl";

            executeSQL(sqlServerDB.connectionString, resourcePath);

            String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_SQLSERVER, CONNECTIONSTRING_SQLSERVER_LOCAL);

            doMapping(tempMappingPath, outputPath);

            deleteTempMappingFile(tempMappingPath);
        } else {
            throw new Error();
        }
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_SQLServer() throws Exception {
        if (!LOCAL_TESTING) {
            String resourcePath = "test-cases/RMLTC0002e-SQLServer/resource.sql";
            String mappingPath = "./test-cases/RMLTC0002e-SQLServer/mapping.ttl";
            String outputPath = "test-cases/RMLTC0002e-SQLServer/output.ttl";

            executeSQL(sqlServerDB.connectionString, resourcePath);

            String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_SQLSERVER, CONNECTIONSTRING_SQLSERVER_LOCAL);

            doMapping(tempMappingPath, outputPath);

            deleteTempMappingFile(tempMappingPath);
        } else {
            throw new Error();
        }
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_SQLServer() throws Exception {
        if (!LOCAL_TESTING) {
            String resourcePath = "test-cases/RMLTC0002i-SQLServer/resource.sql";
            String mappingPath = "./test-cases/RMLTC0002i-SQLServer/mapping.ttl";
            String outputPath = "test-cases/RMLTC0002i-SQLServer/output.ttl";

            executeSQL(sqlServerDB.connectionString, resourcePath);

            String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_SQLSERVER, CONNECTIONSTRING_SQLSERVER_LOCAL);

            doMapping(tempMappingPath, outputPath);

            deleteTempMappingFile(tempMappingPath);
        } else {
            throw new Error();
        }
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_SQLServer() throws Exception {
        if (!LOCAL_TESTING) {
            String resourcePath = "test-cases/RMLTC0003a-SQLServer/resource.sql";
            String mappingPath = "./test-cases/RMLTC0003a-SQLServer/mapping.ttl";
            String outputPath = "test-cases/RMLTC0003a-SQLServer/output.ttl";

            executeSQL(sqlServerDB.connectionString, resourcePath);

            String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING_SQLSERVER, CONNECTIONSTRING_SQLSERVER_LOCAL);

            doMapping(tempMappingPath, outputPath);

            deleteTempMappingFile(tempMappingPath);
        } else {
            throw new Error();
        }
    }



    // Utils -----------------------------------------------------------------------------------------------------------

    private static void startDockerContainer(String image, ContainerConfig containerConfig, DockerDBInfo dockerDBInfo) {
        try {
            final DockerClient docker = new DefaultDockerClient(DOCKER_HOST);

            docker.pull(image);

            final ContainerCreation creation = docker.createContainer(containerConfig);
            final String id = creation.id();

            // Container is now created, let's start it up
            docker.startContainer(id);

            // startContainer swallows errors, so check if the container is in the running state
            final ContainerInfo info = docker.inspectContainer(id);
            if (!info.state().running()) {
                throw new IllegalStateException("Could not start the container of: " + image);
            }

            dockerDBInfo.docker = docker;
            dockerDBInfo.containerID = id;

            // It takes a while for the application to start up inside the container. Time limit: 10 seconds
            Connection conn = null;
            int tries = 1;
            while (conn == null && tries <= 20) {
                try {
                    conn = DriverManager.getConnection(dockerDBInfo.connectionString);
                    conn.close();
                } catch (SQLException ignored) {
                    logger.debug("Retrying ({}/20) with connection string: {}", tries, dockerDBInfo.connectionString);
                    tries++;
                    Thread.sleep(500);
                }
            }
            if (tries > 20) {
                throw new SQLException("Could not connect to the container of: " + image);
            }
        } catch (InterruptedException | DockerException | SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSQL(String connectionString, String sqlFile)  throws Exception {
        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(sqlFile, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        String[] statements = sql.split(";");
        final Connection conn = DriverManager.getConnection(connectionString);
        for (String statement: statements) {
            conn.createStatement().execute(statement + ";");
        }
        conn.close();
    }

    private static String replaceDSNInMappingFile(String path, String connectionString, String connectionStringLocal) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            String dsn = LOCAL_TESTING ? connectionStringLocal: connectionString;

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", dsn);

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

}

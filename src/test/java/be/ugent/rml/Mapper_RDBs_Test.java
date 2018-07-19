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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(ZohhakRunner.class)
public class Mapper_RDBs_Test extends TestCore {

    private static Logger logger = LoggerFactory.getLogger(Mapper_RDBs_Test.class);

    private static final int PORTNUMBER_MYSQL = 50898;
    private static final int PORTNUMBER_POSTGRESQL = 50897;

    private static DB mysqlDB;
    private static PostgreSQLDB postgreSQLDB;

    @BeforeClass
    public static void startDBs() throws Exception {
        startMySQLDB();
    }

    @AfterClass
    public static void stopDBs() throws ManagedProcessException {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }
        if (postgreSQLDB != null) {
            try {
                // Kill container
                postgreSQLDB.docker.killContainer(postgreSQLDB.containerID);

                // Remove container
                postgreSQLDB.docker.removeContainer(postgreSQLDB.containerID);

                // Close the docker client
                postgreSQLDB.docker.close();
            } catch (DockerException | InterruptedException ex) {
                logger.warn("Could not kill the PostgreSQL container!");
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
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }


    @Test
    public void evaluate_0001a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0001b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0002a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0002b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0002g_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002g-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0002h_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002h-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002i-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0002j_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002j-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002j-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002j-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0003b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0003c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003c-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0004a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0004b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0006a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0006a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0006a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0006a-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007b-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007c-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007d_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007d-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007d-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007d-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007e_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007e-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007f_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007f-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007f-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007f-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007g_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007g-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0007h_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007h-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0008a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008a-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0008b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0008c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008c-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0009a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0009a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0009a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0009b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0009b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0009b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009b-MySQL/output.nq";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
    }

    @Test
    public void evaluate_0010a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0010b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0010c_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010c-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0011b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0011b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0011b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0011b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0012a_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012a-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);

    }

    @Test
    public void evaluate_0012b_RDBs_MySQL() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012b-MySQL/output.ttl";
        mysqlDB.source(resourcePath);
        doMapping(mappingPath, outputPath);
        
    }

    // PostgreSQL ------------------------------------------------------------------------------------------------------

    private static class PostgreSQLDB {
        protected String connectionString;
        protected String containerID;
        protected DockerClient docker;

        public PostgreSQLDB(DockerClient docker, String connectionString, String containerID) {
            this.docker = docker;
            this.connectionString = connectionString;
            this.containerID = containerID;
        }
    }

    /*
        USED FOR LOCAL TESTING
        Change   d2rq:jdbcDSN "jdbc:postgresql://postgres:50897/postgres"; to   d2rq:jdbcDSN "jdbc:postgresql://localhost:50897/postgres";
        in the mapping files before executing
        -----
        Start postgres docker container and check connection
        https://github.com/spotify/docker-client
     */
    private static void startPostgreSQLLocal() throws SQLException {
        final String address = "0.0.0.0";
        final String dockerHost = "unix:///var/run/docker.sock";
        final String exportedPort = "5432";
        final String postgresImage = "postgres:10.4";

        try {
            final DockerClient docker = new DefaultDockerClient(dockerHost);

            docker.pull(postgresImage);

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
                    .image(postgresImage).exposedPorts(exportedPort)
                    .build();
            final ContainerCreation creation = docker.createContainer(config);
            final String id = creation.id();

            // Container is now created, let's start it up
            docker.startContainer(id);

            // startContainer swallows errors, so check if the container is in the running state
            final ContainerInfo info = docker.inspectContainer(id);
            if (!info.state().running()) {
                throw new IllegalStateException("Could not start Postgres container");
            }

            // We need to build the connection string to connect to Postgres
            // Find the random port in the network settings
            final String connectionString = String.format("jdbc:postgresql://%s:%d/postgres?user=postgres", address, PORTNUMBER_POSTGRESQL);

            postgreSQLDB = new PostgreSQLDB(docker, connectionString, id);

            // It takes a while for the Postgres application to start up inside the container. Time limit: 10 seconds
            Connection conn = null;
            int tries = 1;
            while (conn == null && tries <= 20) {
                try {
                    conn = DriverManager.getConnection(connectionString);
                    conn.close();
                } catch (SQLException ignored) {
                    logger.debug("Retrying ({}/20)...", tries);
                    tries++;
                    Thread.sleep(500);
                }
            }
            if (tries > 20) {
                throw new SQLException("Could not connect to Postgres container");
            }
        } catch (InterruptedException | DockerException e) {
            e.printStackTrace();
        }
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

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002c-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-PostgreSQL/output.ttl";

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002e-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-PostgreSQL/output.ttl";

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0002i-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-PostgreSQL/output.ttl";

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_PostgreSQL() throws Exception {
        String resourcePath = "test-cases/RMLTC0003a-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-PostgreSQL/output.ttl";

        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(resourcePath, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(postgreSQLDB.connectionString);
        conn.createStatement().execute(sql);
        conn.close();

        doMapping(mappingPath, outputPath);
    }
}

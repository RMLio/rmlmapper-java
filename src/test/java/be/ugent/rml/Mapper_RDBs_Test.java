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
    // Change this if needed
    private static final Boolean LOCAL_TESTING = false;

    private static Logger logger = LoggerFactory.getLogger(Mapper_RDBs_Test.class);


    private static final String DOCKER_HOST = "unix:///var/run/docker.sock";

    private static final int PORTNUMBER_MYSQL = 50898;
    private static final int PORTNUMBER_POSTGRESQL = 5432;
    private static final int PORTNUMBER_SQLSERVER = 50899;

    private static final String CONNECTIONSTRING_POSTGRESQL_LOCAL = String.format("jdbc:postgresql://0.0.0.0:%d/postgres?user=postgres", PORTNUMBER_POSTGRESQL);
    private static final String CONNECTIONSTRING_SQLSERVER_LOCAL = "jdbc:sqlserver://localhost;databaseName=TestDB;user=sa;password=$uP3RC0mpl3Xp@$$w0rD!;";

    private static final String CONNECTIONSTRING_POSTGRESQL = "jdbc:postgresql://postgres/postgres?user=postgres";
    private static final String CONNECTIONSTRING_SQLSERVER = "jdbc:sqlserver://sqlserver;user=sa;password=YourSTRONG!Passw0rd;";


    private static DB mysqlDB;
    private static DockerDBInfo postgreSQLDB = new DockerDBInfo(CONNECTIONSTRING_POSTGRESQL_LOCAL);
    private static DockerDBInfo sqlServerDB = new DockerDBInfo();

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
            startSQLServerLocal();
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
    }

    private static void closeDocker(DockerDBInfo dockerDBInfo) {
        if (dockerDBInfo.docker != null) {
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

    private static void startPostgreSQL() {
        postgreSQLDB = new DockerDBInfo(CONNECTIONSTRING_POSTGRESQL); // see .gitlab-ci.yml file
    }

    /*
      USED FOR LOCAL TESTING
      Change    d2rq:jdbcDSN "jdbc:postgresql://postgres/postgres"; to   d2rq:jdbcDSN "jdbc:postgresql://localhost:5432/postgres";
      in the mapping files before executing
  */
    private static void startPostgreSQLLocal() {
        final String address = "0.0.0.0";
        final String exportedPort = "5432";
        final String image = "postgres:10.4";

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

    // SQL Server ------------------------------------------------------------------------------------------------------

    private static void startSQLServer() {
        sqlServerDB = new DockerDBInfo(CONNECTIONSTRING_SQLSERVER);
        // Creates testing db
        try {
            final Connection conn = DriverManager.getConnection(sqlServerDB.connectionString);
            conn.createStatement().execute("CREATE DATABASE TestDB");
            conn.close();
        } catch (SQLException ex) {
            // Doesn't matter
        }
    }

    /*
     USED FOR LOCAL TESTING
     Change     jdbc:sqlserver://localhost;databaseName=TestDB;   to   d2rq:jdbcDSN "jdbc:sqlserver://sqlserver;databaseName=TestDB";
     in the mapping files before executing
 */
    private static void startSQLServerLocal()  {
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

        final Map<String, String> configLabels = new HashMap<>();
        configLabels.put("ACCEPT_EULA", "Y");
        configLabels.put("SA_PASSWORD", "$uP3RC0mpl3Xp@$$w0rD!");

        final ContainerConfig config = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .labels(configLabels)
                .image(image).exposedPorts(exportedPort)
                .build();

        startDockerContainer(image, config, sqlServerDB);
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
        String resourcePath = "test-cases/" + resourceDir + "/resource.sql";
        String mappingPath = "./test-cases/" + resourceDir + "/mapping.ttl";
        String outputPath = "test-cases/" + resourceDir + "/output." + outputExtension;

        executeSQL(sqlServerDB.connectionString, resourcePath);

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs_SQLServer() throws Exception {
        String resourcePath = "test-cases/RMLTC0002c-SQLServer/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-SQLServer/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-SQLServer/output.ttl";

        executeSQL(sqlServerDB.connectionString, resourcePath);

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs_SQLServer() throws Exception {
        String resourcePath = "test-cases/RMLTC0002e-SQLServer/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-SQLServer/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-SQLServer/output.ttl";

        executeSQL(sqlServerDB.connectionString, resourcePath);

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs_SQLServer() throws Exception {
        String resourcePath = "test-cases/RMLTC0002i-SQLServer/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-SQLServer/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-SQLServer/output.ttl";

        executeSQL(sqlServerDB.connectionString, resourcePath);

        doMapping(mappingPath, outputPath);
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs_SQLServer() throws Exception {
        String resourcePath = "test-cases/RMLTC0003a-SQLServer/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-SQLServer/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-SQLServer/output.ttl";

        executeSQL(sqlServerDB.connectionString, resourcePath);

        doMapping(mappingPath, outputPath);
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
                    logger.debug("Retrying ({}/20)...", tries);
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
}

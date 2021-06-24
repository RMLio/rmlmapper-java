package be.ugent.rml;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;

public abstract class PostgresTestCore extends DBTestCore {

    protected static Logger logger;

    protected static final Boolean LOCAL_TESTING = !Boolean.valueOf(System.getenv("CI"));

    protected static String CONNECTIONSTRING = LOCAL_TESTING ?
            "jdbc:postgresql://dia.test.iminds.be:8970/postgres?user=postgres&password=YourSTRONG!Passw0rd" :
            "jdbc:postgresql://postgres/postgres?user=postgres&password=YourSTRONG!Passw0rd";

    protected static DockerDBInfo remoteDB;

    protected static class DockerDBInfo {
        String connectionString;
        String containerID;
        DockerClient docker;

        public DockerDBInfo(DockerClient docker, String connectionString, String containerID) {
            this.docker = docker;
            this.connectionString = connectionString;
            this.containerID = containerID;
        }

        public DockerDBInfo(String connectionString) {
            this(null, connectionString, null);
        }
    }

    protected static void startDBs() {
        remoteDB = new DockerDBInfo(CONNECTIONSTRING); // see .gitlab-ci.yml file
    }

    protected static void stopDBs() {
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


    // Utils -----------------------------------------------------------------------------------------------------------

    protected static void closeDocker(DockerDBInfo dockerDBInfo) {
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

    protected static void executeSQL(String connectionString, String sqlFile) throws Exception {
        // Execute SQL
        String sql = new String(Files.readAllBytes(Paths.get(Utils.getFile(sqlFile, null).getAbsolutePath())), StandardCharsets.UTF_8);
        sql = sql.replaceAll("\n", "");
        final Connection conn = DriverManager.getConnection(connectionString);
        conn.createStatement().execute(sql);
        conn.close();
    }
}

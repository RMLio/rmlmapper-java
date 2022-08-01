package be.ugent.rml;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class MySQLTestCore extends DBTestCore {
    protected MySQLTestCore(String tag) {
        super("root", "", tag);
        super.container = new MySQLContainer<>(DockerImageName.parse(tag))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withEnv("allowPublicKeyRetrieval", "true")
                .withEnv("useSSL", "false")
                .withEnv("runID", Integer.toString(this.hashCode()))
                .withConfigurationOverride("mysql_override");

        super.container.start();
        super.dbURL = super.container.getJdbcUrl();
    }

    protected MySQLTestCore() {
        this("mysql:latest");
    }
}

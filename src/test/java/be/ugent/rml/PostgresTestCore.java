package be.ugent.rml;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresTestCore extends DBTestCore {
    public PostgresTestCore() {
        this("postgres:latest");
    }

    public PostgresTestCore(String tag) {
        super("postgres", "", tag);
        super.container = new PostgreSQLContainer<>(DockerImageName.parse(tag))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
                .withEnv("runID", Integer.toString(this.hashCode())) // to start a different container for each run
                .withDatabaseName("test");

        super.container.start();
        super.dbURL = super.container.getJdbcUrl();
    }
}

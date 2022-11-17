package be.ugent.rml;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresTestCore extends DBTestCore {

    // will be shared between test methods, i.e., one instance
    @Container
    private static PostgreSQLContainer<?> container;

    public PostgresTestCore() {
        this("postgres:latest");
    }

    public PostgresTestCore(String tag) {
        super("postgres", "", tag);
        container = new PostgreSQLContainer<>(DockerImageName.parse(tag))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
                .withEnv("runID", Integer.toString(this.hashCode())) // to start a different container for each run
                .withDatabaseName("test");
    }

    @Override
    protected String getDbURL() {
        return container.getJdbcUrl();
    }
}

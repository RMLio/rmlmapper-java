package be.ugent.rml;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class MySQLTestCore extends DBTestCore {

    // will be shared between test methods, i.e., one instance
    @Container
    protected static MySQLContainer<?> container;

    protected MySQLTestCore(String tag) {
        super("root", "", tag);
        container = new MySQLContainer<>(DockerImageName.parse(tag))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withEnv("allowPublicKeyRetrieval", "true")
                .withEnv("useSSL", "false")
                .withEnv("runID", Integer.toString(this.hashCode()));
    }

    protected MySQLTestCore() {
        this("mysql:8.2");
    }

    @Override
    protected String getDbURL() {
        return container.getJdbcUrl();
    }
}

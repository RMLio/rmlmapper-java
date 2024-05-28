package be.ugent.rml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SourceNotPresentTest extends TestFunctionCore{


    @Test
    public void evaluate_1035_CSV() throws Exception {
        Executor executor = this.createExecutor("./test-cases/RMLTC1035-CSV/mapping.ttl");
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url = classLoader.getResource("./test-cases/RMLTC1035-CSV");
        assert url != null;
        assertThrows(IOException.class,() -> executor.verifySources(url.getPath(), url.getPath()));
    }

    @Test
    public void evaluate_1036_CSV() throws Exception {
        Executor executor = this.createExecutor("./test-cases/RMLTC1036-CSV/mapping.ttl");
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url = classLoader.getResource("./test-cases/RMLTC1036-CSV");
        assert url != null;
        executor.verifySources(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8), URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
    }


}

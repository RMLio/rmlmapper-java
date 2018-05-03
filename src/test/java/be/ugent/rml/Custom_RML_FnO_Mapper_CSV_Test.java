package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class Custom_RML_FnO_Mapper_CSV_Test extends TestCore {
    @Test
    public void evaluate_0000_CSV() {
        doMapping("./rml-fno-test-cases/RMLFNOTC0000-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0000-CSV/output.ttl");
    }

    @Test
    public void evaluate_0000_CSV_preload() {
        Map<String, Class> libraryMap = new HashMap<>();
        libraryMap.put("GrelFunctions.jar", GrelProcessor.class);
        FunctionLoader functionLoader = new FunctionLoader(libraryMap);
        try {
            Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0000-CSV/mapping.ttl", functionLoader);
            doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0000-CSV/output.ttl");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    public void evaluate_0001_CSV() {
        doMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }
}
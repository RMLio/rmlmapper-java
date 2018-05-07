package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class Custom_RML_FnO_Mapper_CSV_Test extends TestCore {
    @Test
    public void evaluate_0000_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0000-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0000-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV_defaultLoad() {
        doMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0002-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0002-CSV/output.ttl");
    }

    @Test
    public void evaluate_0003_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0003-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0003-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0004_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0004-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0004-CSV/output.ttl");
    }
    
    @Test
    public void evaluate_0005_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0005-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0005-CSV/output.ttl");
    }

    private void doPreloadMapping(String mapPath, String outPath) {
        Map<String, Class> libraryMap = new HashMap<>();
        libraryMap.put("GrelFunctions.jar", GrelProcessor.class);
        FunctionLoader functionLoader = new FunctionLoader(libraryMap);
        try {
            Executor executor = this.createExecutor(mapPath, functionLoader);
            doMapping(executor, outPath);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }
}
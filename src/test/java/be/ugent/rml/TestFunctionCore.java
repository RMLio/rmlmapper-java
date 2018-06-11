package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

abstract class TestFunctionCore extends TestCore {

    Executor doPreloadMapping(String mapPath, String outPath) {
        Map<String, Class> libraryMap = new HashMap<>();
        libraryMap.put("GrelFunctions", GrelProcessor.class);
        FunctionLoader functionLoader = new FunctionLoader(null, null, libraryMap);
        try {
            Executor executor = this.createExecutor(mapPath, functionLoader);
            doMapping(executor, outPath);
            return executor;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        return null;
    }
}

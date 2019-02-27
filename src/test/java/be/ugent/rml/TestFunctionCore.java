package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelTestProcessor;
import be.ugent.rml.functions.lib.IDLabFunctions;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

abstract class TestFunctionCore extends TestCore {

    Executor doPreloadMapping(String mapPath, String outPath) {
        Map<String, Class> libraryMap = new HashMap<>();
        libraryMap.put("GrelFunctions", GrelTestProcessor.class);
        libraryMap.put("IDLabFunctions", IDLabFunctions.class);
        URL url = Resources.getResource("rml-fno-test-cases/functions_test.ttl");
        try {
            File myFile = new File(url.toURI());
            FunctionLoader functionLoader = new FunctionLoader(myFile, null, libraryMap);
            Executor executor = this.createExecutor(mapPath, functionLoader);
            doMapping(executor, outPath);
            return executor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        return null;
    }
}

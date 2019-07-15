package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class Optimizations_Test extends TestCore {

    @Test
    public void xmlFileOnlyReadOnce() {
        String cwd = (new File( "./src/test/resources/test-cases/RMLTC1011-XML")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();

        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
        Main.main(("-v -m " + mappingFilePath).split(" "), cwd);

        int counts = StringUtils.countMatches(stderr.toString(), "No document found for");

        assertEquals(counts, 1);
    }
}

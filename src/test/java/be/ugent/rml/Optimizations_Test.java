package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class Optimizations_Test extends TestCore {

    @Test
    public void xmlFileOnlyReadOnce() throws IOException {
        String cwd = Utils.getFile( "test-cases/RMLTC1011-XML").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();

        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stderr)) {
            System.setErr(ps);
            Main.main(("-v -m " + mappingFilePath).split(" "), cwd);
        } finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }

        String output = stderr.toString();
        logger.debug("Stderr:\n'{}'", output);
        int counts = StringUtils.countMatches(output, "No document found for");

        assertEquals(counts, 1);
    }
}

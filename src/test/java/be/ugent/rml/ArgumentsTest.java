package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.jupiter.api.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.Triples;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ArgumentsTest extends TestCore {

    @Test
    public void withConfigFile() throws Exception {
        Main.run("-c ./argument-config-file-test-cases/config_example.properties".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutConfigFile() throws Exception {
        Main.run("-m ./argument-config-file-test-cases/mapping.ttl -o ./generated_output.nq".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutConfigFileNoSlashes() throws Exception {
        Main.run("-m ./argument-config-file-test-cases/mapping.ttl -o generated_output.nq".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutConfigFileWindowsSlashes() throws Exception {
        Main.run("-m .\\argument-config-file-test-cases\\mapping.ttl -o .\\generated_output.nq".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false);

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nonexistingMappingFile() {
        assertThrows(IllegalArgumentException.class, () -> Main.run("-m ./argument-config-file-test-cases/I_DONT_EXIST.ttl -o ./generated_output.nq".split(" ")));
    }

    @Test
    public void mappingFileAndRawMappingString() throws Exception {
        String arg1 = "./argument-config-file-test-cases/mapping_base.ttl";
        String arg2 = "@prefix rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
                "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n\n" +
                "<LogicalSource1>\n" +
                "    rml:source \"src/test/resources/argument-config-file-test-cases/student.json\";\n" +
                "    rml:referenceFormulation ql:JSONPath;\n" +
                "    rml:iterator \"$.students[*]\".\n" +
                "\n" +
                "<LogicalSource2>\n" +
                "    rml:source \"src/test/resources/argument-config-file-test-cases/sport.json\";\n" +
                "    rml:referenceFormulation ql:JSONPath;\n" +
                "    rml:iterator \"$.sports[*]\".";
        String[] args = {"-m", arg1, arg2, "-o", "./generated_output.nq"};
        Main.run(args);
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleMappingFiles() throws Exception {
        Main.run(
                "-m ./argument-config-file-test-cases/mapping_base.ttl ./argument-config-file-test-cases/mapping1.ttl -o ./generated_output.nq"
                        .split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVerboseWithCustomFunctionFile() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            Main.run("-v -f ./rml-fno-test-cases/functions_test.ttl -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
            assertThat(stdout.toString(), containsString("Using custom path to functions.ttl file: "));
        } finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }
    }

    @Test
    public void testStdOut() throws Exception {
        String cwd = Utils.getFile( "argument/quote-in-literal").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String functionsFilePath = Utils.getFile( "rml-fno-test-cases/functions_test.ttl").getAbsolutePath();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setOut(ps);
            Main.run(new String[]{"-v", "-f" , functionsFilePath , "-m" , mappingFilePath}, cwd);
        } finally {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));   // reset to original System.out
        }

        assertThat(stdout.toString(StandardCharsets.UTF_8), containsString("<http://example.com/10> <http://xmlns.com/foaf/0.1/name> \"Venus\\\"\" ."));
        assertThat(stdout.toString(StandardCharsets.UTF_8), containsString("<http://example.com/10> <http://example.com/id> \"10\" ."));
        assertThat(stdout.toString(StandardCharsets.UTF_8), containsString("<http://example.com/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> ."));
    }



    @Test
    public void testVerboseWithoutCustomFunctionFile() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            Main.run("-v -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        } finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }
        assertThat(stdout.toString(StandardCharsets.UTF_8), not(containsString("Using custom path to functions.ttl file: ")));
    }

    @Test
    public void testWithCustomFunctionFile()  throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            Main.run("-f ./rml-fno-test-cases/functions_test.ttl -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        } finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }
        assertThat(stdout.toString(StandardCharsets.UTF_8), not(containsString("Using custom path to functions.ttl file: ")));
    }

    @Test
    public void testWithCustomFunctionFileInternalFunctionsStillWork() throws Exception {
        String cwd = Utils.getFile("rml-fno-test-cases/RMLFNOTCA005").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualOutPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedOutPath = (new File(cwd, "output.ttl")).getAbsolutePath();
        Main.run(new String[]{"-f", "./rml-fno-test-cases/functions_dynamic.ttl", "-m" , mappingFilePath , "-o" , actualOutPath}, cwd);
        compareFiles(
                expectedOutPath,
                actualOutPath,
                false
        );

        try {
            File outputFile = Utils.getFile(actualOutPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWithCustomFunctionFileVerboseFunctionLogging() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            String cwd = Utils.getFile("rml-fno-test-cases/RMLFNOTCA005").getAbsolutePath();
            String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
            String actualOutPath = (new File("./generated_output.nq")).getAbsolutePath();
            String expectedOutPath = (new File(cwd, "output.ttl")).getAbsolutePath();
            Main.run(new String[]{"-v", "-f" ,"./rml-fno-test-cases/functions_dynamic.ttl", "-m" , mappingFilePath , "-o" , actualOutPath}, cwd);
            compareFiles(
                    expectedOutPath,
                    actualOutPath,
                    false
            );
            assertThat(stdout.toString(StandardCharsets.UTF_8), containsString("Loading function descriptions"));

            try {
                File outputFile = Utils.getFile(actualOutPath);
                assertTrue(outputFile.delete());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }
    }

    @Test
    public void outputTurtle() throws Exception {
        String cwd = Utils.getFile( "argument").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualTrigPath = (new File("./generated_output.trig")).getAbsolutePath();
        String expectedTrigPath = Utils.getFile( "argument/output-trig/target_output.trig").getAbsolutePath();

        Main.run(new String[]{"-m" , mappingFilePath , "-o" , actualTrigPath , "-s", "turtle"}, cwd);
        compareFiles(
                expectedTrigPath,
                actualTrigPath,
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(actualTrigPath));
            String content = new String(encoded, StandardCharsets.UTF_8);

            assertTrue(content.contains("@prefix foaf: <http://xmlns.com/foaf/0.1/> ."));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputFile = Utils.getFile(actualTrigPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void outputJSON() throws Exception {
        String cwd = Utils.getFile( "argument").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualJSONPath = (new File("./generated_output.json")).getAbsolutePath();
        String expectedJSONPath = Utils.getFile( "argument/output-json/target_output.json").getAbsolutePath();

        Main.run(new String[]{"-m", mappingFilePath, "-o" , actualJSONPath , "-s", "jsonld"}, cwd);

        compareFiles(
                expectedJSONPath,
                actualJSONPath,
                false
        );

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(actualJSONPath));
            String content = new String(encoded, StandardCharsets.UTF_8);

            assertTrue(content.contains("\"http://xmlns.com/foaf/0.1/name\" : ["));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File outputFile = Utils.getFile(actualJSONPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputTrig() throws Exception {
        String cwd = Utils.getFile( "argument").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualTrigPath = (new File("./generated_output.trig")).getAbsolutePath();
        String expectedTrigPath = Utils.getFile( "argument/output-trig/target_output.trig").getAbsolutePath();

        Main.run(new String[]{"-m" , mappingFilePath , "-o" , actualTrigPath , "-s", "trig"}, cwd);
        compareFiles(
                expectedTrigPath,
                actualTrigPath,
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(actualTrigPath));
            String content = new String(encoded, StandardCharsets.UTF_8);

            assertTrue(content.contains("{"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputFile = Utils.getFile(actualTrigPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputHDT() throws Exception {
        String cwd = Utils.getFile("argument").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualHDTPath = (new File(cwd, "generated_output.hdt")).getAbsolutePath();
        String expectedHDTPath = Utils.getFile("argument/output-hdt/target_output.hdt").getAbsolutePath();

        Main.run((new String[]{"-v", "-m", mappingFilePath, "-o", actualHDTPath, "-s", "hdt"}), cwd);

        // Load HDT file.

        try (HDT hdt1 = HDTManager.loadHDT(expectedHDTPath, null); HDT hdt2 = HDTManager.loadHDT(actualHDTPath, null)) {
            Triples triples1 = hdt1.getTriples();
            Triples triples2 = hdt2.getTriples();

            assertEquals(triples1.size(), triples2.size());

            IteratorTripleID iteratorTripleID1 = triples1.searchAll();
            IteratorTripleID iteratorTripleID2 = triples2.searchAll();

            while (iteratorTripleID1.hasNext()) {
                TripleID tripleID1 = iteratorTripleID1.next();
                TripleID tripleID2 = iteratorTripleID2.next();

                assertEquals(tripleID1, tripleID2);
            }
        } finally {
            assertTrue((new File(actualHDTPath)).delete());
        }
    }


    @Test
    public void quoteInLiteral() throws Exception {
        String cwd = Utils.getFile( "argument/quote-in-literal").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualNQuadsPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedNQuadsPath = Utils.getFile( "argument/quote-in-literal/target_output.nq").getAbsolutePath();

        Main.run(new String[]{"-m" , mappingFilePath , "-o" , actualNQuadsPath}, cwd);
        compareFiles(
                expectedNQuadsPath,
                actualNQuadsPath,
                false
        );

        try {
            File outputFile = Utils.getFile(actualNQuadsPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMissingBaseIRIInStrictMode() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            Main.run("-m ./argument-config-file-test-cases/mapping.ttl -o ./generated_output.nq --strict".split(" "));
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("When running in strict mode, a base IRI argument must be set."));
        }
        finally {
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));   // reset to original System.err
        }
    }

    @Test
    public void testExplicitBaseIRI() throws Exception {
        String cwd = Utils.getFile( "argument/base-iri").getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setOut(ps);
            Main.run(new String[]{"-v", "--strict", "-b", "http://example2.com/", "-m", mappingFilePath}, cwd);
        } finally {
            stdout.close();
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));   // reset to original System.out
        }
        assertThat(stdout.toString(), containsString("<http://example2.com/10/Venus>"));

    }

    @Test
    public void onlyPipe() throws Exception {
        String mappingFile = URLDecoder.decode(Utils.getFile("argument-config-file-test-cases/mapping.ttl").getAbsolutePath(), StandardCharsets.UTF_8);
        mappingFile = URLDecoder.decode(mappingFile, StandardCharsets.UTF_8);
        try (InputStream is = Files.newInputStream(Paths.get(mappingFile))) {
            System.setIn(is);

            String[] args = {"-o", "./generated_output.nq"};
            Main.run(args);
            compareFiles(
                    "argument-config-file-test-cases/target_output.nq",
                    "./generated_output.nq",
                    false
            );

            File outputFile;
            try {
                outputFile = Utils.getFile("./generated_output.nq");
                assertTrue(outputFile.delete());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            System.setIn(new FileInputStream(FileDescriptor.in));   // rest System.in to original input stream
        }
    }

    @Test
    public void pipeAndMapping() throws Exception {
        String mappingFile = Utils.getFile("argument-config-file-test-cases/mapping_part2.ttl").getAbsolutePath();
        try (InputStream is = Files.newInputStream(Paths.get(mappingFile))) {
            System.setIn(is);

            String[] args = {"-m", "./argument-config-file-test-cases/mapping_part1.ttl", "-o", "./generated_output.nq"};
            Main.run(args);
            compareFiles(
                    "argument-config-file-test-cases/target_output.nq",
                    "./generated_output.nq",
                    false
            );

            File outputFile;
            try {
                outputFile = Utils.getFile("./generated_output.nq");
                assertTrue(outputFile.delete());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            System.setIn(new FileInputStream(FileDescriptor.in));   // rest System.in to original input stream
        }
    }

    @Test
    public void wrongOutPutFile() {
        assertThrows(IllegalArgumentException.class, () ->
                Main.run("-m ./argument-config-file-test-cases/mapping.ttl -o ./wrong/file/output/generated_output.nq".split(" ")));
    }

    @Test
    public void disableEOFMarker() throws Exception {
        String[] args = {"-m", "./argument/mapping.ttl", "-v", "--disable-automatic-eof-marker"};
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(stdout)) {
            System.setErr(ps);
            Main.run(args);
        } finally {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));   // reset to original System.out
        }
        assertThat(stdout.toString(), containsString("Automatic EOF marker disabled!"));
    }
}

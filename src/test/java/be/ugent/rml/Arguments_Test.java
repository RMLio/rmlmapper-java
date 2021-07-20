package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.Triples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class Arguments_Test extends TestCore {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void withConfigFile() throws Exception {
        Main.main("-c ./argument-config-file-test-cases/config_example.properties".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutConfigFile() throws Exception {
        Main.main("-m ./argument-config-file-test-cases/mapping.ttl -o ./generated_output.nq".split(" "));
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
    public void nonexistingMappingFile() throws Exception {
        exit.expectSystemExitWithStatus(1); // Handle System.exit(1)
        Main.main("-m ./argument-config-file-test-cases/I_DONT_EXIST.ttl -o ./generated_output.nq".split(" "));
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
        String[] args = {"-m", arg1, arg2, "-o" , "./generated_output.nq"};
        Main.main(args);
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multipleMappingFiles() throws Exception {
        Main.main("-m ./argument-config-file-test-cases/mapping_base.ttl ./argument-config-file-test-cases/mapping1.ttl -o ./generated_output.nq".split(" "));
        compareFiles(
                "argument-config-file-test-cases/target_output.nq",
                "./generated_output.nq",
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVerboseWithCustomFunctionFile() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdout));
        Main.main("-v -f ./rml-fno-test-cases/functions_test.ttl -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        assertThat(stdout.toString(), containsString("Using custom path to functions.ttl file: "));
    }

    @Test
    public void testStdOut() {
        String cwd = (new File( "./src/test/resources/argument/quote-in-literal")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String functionsFilePath = (new File( "./src/test/resources/rml-fno-test-cases/functions_test.ttl")).getAbsolutePath();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        Main.main(("-v -f " + functionsFilePath + " -m " + mappingFilePath).split(" "), cwd);

        assertThat(stdout.toString(), containsString("<http://example.com/10> <http://xmlns.com/foaf/0.1/name> \"Venus\\\"\"."));
        assertThat(stdout.toString(), containsString("<http://example.com/10> <http://example.com/id> \"10\"."));
        assertThat(stdout.toString(), containsString("<http://example.com/10> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>."));
    }

    @Test
    public void testVerboseWithoutCustomFunctionFile() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdout));
        Main.main("-v -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        assertThat(stdout.toString(), not(containsString("Using custom path to functions.ttl file: ")));
    }

    @Test
    public void testWithCustomFunctionFile() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdout));
        Main.main("-f ./rml-fno-test-cases/functions_test.ttl -m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        assertThat(stdout.toString(), not(containsString("Using custom path to functions.ttl file: ")));
    }

    @Test
    public void testWithCustomFunctionFileInternalFunctionsStillWork() throws Exception {
        String cwd = (new File("./src/test/resources/rml-fno-test-cases/RMLFNOTCA005")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualOutPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedOutPath = (new File(cwd, "output.ttl")).getAbsolutePath();
        Main.main(("-f ./rml-fno-test-cases/functions_dynamic.ttl -m " + mappingFilePath + " -o " + actualOutPath).split(" "), cwd);
        compareFiles(
                expectedOutPath,
                actualOutPath,
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile(actualOutPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputTurtle() throws Exception {
        String cwd = (new File( "./src/test/resources/argument")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualTrigPath = (new File("./generated_output.trig")).getAbsolutePath();
        String expectedTrigPath = (new File( "./src/test/resources/argument/output-trig/target_output.trig")).getAbsolutePath();

        Main.main(("-m " + mappingFilePath + " -o " + actualTrigPath + " -s turtle").split(" "), cwd);
        compareFiles(
                expectedTrigPath,
                actualTrigPath,
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(actualTrigPath));
            String content = new String(encoded, "utf-8");

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
        String cwd = (new File( "./src/test/resources/argument")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualJSONPath = (new File("./generated_output.json")).getAbsolutePath();
        String expectedJSONPath = (new File( "./src/test/resources/argument/output-json/target_output.json")).getAbsolutePath();

        Main.main(("-m " + mappingFilePath + " -o " + actualJSONPath + " -s jsonld").split(" "), cwd);

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
        String cwd = (new File( "./src/test/resources/argument")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualTrigPath = (new File("./generated_output.trig")).getAbsolutePath();
        String expectedTrigPath = (new File( "./src/test/resources/argument/output-trig/target_output.trig")).getAbsolutePath();

        Main.main(("-m " + mappingFilePath + " -o " + actualTrigPath + " -s trig").split(" "), cwd);
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
    public void outputHDT() throws IOException {
        String cwd = (new File( "./src/test/resources/argument")).getAbsolutePath();
        String mappingFilePath = (new File("./src/test/resources/argument/mapping.ttl")).getAbsolutePath();
        String actualHDTPath = (new File("./generated_output.hdt")).getAbsolutePath();
        String expectedHDTPath = (new File( "./src/test/resources/argument/output-hdt/target_output.hdt")).getAbsolutePath();

        Main.main(("-m " + mappingFilePath + " -o " + actualHDTPath + " -s hdt").split(" "), cwd);

        // Load HDT file.
        HDT hdt1 = HDTManager.loadHDT(expectedHDTPath, null);
        HDT hdt2 = HDTManager.loadHDT(actualHDTPath, null);

        try {
            Triples triples1 = hdt1.getTriples();
            Triples triples2 = hdt2.getTriples();

            assertEquals(triples1.size(), triples2.size());

            IteratorTripleID iteratorTripleID1 = triples1.searchAll();
            IteratorTripleID iteratorTripleID2 = triples2.searchAll();

            while(iteratorTripleID1.hasNext()) {
                TripleID tripleID1 = iteratorTripleID1.next();
                TripleID tripleID2 = iteratorTripleID2.next();

                assertTrue(tripleID1.equals(tripleID2));
            }
        } finally {
            hdt1.close();
            hdt2.close();
            assertTrue((new File(actualHDTPath)).delete());
        }
    }


    @Test
    public void quoteInLiteral() throws Exception {
        String cwd = (new File( "./src/test/resources/argument/quote-in-literal")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();
        String actualNQuadsPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedNQuadsPath = (new File( "./src/test/resources/argument/quote-in-literal/target_output.nq")).getAbsolutePath();

        Main.main(("-m " + mappingFilePath + " -o " + actualNQuadsPath).split(" "), cwd);
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
    public void testMissingBaseIRIInStrictMode() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdout));
        Main.main("-m ./argument-config-file-test-cases/mapping.ttl -o ./generated_output.nq --strict".split(" "));
        assertThat(stdout.toString(), containsString("When running in strict mode, a base IRI argument must be set."));
    }

    @Test
    public void testExplicitBaseIRI() throws Exception {
        String cwd = (new File( "./src/test/resources/argument/base-iri")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.ttl")).getAbsolutePath();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        Main.main(("-v --strict -b http://example2.com/ -m " + mappingFilePath).split(" "), cwd);
        assertThat(stdout.toString(), containsString("<http://example2.com/10/Venus>"));

    }
}

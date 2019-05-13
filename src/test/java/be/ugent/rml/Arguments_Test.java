package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class Arguments_Test extends TestCore {

    @Test
    public void withConfigFile() {
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
    public void withoutConfigFile() {
        Main.main("-m ./argument-config-file-test-cases/mapping.ttl -o ./generated_output.nq".split(" "));
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
    public void mappingFileAndRawMappingString() {
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
    public void multipleMappingFiles() {
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
    public void outputTurtle() {
        Main.main("-m ./argument/mapping.ttl -o ./generated_output.ttl -s turtle".split(" "));
        compareFiles(
                "argument/output-turtle/target_output.ttl",
                "./generated_output.ttl",
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get("./generated_output.ttl"));
            String content = new String(encoded, "utf-8");

            assertTrue(content.contains("@prefix foaf: <http://xmlns.com/foaf/0.1/> ."));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputFile = Utils.getFile("./generated_output.ttl");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputJSON() {
        Main.main("-m ./argument/mapping.ttl -o ./generated_output.json -s jsonld".split(" "));
        compareFiles(
                "argument/output-json/target_output.json",
                "./generated_output.json",
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get("./generated_output.json"));
            String content = new String(encoded, StandardCharsets.UTF_8);

            assertTrue(content.contains("\"http://xmlns.com/foaf/0.1/name\" : ["));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputFile = Utils.getFile("./generated_output.json");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputTrig() {
        Main.main("-m ./argument/mapping.ttl -o ./generated_output.trig -s trig".split(" "));
        compareFiles(
                "argument/output-trig/target_output.trig",
                "./generated_output.trig",
                false
        );

        File outputFile;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get("./generated_output.trig"));
            String content = new String(encoded, StandardCharsets.UTF_8);

            assertTrue(content.contains("{"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            outputFile = Utils.getFile("./generated_output.trig");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void outputHDT() throws IOException {
        Main.main("-m ./argument/mapping.ttl -o ./generated_output.hdt -s hdt".split(" "));

        File file1 = new File("./src/test/resources/argument/output-hdt/target_output.hdt");
        File file2 = new File("./generated_output.hdt");

        // Load HDT file.
        HDT hdt1 = HDTManager.loadHDT(file1.getAbsolutePath(), null);
        HDT hdt2 = HDTManager.loadHDT(file2.getAbsolutePath(), null);

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
            assertTrue(file2.delete());
        }
    }


    @Test
    public void quoteInLiteral() {
        Main.main("-m ./argument/quote-in-literal/mapping.ttl -o ./generated_output.nq".split(" "));
        compareFiles(
                "argument/quote-in-literal/target_output.nq",
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
}

package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

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

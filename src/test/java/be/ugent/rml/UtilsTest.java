package be.ugent.rml;


import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static be.ugent.rml.Utils.isValidrrLanguage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void isValidrrLanguageTest() {
        String fileName = "utils/language_tags.txt";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))) {
            br.lines().forEach(tag -> assertTrue(isValidrrLanguage(tag)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isInvalidrrLanguageTest() {
        String fileName = "utils/invalid_language_tags.txt";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))) {
            br.lines().forEach(tag -> assertFalse(isValidrrLanguage(tag)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validateIRITest() {
        assertTrue(Utils.isValidIRI("bob"));
        assertTrue(Utils.isValidIRI("http://example.com/θερμότητα/10"));

        assertFalse(Utils.isValidIRI("bob smith"));
    }
}
package be.ugent.rml.functions.lib;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class IDLabFunctionsTest {

    @Test
    public void stringContainsOtherString_true() {
        String str = "Finding a needle in a haystack";
        String otherStr = "needle";
        String delimiter = " ";
        assertTrue(IDLabFunctions.stringContainsOtherString(str, otherStr, delimiter));
    }

    @Test
    public void stringContainsOtherString_false() {
        String str = "What you are looking for is not here";
        String otherStr = "needle";
        String delimiter = " ";
        assertFalse(IDLabFunctions.stringContainsOtherString(str, otherStr, delimiter));
    }

    @Test
    public void listContainsElement_true() {
        List<String> list = Arrays.asList("apple", "banana", "lemon", "orange");
        String str = "lemon";
        assertTrue(IDLabFunctions.listContainsElement(list, str));
    }

    @Test
    public void listContainsElement_false() {
        List<String> list = Arrays.asList("apple", "banana", "lemon", "orange");
        String str = "pear";
        assertFalse(IDLabFunctions.listContainsElement(list, str));
    }


    @Test
    public void dbpediaSpotlight() {
        String endpoint = "http://193.190.127.195/dbpedia-spotlight/rest";
        List<String> entities = IDLabFunctions.dbpediaSpotlight("Barack Obama", endpoint);
        ArrayList<String> expected = new ArrayList<>();
        expected.add("http://dbpedia.org/resource/Barack_Obama");

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("", endpoint);
        expected = new ArrayList<>();

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("a", endpoint);
        expected = new ArrayList<>();

        assertThat(entities, CoreMatchers.is(expected));
    }

    @Test
    public void trueCondition() {
        Object result = IDLabFunctions.trueCondition("true", "hello");
        assertEquals("hello", result);

        result = IDLabFunctions.trueCondition("false", "hello");
        assertNull(result);

        result = IDLabFunctions.trueCondition("test", "hello");
        assertNull(result);
    }

    @Test
    public void decide_true() {
        String input = "foo";
        String expected = "foo";
        String value = "success!";
        assertEquals(value, IDLabFunctions.decide(input, expected, value));
    }

    @Test
    public void decide_false() {
        String input = "foo";
        String expected = "bar";
        String value = "success!";
        assertNull(IDLabFunctions.decide(input, expected, value));
    }

    @Test
    public void getMIMEType() {
        String result = IDLabFunctions.getMIMEType("test.csv");
        assertEquals("text/csv", result);

        result = IDLabFunctions.getMIMEType("test.json");
        assertEquals("application/json", result);
    }

    @Test
    public void readFile_validPath() {
        String path = "rml-fno-test-cases/student.csv";
        String result = IDLabFunctions.readFile(path);
        assertNotNull(result);
        assertTrue(result.contains("Id,Name,Comment,Class"));
    }

    @Test
    public void readFile_invalidPath() {
        String path = "rml-fno-test-cases/does_not_exist.txt";
        String result = IDLabFunctions.readFile(path);
        assertNull(result);
    }

    @Test
    public void random() {
        String result = IDLabFunctions.random();
        try {
            UUID uuid = UUID.fromString(result);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void toUpperCaseURL() {
        String noProtocol = "www.example.com";
        String withProtocol = "http://www.example.com";

        String result = IDLabFunctions.toUpperCaseURL(noProtocol);
        assertEquals("HTTP://WWW.EXAMPLE.COM", result);
        result = IDLabFunctions.toUpperCaseURL(withProtocol);
        assertEquals("HTTP://WWW.EXAMPLE.COM", result);
    }

    @Test
    public void inRange() {
        assertTrue(IDLabFunctions.inRange(3.0, 1.0, 5.0));
        assertFalse(IDLabFunctions.inRange(3.0, 1.0, 3.0));
        assertTrue(IDLabFunctions.inRange(3.0, 1.0, null));
        assertTrue(IDLabFunctions.inRange(3.0, null, 5.0));
        assertTrue(IDLabFunctions.inRange(3.0, null, null));
        assertFalse(IDLabFunctions.inRange(null, null, null));
    }

    @Test
    public void slugify() {
        String result = IDLabFunctions.slugify("Ben De Mééster");
        assertEquals("ben-de-meester", result);
    }
}

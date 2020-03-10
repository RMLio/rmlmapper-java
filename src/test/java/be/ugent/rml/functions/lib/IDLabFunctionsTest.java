package be.ugent.rml.functions.lib;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class IDLabFunctionsTest {

    @Test
    public void dbpediaSpotlight() {
        List<String> entities = IDLabFunctions.dbpediaSpotlight("Barack Obama", "https://api.dbpedia-spotlight.org/en");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("http://dbpedia.org/resource/Barack_Obama");

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("", "https://api.dbpedia-spotlight.org/en");
        expected = new ArrayList<>();

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("a", "https://api.dbpedia-spotlight.org/en");
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
    public void getMIMEType() {
        String result = IDLabFunctions.getMIMEType("test.csv");
        assertEquals("text/csv", result);

        result = IDLabFunctions.getMIMEType("test.json");
        assertEquals("application/json", result);
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
}

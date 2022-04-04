package be.ugent.rml.functions.lib;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class IDLabFunctionsTest {

    @Test
    public void stringContainsOtherStringTrue() {
        String str = "Finding a needle in a haystack";
        String otherStr = "needle";
        String delimiter = " ";
        assertTrue(IDLabFunctions.stringContainsOtherString(str, otherStr, delimiter));
    }

    @Test
    public void stringContainsOtherStringFalse() {
        String str = "What you are looking for is not here";
        String otherStr = "needle";
        String delimiter = " ";
        assertFalse(IDLabFunctions.stringContainsOtherString(str, otherStr, delimiter));
    }

    @Test
    public void listContainsElementTrue() {
        List<String> list = Arrays.asList("apple", "banana", "lemon", "orange");
        String str = "lemon";
        assertTrue(IDLabFunctions.listContainsElement(list, str));
    }

    @Test
    public void listContainsElementFalse() {
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
    public void decideTrue() {
        String input = "foo";
        String expected = "foo";
        String value = "success!";
        assertEquals(value, IDLabFunctions.decide(input, expected, value));
    }

    @Test
    public void decideFalse() {
        String input = "foo";
        String expected = "bar";
        String value = "success!";
        assertNull(IDLabFunctions.decide(input, expected, value));
    }

    @Test
    public void isNullTrue() {
        String input = null;
        assertTrue(IDLabFunctions.isNull(input));
    }

    @Test
    public void isNullFalse() {
        String input = "Hello";
        assertFalse(IDLabFunctions.isNull(input));
    }

    @Test
    public void getMIMEType() {
        String result = IDLabFunctions.getMIMEType("test.csv");
        assertEquals("text/csv", result);

        result = IDLabFunctions.getMIMEType("test.json");
        assertEquals("application/json", result);
    }

    @Test
    public void readFileValidPath() {
        String path = "rml-fno-test-cases/student.csv";
        String result = IDLabFunctions.readFile(path);
        assertNotNull(result);
        assertTrue(result.contains("Id,Name,Comment,Class"));
    }

    @Test
    public void readFileInvalidPath() {
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

	@Test
	public void normalizeDateWithLang() {
		String input1 = "20220121";
		String format1 = "yyyyMMdd";
		assertEquals("2022-01-21", IDLabFunctions.normalizeDateWithLang(input1, format1, "en"));

		String input2 = "01 April 22";
		// String format2 = "dd LLLL uu";	// This does not work on Java 8!
		String format2 = "dd MMMM uu";
		assertEquals("2022-04-01", IDLabFunctions.normalizeDateWithLang(input2, format2, "en"));

		assertNull(IDLabFunctions.normalizeDateWithLang("rubbish", "yodelahiti", "en"));

		// will fail because "April" is no French
		assertNull(IDLabFunctions.normalizeDateWithLang(input2, format2, "fr"));

        String input3 = "01-avr.-22";   // yes, French abbreviations need a '.' !
        String format3 = "dd-MMM-yy";
        assertEquals("2022-04-01", IDLabFunctions.normalizeDateWithLang(input3, format3, "fr"));
	}

	@Test
	public void normalizeDate() {
		String input1 = "20220121";
		String format1 = "yyyyMMdd";
		assertEquals("2022-01-21", IDLabFunctions.normalizeDate(input1, format1));

		assertNull(IDLabFunctions.normalizeDate("rubbish", "yodelahiti"));

	}

    @Test
    public void normalizeDateTimeWithLang() {
        String input1 = "20220121 7 14 33";
        String format1 = "yyyyMMdd H m s";
        assertEquals("2022-01-21T07:14:33", IDLabFunctions.normalizeDateTimeWithLang(input1, format1, "en"));
    }

    @Test
    public void normalizeDateTime() {
        String input1 = "20200521 17 14 33";
        String format1 = "yyyyMMdd H m s";
        assertEquals("2020-05-21T17:14:33", IDLabFunctions.normalizeDateTime(input1, format1));

        // 20220124T09:36:04,yyyyMMdd'THH:mm:ss
        String input2 = "20220124T09:36:04";
        String format2 = "yyyyMMdd'T'HH:mm:ss";
        assertEquals("2022-01-24T09:36:04", IDLabFunctions.normalizeDateTime(input2, format2));

        String input3 = "01-Apr-20 9u4";
        String format3 = "dd-MMM-yy H'u'm";
        assertEquals("2020-04-01T09:04:00", IDLabFunctions.normalizeDateTime(input3, format3));

    }
    public static class LDESGenerationTests{

        private static final String STATE_DIRECTORY = "/tmp/test-state";


        @After
        public void cleanUp() throws IOException {
            IDLabFunctions.resetState();
            FileUtils.deleteDirectory(Paths.get(STATE_DIRECTORY).toFile());
        }

        @Test
        public void skipGenerateUniqueIRI(){
            String template = "http://example.com/sensor1/";
            String value = "pressure=5";
            boolean isUnique = false;

            IDLabFunctions.generateUniqueIRI(template, value, isUnique, STATE_DIRECTORY);
            String generated_iri = IDLabFunctions.generateUniqueIRI(template, value, isUnique, STATE_DIRECTORY);
            assertNull(generated_iri);
        }


        @Test
        public void generateUniqueIRI(){
            String template = "http://example.com/sensor2/";
            String value = "pressure=5";
            boolean isUnique = true;

            String generated_iri = IDLabFunctions.generateUniqueIRI(template, value, isUnique, STATE_DIRECTORY);
            assertEquals(template, generated_iri);

        }

        @Test
        public void generateUniqueIRIWithDate(){

            String template = "http://example.com/sensor2/";
            String value = "pressure=5";
            boolean isUnique = false;

            String generated_iri = IDLabFunctions.generateUniqueIRI(template, value, isUnique, STATE_DIRECTORY);
            assertNotNull(generated_iri);
            assertTrue(generated_iri.contains(template));
        }
    }

    @Test
    public void lookup() throws CsvValidationException, IOException {
        String searchString = "A";
        String inputFile =  "src/test/resources/rml-fno-test-cases/RMLFNOTCF013/class.csv";
        Integer fromColumn = 0;
        Integer toColumn = 1;
        assertEquals("Class A", IDLabFunctions.lookup(searchString, inputFile, fromColumn, toColumn));

        String delimiter = ",";
        assertEquals("Class A", IDLabFunctions.lookupWithDelimiter(searchString, inputFile, fromColumn, toColumn, delimiter));

        searchString = "Class B";
        assertEquals(null, IDLabFunctions.lookup(searchString, inputFile, fromColumn, toColumn));

        searchString = "Class B";
        fromColumn = 2;
        assertEquals(null, IDLabFunctions.lookup(searchString, inputFile, fromColumn, toColumn));

        searchString = "B";
        fromColumn = 0;
        inputFile = "src/test/resources/rml-fno-test-cases/RMLFNOTCF013/classB.csv";
        delimiter = ";";
        assertEquals("Class B", IDLabFunctions.lookupWithDelimiter(searchString, inputFile, fromColumn, toColumn, delimiter));
    }

}

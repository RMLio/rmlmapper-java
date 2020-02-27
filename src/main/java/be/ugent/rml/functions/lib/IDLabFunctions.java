package be.ugent.rml.functions.lib;

import be.ugent.rml.Utils;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IDLabFunctions {

    private static final Logger logger = LoggerFactory.getLogger(IDLabFunctions.class);

    public static boolean stringContainsOtherString(String str, String otherStr, String delimiter) {
        String[] split = str.split(delimiter);
        List<String> list = Arrays.asList(split);

        return list.contains(otherStr);
    }

    public static boolean listContainsElement(List list, String str) {
        if (list != null) {
            return list.contains(str);
        } else {
            return false;
        }
    }

    public static List<String> dbpediaSpotlight(String text, String endpoint) {
        if (!text.equals("")) {
            try {
                URL url = new URL(endpoint + "/annotate?text=" + URLEncoder.encode(text, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");
                con.setInstanceFollowRedirects(true);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                con.disconnect();

                Object document = Configuration.defaultConfiguration().jsonProvider().parse(content.toString());
                return JsonPath.parse(document).read("$.Resources[*].@URI");
            } catch (PathNotFoundException e) {
                // that means no result was found, so that is fine
                logger.info(e.getMessage(), e);
            } catch (Exception e) {
                // that probably means smth is wrong with the DBpedia Spotlight endpoint, so that is fine: log and continue
                logger.warn(e.getMessage(), e);
            }
        }

        return new ArrayList<>();
    }

    public static Object trueCondition(String bool, String value) {
        if (bool == null || !bool.equals("true")) {
            return null;
        } else {
            return value;
        }
    }

    public static String decide(String input, String expected, String result) {
        if (input != null && input.equals(expected)) {
            return result;
        } else {
            return null;
        }
    }

    public static String getMIMEType(String filename) {
        if (filename == null) {
            return null;
        } else {
            HashMap<String, String> map = new HashMap<>();

            // Put elements in the hashMap
            map.put("csv", "text/csv");
            map.put("json", "application/json");
            map.put("xml", "application/xml");
            map.put("nt", "application/n-triples");
            map.put("ttl", "text/turtle");
            map.put("nq", "application/n-quads");
            map.put("sql", "application/sql");

            String extension = FilenameUtils.getExtension(filename);

            if (map.containsKey(extension)) {
                return map.get(extension);
            } else {
                return null;
            }
        }
    }

    public static String readFile(String path) {
        try {
            logger.debug(Utils.getFile(path).toString());
            return Utils.fileToString(Utils.getFile(path));
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return null;
        }
    }

    public static String random() {
        return UUID.randomUUID().toString();
    }

    public static String toUpperCaseURL(String test) {
        String upperTest = test.toUpperCase();
        if (!upperTest.startsWith("http://")) {
            upperTest = "http://" + upperTest;
        }
        return upperTest;
    }

    /**
     * Tests whether a certain number is in a certain range.
     * Everything is optional:
     *   - function returns false when testNumber is null
     *   - function only tests from constraint when to is null
     *   - function only tests to constraint when from is null
     *   - function returns true when from and to are null.
     * @param testNumber The number put under the test. Optional (function returns false when is null)
     * @param from The number from where (inclusive)
     * @param to The number until where (exclusive)
     * @return whether it's in range or not
     */
    public static boolean inRange(Double testNumber, Double from, Double to) {
        if (testNumber == null) {
            return false;
        }
        if (from == null && to == null) {
            return true;
        }

        if (from == null) {
            return testNumber < to;
        }
        if (to == null) {
            return testNumber >= from;
        }
        return testNumber >= from && testNumber < to;
    }

    // TODO below are currently not part of any tests

    // TODO check whether this is the right place for this
    public static boolean isSet(String valueParameter) {
        return !StringUtils.isEmpty(valueParameter);
    }

    // TODO check whether this is the right place for this
    public static boolean booleanMatch(String valueParameter, String regexParameter) {
        return valueParameter.matches(regexParameter);
    }

    // TODO check whether this is the right place for this
    /**
     * Returns the string obtained by joining two strings `s1` and `s2` with the separator `sep`.
     * For example, `join("foo", "bar", ";")` returns the string `foo;bar`.
     *
     * @param s1  string
     * @param s2  string
     * @param sep separator
     * @return the string obtained by joining two strings `s1` and `s2` with the separator `sep`
     */
    public static String join2(String s1, String s2, String sep) {
        return s1 + sep + s2;
    }

    // TODO check whether this is the right place for this
    /**
     * Returns `s` as a normalized xsd:date string, using `f` as current date form.
     *
     * @param s string
     * @param f format
     * @return a normalized xsd:date string
     */
    public static String normalizeDate(String s, String f) {
        DateFormat format = new SimpleDateFormat(f);
        Date date = null;
        try {
            date = format.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            return s;
        }
        DateFormat xsdDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        return xsdDateFormat.format(date);
    }

    // TODO check whether this is the right place for this
    private static String decodeURIComponent(String encodedURI) {
        char actualChar;

        StringBuffer buffer = new StringBuffer();

        int bytePattern, sumb = 0;

        for (int i = 0, more = -1; i < encodedURI.length(); i++) {
            actualChar = encodedURI.charAt(i);

            switch (actualChar) {
                case '%': {
                    actualChar = encodedURI.charAt(++i);
                    int hb = (Character.isDigit(actualChar) ? actualChar - '0'
                            : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    actualChar = encodedURI.charAt(++i);
                    int lb = (Character.isDigit(actualChar) ? actualChar - '0'
                            : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
                    bytePattern = (hb << 4) | lb;
                    break;
                }
                case '+': {
                    bytePattern = ' ';
                    break;
                }
                default: {
                    bytePattern = actualChar;
                }
            }

            if ((bytePattern & 0xc0) == 0x80) { // 10xxxxxx
                sumb = (sumb << 6) | (bytePattern & 0x3f);
                if (--more == 0)
                    buffer.append((char) sumb);
            } else if ((bytePattern & 0x80) == 0x00) { // 0xxxxxxx
                buffer.append((char) bytePattern);
            } else if ((bytePattern & 0xe0) == 0xc0) { // 110xxxxx
                sumb = bytePattern & 0x1f;
                more = 1;
            } else if ((bytePattern & 0xf0) == 0xe0) { // 1110xxxx
                sumb = bytePattern & 0x0f;
                more = 2;
            } else if ((bytePattern & 0xf8) == 0xf0) { // 11110xxx
                sumb = bytePattern & 0x07;
                more = 3;
            } else if ((bytePattern & 0xfc) == 0xf8) { // 111110xx
                sumb = bytePattern & 0x03;
                more = 4;
            } else { // 1111110x
                sumb = bytePattern & 0x01;
                more = 5;
            }
        }
        return buffer.toString();
    }
}

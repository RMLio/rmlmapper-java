package be.ugent.rml.functions.lib;

import be.ugent.rml.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.slugify.Slugify;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IDLabFunctions {

    private static final Logger logger = LoggerFactory.getLogger(IDLabFunctions.class);
    private static Map<String, Map<String, String>> LDES_FILE_STATE_MAP = new HashMap<>();
    private static Path STATE_DIR = null;

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

    /**
     * Tests whether a given input is null or not.
     * @param input String to evaluate
     * @return whether it's null or not
     */
    public static boolean isNull(String input) {
        return input == null;
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
        if (!upperTest.startsWith("HTTP://")) {
            upperTest = "HTTP://" + upperTest;
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

    /**
     * Convert a string to its slugified equivalent.
     * @param str The String to slugify
     * @return the slugified string. Returns null if the input was also null.
     */
    public static String slugify(String str) {
        if (str != null) {
            Slugify slg = new Slugify();
            return slg.slugify(str);
        }
        return null;
    }

    // TODO The functions below are currently untested and undefined as idlab-fn functions.
    // (They however do no belong to GREL functions either)

    // TODO check whether this is the right place for this
    public static boolean isSet(String valueParameter) {
        return !StringUtils.isEmpty(valueParameter);
    }

    // TODO check whether this is the right place for this
    public static boolean booleanMatch(String valueParameter, String regexParameter) {
        return valueParameter.matches(regexParameter);
    }

    ///////////////////////////////
    // Date formatting functions //
    ///////////////////////////////
    // TODO check whether this is the right place for this

    /**
     * Returns a given date(time) string as an ISO-8601 formatted date(time) string.
     *
     * @param dateStr     Input string representing a parsable date or dateTime, e.g. "01 April 22"
     * @param pattern     DateTime format pattern used to parse the given dateStr as defined in {@link DateTimeFormatter}, e.g. "dd LLLL uu"
     * @param language    The language of dateStr, as defined in {@link Locale}
     * @param includeTime If <code>true</code>, include the time part in the output.
     * @return A normalized date string in the ISO-8601 format uuuu-MM-dd (xs:date) or uuuu-MM-ddTHH:mm:ss,
     * or null if parsing the input fails.
     */
    private static String normalizeDateTimeStr(String dateStr, String pattern, String language, boolean includeTime) {
        try {
            Locale locale = new Locale(language);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
            if (includeTime) {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            } else {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                return date.toString();
            }
        } catch (Throwable e) {
            logger.error("{}; format pattern: \"{}\", input: \"{}\", language: \"{}\"", e.getMessage(), pattern, dateStr, language);
            return null;
        }
    }

    /**
     * Returns `dateStr` as a normalized xs:date string, using `pattern` as current date form.
     *
     * @param dateStr  Input string representing a parsable date, e.g. "01 April 22"
     * @param pattern  Date format pattern used to parse the given dateStr as defined in {@link DateTimeFormatter}, e.g. "dd LLLL uu"
     * @param language The language of dateStr, as defined in {@link Locale}
     * @return A normalized date string in the ISO-8601 format uuuu-MM-dd (xs:date), or null if parsing the input fails.
     */
    public static String normalizeDateWithLang(String dateStr, String pattern, String language) {
        return normalizeDateTimeStr(dateStr, pattern, language, false);
    }

    /**
     * Returns `dateStr` as a normalized xs:date string, using `pattern` as current date form. It uses the language of
     * the current locale of the JVM to parse certain input strings, like names of months.
     *
     * @param dateStr Input string representing a parsable date, e.g. "01 April 22"
     * @param pattern Date format pattern used to parse the given dateStr as defined in {@link SimpleDateFormat}, e.g. "dd LLL y"
     * @return A normalized date string in the ISO-8601 format uuuu-MM-dd (xs:date), or null if parsing the input fails.
     */
    public static String normalizeDate(String dateStr, String pattern) {
        return normalizeDateWithLang(dateStr, pattern, Locale.getDefault().getLanguage());
    }

    /**
     * Returns `dateTimeStr` as a normalized xs:date string, using `pattern` as current datetime form.
     *
     * @param dateTimeStr Input string representing a parsable datetime, e.g. "01 April 22T11:44:00"
     * @param pattern     Date format pattern used to parse the given dateStr as defined in {@link DateTimeFormatter}, e.g. "dd LLLL uuTHH:mm:ss"
     * @param language    The language of dateStr, as defined in {@link Locale}
     * @return A normalized date string in the ISO-8601 format uuuu-MM-ddTHH:mm:ss (xs:datetime), or null if parsing the input fails.
     */
    public static String normalizeDateTimeWithLang(String dateTimeStr, String pattern, String language) {
        return normalizeDateTimeStr(dateTimeStr, pattern, language, true);
    }

    /**
     * Returns `dateTimeStr` as a normalized xs:date string, using `pattern` as current datetime form.
     * It uses the language of the current locale of the JVM to parse certain input strings, like names of months.
     *
     * @param dateTimeStr Input string representing a parsable datetime, e.g. "01 April 22T11:44:00"
     * @param pattern     Date format pattern used to parse the given dateStr as defined in {@link DateTimeFormatter}, e.g. "dd LLLL uuTHH:mm:ss"
     * @return A normalized date string in the ISO-8601 format uuuu-MM-ddTHH:mm:ss (xs:datetime), or null if parsing the input fails.
     */
    public static String normalizeDateTime(String dateTimeStr, String pattern) {
        return normalizeDateTimeWithLang(dateTimeStr, pattern, Locale.getDefault().getLanguage());
    }


    /**
     * Returns a HashMap containing the key,value pairs of the watched properties from the given
     * template string of the form  "key1=val1&key2=val2&...."
     *
     * @param watchedValueTemplate Input string template representing the key,value pairs of
     *                             the form "key1=val1&key2=val2&..."
     * @return A map containing the parsed pairs of the properties which are being watched.
     */
    private static Map<String, String> parsePropertyValueTemplate(Optional<String> watchedValueTemplate){
        Map<String, String> result = new HashMap<>();
        String watchedVal = watchedValueTemplate.orElse("");

        if (watchedVal.length() > 0) {
            Arrays.stream(watchedVal.split("&"))
                    .map(s -> s.split("="))
                    .filter(sArr -> sArr.length == 2)
                    .forEach(sArr -> result.put(sArr[0], sArr[1]));
        }

        return result;
    }

    private static String updateStatePropertyRecord(Map<String, String> watchedMap,
                                                    String hexKey, AtomicBoolean found, AtomicBoolean isDifferent,
                                                    boolean isUnique,
                                                    String storedStateRecord) {
        //No need to check the properties, we just need to know if the
        //given hexKey has already been seen in the state file.
        if (isUnique){
            if (storedStateRecord.equals(hexKey)) {
                found.set(true);
            }
            return storedStateRecord;
        }

        int split_idx = storedStateRecord.indexOf(':');
        // Ignore if no split is possible
        if (split_idx == -1) {
            return storedStateRecord;
        }
        String storedHexKey = storedStateRecord.substring(0, split_idx);
        if (!storedHexKey.equals(hexKey)){
            return storedStateRecord;
        }

        found.set(true);

        List<String> propertyValPairs =
                Arrays.stream(storedStateRecord.substring(split_idx+1)
                                .split("&"))
                        .map(str -> {
                            String[] propVal = str.split("=");
                            String property = propVal[0];
                            String storeVal = "NULL";
                            // Data may not provide a given property value so try and fail gracefully
                            try {
                                storeVal = propVal[1];
                            }
                            catch (IndexOutOfBoundsException e) {

                            }

                            String watchedVal = watchedMap.getOrDefault(property, null);

                            if(watchedVal != null && !watchedVal.equals(storeVal)){
                                isDifferent.set(true);
                                storeVal = watchedVal;
                            }

                           return String.format("%s=%s", property, storeVal);
                        })
                        .collect(Collectors.toList());

        return String.format("%s:%s", storedHexKey, String.join("&", propertyValPairs));
    }


    private static Path getStateFilePath(String stateDirPathStr, int m_buckets, int templateHash) {
        String hexBucketFileName=  Integer.toHexString(templateHash % m_buckets);
        return Paths.get(String.format("%s/%s.log", stateDirPathStr, hexBucketFileName));
    }


    /**
     * The generation of the IRI depends on the value of the watched properties.
     * If any of the watched properties changes in value or gets dropped, a unique IRI will be
     * generated. Otherwise, null String will be returned.
     * In order to check if the watched properties have changed, a file state is written to keep track of
     * previously seen property values.
     * A unique IRI will be generated from the provided "template" string by appending the current
     * date timestamp.
     *
     * @param template The template string used to generate unique IRI by appending current date timestamp
     * @param watchedValueTemplate The template string containing the key-value pairs of properties being watched
     * @param isUnique A flag to indicate if the given template already creates unique IRI
     * @param stateDirPathStr String representation of the directory path in which the state of the function
     *                        will be stored
     * @return A unique IRI will be generated from the provided "template" string by appending the current
     * date timestamp if possible. Otherwise, null is returned
     */
    public static String generateUniqueIRIOLD(String template, String watchedValueTemplate, Boolean isUnique, String stateDirPathStr) {
        //TODO: move this to the parameter of the function? (might get too bloated in RML definitions)
        int m_buckets = 10;

        Map<String,String>  watchedMap = parsePropertyValueTemplate(Optional.ofNullable(watchedValueTemplate));

        int templateHash = template.hashCode();
        Path stateFilePath = getStateFilePath(stateDirPathStr, m_buckets, templateHash);

        String hexKey = Integer.toHexString(templateHash);

        List<String> outputRecords;
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicBoolean isDifferent = new AtomicBoolean(false);
        String output = null;

        try{

            Files.createDirectories(Paths.get(stateDirPathStr));
            if (Files.notExists(stateFilePath) ) {
                Files.createFile(stateFilePath);
            }

            // Get and update the state records which will be rewritten back to the state file by overwriting
            try (BufferedReader reader = new BufferedReader(new FileReader(stateFilePath.toFile()))) {
                outputRecords = reader.lines()
                        .map(s -> updateStatePropertyRecord(watchedMap, hexKey, found, isDifferent, isUnique, s))
                        .collect(Collectors.toList());
            }

            if (!found.get()) {
                String hexedStateRecord = (isUnique)? hexKey:String.format("%s:%s", hexKey, watchedValueTemplate);
                outputRecords.add(hexedStateRecord);
            }

            try (BufferedWriter writer= new BufferedWriter(new FileWriter(stateFilePath.toFile()))){
                for(String line : outputRecords){
                    writer.write(line);
                    writer.newLine();
                }
            }

            output = getOutput(template, isUnique, found, isDifferent);

        } catch (IOException e){
            e.printStackTrace();
        }

        return output;
    }

    private static String getOutput(String template, Boolean isUnique, AtomicBoolean found, AtomicBoolean isDifferent) {
        // The unique IRI will be generated if there's any differences found with the corresponding record in
        // the state file
        String output = null;
        if (isDifferent.get() || !found.get()){
            logger.debug("Update found! Generating IRI...");
            if (isUnique) {
                output = template;
            }else {
                OffsetDateTime now = OffsetDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                output = String.format("%s#%s", template, formatter.format(now));
            }
        }
        return output;
    }

    private static void initStateFile(Path stateDirPath){
        try (Stream<Path> statePaths = Files.find(stateDirPath, 1, (filePath, fileAttr) -> fileAttr.isRegularFile())){

            Stream<AbstractMap.SimpleImmutableEntry> readers = statePaths.map(path -> {
                try {
                    return new AbstractMap.SimpleImmutableEntry(path,  new BufferedReader(new FileReader(path.toFile())));
                } catch (FileNotFoundException e) {
                    return null;
                }
            }).filter(Objects::nonNull);


            readers.forEach( entry -> {
                try (BufferedReader bReader = (BufferedReader) entry.getValue()){
                    String fileName = ((Path) entry.getKey()).toString();
                    bReader.lines().forEach(
                            line -> {
                                String[] pairs = line.split(":");
                                String key = pairs[0];
                                String val = null;
                                try {
                                    val =  pairs[1];
                                }catch (IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }
                                LDES_FILE_STATE_MAP.computeIfAbsent(fileName, k -> new HashMap<>()).put(key, val);
                            }
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void saveState() throws IOException {
        if (Objects.nonNull(STATE_DIR)){

            Files.createDirectories(STATE_DIR);
            for (Map.Entry<String, Map<String,String>> entry: LDES_FILE_STATE_MAP.entrySet()){

                Path file = Paths.get(entry.getKey());
                Map<String, String> storedMap = entry.getValue();

                if (Files.notExists(file)){
                    Files.createFile(file);
                }


                try( BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))){
                    for( Map.Entry<String, String> record: storedMap.entrySet()){
                        String line = record.getKey();
                        if (record.getValue() != null) {
                            line = String.format("%s:%s", record.getKey(), record.getValue());
                        }

                        writer.write(line);
                        writer.newLine();
                    }
                }

            }
        }

    }

    public static void resetState(){
        LDES_FILE_STATE_MAP = new HashMap<>();
        STATE_DIR = null;
    }
    public static String generateUniqueIRI(String template, String watchedValueTemplate, Boolean isUnique, String stateDirPathStr) {
        if (STATE_DIR == null){
            STATE_DIR = Paths.get(stateDirPathStr);
            initStateFile(STATE_DIR);
        }
        int m_buckets = 10;

        // null check just in case idlab-fn:_watchedProperty is not provided in the mapping file

        Optional<String> watchedValueOption = Optional.ofNullable(watchedValueTemplate);
        Map<String,String>  watchedMap = parsePropertyValueTemplate(watchedValueOption);

        int templateHash = template.hashCode();
        String stateFilePathStr = getStateFilePath(stateDirPathStr, m_buckets, templateHash).toString();

        String hexKey = Integer.toHexString(templateHash);

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicBoolean isDifferent = new AtomicBoolean(false);

        Map<String, String> keyValMap = LDES_FILE_STATE_MAP.computeIfAbsent(stateFilePathStr, f ->  new HashMap<>());
        if (keyValMap.containsKey(hexKey)){
            found.set(true);
            String storedProp = keyValMap.get(hexKey);
            Map<String, String> storedPropMap = parsePropertyValueTemplate(Optional.ofNullable(storedProp));
            for (Map.Entry<String, String> kv : watchedMap.entrySet()){
                String prop = kv.getKey();
                String val = kv.getValue();

                String storedVal = storedPropMap.getOrDefault(prop, null);
                if (!val.equals(storedVal)){
                    isDifferent.set(true);
                }
            }

        }
        keyValMap.put(hexKey, watchedValueTemplate);






        return getOutput(template,isUnique, found, isDifferent);

    }



    // TODO check whether this is the right place for this
    public static String jsonize(Object s) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(s);
    }
}

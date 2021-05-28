package be.ugent.rml;

import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * General static utility functions
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    // Without support for custom registered languages of length 5-8 of the IANA language-subtag-registry
    private static final Pattern regexPatternLanguageTag = Pattern.compile("^((?:(en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang))|((?:([A-Za-z]{2,3}(-(?:[A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4})(-(?:[A-Za-z]{4}))?(-(?:[A-Za-z]{2}|[0-9]{3}))?(-(?:[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-(?:[0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(?:x(-[A-Za-z0-9]{1,8})+))?)|(?:x(-[A-Za-z0-9]{1,8})+))$");

    public static Reader getReaderFromLocation(String location) throws IOException {
        return getReaderFromLocation(location, null, "");
    }

    public static Reader getReaderFromLocation(String location, File basePath, String contentType) throws IOException {
        if (isRemoteFile(location)) {
            try {
                return getReaderFromURL(new URL(location), contentType);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return getReaderFromFile(getFile(location, basePath));
        }
    }

    public static InputStream getInputStreamFromLocation(String location) throws IOException {
        return getInputStreamFromLocation(location, null, "");
    }

    public static InputStream getInputStreamFromLocation(String location, File basePath, String contentType) throws IOException {
        return getInputStreamFromLocation(location, basePath, contentType, new HashMap<String, String>());
    }

    public static InputStream getInputStreamFromLocation(String location, File basePath, String contentType, HashMap<String, String> headers) throws IOException {
        if (isRemoteFile(location)) {
            return getInputStreamFromURL(new URL(location), contentType, headers);
        } else {
            return getInputStreamFromFile(getFile(location, basePath));
        }
    }

    /**
     * Get an InputStream from a string. This string is either a path (local or remote) to an RDF file, or a raw RDF text.
     * @param s input, either RDF file path or raw RDF text
     * @return input stream
     */
    public static InputStream getInputStreamFromFileOrContentString(String s) {
        InputStream out;
        logger.debug("{} mapping file", s);
        try {
            out = getInputStreamFromLocation(s, null, "text/turtle");
        } catch (IOException e) {
            try {
                // raw mapping input string
                out = IOUtils.toInputStream(s, "UTF-8");
            } catch (IOException e2) {
                logger.error("Cannot read mapping option {}", s);
                out = new ByteArrayInputStream(new byte[0]);
            }
        }
        return out;
    }

    public static File getFile(String path) throws IOException {
        return Utils.getFile(path, null);
    }

    public static File getFile(String path, File basePath) throws IOException {
        // Absolute path?
        File f = new File(path);
        if (f.isAbsolute()) {
            if (f.exists()) {
                return f;
            } else {
                throw new FileNotFoundException();
            }
        }

        if (basePath == null) {
            try {
                basePath = new File(System.getProperty("user.dir"));
            } catch (Exception e) {
                throw new FileNotFoundException();
            }
        }

        logger.debug("Looking for file " + path + " in basePath " + basePath);

        // Relative from user dir?
        f = new File(basePath, path);
        if (f.exists()) {
            return f;
        }

        logger.debug("File " + path + " not found in " + basePath);
        logger.debug("Looking for file " + path + " in " + basePath + "/../");


        // Relative from parent of user dir?
        f = new File(basePath, "../" + path);
        if (f.exists()) {
            return f;
        }

        logger.debug("File " + path + " not found in " + basePath);
        logger.debug("Looking for file " + path + " in the resources directory");

        // Resource path?
        try {
            return MyFileUtils.getResourceAsFile(path);
        } catch (IOException e) {
            // Too bad
        }

        logger.debug("File " + path + " not found in the resources directory");

        throw new FileNotFoundException(path);
    }

    public static Reader getReaderFromURL(URL url) throws IOException {
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    public static Reader getReaderFromURL(URL url, String contentType) throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStreamFromURL(url, contentType)));
    }

    public static Reader getReaderFromFile(File file) throws FileNotFoundException {
        return new FileReader(file);
    }

    public static InputStream getInputStreamFromURL(URL url) throws IOException {
        return url.openStream();
    }

    public static InputStream getInputStreamFromURL(URL url, String contentType) {
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            inputStream = connection.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return inputStream;
    }

    public static InputStream getInputStreamFromURL(URL url, String contentType, HashMap<String, String> headers) {
        InputStream inputStream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", contentType);
            // Set encoding if not set before
            if(!headers.containsKey("charset")) {
                headers.put("charset", "utf-8");
            }
            // Apply all headers
            headers.forEach((name, value) -> {
                logger.debug(name + ": " + value);
                connection.setRequestProperty(name, value);
            });
            connection.connect();
            inputStream = connection.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return inputStream;
    }

    public static InputStream getInputStreamFromFile(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public static boolean isRemoteFile(String location) {
        return location.startsWith("https://") || location.startsWith("http://");
    }

    public static List<Term> getSubjectsFromQuads(List<Quad> quads) {
        ArrayList<Term> subjects = new ArrayList<>();

        for (Quad quad : quads) {
            subjects.add(quad.getSubject());
        }

        return subjects;
    }

    public static List<Term> getObjectsFromQuads(List<Quad> quads) {
        ArrayList<Term> objects = new ArrayList<>();

        for (Quad quad : quads) {
            objects.add(quad.getObject());
        }

        return objects;
    }

    public static List<String> getLiteralObjectsFromQuads(List<Quad> quads) {
        ArrayList<String> objects = new ArrayList<>();

        for (Quad quad : quads) {
            objects.add(((Literal) quad.getObject()).getValue());
        }

        return objects;
    }

    public static List<Term> getList(QuadStore store, Term first) {
        List<Term> list = new ArrayList<>();

        return getList(store, first, list);
    }

    public static List<Term> getList(QuadStore store, Term first, List<Term> list) {
        if (first.equals(new NamedNode(NAMESPACES.RDF + "nil"))) {
            return list;
        }

        Term value = Utils.getObjectsFromQuads(store.getQuads(first, new NamedNode(NAMESPACES.RDF + "first"), null)).get(0);
        Term next = Utils.getObjectsFromQuads(store.getQuads(first, new NamedNode(NAMESPACES.RDF + "rest"), null)).get(0);
        list.add(value);

        if (next.equals(new NamedNode(NAMESPACES.RDF + "nil"))) {
            return list;
        } else {
            list = getList(store, next, list);
        }
        return list;
    }

    /**
     * Check if conforming to https://tools.ietf.org/html/bcp47#section-2.2.9
     *
     * @param s language tag
     * @return True if valid language tag according to BCP 47
     */
    public static boolean isValidrrLanguage(String s) {
        return regexPatternLanguageTag.matcher(s).matches();
    }

    public static String encodeURI(String url) {
        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        String result = escaper.escape(url);

        result = result.replaceAll("!", "%21");
        result = result.replaceAll("#", "%23");
        result = result.replaceAll("\\$", "%24");
        result = result.replaceAll("&", "%26");
        result = result.replaceAll("'", "%27");
        result = result.replaceAll("\\(", "%28");
        result = result.replaceAll("\\)", "%29");
        result = result.replaceAll("\\*", "%2A");
        result = result.replaceAll("\\+", "%2B");
        result = result.replaceAll(",", "%2C");
        result = result.replaceAll("/", "%2F");
        result = result.replaceAll(":", "%3A");
        result = result.replaceAll(";", "%3B");
        result = result.replaceAll("=", "%3D");
        result = result.replaceAll("\\?", "%3F");
        result = result.replaceAll("@", "%40");
        result = result.replaceAll("\\[", "%5B");
        result = result.replaceAll("]", "%5D");

        return result;
    }

    public static String fileToString(File file) throws IOException {
        Reader reader = getReaderFromFile(file);
        int intValueOfChar;
        String targetString = "";
        while ((intValueOfChar = reader.read()) != -1) {
            targetString += (char) intValueOfChar;
        }
        reader.close();
        return targetString;
    }

    /*
        Extracts the selected columns from the SQL query
        Orders them alphabetically
        Returns hash of concatenated string
     */
    // todo: Take subquerries into account
    public static int selectedColumnHash(String query) {
        Pattern p = Pattern.compile("^SELECT(.*)FROM");
        Matcher m = p.matcher(query.replace("\n", " ").replace("\r", " ").trim());

        if (m.find()) {
            String columns = m.group(1);
            String[] columnNames = columns.replace("DISTINCT", "").replace(" ", "").split(",");
            Arrays.sort(columnNames);
            return String.join("", columnNames).hashCode();
        }

        throw new Error("Invalid query: " + query);
    }

    // Simpler version of above method. Hashes the whole query.
    public static int getHash(String query) {
        return query.hashCode();
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8;
        }
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String getURLParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1) // remove final '&'
                : resultString;
    }

    public static int getFreePortNumber() throws IOException {
        ServerSocket temp = new ServerSocket(0);
        temp.setReuseAddress(true);
        int portNumber = temp.getLocalPort();
        temp.close();
        return portNumber;

    }

    /**
     * This method parse the generic template and returns a list of Extractors
     * that can later be used by the executor
     * to get the data values from the records.
     *
     * @param template template string
     * @return list of extractors
     **/
    public static List<Extractor> parseTemplate(String template, boolean ignoreDoubleQuotes) {
        ArrayList<Extractor> extractors = new ArrayList<>();
        String current = "";
        boolean previousWasBackslash = false;
        boolean variableBusy = false;

        if (template != null) {
            for (Character c : template.toCharArray()) {

                if (c == '{') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if (variableBusy) {
                        throw new Error("Parsing of template failed. Probably a { was followed by a second { without first closing the first {. Make sure that you use { and } correctly.");
                    } else {
                        variableBusy = true;

                        if (!current.equals("")) {
                            extractors.add(new ConstantExtractor(current));
                        }

                        current = "";
                    }
                } else if (c == '}') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if (variableBusy) {
                        extractors.add(new ReferenceExtractor(current, ignoreDoubleQuotes));
                        current = "";
                        variableBusy = false;
                    } else {
                        throw new Error("Parsing of template failed. Probably a } as used before a { was used. Make sure that you use { and } correctly.");
                    }
                } else if (c == '\\') {
                    if (previousWasBackslash) {
                        previousWasBackslash = false;
                        current += c;
                    } else {
                        previousWasBackslash = true;
                    }
                } else {
                    current += c;
                }
            }

            if (!current.equals("")) {
                extractors.add(new ConstantExtractor(current));
            }
        }

        return extractors;
    }

    public static String randomString(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();

    }

    public static String hashCode(String s) {
        int hash = 0;
        for (int i = 0; i < s.toCharArray().length; i++) {
            hash += s.toCharArray()[i] * 31 ^ (s.toCharArray().length - 1 - i);
        }
        return Integer.toString(Math.abs(hash));
    }

    public static void ntriples2hdt(String rdfInputPath, String hdtOutputPath) {
        // Configuration variables
        String baseURI = "http://example.com/mydataset";
        String inputType = "ntriples";

        try {
            // Create HDT from RDF file
            HDT hdt = HDTManager.generateHDT(rdfInputPath, baseURI, RDFNotation.parse(inputType), new HDTSpecification(), null);
            // Save generated HDT to a file
            hdt.saveToHDT(hdtOutputPath, null);
            // IMPORTANT: Free resources
            hdt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns true if a string is valid IRI.
     *
     * @param iri the IRI to validate.
     * @return true if the IRI is valid, else false.
     */
    public static boolean isValidIRI(String iri) {
        try {
            new ParsedIRI(iri);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method returns true if a string is a relative IRI.
     *
     * @param iri the IRI to check.
     * @return true if the IRI is relative, else false.
     */
    public static boolean isRelativeIRI(String iri) {
        try {
            ParsedIRI parsedIRI = new ParsedIRI(iri);

            return !parsedIRI.isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getBaseDirectiveTurtle(File file) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String turtle = contentBuilder.toString();
        return Utils.getBaseDirectiveTurtle(turtle);
    }

    public static String getBaseDirectiveTurtle(InputStream is) {
        String turtle = null;
        try {
            turtle = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            turtle = "";
        }
        return Utils.getBaseDirectiveTurtle(turtle);
    }

    public static String getBaseDirectiveTurtle(String turtle) {
        Pattern p = Pattern.compile("@base <([^<>]*)>");
        Matcher m = p.matcher(turtle);

        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public static String transformDatatypeString(String input, String datatype) {
        switch (datatype) {
            case "http://www.w3.org/2001/XMLSchema#hexBinary":
                // TODO
                return input;
            case "http://www.w3.org/2001/XMLSchema#decimal":
                return "" + Double.parseDouble(input);
            case "http://www.w3.org/2001/XMLSchema#integer":
                return "" + Integer.parseInt(input);
            case "http://www.w3.org/2001/XMLSchema#double":
                return formatToScientific(Double.parseDouble(input));
            case "http://www.w3.org/2001/XMLSchema#boolean":
                switch (input) {
                    case "t":
                    case "true":
                    case "TRUE":
                    case "1":
                        return "true";
                    default:
                        return "false";
                }
            case "http://www.w3.org/2001/XMLSchema#date":
                return input;
            case "http://www.w3.org/2001/XMLSchema#time":
                return input;
            case "http://www.w3.org/2001/XMLSchema#dateTime":
                return input.replace(" ", "T");
            default:
                return input;
        }

    }

    public static int getHashOfString(String str) {
        int hash = 7;

        for (int i = 0; i < str.length(); i++) {
            hash = hash * 31 + str.charAt(i);
        }

        return hash;
    }

    private static String formatToScientific(Double d) {
        BigDecimal input = BigDecimal.valueOf(d).stripTrailingZeros();
        int precision = input.scale() < 0
                ? input.precision() - input.scale()
                : input.precision();
        StringBuilder s = new StringBuilder("0.0");
        for (int i = 2; i < precision; i++) {
            s.append("#");
        }
        s.append("E0");
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern(s.toString());
        return df.format(d);
    }
}

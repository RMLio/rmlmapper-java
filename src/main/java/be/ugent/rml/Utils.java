package be.ugent.rml;

import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

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
        if (isRemoteFile(location)) {
            try {
                return getInputStreamFromURL(new URL(location), contentType);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return getInputStreamFromFile(getFile(location, basePath));
        }
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    public static File getFile(String path) throws IOException {
        return Utils.getFile(path, null);
    }

    /**
     * @param path
     * @param basePath
     * @return
     * @throws IOException
     */
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

        // Relative from parent of user dir?
        f = new File(basePath, "../" + path);
        if (f.exists()) {
            return f;
        }

        // Resource path?
        try {
            return MyFileUtils.getResourceAsFile(path);
        } catch (IOException e) {
            // Too bad
        }

        throw new FileNotFoundException();
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
            connection.setInstanceFollowRedirects(false);
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

    public static RDF4JStore readTurtle(File file, RDFFormat format) {
        InputStream is;
        Model model = null;
        try {
            is = new FileInputStream(file);
            //model = Rio.parse(mappingStream, "", format);

            ParserConfig config = new ParserConfig();
            config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
            logger.debug("Reading from " + file.getAbsolutePath());
            model = Rio.parse(is, "", format, config, SimpleValueFactory.getInstance(), null);
            is.close();
        } catch (IOException | RDFParseException e) {
            e.printStackTrace();
        }
        return new RDF4JStore(model);
    }

    public static RDF4JStore readTurtle(File mappingFile) {
        return Utils.readTurtle(mappingFile, RDFFormat.TURTLE);
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
        Matcher m = p.matcher(query);

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

    public static String readFile(String path, Charset encoding) throws IOException
    {
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
     **/
    public static List<Extractor> parseTemplate(String template) {
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
                    } else if (variableBusy){
                        extractors.add(new ReferenceExtractor(current));
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
}

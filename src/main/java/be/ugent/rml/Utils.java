package be.ugent.rml;

import be.ugent.rml.records.Record;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.commons.codec.binary.Hex;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return getInputStreamFromLocation(location,null,"");
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

    public static List<String> applyTemplate(Template template, Record record) {
        return Utils.applyTemplate(template, record, false);
    }

    public static List<String> applyTemplate(Template template, Record record, boolean encodeURIEnabled) {
        List<String> result = new ArrayList<String>();
        result.add("");
        //we only return a result when all elements of the template are found
        boolean allValuesFound = true;

        //we iterate over all elements of the template, unless one is not found
        for (int i = 0; allValuesFound && i < template.getTemplateElements().size(); i++) {
            TemplateElement element = template.getTemplateElements().get(i);
            //if the element is constant, we don't need to look at the data, so we can just add it to the result
            if (element.getType() == TEMPLATETYPE.CONSTANT) {
                for (int j = 0; j < result.size(); j ++) {
                    result.set(j, result.get(j) + element.getValue());
                }
            } else {
                //we need to get the variables from the data
                //we also need to keep all combinations of multiple results are returned for variable; pretty tricky business
                List<String> temp = new ArrayList<>();
                List<String> values = record.get(element.getValue());

                for (String value : values) {

                    if (encodeURIEnabled) {
                        value = Utils.encodeURI(value);
                    }

                    for (String aResult : result) {
                        temp.add(aResult + value);
                    }
                }

                if (!values.isEmpty()) {
                    result = temp;
                }

                if (values.isEmpty()) {
                    logger.warn("Not all values for a template where found. More specific, the variable " + element.getValue() + " did not provide any results.");
                    allValuesFound = false;
                }
            }
        }

        if (allValuesFound) {
            if (template.countVariables() > 0) {
                String emptyTemplate = getEmptyTemplate(template);
                result.removeIf(s -> s.equals(emptyTemplate));
            }

            return result;
        } else {
            return new ArrayList<>();
        }
    }

    private static String getEmptyTemplate(Template template) {
        String output = "";

        for (TemplateElement t : template.getTemplateElements()) {
            if (t.getType() == TEMPLATETYPE.CONSTANT) {
                output += t.getValue();
            }
        }

        return output;
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

    public static String getLiteral(String value) {
        Pattern pattern = Pattern.compile("^\"(.*)\"");
        Matcher matcher = pattern.matcher(value);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new Error("Invalid Literal: " + value);
    }

    public static boolean isLiteral(String value) {
        try {
            getLiteral(value);
            return true;
        } catch (Error e){
            return false;
        }
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

    public static QuadStore readTurtle(File file, RDFFormat format) {
        InputStream is;
        Model model = null;
        try {
            is = new FileInputStream(file);
            //model = Rio.parse(mappingStream, "", format);

            ParserConfig config = new ParserConfig();
            config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
            model = Rio.parse(is, "", format, config, SimpleValueFactory.getInstance(), null);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RDF4JStore(model);
    }

    public static QuadStore readTurtle(File mappingFile) {
        return Utils.readTurtle(mappingFile, RDFFormat.TURTLE);
    }

    public static boolean isBlankNode(String value) {
        return value.startsWith("_:");
    }

    public static String toNTriples(List<Quad> quads) {
        StringBuilder output = new StringBuilder();

        for (Quad q : quads) {
            output.append(Utils.getNTripleOfQuad(q) + "\n");
        }

        return output.toString();
    }

    public static String toNQuads(List<Quad> quads) {
        StringBuilder output = new StringBuilder();

        for (Quad q : quads) {
            output.append(Utils.getNQuadOfQuad(q) + "\n");
        }

        return output.toString();
    }

    public static void toNTriples(List<Quad> quads, Writer out) throws IOException {
        for (Quad q : quads) {
            out.write(Utils.getNTripleOfQuad(q) + "\n");
        }
    }

    public static void toNQuads(List<Quad> quads, Writer out) throws IOException {
        for (Quad q : quads) {
            out.write(Utils.getNQuadOfQuad(q) + "\n");
        }
    }

    public static TriplesQuads getTriplesAndQuads(List<Quad> all) {
        List<Quad> triples = new ArrayList<>();
        List<Quad> quads = new ArrayList<>();

        for (Quad q: all) {
            if (q.getGraph() == null || q.getGraph().equals("")) {
                triples.add(q);
            } else {
                quads.add(q);
            }
        }

        return new TriplesQuads(triples, quads);
    }

    private static String getNTripleOfQuad(Quad q) {
        return q.getSubject() + " " + q.getPredicate() + " " + q.getObject() + ".";
    }

    private static String getNQuadOfQuad(Quad q) {
        return q.getSubject() + " " + q.getPredicate() + " " + q.getObject() + " " + q.getGraph() + ".";
    }

    public static String encodeURI(String url) {
        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        String result =  escaper.escape(url);

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

    public static String readFile(String path, Charset encoding) throws IOException
    {
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8;
        }
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static int getFreePortNumber() throws IOException {
        ServerSocket temp = new ServerSocket(0);
        temp.setReuseAddress(true);
        int portNumber = temp.getLocalPort();
        temp.close();
        return portNumber;

    }

    /**
     * This method parse the generic template and returns an array
     * that can later be used by the executor (via applyTemplate)
     * to get the data values from the records.
     **/
    public static Template parseTemplate(String template) {
        Template result = new Template();
        String current = "";
        boolean previousWasBackslash = false;
        boolean variableBusy = false;

        if (template != null) {
            for (Character c : template.toCharArray()) {

                if (c == '{') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if(variableBusy) {
                        throw new Error("Parsing of template failed. Probably a { was followed by a second { without first closing the first {. Make sure that you use { and } correctly.");
                    } else {
                        variableBusy = true;

                        if (!current.equals("")) {
                            result.addElement(new TemplateElement(current, TEMPLATETYPE.CONSTANT));
                        }

                        current = "";
                    }
                } else if (c == '}') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if (variableBusy){
                        result.addElement(new TemplateElement(current, TEMPLATETYPE.VARIABLE));
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
                result.addElement(new TemplateElement(current, TEMPLATETYPE.CONSTANT));
            }
        }

        return result;
    }

    public static void writeOutput(String what, List<Quad> output, String extension, String outputFile) {
        if (output.size() > 1) {
            logger.info(output.size() + " " + what + "s were generated");
        } else {
            logger.info(output.size() + " " + what + " was generated");
        }

        //if output file provided, write to triples output file
        if (outputFile != null) {
            File targetFile = new File(outputFile + "." + extension);
            logger.info("Writing " + what + " to " + targetFile.getPath() + "...");

            if (!targetFile.isAbsolute()) {
                targetFile = new File(System.getProperty("user.dir") + "/" + outputFile + "." +  extension);
            }

            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

                if (what.equals("triple")) {
                    Utils.toNTriples(output, out);
                } else {
                    Utils.toNQuads(output, out);
                }

                out.close();
                logger.info("Writing to " + targetFile.getPath() + " is done.");
            } catch(IOException e) {
                System.err.println( "Writing output to file failed. Reason: " + e.getMessage() );
            }
        } else {
            if (what.equals("triple")) {
                System.out.println(Utils.toNTriples(output));
            } else {
                System.out.println(Utils.toNQuads(output));
            }
        }
    }

    public static String randomString(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();

    }

    public static String hashCode(String s) {
        int hash = 0;
        for (int i = 0; i < s.toCharArray().length; i++) {
            hash += s.toCharArray()[i] * 31^(s.toCharArray().length - 1 - i);
        }
        return Integer.toString(Math.abs(hash));
    }
}

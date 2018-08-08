package be.ugent.rml;

import be.ugent.rml.records.Record;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.store.RDF4JStore;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static List<String> getSubjectsFromQuads(List<Quad> quads) {
        ArrayList<String> subjects = new ArrayList<String>();

        for (Quad quad : quads) {
            subjects.add(quad.getSubject());
        }

        return subjects;
    }

    public static List<String> getObjectsFromQuads(List<Quad> quads) {
        ArrayList<String> objects = new ArrayList<String>();

        for (Quad quad : quads) {
            objects.add(quad.getObject());
        }

        return objects;
    }

    public static List<String> getLiteralObjectsFromQuads(List<Quad> quads) {
        ArrayList<String> objects = new ArrayList<String>();

        for (Quad quad : quads) {
            objects.add(getLiteral(quad.getObject()));
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

    public static List<String> getList(QuadStore store, String first) {
        List<String> list = new ArrayList<>();
        return getList(store, first, list);
    }

    public static List<String> getList(QuadStore store, String first, List<String> list) {
        if (first.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
            return list;
        }
        String value = Utils.getObjectsFromQuads(store.getQuads(first, "http://www.w3.org/1999/02/22-rdf-syntax-ns#first", null)).get(0);
        String next = Utils.getObjectsFromQuads(store.getQuads(first, "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest", null)).get(0);
        list.add(value);
        if (next.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
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
        String s = q.getSubject();

        if (!Utils.isBlankNode(s)) {
            s = "<" + s + ">";
        }

        String p = "<" + q.getPredicate() + ">";
        String o = q.getObject();

        if (!Utils.isBlankNode(o) && !Utils.isLiteral(o)) {
            o = "<" + o + ">";
        }

        return s + " " + p + " " + o + ".";
    }

    private static String getNQuadOfQuad(Quad q) {
        String s = q.getSubject();
        String p = "<" + q.getPredicate() + ">";

        if (!Utils.isBlankNode(s)) {
            s = "<" + s + ">";
        }

        String o = q.getObject();

        if (!Utils.isBlankNode(o) && !Utils.isLiteral(o)) {
            o = "<" + o + ">";
        }

        String g = q.getGraph();

        if (!Utils.isBlankNode(g)) {
            g = "<" + g + ">";
        }

        return s + " " + p + " " + o + " " + g + ".";
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
}

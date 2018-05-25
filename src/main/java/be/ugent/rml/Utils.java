package be.ugent.rml;

import be.ugent.rml.records.Record;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.store.RDF4JStore;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static List<String> applyTemplate(List<Element> template, Record record) {
        return Utils.applyTemplate(template, record, false);
    }

    public static List<String> applyTemplate(List<Element> template, Record record, boolean encodeURIEnabled) {
        List<String> result = new ArrayList<String>();
        result.add("");
        //we only return a result when all elements of the template are found
        boolean allValuesFound = true;

        //we iterate over all elements of the template, unless one is not found
        for (int i = 0; allValuesFound && i < template.size(); i ++) {
            //if the element is constant, we don't need to look at the data, so we can just add it to the result
            if (template.get(i).getType() == TEMPLATETYPE.CONSTANT) {
                for (int j = 0; j < result.size(); j ++) {
                    result.set(j, result.get(j) + template.get(i).getValue());
                }
            } else {
                //we need to get the variables from the data
                //we also need to keep all combinations of multiple results are returned for variable; pretty tricky business
                List<String> temp = new ArrayList<>();
                List<String> values = record.get(template.get(i).getValue());

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
                    logger.warn("Not all values for a template where found. More specific, the variable " + template.get(i).getValue() + " did not provide any results.");
                    allValuesFound = false;
                }
            }
        }

        if (allValuesFound) {
            if (countVariablesInTemplate(template) > 0) {
                String emptyTemplate = getEmptyTemplate(template);
                result.removeIf(s -> s.equals(emptyTemplate));
            }

            return result;
        } else {
            return new ArrayList<>();
        }
    }

    private static String getEmptyTemplate(List<Element> template) {
        String output = "";

        for (Element t : template) {
            if (t.getType() == TEMPLATETYPE.CONSTANT) {
                output += t.getValue();
            }
        }

        return output;
    }

    private static int countVariablesInTemplate(List<Element> template) {
        int counter = 0;

        for (Element aTemplate : template) {
            if (aTemplate.getType() == TEMPLATETYPE.VARIABLE) {
                counter++;
            }
        }

        return counter;
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

        return s + " " + s + " " + o + " " + g + ".";
    }

    public static String encodeURI(String url) {
        Escaper escaper = UrlEscapers.urlFragmentEscaper();
        return escaper.escape(url);
    }
}

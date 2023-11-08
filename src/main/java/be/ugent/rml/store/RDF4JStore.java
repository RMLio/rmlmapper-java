package be.ugent.rml.store;

import org.apache.jena.base.Sys;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.*;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of QuadStore with RDF4J
 * Package-private
 */
public class RDF4JStore extends QuadStore {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(RDF4JStore.class);
    private Model model;
    private int triplesWithGraphCounter;

    public RDF4JStore() {
        model = new TreeModel();
        triplesWithGraphCounter = 0;
    }

    @Override
    public void removeDuplicates() {
        /*
         * Model is an extension of the default Java Collection class java.util.Set<Statement>.
         * Sets don't contain any duplicates, thus no removal is needed.
         * See https://rdf4j.org/documentation/programming/model/
         */
    }

    @Override
    public void addQuad(Value subject, Value predicate, Value object, Value graph) {
        Resource s = getFilterSubject(subject);
        IRI p = getFilterPredicate(predicate);
        Value o = getFilterObject(object);
        Resource g = getFilterGraph(graph);

        model.add(s, p, o, g);

        if (g != null) {
            triplesWithGraphCounter++;
        }
    }

    @Override
    public List<Value> getSubjects() {
        List<Value> terms = new ArrayList<>();
        for (Resource subject : model.subjects()) {
            terms.add(convertStringToTerm(subject.toString()));
        }
        return terms;
    }

    @Override
    public List<Quad> getQuads(Value subject, Value predicate, Value object, Value graph) {
        Model result;
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);

        // Needed to get quads with any graph (null as wildcard)
        if (graph != null) {
            result = model.filter(filterSubject, filterPredicate, filterObject, filterGraph);
        } else {
            result = model.filter(filterSubject, filterPredicate, filterObject);
        }

        List<Quad> quads = new ArrayList<>();

        for (Statement st : result) {
            Value s = convertStringToTerm(st.getSubject().toString());
            Value p = convertStringToTerm(st.getPredicate().toString());
            Value o = convertStringToTerm(st.getObject().toString());

            if (st.getContext() == null) {
                quads.add(new Quad(s, p, o));
            } else {
                quads.add(new Quad(s, p, o, convertStringToTerm(st.getContext().toString())));
            }
        }

        return quads;
    }

    @Override
    public void copyNameSpaces(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;

            rdf4JStore.getModel()
                    .getNamespaces()
                    .forEach(namespace -> model.setNamespace(namespace));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void read(InputStream is, String base, RDFFormat format) throws Exception {
        if (base == null) {
           base = "";
        }

        try {
            RDFParser parser = Rio.createParser(format);
            parser.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
            parser.setRDFHandler(new StatementCollector(model));
            parser.parse(is, base);
        } finally {
            is.close();
        }
    }

    @Override
    public void write(Writer out, String format) throws Exception {
        switch (format) {
            case "turtle":
                Rio.write(model, out, RDFFormat.TURTLE);
                if (triplesWithGraphCounter > 0) {
                    logger.warn("There are graphs generated. However, Turtle does not support graphs. Use Trig instead.");
                }
                break;
            case "trig":
                Rio.write(model, out, RDFFormat.TRIG);
                break;
            case "trix":
                Rio.write(model, out, RDFFormat.TRIX);
                break;
            case "jsonld":
                Rio.write(model, out, RDFFormat.JSONLD);
                break;
            case "nquads":
                Rio.write(model, out, RDFFormat.NQUADS);
                break;
            case "ntriples":
                Rio.write(model, out, RDFFormat.NTRIPLES);
                break;
            default:
                throw new Exception("Serialization " + format + " not supported");
        }
    }

    public String getBase() {
        Optional<Namespace> base = model.getNamespace("");
        if (base.isPresent()) {
            return base.get().getName();
        } else {
            return "";
        }
    }

    @Override
    public boolean isEmpty() {
        return model.isEmpty();
    }

    @Override
    public int size() {
        return model.size();
    }

    /**
     * TODO remove all need for this. Currently:
     *  - store equality/isomorphism
     *  - namespace passing
     *  - store difference
     *  - store isSubset
     */
    public Model getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDF4JStore) {
            RDF4JStore otherStore = (RDF4JStore) o;

            return Models.isomorphic(model, otherStore.getModel());
        } else {
            return false;
        }
    }

    @Override
    public void removeQuads(Value subject, Value predicate, Value object, Value graph) {
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);
        model.remove(filterSubject, filterPredicate, filterObject, filterGraph);
    }

    @Override
    public boolean contains(Value subject, Value predicate, Value object, Value graph) {
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);

        return model.contains(filterSubject, filterPredicate, filterObject, filterGraph);
    }

    @Override
    public boolean isIsomorphic(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;
            return Models.isomorphic(model, rdf4JStore.getModel());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean isSubset(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;
            return Models.isSubset(model, rdf4JStore.getModel());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Resource getFilterSubject(Value subject) {
        if (subject != null) {
            ValueFactory vf = SimpleValueFactory.getInstance();

            if (subject.isBNode()) {
                return vf.createBNode(subject.stringValue());
            } else {
                return vf.createIRI(subject.stringValue());
            }
        } else {
            return null;
        }
    }

    private IRI getFilterPredicate(Value predicate) {
        if (predicate != null) {
            return SimpleValueFactory.getInstance().createIRI(predicate.stringValue());
        } else {
            return null;
        }
    }

    private Value getFilterObject(Value object) {
        if (object != null) {

            if (object.isBNode()) {
                return valueFactory.createBNode(object.stringValue());
            } else if (object.isLiteral()) {
                Literal literal = (Literal) object;
                if (literal.getDatatype() != null) {
                    return valueFactory.createLiteral(object.stringValue(), literal.getDatatype());
                } else if (literal.getLanguage().isPresent()) {
                    return valueFactory.createLiteral(object.stringValue(), literal.getLanguage().get());
                } else {
                    return valueFactory.createLiteral(object.stringValue());
                }
            } else {
                return valueFactory.createIRI(object.stringValue());
            }
        } else {
            return null;
        }
    }

    private Resource getFilterGraph(Value graph) {
        return getFilterSubject(graph);
    }

    /**
     * Convert given string to Value
     *
     * @param str
     * @return
     */
    // TODO refactor Value class to use library (Jena/RDF4J) and have this built-in
    private Value convertStringToTerm(String str) {
        if (str.startsWith("_:")) {
            return valueFactory.createBNode(str.replace("_:", ""));
        } else if (str.startsWith("\"\"\"")) {
            // Triple quoted literal
            return valueFactory.createLiteral(str.substring(4, str.length() - 3));
        } else if (str.startsWith("\"")) {
            Pattern pattern;
            boolean hasLanguage = str.contains("@") && str.lastIndexOf("@") > str.lastIndexOf("\"");
            boolean hasDatatype = str.contains("^^");
            if (hasLanguage) {
                pattern = Pattern.compile("^\"(.*)\"@([^@]*)", Pattern.DOTALL);
            } else if (hasDatatype) {
                pattern = Pattern.compile("^\"(.*)\"\\^\\^<([^>]*)>", Pattern.DOTALL);
            } else {
                pattern = Pattern.compile("^\"(.*)\"$", Pattern.DOTALL);
            }

            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                if (hasLanguage) {
                    return valueFactory.createLiteral(matcher.group(1), matcher.group(2));
                } else if (hasDatatype) {
                    return valueFactory.createLiteral(matcher.group(1), valueFactory.createIRI(matcher.group(2)));
                } else {
                    return valueFactory.createLiteral(matcher.group(1));
                }
            } else {
                throw new Error("Invalid Literal: " + str);
            }
        } else {
            return valueFactory.createIRI(str);
        }
    }
}
